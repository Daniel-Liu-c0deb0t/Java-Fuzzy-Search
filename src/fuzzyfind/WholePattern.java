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

        // handle arbitrary length patterns
        // implement offset for text
        // implement Pattern stuff

        for(int i = 0; i < patterns.length(); i++){
            int j = i;

            while(patterns.get(j).fixedLength())
                j++;

            List<Pattern> subList = patterns.subList(i, j);

            if(i == 0 && anchoredStart){
                res.addAll(search(text, subList, true));
            }else if(i == pattern.length() - 1 && anchoredEnd){
                res.addAll(search(text, subList, false));
            }else{
                List<FuzzyMatch> allMatches = patterns.get(i).searchAll(text, true);
                List<Pattern> subSubList = patterns.subList(i + 1, j);

                for(FuzzyMatch match : allMatches){
                    res.addAll(search(text, subSubList, true));
                }
            }
        }
    }

    private List<FuzzyMatch> search(StrView text, int start, List<Pattern> patterns, boolean reversed){
        List<FuzzyMatch> res = new ArrayList<>();
        int end = 0;

        for(Pattern pattern : patterns){
            FuzzyMatch matches = pattern.matchBest(text.substring(end), reversed);

            res.add(matches);
            end += matches.getIndex() + 1;
        }

        return res;
    }
}
