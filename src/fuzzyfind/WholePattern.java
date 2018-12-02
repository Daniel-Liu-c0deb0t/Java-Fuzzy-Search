package fuzzyfind;

import java.util.List;
import java.util.ArrayList;

import javafuzzysearch.utils.FuzzyMatch;

public class WholePattern{
    private List<List<Pattern>> patternList;
    private List<List<Boolean>> anchored;

    public WholePattern(List<List<Pattern>> patternList, List<List<Boolean>> anchored){
        this.patternList = patternList;
        this.anchored = anchored;
    }

    public List<StrView> search(List<StrView> texts){
        return null;
    }

    private List<FuzzyMatch> search(StrView text, List<Pattern> patterns, boolean anchoredStart, boolean anchoredEnd){
        List<FuzzyMatch> res = new ArrayList<>();
        int start = 0;

        // handle required patterns
        // find best match for not anchored patterns
        // handle no match
        // implement Pattern stuff

        for(int i = 0; i < patterns.length(); i++){
            int j = i;

            while(patterns.get(j).isFixedLength())
                j++;

            List<Pattern> subList = patterns.subList(i, j);

            if(i == 0 && anchoredStart){
                res.addAll(searchFixedLength(text, start, text.length() - 1, subList, true));
            }else if(i == pattern.length() - 1 && anchoredEnd){
                res.addAll(searchFixedLength(text, start, text.length() - 1, subList, false));
            }else{
                Pattern pattern = patterns.get(i);
                StrView s = text.substring(start, Math.min(text.length(), start + pattern.maxLength()));
                List<FuzzyMatch> allMatches = pattern.searchAll(s, true);
                List<Pattern> subSubList = patterns.subList(i + 1, j);
                List<FuzzyMatch> matches;

                for(int j = allMatches.size() - 1; j >= 0; j--){
                    FuzzyMatch match = allMatches.get(j);
                    match.setIndex(start + s.length() - 1 - match.getIndex() + match.getLength() - 1);
                    matches = searchFixedLength(text, match.getIndex() + 1, text.length() - 1, subSubList, true);
                    break;
                }

                res.add(match);
                res.addAll(matches);
            }

            start = res.get(res.size() - 1).getIndex() + 1;
        }
    }

    private List<FuzzyMatch> searchArbitraryLength(StrView text, int start, int end, List<Pattern> patterns){
        text = text.substring(start, end + 1);

        boolean[][] dp = new boolean[text.length() + 1][patterns.size() + 1];
        int[][] prefix = new int[text.length() + 1][patterns.size() + 1];
        int[][] prev = new int[text.length() + 1][patterns.size() + 1];
        int[][] prevTrueLength = new int[text.length() + 1][patterns.size() + 1];

        dp[0][0] = true;
        prefix[0][0] = 1;
        prev[0][0] = -1;

        for(int i = 1; i <= patterns.size(); i++){
            if(((RepeatingPattern)patterns.get(i - 1)).getMinLength() != 0)
                break;

            dp[0][i] = true;
            prefix[0][i] = 1;
            prev[0][i] = -1;
        }

        int[] patternLength = new int[patterns.size()];

        for(int i = 1; i <= text.length(); i++){
            prefix[i][0] = 1;
            prevTrueLength[i][0] = i;

            for(int j = 1; j <= patterns.size(); j++){
                RepeatingPattern pattern = (RepeatingPattern)patterns.get(j - 1);

                if(pattern.isAcceptable(text.charAt(i - 1)))
                    patternLength[j - 1]++;
                else
                    patternLength[j - 1] = 0;

                int textHi = i - pattern.getMinLength();
                int textLo = Math.max(i - pattern.getMaxLength(), i - patternLength[j - 1]);

                if(textLo <= textHi){
                    int sum = prefix[textHi][j - 1];
                    sum -= textLo == 0 ? 0 : prefix[textLo - 1][j - 1];
                    dp[i][j] = sum > 0;

                    if(dp[i][j])
                        prevTrueLength[i][j] = 0;
                    else
                        prevTrueLength[i][j] = prevTrueLength[i - 1][j] + 1;

                    prev[i][j] = pattern.getMinLength() + prevTrueLength[textHi][j - 1];
                }

                prefix[i][j] = prefix[i - 1][j] + (dp[i][j] ? 1 : 0);
            }
        }

        List<FuzzyMatch> matches = null;

        if(dp[text.length()][patterns.size()]){
            matches = new ArrayList<FuzzyMatch>();
            int x = text.length(), y = patterns.size();

            while(prev[x][y] > -1){
                matches.add(new FuzzyMatch(start + x - 1, prev[x][y], prev[x][y], 0));
                x -= prev[x][y];
                y -= 1;
            }
        }

        return matches;
    }

    private List<FuzzyMatch> searchFixedLength(StrView text, int start, int end, List<Pattern> patterns, boolean reversed){
        List<FuzzyMatch> res = new ArrayList<>();
        int idx = reversed ? start : end;

        for(int i = 0; i < patterns.size(); i++){
            FuzzyPattern pattern = (FuzzyPattern)patterns.get(reversed ? i : (patterns.size() - 1 - i));
            StrView s;

            if(reversed)
                s = text.substring(idx, Math.min(end + 1, idx + pattern.getMaxLength()));
            else
                s = text.substring(Math.max(start, idx - pattern.getMaxLength() + 1), idx + 1);

            FuzzyMatch match = pattern.matchBest(s, reversed);

            if(reversed){
                match.setIndex(idx + match.getLength() - 1);
                idx += match.getLength();
            }else{
                match.setIndex(idx);
                idx -= match.getLength();
            }

            res.add(match);
        }

        return res;
    }
}
