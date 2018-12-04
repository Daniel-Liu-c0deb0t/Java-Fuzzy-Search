package fuzzyfind;

import java.util.List;
import java.util.Set;

import javafuzzysearch.utils.FuzzyMatch;
import javafuzzysearch.utils.StrView;

public class RepeatingFixedPattern implements FixedPattern{
    private Set<Character> acceptableChars;
    private int length;
    private boolean required;

    public RepeatingFixedPattern(){
        // parse strings
    }

    public int getLength(){
        return length;
    }

    @Override
    public List<FuzzyMatch> searchAll(StrView text, Set<Integer> textEscapeIdx, int start, boolean reversed){
        return null; // return best match!
    }

    @Override
    public FuzzyMatch matchBest(StrView text, Set<Integer> textEscapeIdx, int start, boolean reversed){
        return null;
    }

    @Override
    public boolean isRequired(){
        return required;
    }
}
