package fuzzyfind;

import javafuzzysearch.utils.FuzzyMatch;
import javafuzzysearch.utils.StrView;

import java.util.List;
import java.util.Set;

public interface FixedPattern extends Pattern{
    public List<FuzzyMatch> searchAll(StrView text, Set<Integer> textEscapeIdx, int start, boolean reversed);
    public FuzzyMatch matchBest(StrView text, Set<Integer> textEscapeIdx, int start, boolean reversed);
}
