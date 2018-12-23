package fuzzyfind.patterns;

import fuzzyfind.utils.PatternMatch;

import javafuzzysearch.utils.StrView;

import java.util.List;

public interface FixedPattern extends Pattern{
    public List<PatternMatch> searchAll(StrView text, boolean reversed);
    public PatternMatch matchBest(StrView text, boolean reversed);
}
