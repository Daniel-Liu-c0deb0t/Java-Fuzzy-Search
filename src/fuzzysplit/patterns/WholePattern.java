package fuzzysplit.patterns;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Arrays;

import javafuzzysearch.utils.StrView;
import javafuzzysearch.utils.Array2D;

import fuzzysplit.utils.PatternMatch;
import fuzzysplit.utils.Variables;
import fuzzysplit.utils.Parameters;

public class WholePattern{
    private List<List<List<Pattern>>> patternList;
    private List<List<Integer>> indexToPattern;

    public WholePattern(List<List<List<Pattern>>> patternList, List<List<Integer>> indexList){
        this.patternList = patternList;
        this.indexToPattern = new ArrayList<List<Integer>>();

        int idx = Integer.MIN_VALUE;

        for(int i = 0; i < indexList.size(); i++){
            List<Integer> list = indexList.get(i);

            for(int j = 0; j < list.size(); j++){
                int curr = list.get(j) == null ? idx++ : list.get(j);
                indexToPattern.add(Arrays.asList(curr, i, j));
            }
        }

        Collections.sort(indexToPattern, (a, b) -> Integer.compare(a.get(0), b.get(0)));
    }

    public List<List<List<PatternMatch>>> search(List<List<StrView>> texts, Variables vars){
        List<List<List<PatternMatch>>> res = new ArrayList<>();

        for(int i = 0; i < patternList.size(); i++){
            int length = patternList.get(i).size();
            res.add(new ArrayList<List<PatternMatch>>());

            for(int j = 0; j < length; j++){
                res.get(i).add(null);
            }
        }

        for(List<Integer> indexes : indexToPattern){
            StrView currText = texts.get(indexes.get(1)).get(indexes.get(2));
            List<Pattern> currPatterns = patternList.get(indexes.get(1)).get(indexes.get(2));
            List<PatternMatch> matches = search(currText, currPatterns, vars);

            if(matches == null)
                return null;

            res.get(indexes.get(1)).set(indexes.get(2), matches);
        }

        return res;
    }

    private List<PatternMatch> search(StrView text, List<Pattern> patterns, Variables vars){
        List<Parameters> params = new ArrayList<>();

        for(Pattern p : patterns)
            params.add(p.updateParams(vars));

        List<PatternMatch> res = new ArrayList<>();
        List<Pattern> intervalPatterns = new ArrayList<>();
        List<Parameters> intervalParams = new ArrayList<>();
        int intervalPatternMinSum = 0;
        int intervalPatternMaxSum = 0;
        Map<Integer, Integer> emptyOptionalMatchIdx = new HashMap<>();
        int start = 0;
        int i = 0;

        while(i < patterns.size()){
            while(i < patterns.size() && !(patterns.get(i) instanceof FixedPattern)){
                intervalPatterns.add(patterns.get(i));
                intervalParams.add(params.get(i));
                intervalPatternMinSum += ((RepeatingIntervalPattern)patterns.get(i)).getMinLength(params.get(i));
                intervalPatternMaxSum += ((RepeatingIntervalPattern)patterns.get(i)).getMaxLength(params.get(i));
                i++;
            }

            // used for interval-length patterns
            Array2D<Boolean> dp = new Array2D<Boolean>(intervalPatterns.size() + 1);
            Array2D<Integer> prefix = new Array2D<Integer>(intervalPatterns.size() + 1);
            Array2D<Integer> prev = new Array2D<Integer>(intervalPatterns.size() + 1);
            Array2D<Integer> prevTrueLength = new Array2D<Integer>(intervalPatterns.size() + 1);
            int[] patternLength = new int[intervalPatterns.size()];

            if(i >= patterns.size()){
                if(!intervalPatterns.isEmpty()){
                    List<PatternMatch> repeatingMatches =
                        searchIntervalPatterns(dp, prefix, prev, prevTrueLength, patternLength,
                                text, start, text.length() - 1, intervalPatterns, intervalParams);

                    if(repeatingMatches == null)
                        return null;

                    for(int m = 0; m < repeatingMatches.size(); m++){
                        PatternMatch match = repeatingMatches.get(m);
                        res.add(match);

                        if(emptyOptionalMatchIdx.containsKey(m)){
                            int val = emptyOptionalMatchIdx.get(m);

                            for(int n = 0; n < val; n++)
                                res.add(new PatternMatch(match.getIndex(), 0, 0, 0, -1));
                        }
                    }
                }

                break;
            }

            int j = i;

            while(j < patterns.size() && patterns.get(j) instanceof FixedPattern)
                j++;

            List<Pattern> subList = patterns.subList(i, j);
            List<Parameters> paramsSubList = params.subList(i, j);

            if(i == 0){
                List<PatternMatch> matches = searchFixedPatterns(text, start, text.length() - 1, subList, true, null, 0, paramsSubList);

                if(matches == null)
                    return null;

                res.addAll(matches);
            }else if(j == patterns.size() - 1){
                List<PatternMatch> fuzzyMatches = searchFixedPatterns(text, start, text.length() - 1, subList, false, null, patterns.size() - 1, paramsSubList);

                if(fuzzyMatches == null)
                    return null;

                if(!intervalPatterns.isEmpty()){
                    int endIdx = fuzzyMatches.get(0).getIndex() - fuzzyMatches.get(0).getLength();

                    List<PatternMatch> repeatingMatches =
                        searchIntervalPatterns(dp, prefix, prev, prevTrueLength, patternLength,
                                text, start, endIdx, intervalPatterns, intervalParams);

                    if(repeatingMatches == null)
                        return null;

                    for(int m = 0; m < repeatingMatches.size(); m++){
                        PatternMatch match = repeatingMatches.get(m);
                        res.add(match);

                        if(emptyOptionalMatchIdx.containsKey(m)){
                            int val = emptyOptionalMatchIdx.get(m);

                            for(int n = 0; n < val; n++)
                                res.add(new PatternMatch(match.getIndex(), 0, 0, 0, -1));
                        }
                    }
                    emptyOptionalMatchIdx.clear();
                    intervalPatterns.clear();
                    intervalParams.clear();
                    intervalPatternMinSum = 0;
                    intervalPatternMaxSum = 0;
                }

                res.addAll(fuzzyMatches);
            }else{
                StrView s = text.substring(start);
                boolean foundMatch = false;
                Map<Integer, Map<Integer, PatternMatch>> matchCache = new HashMap<>();

                outerLoop:
                for(int k = i; k < j; k++){
                    FixedPattern pattern = (FixedPattern)patterns.get(k);
                    List<PatternMatch> allMatches = pattern.searchAll(s, true, params.get(k));

                    if(allMatches.isEmpty()){
                        if(pattern.isRequired())
                            return null;
                        continue;
                    }

                    List<Pattern> fuzzyList = patterns.subList(k + 1, j);
                    List<Parameters> paramList = params.subList(k + 1, j);

                    for(int l = allMatches.size() - 1; l >= 0; l--){
                        PatternMatch startMatch = allMatches.get(l);
                        startMatch.setIndex(start + s.length() - 1 - startMatch.getIndex() + startMatch.getLength() - 1);

                        if(startMatch.getIndex() - startMatch.getLength() - start + 1 > intervalPatternMaxSum ||
                                startMatch.getIndex() - startMatch.getLength() - start + 1 < intervalPatternMinSum){
                            continue;
                        }

                        List<PatternMatch> repeatingMatches = null;

                        if(!intervalPatterns.isEmpty()){
                            repeatingMatches = searchIntervalPatterns(dp, prefix, prev, prevTrueLength, patternLength,
                                    text, start, startMatch.getIndex() - startMatch.getLength(), intervalPatterns, intervalParams);

                            if(repeatingMatches == null)
                                continue;
                        }

                        List<PatternMatch> fuzzyMatches =
                            searchFixedPatterns(text, startMatch.getIndex() + 1, text.length() - 1, fuzzyList, true, matchCache, k + 1, paramList);

                        if(fuzzyMatches == null)
                            continue;

                        if(!intervalPatterns.isEmpty()){
                            for(int m = 0; m < repeatingMatches.size(); m++){
                                PatternMatch match = repeatingMatches.get(m);
                                res.add(match);

                                if(emptyOptionalMatchIdx.containsKey(m)){
                                    int val = emptyOptionalMatchIdx.get(m);

                                    for(int n = 0; n < val; n++)
                                        res.add(new PatternMatch(match.getIndex(), 0, 0, 0, -1));
                                }
                            }
                            emptyOptionalMatchIdx.clear();
                        }

                        for(int m = i; m < k; m++)
                            res.add(new PatternMatch(startMatch.getIndex() - startMatch.getLength(), 0, 0, 0, -1));
                        res.add(startMatch);
                        res.addAll(fuzzyMatches);
                        foundMatch = true;
                        break outerLoop;
                    }

                    if(pattern.isRequired())
                        return null;
                }

                if(foundMatch){
                    intervalPatterns.clear();
                    intervalParams.clear();
                    intervalPatternMinSum = 0;
                    intervalPatternMaxSum = 0;
                }else{
                    emptyOptionalMatchIdx.put(intervalPatterns.size() - 1, j - i);
                }
            }

            if(!res.isEmpty())
                start = res.get(res.size() - 1).getIndex() + 1;
            i = j;
        }

        for(int k = 0; k < patterns.size(); k++){
            patterns.get(k).getVars(vars, params.get(k), res.get(k));
        }

        return res;
    }

    private List<PatternMatch> searchIntervalPatterns(Array2D<Boolean> dp, Array2D<Integer> prefix, Array2D<Integer> prev,
            Array2D<Integer> prevTrueLength, int[] patternLength, StrView text, int start, int end, List<Pattern> patterns, List<Parameters> params){
        text = text.substring(start, end + 1);

        if(dp.getLength(0) == 0){
            // left index = patterns, right index = text
            // right index is automatically extended
            dp.set(0, 0, true);
            prefix.set(0, 0, 1);
            prev.set(0, 0, -1);
            prevTrueLength.set(0, 0, 0);

            for(int i = 1; i <= patterns.size(); i++){
                if(((RepeatingIntervalPattern)patterns.get(i - 1)).getMinLength(params.get(i - 1)) != 0)
                    break;

                dp.set(i, 0, true);
                prefix.set(i, 0, 1);
                prev.set(i, 0, 0);
                prevTrueLength.set(i, 0, 0);
            }
        }

        for(int i = prefix.getLength(0); i <= text.length(); i++){
            prefix.set(0, i, 1);
            prevTrueLength.set(0, i, i);

            for(int j = 1; j <= patterns.size(); j++){
                RepeatingIntervalPattern pattern = (RepeatingIntervalPattern)patterns.get(j - 1);
                Parameters param = params.get(j - 1);

                if(pattern.isAcceptable(text.charAt(i - 1)))
                    patternLength[j - 1]++;
                else
                    patternLength[j - 1] = 0;

                int textHi = i - pattern.getMinLength(param);
                int textLo = Math.max(i - pattern.getMaxLength(param), i - patternLength[j - 1]);

                if(textLo <= textHi){
                    int sum = prefix.get(j - 1, textHi);
                    sum -= textLo == 0 ? 0 : prefix.get(j - 1, textLo - 1);
                    dp.set(j, i, sum > 0);

                    if(sum > 0)
                        prevTrueLength.set(j, i, 0);
                    else
                        prevTrueLength.set(j, i, prevTrueLength.get(j, i - 1) + 1);

                    prev.set(j, i, pattern.getMinLength(param) + prevTrueLength.get(j - 1, textHi));
                }

                prefix.set(j, i, prefix.get(j, i - 1) + (dp.get(j, i) ? 1 : 0));
            }
        }

        List<PatternMatch> matches = null;

        if(dp.get(patterns.size(), text.length())){
            matches = new ArrayList<PatternMatch>();
            int x = text.length(), y = patterns.size();

            while(prev.get(y, x) > -1){
                matches.add(new PatternMatch(start + x - 1, prev.get(y, x), prev.get(y, x), 0, -1));
                x -= prev.get(y, x);
                y -= 1;
            }
        }

        return matches;
    }

    private List<PatternMatch> searchFixedPatterns(StrView text, int start, int end, List<Pattern> patterns, boolean reversed, Map<Integer, Map<Integer, PatternMatch>> cache, int patternOffset, List<Parameters> params){
        List<PatternMatch> matches = new ArrayList<>();
        int idx = reversed ? start : end;

        for(int i = 0; i < patterns.size(); i++){
            int curr = reversed ? i : (patterns.size() - 1 - i);
            FixedPattern pattern = (FixedPattern)patterns.get(curr);
            Parameters param = params.get(curr);
            StrView s;

            if(reversed)
                s = text.substring(idx, end + 1);
            else
                s = text.substring(start, idx + 1);

            PatternMatch match;

            if(cache == null){
                match = pattern.matchBest(s, reversed, param);
            }else{
                if(cache.containsKey(idx) && cache.get(idx).containsKey(curr + patternOffset))
                    match = cache.get(idx).get(curr + patternOffset);
                else
                    match = pattern.matchBest(s, reversed, param);

                if(!cache.containsKey(idx))
                    cache.put(idx, new HashMap<Integer, PatternMatch>());

                cache.get(idx).put(curr + patternOffset, match);
            }

            if(match == null)
                return null;

            if(reversed){
                match.setIndex(idx + match.getLength() - 1);
                idx += match.getLength();
            }else{
                match.setIndex(idx);
                idx -= match.getLength();
            }

            matches.add(match);
        }

        return matches;
    }

    public int getLength(int i){
        return patternList.get(i).size();
    }
}
