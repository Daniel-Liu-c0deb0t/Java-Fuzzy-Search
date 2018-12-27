package fuzzyfind.patterns;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

import javafuzzysearch.utils.StrView;
import javafuzzysearch.utils.Utils;

import fuzzyfind.parameters.IntParameter;

import fuzzyfind.utils.ParsingUtils;
import fuzzyfind.utils.PatternMatch;

public class RepeatingFixedPattern implements FixedPattern{
    private Set<Character> acceptableChars;
    private IntParameter lengthParam;
    private int length;
    private boolean required, trim;
    private StrView name;

    public RepeatingFixedPattern(Map<StrView, StrView> params){
        int requiredParams = 1;

        StrView s = new StrView("required");

        if(params.containsKey(s))
            required = true;

        s = new StrView("trim");

        if(params.containsKey(s))
            trim = true;

        s = new StrView("name");

        if(params.containsKey(s))
            name = ParsingUtils.removeOuterQuotes(params.get(s));

        s = new StrView("length");

        if(params.containsKey(s)){
            lengthParam = new IntParameter(ParsingUtils.splitByVars(params.get(s)));
            requiredParams--;
        }

        s = new StrView("pattern");

        if(params.containsKey(s))
            acceptableChars = ParsingUtils.parseCharRanges(ParsingUtils.resolveStr(params.get(s)));

        if(requiredParams != 0)
            throw new IllegalArgumentException("Repeating fixed pattern requires " + requiredParams + " more arguments!");
    }

    @Override
    public void updateParams(){
        length = lengthParam.get();
    }

    @Override
    public List<PatternMatch> searchAll(StrView text, boolean reversed){
        if(length > text.length())
            return null;

        if(reversed)
            text = text.reverse();

        int unacceptableChars = 0;
        List<PatternMatch> matches = new ArrayList<>();

        for(int i = 0; i < length; i++){
            if(acceptableChars != null && !acceptableChars.contains(text.charAt(i)))
                unacceptableChars++;
        }

        if(unacceptableChars == 0)
            matches.add(new PatternMatch(length - 1, length, length, 0));

        for(int i = length; i < text.length(); i++){
            if(acceptableChars != null && !acceptableChars.contains(text.charAt(i)))
                unacceptableChars++;

            if(acceptableChars != null && !acceptableChars.contains(text.charAt(i - length)))
                unacceptableChars--;

            if(unacceptableChars == 0)
                matches.add(new PatternMatch(i, length, length, 0));
        }

        return matches;
    }

    @Override
    public PatternMatch matchBest(StrView text, boolean reversed){
        if(length > text.length())
            return required ? null : new PatternMatch(text.length() - 1, 0, 0, 0);

        if(reversed)
            text = text.reverse();

        for(int i = 0; i < length; i++){
            if(acceptableChars != null && !acceptableChars.contains(text.charAt(text.length() - 1 - i)))
                return required ? null : new PatternMatch(text.length() - 1, 0, 0, 0);
        }

        return new PatternMatch(text.length() - 1, length, length, 0);
    }

    @Override
    public boolean isRequired(){
        return required;
    }

    @Override
    public boolean shouldTrim(){
        return trim;
    }

    @Override
    public StrView getName(){
        return name;
    }

    @Override
    public Map<StrView, StrView> getVars(PatternMatch m){
        if(name == null)
            return null;

        Map<StrView, StrView> res = new HashMap<>();

        res.put(Utils.concatenate(name, new StrView(".length")), new StrView(String.valueOf(m.getLength())));

        return res;
    }
}
