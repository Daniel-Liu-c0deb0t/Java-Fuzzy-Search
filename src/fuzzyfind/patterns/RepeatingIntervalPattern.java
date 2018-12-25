package fuzzyfind.patterns;

import javafuzzysearch.utils.StrView;
import javafuzzysearch.utils.Utils;

import fuzzyfind.parameters.IntParameter;
import fuzzyfind.parameters.StrParameter;

import fuzzyfind.utils.ParsingUtils;
import fuzzyfind.utils.PatternMatch;

import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

public class RepeatingIntervalPattern implements Pattern{
    private StrParameter acceptableCharsParam;
    private Set<Character> acceptableChars;
    private IntParameter minLengthParam, maxLengthParam;
    private int minLength, maxLength;
    private StrView name;

    public RepeatingIntervalPattern(Map<StrView, StrView> params){
        int requiredParams = 1;

        StrView s = new StrView("name");

        if(params.containsKey(s))
            name = params.get(s);

        s = new StrView("length");

        if(params.containsKey(s)){
            int idx = params.get(s).indexOf('-');
            minLengthParam = new IntParameter(ParsingUtils.splitByVars(params.get(s).substring(0, idx)));
            maxLengthParam = new IntParameter(ParsingUtils.splitByVars(params.get(s).substring(idx + 1)));
            requiredParams--;
        }

        s = new StrView("pattern");

        if(params.containsKey(s))
            acceptableCharsParam = new StrParameter(ParsingUtils.splitByVars(params.get(s)));

        if(requiredParams != 0)
            throw new IllegalArgumentException("Repeating interval pattern requires " + requiredParams + " more arguments!");
    }

    @Override
    public void updateParams(){
        minLength = minLengthParam.get();
        maxLength = maxLengthParam.get();

        if(acceptableCharsParam == null)
            acceptableChars = null;
        else
            acceptableChars = ParsingUtils.parseCharRanges(acceptableCharsParam.get());
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
