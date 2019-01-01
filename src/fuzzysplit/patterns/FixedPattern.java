package fuzzysplit.patterns;

import fuzzysplit.utils.PatternMatch;
import fuzzysplit.utils.Parameters;

import javafuzzysearch.utils.StrView;

import java.util.List;

public interface FixedPattern extends Pattern{
    public List<PatternMatch> searchAll(StrView text, boolean reversed, Parameters params);
    public PatternMatch matchBest(StrView text, boolean reversed, Parameters params);
}
