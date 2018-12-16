package fuzzyfind;

import javafuzzysearch.utils.FuzzyMatch;
import javafuzzysearch.searchers.CutoffSearcher;
import javafuzzysearch.utils.StrView;
import javafuzzysearch.utils.LengthParam;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.ArrayList;

public class FuzzyPattern implements FixedPattern{
    private CutoffSearcher searcher;
    private List<StrView> patterns;
    private List<Set<Integer>> patternEscapeIdx;
    private List<Set<Integer>> patternEscapeIdxReversed;
    private boolean required;

    public FuzzyPattern(){
        searcher = new CutoffSearcher();
        searcher.scoreThreshold(new LengthParam(1));
        patterns = new ArrayList<StrView>();
        patterns.add(new StrView("hello"));
        patternEscapeIdx = new ArrayList<Set<Integer>>();
        patternEscapeIdx.add(new HashSet<Integer>());
        patternEscapeIdxReversed = new ArrayList<Set<Integer>>();
        patternEscapeIdxReversed.add(new HashSet<Integer>());
    }

    @Override
    public List<FuzzyMatch> searchAll(StrView text, boolean reversed){
        if(reversed)
            text = text.reverse();

        Map<Integer, FuzzyMatch> map = new HashMap<>();

        for(int i = 0; i < patterns.size(); i++){
            StrView pattern = reversed ? patterns.get(i).reverse() : patterns.get(i);
            Set<Integer> escapeIdx = reversed ? patternEscapeIdxReversed.get(i) : patternEscapeIdx.get(i);

            List<FuzzyMatch> matches = searcher.search(text, pattern, false, new HashSet<Integer>(), escapeIdx);

            for(int j = 0; j < matches.size(); j++){
                FuzzyMatch curr = matches.get(j);

                if(!map.containsKey(curr.getIndex()) || map.get(curr.getIndex()).getScore() > curr.getScore())
                    map.put(curr.getIndex(), curr);
            }
        }

        List<FuzzyMatch> list = new ArrayList<>();

        for(int i : map.keySet())
            list.add(map.get(i));

        Collections.sort(list, (a, b) -> a.getIndex() - b.getIndex());

        return list;
    }

    @Override
    public FuzzyMatch matchBest(StrView text, boolean reversed){
        if(reversed)
            text = text.reverse();

        FuzzyMatch match = required ? null : new FuzzyMatch(text.length() - 1, 0, 0, 0);

        for(int i = 0; i < patterns.size(); i++){
            StrView pattern = reversed ? patterns.get(i).reverse() : patterns.get(i);
            Set<Integer> escapeIdx = reversed ? patternEscapeIdxReversed.get(i) : patternEscapeIdx.get(i);

            List<FuzzyMatch> matches = searcher.search(text, pattern, false, new HashSet<Integer>(), escapeIdx);

            for(int j = matches.size() - 1; j >= 0; j--){
                FuzzyMatch m = matches.get(j);

                if(m.getIndex() == text.length() - 1){
                    if(match == null || match.getScore() > m.getScore())
                        match = m;

                    break;
                }
            }
        }

        return match;
    }

    @Override
    public boolean isRequired(){
        return required;
    }
}
