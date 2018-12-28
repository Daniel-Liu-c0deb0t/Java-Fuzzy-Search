package fuzzyfind.patterns;

import java.util.Map;

import javafuzzysearch.utils.StrView;

import fuzzyfind.utils.PatternMatch;
import fuzzyfind.utils.Variables;
import fuzzyfind.utils.Parameters;

public interface Pattern{
    public boolean isRequired();
    public boolean shouldTrim();
    public Parameters updateParams(Variables vars);
    public StrView getName();
    public void getVars(Variables vars, Parameters params, PatternMatch m);
}
