package fuzzyfind.patterns;

import javafuzzysearch.utils.FuzzyMatch;
import javafuzzysearch.utils.StrView;

import java.util.List;

public interface FixedPattern extends Pattern{
    public List<FuzzyMatch> searchAll(StrView text, boolean reversed);
    public FuzzyMatch matchBest(StrView text, boolean reversed);
}
