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
import javafuzzysearch.utils.StrView;

/**
 * Implementation of vanilla DP with Ukkonen's cutoff algorithm for computing Levenshtein distance.
 */
public class CutoffSearcher{
    private LengthParam scoreThreshold = new LengthParam(0, false, false);
    private LengthParam minOverlap = new LengthParam(0, false, true);
    private EditWeights editWeights = new EditWeights();
    private boolean allowTranspositions = false, maximizeScore = false;
    private Map<Character, Set<Character>> patternWildcard = new HashMap<>();
    private Map<Character, Set<Character>> textWildcard = new HashMap<>();
    private Location nonOverlapLocation = Location.ANY;
    private boolean useCutoff = true;

    public CutoffSearcher scoreThreshold(LengthParam scoreThreshold){
        this.scoreThreshold = scoreThreshold;
        return this;
    }

    public CutoffSearcher minOverlap(LengthParam minOverlap, Location nonOverlapLocation){
        this.minOverlap = minOverlap;
        this.nonOverlapLocation = nonOverlapLocation;
        return this;
    }

    public CutoffSearcher allowTranspositions(){
        this.allowTranspositions = true;
        return this;
    }

    public CutoffSearcher editWeights(EditWeights editWeights){
        this.editWeights = editWeights;
        useCutoff = editWeights.isDiagonalMonotonic();
        return this;
    }

    public CutoffSearcher maximizeScore(){
        this.maximizeScore = true;
        return this;
    }

    public CutoffSearcher wildcardChars(Map<Character, Set<Character>> textWildcard, Map<Character, Set<Character>> patternWildcard){
        this.patternWildcard = patternWildcard;
        this.textWildcard = textWildcard;
        return this;
    }

    public List<FuzzyMatch> search(StrView text, StrView pattern, boolean returnPath){
        return search(text, pattern, returnPath, new HashSet<Integer>(), new HashSet<Integer>());
    }

    public List<FuzzyMatch> search(StrView text, StrView pattern, boolean returnPath, Set<Integer> textEscapeIdx, Set<Integer> patternEscapeIdx){
        if(pattern.isEmpty())
            return new ArrayList<FuzzyMatch>();

        int currMinOverlap = minOverlap.get(pattern.length());
        int currMaxNonOverlap = pattern.length() - currMinOverlap;

        int startMaxNonOverlap = 0;
        if(nonOverlapLocation != Location.END)
            startMaxNonOverlap = currMaxNonOverlap;

        int endMaxNonOverlap = 0;
        if(nonOverlapLocation != Location.START)
            endMaxNonOverlap = currMaxNonOverlap;

        int dpLength;

        if(returnPath)
            dpLength = startMaxNonOverlap + endMaxNonOverlap + text.length() + 1;
        else if(allowTranspositions)
            dpLength = 3;
        else
            dpLength = 2;

        int[][] dp = new int[dpLength][pattern.length() + 1];
        int[][] start = new int[dpLength][pattern.length() + 1];
        Edit[][] path = null;

        if(returnPath)
            path = new Edit[dpLength][pattern.length() + 1];

        int currFullScoreThreshold = scoreThreshold.get(pattern.length());

        List<FuzzyMatch> matches = new ArrayList<>();

        if(!returnPath && allowTranspositions){
            for(int i = 1; i < dpLength; i++){
                dp[i][0] = 0;
                start[i][0] = i - 1;
            }
        }else{
            for(int i = 0; i < dpLength; i++){
                dp[i][0] = 0;
                start[i][0] = i;
                if(returnPath)
                    path[i][0] = null;
            }
        }

        int last = pattern.length();
        for(int i = 1; i <= pattern.length(); i++){
            dp[0][i] = Utils.addInt(dp[0][i - 1], editWeights.get(pattern.charAt(i - 1), Edit.Type.DEL));
            start[0][i] = 0;
            if(returnPath)
                path[0][i] = new Edit.Delete(pattern.charAt(i - 1));

            if(useCutoff && ((maximizeScore && dp[0][i] < currFullScoreThreshold) ||
                    (!maximizeScore && dp[0][i] > currFullScoreThreshold))){
                last = i;
                break;
            }
        }

        int prevLast = last;

        for(int i = 1; i <= startMaxNonOverlap + endMaxNonOverlap + text.length(); i++){
            int currIdx;

            if(returnPath)
                currIdx = i;
            else if(allowTranspositions)
                currIdx = 2;
            else
                currIdx = 1;

            for(int j = 1; j <= last; j++){
                if(i <= startMaxNonOverlap || i > text.length() + startMaxNonOverlap ||
                        Utils.equalsWildcard(text, i - 1 - startMaxNonOverlap, textEscapeIdx, pattern, j - 1, patternEscapeIdx, textWildcard, patternWildcard)){
                    dp[currIdx][j] = Utils.addInt(dp[currIdx - 1][j - 1], editWeights.get(pattern.charAt(j - 1), Edit.Type.SAME));
                    start[currIdx][j] = start[currIdx - 1][j - 1];
                    if(returnPath)
                        path[currIdx][j] = new Edit.Same(pattern.charAt(j - 1));
                }else{
                    int inf = maximizeScore ? Integer.MIN_VALUE : Integer.MAX_VALUE;
                    int sub = Utils.addInt(dp[currIdx - 1][j - 1],
                        editWeights.get(pattern.charAt(j - 1), text.charAt(i - 1 - startMaxNonOverlap), Edit.Type.SUB));
                    int ins = j > prevLast ? inf : Utils.addInt(dp[currIdx - 1][j],
                            editWeights.get(text.charAt(i - 1 - startMaxNonOverlap), Edit.Type.INS));
                    int del = Utils.addInt(dp[currIdx][j - 1],
                        editWeights.get(pattern.charAt(j - 1), Edit.Type.DEL));
                    int tra = inf;

                    if(allowTranspositions && j > 1 && i > 1 + startMaxNonOverlap && i <= text.length() + startMaxNonOverlap &&
                            Utils.equalsWildcard(text, i - 1 - startMaxNonOverlap, textEscapeIdx, pattern, j - 2, patternEscapeIdx, textWildcard, patternWildcard) &&
                            Utils.equalsWildcard(text, i - 2 - startMaxNonOverlap, textEscapeIdx, pattern, j - 1, patternEscapeIdx, textWildcard, patternWildcard)){
                        tra = Utils.addInt(dp[currIdx - 2][j - 2],
                            editWeights.get(pattern.charAt(j - 2), pattern.charAt(j - 1), Edit.Type.TRA));
                    }

                    if((maximizeScore && sub >= ins && sub >= del && sub >= tra) ||
                            (!maximizeScore && sub <= ins && sub <= del && sub <= tra)){
                        dp[currIdx][j] = sub;
                        start[currIdx][j] = start[currIdx - 1][j - 1];
                        if(returnPath)
                            path[currIdx][j] = new Edit.Substitute(pattern.charAt(j - 1), text.charAt(i - 1 - startMaxNonOverlap));
                    }else if((maximizeScore && ins >= sub && ins >= del && ins >= tra) ||
                            (!maximizeScore && ins <= sub && ins <= del && ins <= tra)){
                        dp[currIdx][j] = ins;
                        start[currIdx][j] = start[currIdx - 1][j];
                        if(returnPath)
                            path[currIdx][j] = new Edit.Insert(text.charAt(i - 1 - startMaxNonOverlap));
                    }else if((maximizeScore && del >= sub && del >= ins && del >= tra) ||
                            (!maximizeScore && del <= sub && del <= ins && del <= tra)){
                        dp[currIdx][j] = del;
                        start[currIdx][j] = start[currIdx][j - 1];
                        if(returnPath)
                            path[currIdx][j] = new Edit.Delete(pattern.charAt(j - 1));
                    }else{
                        dp[currIdx][j] = tra;
                        start[currIdx][j] = start[currIdx - 2][j - 2];
                        if(returnPath)
                            path[currIdx][j] = new Edit.Transpose(pattern.charAt(j - 1), pattern.charAt(j - 2));
                    }
                }
            }

            prevLast = last;

            if(useCutoff){
                while(last > 0 && ((maximizeScore && dp[currIdx][last] < currFullScoreThreshold) ||
                            (!maximizeScore && dp[currIdx][last] > currFullScoreThreshold))){
                    last--;
                }
            }

            if(last == pattern.length()){
                int score = dp[currIdx][last];
                int index = i - 1 - startMaxNonOverlap;
                int nonOverlapLength = Math.max(startMaxNonOverlap - start[currIdx][last], 0) + Math.max(index + 1 - text.length(), 0);
                int overlapLength = pattern.length() - nonOverlapLength;
                int currPartialScoreThreshold = scoreThreshold.get(overlapLength);

                if(((maximizeScore && score >= currPartialScoreThreshold) ||
                            (!maximizeScore && score <= currPartialScoreThreshold)) && overlapLength >= currMinOverlap){
                    FuzzyMatch m = new FuzzyMatch(index, i - start[currIdx][last], overlapLength, score);

                    if(returnPath){
                        List<Edit> pathList = new ArrayList<>();
                        Edit curr = path[currIdx][last];
                        int x = currIdx, y = last;

                        while(curr != null){
                            pathList.add(curr);
                            x += curr.getX();
                            y += curr.getY();
                            curr = path[x][y];
                        }

                        Collections.reverse(pathList);
                        m.setPath(pathList);
                    }

                    matches.add(m);
                }
            }else if(useCutoff){
                last++;
            }

            if(!returnPath){
                for(int j = 0; j <= pattern.length(); j++){
                    dp[0][j] = dp[1][j];
                    start[0][j] = start[1][j];

                    if(allowTranspositions){
                        dp[1][j] = dp[2][j];
                        start[1][j] = start[2][j];
                    }
                }

                dp[currIdx][0] = 0;
                start[currIdx][0] = i + 1;
            }
        }

        return matches;
    }
}
