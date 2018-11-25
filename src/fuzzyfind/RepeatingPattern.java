package fuzzyfind;

import java.util.List;
import java.util.Set;

import javafuzzysearch.utils.FuzzyMatch;

public class RepeatingPattern implements Pattern{
    private Set<Character> acceptableChars;
    private Integer minLength, maxLength;

    public RepeatingPattern(){
        // parse strings
    }

    public List<FuzzyMatch> searchAll(StrView text, Set<Integer> textEscapeIdx){
        return null;
    }

    public FuzzyMatch matchBest(StrView text, Set<Integer> textEscapeIdx){
        return null;
    }
}
