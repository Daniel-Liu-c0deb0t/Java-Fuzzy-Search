package fuzzyfind;

import java.util.List;
import java.util.Set;

import javafuzzysearch.utils.FuzzyMatch;

public class RepeatingPattern implements Pattern{
    private Set<Character> acceptableChars;
    private int minLength, maxLength;

    public RepeatingPattern(){
        // parse strings
    }

    public boolean isAcceptable(char c){
        return acceptableChars.contains(c);
    }

    public int getMinLength(){
        return minLength;
    }

    public int getMaxLength(){
        return maxLength;
    }

    public boolean isFixedLength(){
        return minLength == maxLength;
    }
}
