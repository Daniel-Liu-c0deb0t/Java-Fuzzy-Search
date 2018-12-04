package fuzzyfind;

import javafuzzysearch.utils.StrView;

import java.util.List;
import java.util.Set;

public class RepeatingIntervalPattern implements Pattern{
    private Set<Character> acceptableChars;
    private int minLength, maxLength;

    public RepeatingIntervalPattern(){
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

    @Override
    public boolean isRequired(){
        return true;
    }
}
