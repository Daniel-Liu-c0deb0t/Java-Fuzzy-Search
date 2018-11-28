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

        // handle arbitrary length patterns
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
        // dp?
    }

    private List<FuzzyMatch> searchFixedLength(StrView text, int start, int end, List<Pattern> patterns, boolean reversed){
        List<FuzzyMatch> res = new ArrayList<>();
        int idx = reversed ? start : end;

        for(int i = 0; i < patterns.size(); i++){
            Pattern pattern = patterns.get(reversed ? i : (patterns.size() - 1 - i));
            StrView s;

            if(reversed)
                s = text.substring(idx, Math.min(end + 1, idx + pattern.maxLength()));
            else
                s = text.substring(Math.max(start, idx - pattern.maxLength() + 1), idx + 1);

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
