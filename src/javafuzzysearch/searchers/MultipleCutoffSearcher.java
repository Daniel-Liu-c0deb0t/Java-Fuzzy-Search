package javafuzzysearch.searchers;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

import javafuzzysearch.utils.FuzzyMatch;
import javafuzzysearch.utils.Edit;
import javafuzzysearch.utils.Utils;
import javafuzzysearch.utils.LengthParam;
import javafuzzysearch.utils.EditWeights;
import javafuzzysearch.utils.Location;
import javafuzzysearch.utils.Array2D;

/**
 * Implementation of stateful DP between multiple patterns and Ukkonen's cutoff algorithm for computing Levenshtein distance.
 */
public class MultipleCutoffSearcher{
    private LengthParam scoreThreshold = new LengthParam(0, false, false);
    private int maxNonOverlap = 0;
    private EditWeights editWeights = new EditWeights();
    private boolean allowTranspositions = false, maximizeScore = false;
    private Map<Character, Set<Character>> wildcardChars = new HashMap<>();
    private Location nonOverlapLocation = Location.ANY;
    private boolean useCutoff = true;

    public MultipleCutoffSearcher scoreThreshold(LengthParam scoreThreshold){
        this.scoreThreshold = scoreThreshold;
        return this;
    }

    public MultipleCutoffSearcher maxNonOverlap(int maxNonOverlap, Location nonOverlapLocation){
        this.maxNonOverlap = maxNonOverlap;
        this.nonOverlapLocation = nonOverlapLocation;
        return this;
    }

    public MultipleCutoffSearcher allowTranspositions(){
        this.allowTranspositions = true;
        return this;
    }

    public MultipleCutoffSearcher editWeights(EditWeights editWeights){
        this.editWeights = editWeights;
        useCutoff = editWeights.isDiagonalMonotonic();
        return this;
    }

    public MultipleCutoffSearcher maximizeScore(){
        this.maximizeScore = true;
        return this;
    }

    public MultipleCutoffSearcher wildcardChars(Map<Character, Set<Character>> wildcardChars){
        this.wildcardChars = wildcardChars;
        return this;
    }

    public List<PatternEscape> preprocessPatterns(List<String> patterns, List<Set<Integer>> patternEscapeIdx){
        List<PatternEscape> patternEscapePairs = new ArrayList<>();

        for(int i = 0; i < patterns.size(); i++)
            patternEscapePairs.add(new PatternEscape(patterns.get(i), patternEscapeIdx.get(i), i));

        Collections.sort(patternEscapePairs, (a, b) -> a.pattern.compareTo(b.pattern));

        for(int i = 0; i < patternEscapePairs.size() - 1; i++){
            PatternEscape curr = patternEscapePairs.get(i);
            curr.lcp = Utils.longestCommonPrefix(curr.pattern, patternEscapePairs.get(i + 1).pattern);
        }

        return patternEscapePairs;
    }

    public List<List<FuzzyMatch>> search(String text, List<String> patterns, boolean returnPath){
        List<Set<Integer>> empty = new ArrayList<>();

        for(int i = 0; i < patterns.size(); i++)
            empty.add(new HashSet<Integer>());

        return search(text, preprocessPatterns(patterns, empty), returnPath, new HashSet<Integer>());
    }

    private Array2D<Integer> dp;
    private Array2D<Integer> start;
    private Array2D<Edit> path;

    public List<List<FuzzyMatch>> search(String text, List<PatternEscape> patternEscapePairs, boolean returnPath, Set<Integer> textEscapeIdx){
        List<List<FuzzyMatch>> matches = new ArrayList<>(patternEscapePairs.size());

        for(int i = 0; i < patternEscapePairs.size(); i++)
            matches.add(null);

        dp = null;
        start = null;
        path = null;

        for(int i = 0; i < patternEscapePairs.size(); i++){
            PatternEscape curr = patternEscapePairs.get(i);
            matches.set(curr.idx, search(text, curr.pattern, returnPath, textEscapeIdx, curr.escapeIdx));

            if(i < patternEscapePairs.size() - 1){
                int length = curr.pattern.length();

                while(length > curr.lcp){
                    dp.pop(length);
                    start.pop(length);
                    if(returnPath)
                        path.pop(length);

                    length--;
                }
            }
        }

        return matches;
    }

    private List<FuzzyMatch> search(String text, String pattern, boolean returnPath, Set<Integer> textEscapeIdx, Set<Integer> patternEscapeIdx){
        if(pattern.isEmpty())
            return new ArrayList<FuzzyMatch>();

        int currMinOverlap = pattern.length() - maxNonOverlap;

        int startMaxNonOverlap = 0;
        if(nonOverlapLocation != Location.END)
            startMaxNonOverlap = maxNonOverlap;

        int endMaxNonOverlap = 0;
        if(nonOverlapLocation != Location.START)
            endMaxNonOverlap = maxNonOverlap;

        int currFullScoreThreshold = scoreThreshold.get(pattern.length());

        List<FuzzyMatch> matches = new ArrayList<>();

        if(dp == null || start == null || (returnPath && path == null)){
            dp = new Array2D<>(startMaxNonOverlap + endMaxNonOverlap + text.length() + 1);
            start = new Array2D<>(startMaxNonOverlap + endMaxNonOverlap + text.length() + 1);
            path = null;

            if(returnPath)
                path = new Array2D<Edit>(startMaxNonOverlap + endMaxNonOverlap + text.length() + 1);

            for(int i = 0; i <= startMaxNonOverlap + endMaxNonOverlap + text.length(); i++){
                dp.set(i, 0, 0);
                start.set(i, 0, i);
                if(returnPath)
                    path.set(i, 0, null);
            }
        }

        int last = pattern.length();
        for(int i = dp.getLength(0); i <= pattern.length(); i++){
            dp.set(0, i, Utils.addInt(dp.get(0, i - 1), editWeights.get(pattern.charAt(i - 1), Edit.Type.DEL)));
            start.set(0, i, 0);
            if(returnPath)
                path.set(0, i, new Edit.Delete(pattern.charAt(i - 1)));

            if(useCutoff && ((maximizeScore && dp.get(0, i) < currFullScoreThreshold) ||
                    (!maximizeScore && dp.get(0, i) > currFullScoreThreshold))){
                last = i;
                break;
            }
        }

        int prevLast = last;

        for(int i = 1; i <= startMaxNonOverlap + endMaxNonOverlap + text.length(); i++){
            for(int j = dp.getLength(i); j <= last; j++){
                if(i <= startMaxNonOverlap || i > text.length() + startMaxNonOverlap ||
                        Utils.equalsWildcard(text, i - 1 - startMaxNonOverlap, textEscapeIdx, pattern, j - 1, patternEscapeIdx, wildcardChars)){
                    dp.set(i, j, Utils.addInt(dp.get(i - 1, j - 1), editWeights.get(pattern.charAt(j - 1), Edit.Type.SAME)));
                    start.set(i, j, start.get(i - 1, j - 1));
                    if(returnPath)
                        path.set(i, j, new Edit.Same(pattern.charAt(j - 1)));
                }else{
                    int inf = maximizeScore ? Integer.MIN_VALUE : Integer.MAX_VALUE;
                    int sub = Utils.addInt(dp.get(i - 1, j - 1),
                        editWeights.get(pattern.charAt(j - 1), text.charAt(i - 1 - startMaxNonOverlap), Edit.Type.SUB));
                    int ins = j > prevLast ? inf : Utils.addInt(dp.get(i - 1, j),
                            editWeights.get(text.charAt(i - 1 - startMaxNonOverlap), Edit.Type.INS));
                    int del = Utils.addInt(dp.get(i, j - 1),
                        editWeights.get(pattern.charAt(j - 1), Edit.Type.DEL));
                    int tra = inf;

                    if(allowTranspositions && j > 1 && i > 1 + startMaxNonOverlap && i <= text.length() + startMaxNonOverlap &&
                            Utils.equalsWildcard(text, i - 1 - startMaxNonOverlap, textEscapeIdx, pattern, j - 2, patternEscapeIdx, wildcardChars) &&
                            Utils.equalsWildcard(text, i - 2 - startMaxNonOverlap, textEscapeIdx, pattern, j - 1, patternEscapeIdx, wildcardChars)){
                        tra = Utils.addInt(dp.get(i - 2, j - 2),
                            editWeights.get(pattern.charAt(j - 2), pattern.charAt(j - 1), Edit.Type.TRA));
                    }

                    if((maximizeScore && sub >= ins && sub >= del && sub >= tra) ||
                            (!maximizeScore && sub <= ins && sub <= del && sub <= tra)){
                        dp.set(i, j, sub);
                        start.set(i, j, start.get(i - 1, j - 1));
                        if(returnPath)
                            path.set(i, j, new Edit.Substitute(pattern.charAt(j - 1), text.charAt(i - 1 - startMaxNonOverlap)));
                    }else if((maximizeScore && ins >= sub && ins >= del && ins >= tra) ||
                            (!maximizeScore && ins <= sub && ins <= del && ins <= tra)){
                        dp.set(i, j, ins);
                        start.set(i, j, start.get(i - 1, j));
                        if(returnPath)
                            path.set(i, j, new Edit.Insert(text.charAt(i - 1 - startMaxNonOverlap)));
                    }else if((maximizeScore && del >= sub && del >= ins && del >= tra) ||
                            (!maximizeScore && del <= sub && del <= ins && del <= tra)){
                        dp.set(i, j, del);
                        start.set(i, j, start.get(i, j - 1));
                        if(returnPath)
                            path.set(i, j, new Edit.Delete(pattern.charAt(j - 1)));
                    }else{
                        dp.set(i, j, tra);
                        start.set(i, j, start.get(i - 2, j - 2));
                        if(returnPath)
                            path.set(i, j, new Edit.Transpose(pattern.charAt(j - 1), pattern.charAt(j - 2)));
                    }
                }
            }

            prevLast = last;

            if(useCutoff){
                while(last > 0 && ((maximizeScore && dp.get(i, last) < currFullScoreThreshold) ||
                            (!maximizeScore && dp.get(i, last) > currFullScoreThreshold))){
                    last--;
                }
            }

            if(last == pattern.length()){
                int score = dp.get(i, last);
                int index = i - 1 - startMaxNonOverlap;
                int nonOverlapLength = Math.max(startMaxNonOverlap - start.get(i, last), 0) + Math.max(index + 1 - text.length(), 0);
                int overlapLength = pattern.length() - nonOverlapLength;
                int currPartialScoreThreshold = scoreThreshold.get(overlapLength);

                if(((maximizeScore && score >= currPartialScoreThreshold) ||
                            (!maximizeScore && score <= currPartialScoreThreshold)) && overlapLength >= currMinOverlap){
                    FuzzyMatch m = new FuzzyMatch(index, i - start.get(i, last), overlapLength, score);

                    if(returnPath){
                        List<Edit> pathList = new ArrayList<>();
                        Edit curr = path.get(i, last);
                        int x = i, y = last;

                        while(curr != null){
                            pathList.add(curr);
                            x += curr.getX();
                            y += curr.getY();
                            curr = path.get(x, y);
                        }

                        Collections.reverse(pathList);
                        m.setPath(pathList);
                    }

                    matches.add(m);
                }
            }else if(useCutoff){
                last++;
            }
        }

        return matches;
    }

    public static class PatternEscape{
        String pattern;
        Set<Integer> escapeIdx;
        int idx, lcp;

        public PatternEscape(String pattern, Set<Integer> escapeIdx, int idx){
            this.pattern = pattern;
            this.escapeIdx = escapeIdx;
            this.idx = idx;
        }
    }
}
