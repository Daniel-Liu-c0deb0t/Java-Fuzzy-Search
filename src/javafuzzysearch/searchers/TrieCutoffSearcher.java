package javafuzzysearch.searchers;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import javafuzzysearch.utils.FuzzyMatch;
import javafuzzysearch.utils.Edit;
import javafuzzysearch.utils.Utils;
import javafuzzysearch.utils.LengthParam;
import javafuzzysearch.utils.EditWeights;
import javafuzzysearch.utils.Location;
import javafuzzysearch.utils.Array2D;

/**
 * Implementation of vanilla DP with Ukkonen's cutoff algorithm for computing Levenshtein distance.
 */
public class TrieCutoffSearcher{
    private LengthParam scoreThreshold = new LengthParam(0, false, false);
    private int maxNonOverlap = 0;
    private EditWeights editWeights = new EditWeights();
    private boolean allowTranspositions = false, maximizeScore = false;
    private Map<Character, Set<Character>> wildcardChars = new HashMap<>();
    private Location nonOverlapLocation = Location.ANY;

    public TrieCutoffSearcher scoreThreshold(LengthParam scoreThreshold){
        this.scoreThreshold = scoreThreshold;
        return this;
    }

    public TrieCutoffSearcher maxNonOverlap(int maxNonOverlap, Location nonOverlapLocation){
        this.maxNonOverlap = maxNonOverlap;
        this.nonOverlapLocation = nonOverlapLocation;
        return this;
    }

    public TrieCutoffSearcher allowTranspositions(){
        this.allowTranspositions = true;
        return this;
    }

    public TrieCutoffSearcher editWeights(EditWeights editWeights){
        this.editWeights = editWeights;
        return this;
    }

    public TrieCutoffSearcher maximizeScore(){
        this.maximizeScore = true;
        return this;
    }

    public TrieCutoffSearcher wildcardChars(Map<Character, Set<Character>> wildcardChars){
        this.wildcardChars = wildcardChars;
        return this;
    }

    public List<FuzzyMatch> search(String text, String pattern, Array2D<Integer> dp, Array2D<Integer> start,
            Array2D<Edit> path, boolean returnPath, Set<Integer> textEscapeIdx, Set<Integer> patternEscapeIdx){
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

        if(dp == null || start == null || path == null){
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

            if((maximizeScore && dp.get(0, i) < currFullScoreThreshold) ||
                    (!maximizeScore && dp.get(0, i) > currFullScoreThreshold)){
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
                    int sub = Utils.addInt(dp.get(i - 1, j - 1),
                        editWeights.get(pattern.charAt(j - 1), text.charAt(i - 1 - startMaxNonOverlap), Edit.Type.SUB));
                    int ins = j > prevLast ? Integer.MAX_VALUE : Utils.addInt(dp.get(i - 1, j),
                            editWeights.get(text.charAt(i - 1 - startMaxNonOverlap), Edit.Type.INS));
                    int del = Utils.addInt(dp.get(i, j - 1),
                        editWeights.get(pattern.charAt(j - 1), Edit.Type.DEL));
                    int tra = Integer.MAX_VALUE;

                    if(allowTranspositions && j > 1 && i > 1 + startMaxNonOverlap && i <= text.length() + startMaxNonOverlap &&
                            Utils.equalsWildcard(text, i - 1 - startMaxNonOverlap, textEscapeIdx, pattern, j - 2, patternEscapeIdx, wildcardChars) &&
                            Utils.equalsWildcard(text, i - 2 - startMaxNonOverlap, textEscapeIdx, pattern, j - 1, patternEscapeIdx, wildcardChars)){
                        tra = Utils.addInt(dp.get(i - 2, j - 2),
                            editWeights.get(pattern.charAt(j - 2), pattern.charAt(j - 1), Edit.Type.TRA));
                    }

                    if(sub <= ins && sub <= del && sub <= tra){
                        dp.set(i, j, sub);
                        start.set(i, j, start.get(i - 1, j - 1));
                        if(returnPath)
                            path.set(i, j, new Edit.Substitute(pattern.charAt(j - 1), text.charAt(i - 1 - startMaxNonOverlap)));
                    }else if(ins <= sub && ins <= del && ins <= tra){
                        dp.set(i, j, ins);
                        start.set(i, j, start.get(i - 1, j));
                        if(returnPath)
                            path.set(i, j, new Edit.Insert(text.charAt(i - 1 - startMaxNonOverlap)));
                    }else if(del <= sub && del <= ins && del <= tra){
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

            while((maximizeScore && dp.get(i, last) < currFullScoreThreshold) ||
                    (!maximizeScore && dp.get(i, last) > currFullScoreThreshold)){
                last--;
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
            }else{
                last++;
            }
        }

        return matches;
    }
}
