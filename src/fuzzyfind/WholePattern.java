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

    private List<FuzzyMatch> search(StrView text, List<Pattern> patterns, boolean anchoredStart, boolean anchoredEnd, boolean greedy){
        List<FuzzyMatch> res = new ArrayList<>();
        int start = 0;

        // handle arbitrary length patterns
        // handle anchored at end
        // find best match for not anchored patterns
        // implement Pattern stuff

        for(int i = 0; i < patterns.length(); i++){
            int j = i;

            while(patterns.get(j).isFixedLength())
                j++;

            List<Pattern> subList = patterns.subList(i, j);

            if(i == 0 && anchoredStart){
                res.addAll(search(text, start, subList, true));
            }else if(i == pattern.length() - 1 && anchoredEnd){
                res.addAll(search(text, start, subList, false));
            }else{
                StrView s = text.substring(start, start + pattern.get(i).maxLength());
                List<FuzzyMatch> allMatches = patterns.get(i).searchAll(s, true);
                List<Pattern> subSubList = patterns.subList(i + 1, j);

                for(int j = allMatches.size() - 1; j >= 0; j--){
                    FuzzyMatch match = allMatches.get(j);
                    match.setIndex(start + s.length() - 1 - match.getIndex() + match.getLength() - 1);
                    res.add(match);
                    res.addAll(search(text, match.getIndex() + 1, subSubList, true));
                }
            }

            start = res.get(res.size() - 1).getIndex() + 1;
        }
    }

    private List<FuzzyMatch> search(StrView text, int start, List<Pattern> patterns, boolean reversed){
        List<FuzzyMatch> res = new ArrayList<>();

        for(Pattern pattern : patterns){
            StrView s = text.substring(start, start + pattern.maxLength());
            FuzzyMatch match = pattern.matchBest(s, reversed);
            match.setIndex(start + match.getLength() - 1);
            res.add(match);
            start += match.getLength();
        }

        return res;
    }
}
