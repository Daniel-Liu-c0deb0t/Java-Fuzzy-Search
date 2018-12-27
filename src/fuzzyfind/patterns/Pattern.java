package fuzzyfind.patterns;

import java.util.Map;

import javafuzzysearch.utils.StrView;

import fuzzyfind.utils.PatternMatch;

public interface Pattern{
    public boolean isRequired();
    public boolean shouldTrim();
    public void updateParams();
    public StrView getName();
    public Map<StrView, StrView> getVars(PatternMatch m);
}
