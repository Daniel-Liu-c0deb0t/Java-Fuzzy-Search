package fuzzyfind;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.Map;

import javafuzzysearch.utils.FuzzyMatch;
import javafuzzysearch.utils.StrView;
import javafuzzysearch.utils.Utils;

public class RepeatingFixedPattern implements FixedPattern{
    private Set<Character> acceptableChars;
    private int length;
    private boolean required;

    public RepeatingFixedPattern(Map<StrView, StrView> params){
        int requiredParams = 2;

        StrView s = new StrView("required");

        if(params.containsKey(s))
            required = true;

        s = new StrView("length");

        if(params.containsKey(s)){
            //length = ParsingUtils.parseInt(params.get(s));
            length = Integer.parseInt(params.get(s).toString());
            requiredParams--;
        }

        s = new StrView("pattern");

        if(params.containsKey(s)){
            //acceptableChars = ParsingUtils.parseRepeatingPattern(ParsingUtils.parseStr(s));
            acceptableChars = Utils.uniqueChars(params.get(s));
            requiredParams--;
        }

        if(requiredParams != 0)
            throw new IllegalArgumentException("Repeating fixed pattern requires " + requiredParams + " arguments!");
    }

    public int getLength(){
        return length;
    }

    @Override
    public List<FuzzyMatch> searchAll(StrView text, boolean reversed){
        if(length > text.length())
            return null;

        if(reversed)
            text = text.reverse();

        int unacceptableChars = 0;
        List<FuzzyMatch> matches = new ArrayList<>();

        for(int i = 0; i < length; i++){
            if(!acceptableChars.contains(text.charAt(i)))
                unacceptableChars++;
        }

        if(unacceptableChars == 0)
            matches.add(new FuzzyMatch(length - 1, length, length, 0));

        for(int i = length; i < text.length(); i++){
            if(!acceptableChars.contains(text.charAt(i)))
                unacceptableChars++;

            if(!acceptableChars.contains(text.charAt(i - length)))
                unacceptableChars--;

            if(unacceptableChars == 0)
                matches.add(new FuzzyMatch(i, length, length, 0));
        }

        return matches;
    }

    @Override
    public FuzzyMatch matchBest(StrView text, boolean reversed){
        if(length > text.length())
            return required ? null : new FuzzyMatch(text.length() - 1, 0, 0, 0);

        if(reversed)
            text = text.reverse();

        for(int i = 0; i < length; i++){
            if(!acceptableChars.contains(text.charAt(text.length() - 1 - i)))
                return required ? null : new FuzzyMatch(text.length() - 1, 0, 0, 0);
        }

        return new FuzzyMatch(text.length() - 1, length, length, 0);
    }

    @Override
    public boolean isRequired(){
        return required;
    }
}
