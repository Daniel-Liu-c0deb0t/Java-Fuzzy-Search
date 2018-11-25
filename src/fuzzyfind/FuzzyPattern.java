package fuzzyfind;

import javafuzzysearch.utils.FuzzyMatch;

import java.util.List;
import java.util.CutoffSearcher;

public class FuzzyPattern implements Pattern{
    private CutoffSearcher searcher;
    private List<StrView> patterns;
    private List<StrView> patternEscapeIdx;

    public FuzzyPattern(){ // accept strings to parse
        this.searcher = new CutoffSearcher();
        // initialize patterns and searcher
    }

    public List<FuzzyMatch> searchAll(StrView text, Set<Integer> textEscapeIdx, boolean reversed){
        return null; // return best match!
    }

    public FuzzyMatch matchBest(StrView text, Set<Integer> textEscapeIdx, boolean reversed){
        return null;
    }
}
