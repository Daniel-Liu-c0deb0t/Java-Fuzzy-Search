package fuzzyfind;

import java.util.List;
import java.util.ArrayList;

import javafuzzysearch.utils.FuzzyMatch;

public class WholePattern{
    private List<List<Pattern>> patternList;

    public WholePattern(List<List<Pattern>> patternList){
        this.patternList = patternList;
    }

    public List<String> search(List<String> texts){
        return null;
    }

    public List<FuzzyMatch> search(String text, List<Pattern> patterns){
        return null;
    }
}
