package javafuzzysearch.searchers;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import javafuzzysearch.utils.FuzzyMatch;
import javafuzzysearch.utils.Utils;
import javafuzzysearch.utils.LengthParam;

/**
 * Implementation of vanilla DP with Ukkonen's cutoff algorithm for computing Levenshtein distance.
 */
public class CutoffSearcher{
    private LengthParam maxEdits;
    private LengthParam minOverlap;
    private boolean allowTranspositions, useMinOverlap;
    
    public CutoffSearcher(LengthParam maxEdits, LengthParam minOverlap, boolean allowTranspositions){
        this.maxEdits = maxEdits;
        this.minOverlap = minOverlap;
        this.allowTranspositions = allowTranspositions;
        this.useMinOverlap = true;
    }

    public CutoffSearcher(LengthParam maxEdits, boolean allowTranspositions){
        this.maxEdits = maxEdits;
        this.minOverlap = new LengthParam(0, false, true);
        this.allowTranspositions = allowTranspositions;
        this.useMinOverlap = false;
    }

    public List<FuzzyMatch> search(String text, String pattern, boolean returnPath){
        if(pattern.isEmpty()){
            return new ArrayList<FuzzyMatch>();
        }

        int currMinOverlap = minOverlap.get(pattern.length());
        int currMaxNonOverlap = pattern.length() - currMinOverlap;

        int[][] dp = new int[currMaxNonOverlap * 2 + text.length() + 1][pattern.length() + 1];
        int[][] start = new int[currMaxNonOverlap * 2 + text.length() + 1][pattern.length() + 1];
        FuzzyMatch.Edit[][] path = null;

        if(returnPath)
            path = new FuzzyMatch.Edit[currMaxNonOverlap * 2 + text.length() + 1][pattern.length() + 1];

        int currFullMaxEdits = maxEdits.get(pattern.length());
        int last = Math.min(currFullMaxEdits + 1, pattern.length());
        int prevLast = last;

        List<FuzzyMatch> matches = new ArrayList<>();

        for(int i = 0; i <= currMaxNonOverlap * 2 + text.length(); i++){
            dp[i][0] = 0;
            start[i][0] = i;
            if(returnPath)
                path[i][0] = null;
        }

        for(int i = 1; i <= last; i++){
            dp[0][i] = i;
            start[0][i] = 0;
            if(returnPath)
                path[0][i] = FuzzyMatch.Edit.DEL;
        }

        for(int i = 1; i <= currMaxNonOverlap * 2 + text.length(); i++){
            for(int j = 1; j <= last; j++){
                if(i <= currMaxNonOverlap || i > text.length() + currMaxNonOverlap ||
                        text.charAt(i - 1 - currMaxNonOverlap) == pattern.charAt(j - 1)){
                    dp[i][j] = dp[i - 1][j - 1];
                    start[i][j] = start[i - 1][j - 1];
                    if(returnPath)
                        path[i][j] = FuzzyMatch.Edit.SAME;
                }else{
                    int sub = dp[i - 1][j - 1];
                    int ins = j > prevLast ? Integer.MAX_VALUE : dp[i - 1][j];
                    int del = dp[i][j - 1];
                    int tra = Integer.MAX_VALUE;

                    if(allowTranspositions && j > 1 && i > 1 + currMaxNonOverlap && i <= text.length() + currMaxNonOverlap &&
                            text.charAt(i - 1 - currMaxNonOverlap) == pattern.charAt(j - 2) &&
                            text.charAt(i - 2 - currMaxNonOverlap) == pattern.charAt(j - 1)){
                        tra = dp[i - 2][j - 2];
                    }

                    if(sub <= ins && sub <= del && sub <= tra){
                        dp[i][j] = sub + 1;
                        start[i][j] = start[i - 1][j - 1];
                        if(returnPath)
                            path[i][j] = FuzzyMatch.Edit.SUB;
                    }else if(ins <= sub && ins <= del && ins <= tra){
                        dp[i][j] = ins + 1;
                        start[i][j] = start[i - 1][j];
                        if(returnPath)
                            path[i][j] = FuzzyMatch.Edit.INS;
                    }else if(del <= sub && del <= ins && del <= tra){
                        dp[i][j] = del + 1;
                        start[i][j] = start[i][j - 1];
                        if(returnPath)
                            path[i][j] = FuzzyMatch.Edit.DEL;
                    }else{
                        dp[i][j] = tra + 1;
                        start[i][j] = start[i - 2][j - 2];
                        if(returnPath)
                            path[i][j] = FuzzyMatch.Edit.TRA;
                    }
                }
            }

            prevLast = last;

            while(dp[i][last] > currFullMaxEdits)
                last--;

            if(last == pattern.length()){
                int dist = dp[i][last];
                int index = i - 1 - currMaxNonOverlap;
                int length = Math.min(index + 1, Math.min(i - start[i][last], currMaxNonOverlap + text.length() + 1 - start[i][last]));
                int currPartialMaxEdits = maxEdits.get(length);

                if(dist <= currPartialMaxEdits && (!useMinOverlap || length >= currMinOverlap)){
                    FuzzyMatch m = new FuzzyMatch(index, useMinOverlap ? length : (i - start[i][last]), dist);
                    if(returnPath){
                        List<FuzzyMatch.Edit> pathList = new ArrayList<>();
                        FuzzyMatch.Edit curr = path[i][last];
                        int x = i, y = last;
                        while(curr != null){
                            pathList.add(curr);
                            x += curr.x;
                            y += curr.y;
                            curr = path[x][y];
                        }

                        Collections.reverse(pathList);
                        m.paths(pathList);
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
