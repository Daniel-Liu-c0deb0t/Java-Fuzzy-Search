package fuzzyfind.patterns;

import javafuzzysearch.utils.StrView;
import javafuzzysearch.utils.Utils;

import fuzzyfind.parameters.Parameter;
import fuzzyfind.parameters.IntParameter;
import fuzzyfind.parameters.StrParameter;

import fuzzyfind.utils.ParsingUtils;

import java.util.List;
import java.util.Set;
import java.util.Map;

public class RepeatingIntervalPattern implements Pattern{
    private StrParameter acceptableCharsParam;
    private Set<Character> acceptableChars;
    private IntParameter minLengthParam, maxLengthParam;
    private int minLength, maxLength;

    public RepeatingIntervalPattern(Map<StrView, StrView> params){
        int requiredParams = 2;

        StrView s = new StrView("length");

        if(params.containsKey(s)){
            List<StrView> lengths = ParsingUtils.splitInLiteralStr(params.get(s), new StrView("-"));
            minLengthParam = new IntParameter(ParsingUtils.splitByVars(lengths.get(0)));
            maxLengthParam = new IntParameter(ParsingUtils.splitByVars(lengths.get(1)));
            requiredParams--;
        }

        s = new StrView("pattern");

        if(params.containsKey(s)){
            if(params.get(s) == null)
                acceptableCharsParam = null;
            else
                acceptableCharsParam = new StrParameter(ParsingUtils.splitByVars(params.get(s)));
            requiredParams--;
        }

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

    @Override
    public boolean isRequired(){
        return true;
    }
}
