package fuzzyfind;

import javafuzzysearch.utils.FuzzyMatch;
import javafuzzysearch.searchers.CutoffSearcher;
import javafuzzysearch.utils.StrView;

import java.util.List;
import java.util.Set;

public class FuzzyPattern implements FixedPattern{
    private CutoffSearcher searcher;
    private List<StrView> patterns;
    private List<StrView> patternEscapeIdx;
    private boolean required;

    public FuzzyPattern(){ // accept strings to parse
        this.searcher = new CutoffSearcher();
        // initialize patterns and searcher
    }

    @Override
    public List<FuzzyMatch> searchAll(StrView text, Set<Integer> textEscapeIdx, int start, boolean reversed){
        return null; // return best match!
    }

    @Override
    public FuzzyMatch matchBest(StrView text, Set<Integer> textEscapeIdx, int start, boolean reversed){
        return null;
    }

    @Override
    public boolean isRequired(){
        return required;
    }
}
