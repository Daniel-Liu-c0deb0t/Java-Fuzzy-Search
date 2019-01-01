package fuzzysplit.patterns;

import java.util.Map;

import javafuzzysearch.utils.StrView;

import fuzzysplit.utils.PatternMatch;
import fuzzysplit.utils.Variables;
import fuzzysplit.utils.Parameters;

public interface Pattern{
    public boolean isRequired();
    public boolean shouldTrim();
    public Parameters updateParams(Variables vars);
    public StrView getName();
    public void getVars(Variables vars, Parameters params, PatternMatch m);
}
