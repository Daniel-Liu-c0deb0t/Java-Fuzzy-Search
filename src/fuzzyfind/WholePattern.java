package fuzzyfind;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;

import javafuzzysearch.utils.FuzzyMatch;
import javafuzzysearch.utils.StrView;

public class WholePattern{
    private List<List<Pattern>> patternList;
    private List<List<Boolean>> anchored;

    public WholePattern(List<List<Pattern>> patternList, List<List<Boolean>> anchored){
        this.patternList = patternList;
        this.anchored = anchored;
    }

    public List<List<StrView>> search(List<List<StrView>> texts, List<List<Set<Integer>>> textEscapeIdx){
        return null;
    }

    private List<FuzzyMatch> search(StrView text, Set<Integer> textEscapeIdx, List<Pattern> patterns, boolean anchoredStart, boolean anchoredEnd){
        List<FuzzyMatch> res = new ArrayList<>();
        int start = 0;
        int prev = -1;
        int i = 0;

        while(i < patterns.size()){
            while(i < patterns.size() && !(patterns.get(i) instanceof FixedPattern)){
                if(prev == -1)
                    prev = 0;
                i++;
            }

            if(i >= patterns.size()){
                List<Pattern> repeatingList = patterns.subList(prev, i);
                List<FuzzyMatch> repeatingMatches =
                    searchRepeatingPatterns(text, start, text.length() - 1, repeatingList);

                if(repeatingMatches == null)
                    return null;

                res.addAll(repeatingMatches);
                break;
            }

            int j = i;

            while(j < patterns.size() && patterns.get(j) instanceof FixedPattern)
                j++;

            List<Pattern> subList = patterns.subList(i, j);

            if(i == 0 && anchoredStart){
                List<FuzzyMatch> matches = searchFuzzyPatterns(text, textEscapeIdx, start, text.length() - 1, subList, true);

                if(matches == null)
                    return null;

                res.addAll(matches);
            }else if(i == patterns.size() - 1 && anchoredEnd){
                List<FuzzyMatch> fuzzyMatches = searchFuzzyPatterns(text, textEscapeIdx, start, text.length() - 1, subList, false);

                if(fuzzyMatches == null)
                    return null;

                if(prev != -1){
                    int endIdx = text.length() - 1;
                    int k = 0;

                    while(k < fuzzyMatches.size() && fuzzyMatches.get(k) == null)
                        k++;

                    if(k < fuzzyMatches.size()){
                        endIdx = fuzzyMatches.get(k).getIndex() - fuzzyMatches.get(k).getLength();
                    }

                    List<Pattern> repeatingList = patterns.subList(prev, i);
                    List<FuzzyMatch> repeatingMatches =
                        searchRepeatingPatterns(text, start, endIdx, repeatingList);

                    if(repeatingMatches == null)
                        return null;

                    res.addAll(repeatingMatches);
                }

                res.addAll(fuzzyMatches);
            }else{
                StrView s = text.substring(start);
                boolean foundMatch = false;

                outerLoop:
                for(int k = i; k < j; k++){
                    FixedPattern pattern = (FixedPattern)patterns.get(k);
                    List<FuzzyMatch> allMatches = pattern.searchAll(s, textEscapeIdx, start, true);

                    if(allMatches.isEmpty())
                        continue;

                    List<Pattern> fuzzyList = patterns.subList(k + 1, j);

                    for(int l = allMatches.size() - 1; l >= 0; l--){
                        FuzzyMatch startMatch = allMatches.get(l);
                        startMatch.setIndex(start + s.length() - 1 - startMatch.getIndex() + startMatch.getLength() - 1);

                        List<Pattern> repeatingList = null;
                        List<FuzzyMatch> repeatingMatches = null;

                        if(prev != -1){
                            repeatingList = patterns.subList(prev, i);
                            repeatingMatches = searchRepeatingPatterns(text, start, startMatch.getIndex() - startMatch.getLength(), repeatingList);

                            if(repeatingMatches == null)
                                continue;
                        }

                        List<FuzzyMatch> fuzzyMatches =
                            searchFuzzyPatterns(text, textEscapeIdx, startMatch.getIndex() + 1, text.length() - 1, fuzzyList, true);

                        if(fuzzyMatches == null)
                            continue;

                        if(prev != -1)
                            res.addAll(repeatingMatches);

                        res.add(startMatch);
                        res.addAll(fuzzyMatches);
                        foundMatch = true;
                        break outerLoop;
                    }

                    if(pattern.isRequired())
                        break;
                }

                if(!foundMatch)
                    return null;
            }

            for(int k = j - 1; k >= i; k--){
                if(res.get(k) == null)
                    continue;

                start = res.get(k).getIndex() + 1;
                break;
            }

            prev = j;
            i = j;
        }

        return res;
    }

    private List<FuzzyMatch> searchRepeatingPatterns(StrView text, int start, int end, List<Pattern> patterns){
        text = text.substring(start, end + 1);

        boolean[][] dp = new boolean[text.length() + 1][patterns.size() + 1];
        int[][] prefix = new int[text.length() + 1][patterns.size() + 1];
        int[][] prev = new int[text.length() + 1][patterns.size() + 1];
        int[][] prevTrueLength = new int[text.length() + 1][patterns.size() + 1];

        dp[0][0] = true;
        prefix[0][0] = 1;
        prev[0][0] = -1;

        for(int i = 1; i <= patterns.size(); i++){
            if(((RepeatingIntervalPattern)patterns.get(i - 1)).getMinLength() != 0)
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
                RepeatingIntervalPattern pattern = (RepeatingIntervalPattern)patterns.get(j - 1);

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

    private List<FuzzyMatch> searchFuzzyPatterns(StrView text, Set<Integer> textEscapeIdx, int start, int end, List<Pattern> patterns, boolean reversed){
        List<FuzzyMatch> matches = new ArrayList<>();
        int idx = reversed ? start : end;

        for(int i = 0; i < patterns.size(); i++){
            FixedPattern pattern = (FixedPattern)patterns.get(reversed ? i : (patterns.size() - 1 - i));
            StrView s;

            if(reversed)
                s = text.substring(idx, end + 1);
            else
                s = text.substring(start, idx + 1);

            FuzzyMatch match = pattern.matchBest(s, textEscapeIdx, reversed ? idx : start, reversed);

            if(match == null){
                if(pattern.isRequired())
                    return null;
            }else{
                if(reversed){
                    match.setIndex(idx + match.getLength() - 1);
                    idx += match.getLength();
                }else{
                    match.setIndex(idx);
                    idx -= match.getLength();
                }
            }

            matches.add(match);
        }

        return matches;
    }
}
