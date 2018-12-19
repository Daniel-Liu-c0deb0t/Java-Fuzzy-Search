package fuzzyfind.patterns;

import javafuzzysearch.utils.StrView;
import javafuzzysearch.utils.Utils;

import java.util.List;
import java.util.Set;
import java.util.Map;

public class RepeatingIntervalPattern implements Pattern{
    private Set<Character> acceptableChars;
    private int minLength, maxLength;

    public RepeatingIntervalPattern(Map<StrView, StrView> params){
        int requiredParams = 2;

        StrView s = new StrView("length");

        if(params.containsKey(s)){
            //int[] length = ParsingUtils.parseIntRange(params.get(s));
            String[] lengthStr = params.get(s).toString().split("-");
            int[] length = {Integer.parseInt(lengthStr[0]), Integer.parseInt(lengthStr[1])};
            minLength = length[0];
            maxLength = length[1];
            requiredParams--;
        }

        s = new StrView("pattern");

        if(params.containsKey(s)){
            //acceptableChars = ParsingUtils.parseRepeatingPattern(ParsingUtils.parseStr(s));
            if(params.get(s) == null)
                acceptableChars = null;
            else
                acceptableChars = Utils.uniqueChars(params.get(s));
            requiredParams--;
        }

        if(requiredParams != 0)
            throw new IllegalArgumentException("Repeating fixed pattern requires " + requiredParams + " arguments!");
    }

    public boolean isAcceptable(char c){
        return acceptableChars == null || acceptableChars.contains(c);
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
