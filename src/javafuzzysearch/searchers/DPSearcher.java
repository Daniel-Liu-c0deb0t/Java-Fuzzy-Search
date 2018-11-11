package javafuzzysearch.searchers;

import java.util.ArrayList;
import java.util.List;

import javafuzzysearch.utils.FuzzyMatch;
import javafuzzysearch.utils.Utils;
import javafuzzysearch.utils.LengthParam;

public class DPSearcher{
    private LengthParam maxEdits;
    private LengthParam minOverlap;
    private boolean allowTranspositions;
    
    public DPSearcher(LengthParam maxEdits, LengthParam minOverlap, boolean allowTranspositions){
        this.maxEdits = maxEdits;
        this.minOverlap = minOverlap;
        this.allowTranspositions = allowTranspositions;
    }

    public List<FuzzyMatch> search(String text, String pattern){
        if(pattern.isEmpty()){
            return new ArrayList<FuzzyMatch>();
        }

        int[][] dp = new int[text.length() + 1][pattern.length() + 1];
        int[][] start = new int[text.length() + 1][pattern.length() + 1];

        int currFullMaxEdits = maxEdits.get(pattern.length());
        int last = Math.min(currFullMaxEdits + 1, pattern.length());
        int prevLast = last;

        List<FuzzyMatch> matches = new ArrayList<>();

        for(int i = 0; i <= text.length(); i++){
            dp[i][0] = 0;
            start[i][0] = i;
        }

        for(int i = 0; i <= last; i++){
            dp[0][i] = i;
            start[0][i] = 0;
        }

        for(int i = 1; i <= text.length(); i++){
            for(int j = 1; j <= last; j++){
                if(text.charAt(i - 1) == pattern.charAt(j - 1)){
                    dp[i][j] = dp[i - 1][j - 1];
                    start[i][j] = start[i - 1][j - 1];
                }else{
                    int sub = dp[i - 1][j - 1];
                    int ins = j > prevLast ? Integer.MAX_VALUE : dp[i - 1][j];
                    int del = dp[i][j - 1];
                    int tra = Integer.MAX_VALUE;

                    if(allowTranspositions && i > 1 && j > 1 &&
                            text.charAt(i - 1) == pattern.charAt(j - 2) && text.charAt(i - 2) == pattern.charAt(j - 1)){
                        tra = dp[i - 2][j - 2];
                    }

                    if(sub <= ins && sub <= del && sub <= tra){
                        dp[i][j] = sub + 1;
                        start[i][j] = start[i - 1][j - 1];
                    }else if(ins <= sub && ins <= del && ins <= tra){
                        dp[i][j] = ins + 1;
                        start[i][j] = start[i - 1][j];
                    }else if(del <= sub && del <= ins && del <= tra){
                        dp[i][j] = del + 1;
                        start[i][j] = start[i][j - 1];
                    }else{
                        dp[i][j] = tra + 1;
                        start[i][j] = start[i - 2][j - 2];
                    }
                }
            }

            prevLast = last;

            while(dp[i][last] > currFullMaxEdits)
                last--;

            if(last == pattern.length()){
                int dist = dp[i][last];
                int index = i - 1;
                int length = index - start[i][last] + 1;
                int currPartialMaxEdits = maxEdits.get(length);

                if(dist <= currPartialMaxEdits){
                    matches.add(new FuzzyMatch(index, length, dist));
                }
            }else{
                last++;
            }
        }

        return matches;
    }
}
