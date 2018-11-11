package javafuzzysearch.searchers;

import java.util.ArrayList;

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

    public ArrayList<FuzzyMatch> search(String text, String pattern){
        int[][] dp = new int[text.length() + 1][pattern.length() + 1];
        int currentFullMaxEdits = maxEdits.get(pattern.length());
        int last = Math.min(currentFullMaxEdits + 1, pattern.length());

        for(int i = 0; i <= text.length(); i++)
            dp[i][0] = 0;
        for(int i = 0; i <= last; i++)
            dp[0][i] = i;

        for(int i = 1; i <= text.length(); i++){
            for(int j = 1; j <= last; j++){
                if(text.charAt(i - 1) == pattern.charAt(j - 1)){
                    dp[i][j] = dp[i - 1][j - 1];
                }else{
                    int sub = dp[i - 1][j - 1];
                    int ins = dp[i - 1][j];
                    int del = dp[i][j - 1];
                    int tra = Integer.MAX_VALUE;

                    if(allowTranspositions)
                        tra = dp[i - 2][j - 2];

                    if(sub <= ins && sub <= del && sub <= tra){
                        dp[i][j] = sub + 1;
                    }else if(ins <= sub && ins <= del && ins <= tra){
                        dp[i][j] = ins + 1;
                    }else if(del <= sub && del <= ins && del <= tra){
                        dp[i][j] = del + 1;
                    }else{

                    }
                }
            }

            while(dp[i][last] > currentFullMaxEdits)
                last--;

            if(last == pattern.length()){
                System.out.println(dp[i][last] + " " + (i - 1));
            }else{
                last++;
            }
        }

        return null;
    }
}
