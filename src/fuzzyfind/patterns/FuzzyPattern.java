package fuzzyfind.patterns;

import javafuzzysearch.utils.FuzzyMatch;
import javafuzzysearch.utils.Location;
import javafuzzysearch.searchers.CutoffSearcher;
import javafuzzysearch.utils.StrView;
import javafuzzysearch.utils.LengthParam;
import javafuzzysearch.utils.Utils;

import fuzzyfind.parameters.FloatParameter;
import fuzzyfind.parameters.StrParameter;

import fuzzyfind.utils.ParsingUtils;
import fuzzyfind.utils.PatternMatch;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.ArrayList;

public class FuzzyPattern implements FixedPattern{
    private FloatParameter scoreThresholdParam;
    private FloatParameter minOverlapParam;
    private CutoffSearcher searcher;
    private List<StrParameter> patternsParam;
    private List<StrView> patterns;
    private List<Set<Integer>> patternEscapeIdx;
    private boolean required;
    private StrView name;
    private StrParameter selector;
    private Map<StrView, List<Integer>> patternNames;
    private List<StrView> patternNameList;
    private List<Integer> patternToIdx;

    public FuzzyPattern(Map<StrView, StrView> params){
        int requiredParams = 1;

        StrView s = new StrView("required");

        if(params.containsKey(s))
            required = true;

        s = new StrView("name");

        if(params.containsKey(s))
            name = ParsingUtils.removeOuterQuotes(params.get(s));

        s = new StrView("edits");

        if(params.containsKey(s))
            scoreThresholdParam = new FloatParameter(ParsingUtils.splitByVars(params.get(s)));
        else
            scoreThresholdParam = new FloatParameter(0.0f);

        s = new StrView("min_overlap");

        if(params.containsKey(s))
            minOverlapParam = new FloatParameter(ParsingUtils.splitByVars(params.get(s)));
        else
            minOverlapParam = new FloatParameter(-0.0f);

        s = new StrView("patterns");

        if(params.containsKey(s)){
            patternsParam = new ArrayList<StrParameter>();
            patternEscapeIdx = new ArrayList<Set<Integer>>();
            patterns = new ArrayList<StrView>();
            patternToIdx = new ArrayList<Integer>();
            patternNames = new HashMap<StrView, List<Integer>>();
            patternNameList = new ArrayList<StrView>();

            StrView[] str = ParsingUtils.resolveStrWithSelector(params.get(s));
            List<List<StrView>> pairs = ParsingUtils.splitKeyValuePairs(str[0]);

            if(str[1] != null)
                selector = new StrParameter(ParsingUtils.splitByVars(str[1]));

            for(int i = 0; i < pairs.size(); i++){
                List<StrView> pair = pairs.get(i);
                patternsParam.add(new StrParameter(ParsingUtils.splitByVars(
                                ParsingUtils.removeOuterQuotes(pair.get(pair.size() - 1)))));

                if(pair.size() > 1){
                    StrView patternName = ParsingUtils.removeOuterQuotes(pair.get(0));

                    if(!patternNames.containsKey(patternName))
                        patternNames.put(patternName, new ArrayList<Integer>());

                    patternNames.get(patternName).add(i);
                    patternNameList.add(patternName);
                }else{
                    patternNameList.add(new StrView(""));
                }

                patternEscapeIdx.add(new HashSet<Integer>());
            }

            requiredParams--;
        }

        if(requiredParams != 0)
            throw new IllegalArgumentException("Fuzzy pattern requires " + requiredParams + " more arguments!");

        searcher = new CutoffSearcher();
    }

    @Override
    public void updateParams(){
        searcher.scoreThreshold(ParsingUtils.toLengthParam(scoreThresholdParam.get()));
        searcher.minOverlap(ParsingUtils.toLengthParam(minOverlapParam.get()), Location.END);

        patterns.clear();
        patternToIdx.clear();

        if(selector == null){
            for(int i = 0; i < patternsParam.size(); i++){
                patterns.add(patternsParam.get(i).get());
                patternToIdx.add(i);
            }
        }else{
            StrView selected = selector.get();

            if(patternNames.containsKey(selected)){
                List<Integer> idx = patternNames.get(selected);

                for(int i : idx){
                    patterns.add(patternsParam.get(i).get());
                    patternToIdx.add(i);
                }
            }else{
                int idx = Integer.parseInt(selected.toString());

                if(idx >= 0){
                    patterns.add(patternsParam.get(idx).get());
                    patternToIdx.add(idx);
                }
            }
        }
    }

    @Override
    public List<PatternMatch> searchAll(StrView text, boolean reversed){
        if(reversed)
            text = text.reverse();

        Map<Integer, PatternMatch> map = new HashMap<>();

        for(int i = 0; i < patterns.size(); i++){
            StrView pattern = reversed ? patterns.get(i).reverse() : patterns.get(i);
            Set<Integer> escapeIdx = patternEscapeIdx.get(i);

            List<FuzzyMatch> matches = searcher.search(text, pattern, false, new HashSet<Integer>(), escapeIdx);

            for(int j = 0; j < matches.size(); j++){
                PatternMatch curr = new PatternMatch(matches.get(j), patternToIdx.get(i));
                curr.setIndex(Math.max(Math.min(curr.getIndex(), text.length() - 1), 0));
                curr.setLength(curr.getLength() - pattern.length() + curr.getOverlap());

                if(!map.containsKey(curr.getIndex()) || map.get(curr.getIndex()).getScore() > curr.getScore())
                    map.put(curr.getIndex(), curr);
            }
        }

        List<PatternMatch> list = new ArrayList<>();

        for(int i : map.keySet())
            list.add(map.get(i));

        Collections.sort(list, (a, b) -> a.getIndex() - b.getIndex());

        return list;
    }

    @Override
    public PatternMatch matchBest(StrView text, boolean reversed){
        if(reversed)
            text = text.reverse();

        PatternMatch match = required ? null : new PatternMatch(text.length() - 1, 0, 0, 0, -1);
        boolean first = true;

        for(int i = 0; i < patterns.size(); i++){
            StrView pattern = reversed ? patterns.get(i).reverse() : patterns.get(i);
            Set<Integer> escapeIdx = patternEscapeIdx.get(i);

            List<FuzzyMatch> matches = searcher.search(text, pattern, false, new HashSet<Integer>(), escapeIdx);

            for(int j = matches.size() - 1; j >= 0; j--){
                PatternMatch m = new PatternMatch(matches.get(j), patternToIdx.get(i));
                m.setIndex(Math.max(Math.min(m.getIndex(), text.length() - 1), 0));
                m.setLength(m.getLength() - pattern.length() + m.getOverlap());

                if(m.getIndex() == text.length() - 1){
                    if(first || match.getScore() > m.getScore()){
                        match = m;
                        first = false;
                    }

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

    @Override
    public StrView getName(){
        return name;
    }

    @Override
    public Map<StrView, StrView> getVars(PatternMatch m){
        if(name == null)
            return null;

        Map<StrView, StrView> res = new HashMap<>();

        res.put(Utils.concatenate(name, new StrView(".length")), new StrView(String.valueOf(m.getLength())));

        int patternIdx = m.getPatternIdx();
        res.put(Utils.concatenate(name, new StrView(".pattern_idx")), new StrView(String.valueOf(patternIdx)));
        res.put(Utils.concatenate(name, new StrView(".pattern_name")),
                patternIdx == -1 ? new StrView("") : patternNameList.get(m.getPatternIdx()));
        res.put(Utils.concatenate(name, new StrView(".pattern")),
                patternIdx == -1 ? new StrView("") : patterns.get(m.getPatternIdx()));

        return res;
    }
}
