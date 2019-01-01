package fuzzysplit.patterns;

import javafuzzysearch.utils.StrView;
import javafuzzysearch.utils.Utils;

import fuzzysplit.parameters.IntParameter;

import fuzzysplit.utils.ParsingUtils;
import fuzzysplit.utils.PatternMatch;
import fuzzysplit.utils.Variables;
import fuzzysplit.utils.Parameters;

import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

public class RepeatingIntervalPattern implements Pattern{
    private Set<Character> acceptableChars;
    private IntParameter minLengthParam, maxLengthParam;
    private boolean trim;
    private StrView name;

    public RepeatingIntervalPattern(Map<StrView, StrView> params){
        StrView s = new StrView("name");

        if(params.containsKey(s))
            name = ParsingUtils.removeOuterQuotes(params.get(s));

        s = new StrView("trim");

        if(params.containsKey(s))
            trim = true;

        s = new StrView("length");

        if(params.containsKey(s)){
            int idx = params.get(s).indexOf('-');
            minLengthParam = new IntParameter(ParsingUtils.splitByVars(params.get(s).substring(0, idx)));
            maxLengthParam = new IntParameter(ParsingUtils.splitByVars(params.get(s).substring(idx + 1)));
        }else{
            minLengthParam = new IntParameter(0);
            maxLengthParam = new IntParameter(Integer.MAX_VALUE);
        }

        s = new StrView("pattern");

        if(params.containsKey(s))
            acceptableChars = ParsingUtils.parseCharRanges(ParsingUtils.resolveStr(params.get(s)));
    }

    @Override
    public Parameters updateParams(Variables vars){
        Parameters params = new Parameters();
        Integer minLength = minLengthParam.get(vars);
        Integer maxLength = maxLengthParam.get(vars);
        params.add("minLength", minLength);
        params.add("maxLength", maxLength);
        return params;
    }

    public boolean isAcceptable(char c){
        return acceptableChars == null || acceptableChars.contains(c);
    }

    public int getMinLength(Parameters params){
        return params.getInt("minLength");
    }

    public int getMaxLength(Parameters params){
        return params.getInt("maxLength");
    }

    @Override
    public boolean isRequired(){
        return true;
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
    public void getVars(Variables vars, Parameters params, PatternMatch m){
        if(name == null)
            return;

        vars.add(Utils.concatenate(name, new StrView(".length")), new StrView(String.valueOf(m.getLength())));
    }
}
