package fuzzysplit.patterns;

import javafuzzysearch.utils.FuzzyMatch;
import javafuzzysearch.utils.Location;
import javafuzzysearch.searchers.CutoffSearcher;
import javafuzzysearch.searchers.BitapSearcher;
import javafuzzysearch.utils.StrView;
import javafuzzysearch.utils.LengthParam;
import javafuzzysearch.utils.Utils;
import javafuzzysearch.utils.Ngrams;

import fuzzysplit.parameters.FloatParameter;
import fuzzysplit.parameters.StrParameter;

import fuzzysplit.utils.ParsingUtils;
import fuzzysplit.utils.PatternMatch;
import fuzzysplit.utils.Parameters;
import fuzzysplit.utils.Variables;

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
    private List<StrParameter> patternsParam;
    private boolean required, trim, transpositions, caseInsensitive, hamming, autoNgramSize;
    private int ngramSize;
    private StrView name;
    private StrParameter selector;
    private Map<StrView, List<Integer>> patternNames;
    private List<StrView> patternNameList;
    private Map<Character, Set<Character>> textWildcardChars, patternWildcardChars;

    public FuzzyPattern(Map<StrView, StrView> params){
        int requiredParams = 1;

        StrView s = new StrView("required");

        if(params.containsKey(s))
            required = true;

        s = new StrView("trim");

        if(params.containsKey(s))
            trim = true;

        s = new StrView("transpose");

        if(params.containsKey(s))
            transpositions = true;

        s = new StrView("no_case");

        if(params.containsKey(s))
            caseInsensitive = true;

        s = new StrView("hamming");

        if(params.containsKey(s))
            hamming = true;

        s = new StrView("ngram");

        if(params.containsKey(s))
            ngramSize = Integer.parseInt(params.get(s).toString());
        else
            autoNgramSize = true;

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

        textWildcardChars = new HashMap<Character, Set<Character>>();
        patternWildcardChars = new HashMap<Character, Set<Character>>();

        s = new StrView("wildcard");

        if(params.containsKey(s)){
            List<List<StrView>> pairs = ParsingUtils.splitKeyValuePairs(
                    ParsingUtils.removeWhitespace(ParsingUtils.resolveStr(params.get(s))));

            for(List<StrView> pair : pairs){
                StrView key = ParsingUtils.removeOuterQuotes(pair.get(0));
                Set<Character> val = pair.size() <= 1 ? null : ParsingUtils.parseCharRanges(ParsingUtils.removeOuterQuotes(pair.get(1)));

                for(int i = 0; i < key.length(); i++){
                    textWildcardChars.put(key.charAt(i), val);
                    patternWildcardChars.put(key.charAt(i), val);
                }
            }
        }

        s = new StrView("pattern_wildcard");

        if(params.containsKey(s)){
            List<List<StrView>> pairs = ParsingUtils.splitKeyValuePairs(
                    ParsingUtils.removeWhitespace(ParsingUtils.resolveStr(params.get(s))));

            for(List<StrView> pair : pairs){
                StrView key = ParsingUtils.removeOuterQuotes(pair.get(0));
                Set<Character> val = pair.size() <= 1 ? null : ParsingUtils.parseCharRanges(ParsingUtils.removeOuterQuotes(pair.get(1)));

                for(int i = 0; i < key.length(); i++)
                    patternWildcardChars.put(key.charAt(i), val);
            }
        }

        s = new StrView("text_wildcard");

        if(params.containsKey(s)){
            List<List<StrView>> pairs = ParsingUtils.splitKeyValuePairs(
                    ParsingUtils.removeWhitespace(ParsingUtils.resolveStr(params.get(s))));

            for(List<StrView> pair : pairs){
                StrView key = ParsingUtils.removeOuterQuotes(pair.get(0));
                Set<Character> val = pair.size() <= 1 ? null : ParsingUtils.parseCharRanges(ParsingUtils.removeOuterQuotes(pair.get(1)));

                for(int i = 0; i < key.length(); i++)
                    textWildcardChars.put(key.charAt(i), val);
            }
        }

        s = new StrView("pattern");

        if(params.containsKey(s)){
            patternsParam = new ArrayList<StrParameter>();
            patternNames = new HashMap<StrView, List<Integer>>();
            patternNameList = new ArrayList<StrView>();

            StrView[] str = ParsingUtils.resolveStrWithSelector(params.get(s));
            List<List<StrView>> pairs = ParsingUtils.splitKeyValuePairs(ParsingUtils.removeWhitespace(str[0]));

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
            }

            requiredParams--;
        }

        if(requiredParams != 0)
            throw new IllegalArgumentException("Fuzzy pattern requires " + requiredParams + " more arguments!");
    }

    @Override
    public Parameters updateParams(Variables vars){
        Parameters res = new Parameters();

        LengthParam scoreThreshold = ParsingUtils.toLengthParam(scoreThresholdParam.get(vars));
        LengthParam minOverlap = ParsingUtils.toLengthParam(minOverlapParam.get(vars));

        res.add("scoreThreshold", scoreThreshold);
        res.add("minOverlap", minOverlap);

        if(hamming){
            BitapSearcher searcher = new BitapSearcher();
            searcher.maxEdits(scoreThreshold);
            searcher.minOverlap(minOverlap, Location.END);
            searcher.wildcardChars(textWildcardChars, patternWildcardChars);

            res.add("searcher", searcher);
        }else{
            CutoffSearcher searcher = new CutoffSearcher();
            searcher.scoreThreshold(scoreThreshold);
            searcher.minOverlap(minOverlap, Location.END);
            searcher.wildcardChars(textWildcardChars, patternWildcardChars);

            if(transpositions)
                searcher.allowTranspositions();

            res.add("searcher", searcher);
        }

        List<StrView> patterns = new ArrayList<>();
        List<Integer> patternToIdx = new ArrayList<>();

        if(selector == null){
            for(int i = 0; i < patternsParam.size(); i++){
                patterns.add(patternsParam.get(i).get(vars));
                patternToIdx.add(i);
            }
        }else{
            StrView selected = selector.get(vars);

            if(patternNames.containsKey(selected)){
                List<Integer> idx = patternNames.get(selected);

                for(int i : idx){
                    patterns.add(patternsParam.get(i).get(vars));
                    patternToIdx.add(i);
                }
            }else{
                int idx = Integer.parseInt(selected.toString());

                if(idx >= 0){
                    patterns.add(patternsParam.get(idx).get(vars));
                    patternToIdx.add(idx);
                }
            }
        }

        res.add("patterns", patterns);
        res.add("patternToIdx", patternToIdx);

        int min = Integer.MAX_VALUE;

        if(autoNgramSize){
            for(StrView pattern : patterns){
                min = Math.min(min, maxNgramSize(minOverlap.get(pattern.length()), scoreThreshold.get(pattern.length())));
            }
        }

        if(ngramSize > 0 || (min > 0 && min != Integer.MAX_VALUE))
            res.add("ngrams", new Ngrams(patterns, ngramSize > 0 ? ngramSize : min));

        return res;
    }

    @Override
    public List<PatternMatch> searchAll(StrView text, boolean reversed, Parameters params){
        BitapSearcher hammingSearcher = null;
        CutoffSearcher levenshteinSearcher = null;

        if(hamming)
            hammingSearcher = params.getHammingSearcher("searcher");
        else
            levenshteinSearcher = params.getLevenshteinSearcher("searcher");

        List<StrView> patterns = params.getStrList("patterns");
        List<Integer> patternToIdx = params.getIntList("patternToIdx");

        LengthParam scoreThreshold = params.getLengthParam("scoreThreshold");
        LengthParam minOverlap = params.getLengthParam("minOverlap");

        Ngrams ngrams = params.getNgrams("ngrams"); 

        Set<Integer> idx;

        if(ngrams == null){
            idx = new HashSet<Integer>();

            for(int i = 0; i < patterns.size(); i++)
                idx.add(i);
        }else{
            idx = ngrams.getIdx(text);

            if(!autoNgramSize){
                for(int i = 0; i < patterns.size(); i++){
                    int length = patterns.get(i).length();

                    if(!idx.contains(i) &&
                            !guaranteedNgramMatch(minOverlap.get(length), scoreThreshold.get(length), ngrams.getN())){
                        idx.add(i);
                    }
                }
            }
        }

        if(reversed)
            text = text.reverse();

        Map<Integer, PatternMatch> map = new HashMap<>();

        for(int i : idx){
            StrView pattern = reversed ? patterns.get(i).reverse() : patterns.get(i);

            List<FuzzyMatch> matches = null;

            if(hamming){
                matches = hammingSearcher.search(
                        caseInsensitive ? text.toLowerCase() : text, caseInsensitive ? pattern.toLowerCase() : pattern);
            }else{
                matches = levenshteinSearcher.search(
                        caseInsensitive ? text.toLowerCase() : text, caseInsensitive ? pattern.toLowerCase() : pattern, false);
            }

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
    public PatternMatch matchBest(StrView text, boolean reversed, Parameters params){
        BitapSearcher hammingSearcher = null;
        CutoffSearcher levenshteinSearcher = null;

        if(hamming)
            hammingSearcher = params.getHammingSearcher("searcher");
        else
            levenshteinSearcher = params.getLevenshteinSearcher("searcher");

        List<StrView> patterns = params.getStrList("patterns");
        List<Integer> patternToIdx = params.getIntList("patternToIdx");

        LengthParam scoreThreshold = params.getLengthParam("scoreThreshold");
        LengthParam minOverlap = params.getLengthParam("minOverlap");

        Ngrams ngrams = params.getNgrams("ngrams"); 

        Set<Integer> idx;

        if(ngrams == null){
            idx = new HashSet<Integer>();

            for(int i = 0; i < patterns.size(); i++)
                idx.add(i);
        }else{
            int maxLength = 0;

            for(StrView pattern : patterns){
                int length = pattern.length();
                maxLength = Math.max(maxLength, length + scoreThreshold.get(length));
            }

            idx = ngrams.getIdx(reversed ? text.substring(0, maxLength) : text.substring(text.length() - maxLength));

            if(!autoNgramSize){
                for(int i = 0; i < patterns.size(); i++){
                    int length = patterns.get(i).length();

                    if(!idx.contains(i) &&
                            !guaranteedNgramMatch(minOverlap.get(length), scoreThreshold.get(length), ngrams.getN())){
                        idx.add(i);
                    }
                }
            }
        }

        if(reversed)
            text = text.reverse();

        PatternMatch match = required ? null : new PatternMatch(text.length() - 1, 0, 0, 0, -1);
        boolean first = true;

        for(int i : idx){
            StrView pattern = reversed ? patterns.get(i).reverse() : patterns.get(i);
            int textOffset = text.length() - pattern.length() - scoreThreshold.get(pattern.length());
            StrView currText = text.substring(textOffset);

            List<FuzzyMatch> matches = null;

            if(hamming){
                matches = hammingSearcher.search(
                        caseInsensitive ? currText.toLowerCase() : currText, caseInsensitive ? pattern.toLowerCase() : pattern);
            }else{
                matches = levenshteinSearcher.search(
                        caseInsensitive ? currText.toLowerCase() : currText, caseInsensitive ? pattern.toLowerCase() : pattern, false);
            }

            for(int j = matches.size() - 1; j >= 0; j--){
                PatternMatch m = new PatternMatch(matches.get(j), patternToIdx.get(i));
                m.setIndex(textOffset + Math.max(Math.min(m.getIndex(), currText.length() - 1), 0));
                m.setLength(m.getLength() - pattern.length() + m.getOverlap());

                if(m.getIndex() == text.length() - 1){
                    if(first || match.getScore() > m.getScore()){
                        match = m;
                        first = false;

                        if(match.getScore() <= 0)
                            return match;
                    }
                }else{
                    break;
                }
            }
        }

        return match;
    }

    private boolean guaranteedNgramMatch(int length, int edits, int n){
        return length > (n - 1) * (edits + 1) + (transpositions ? edits * 2 : edits);
    }

    private int maxNgramSize(int length, int edits){
        float res = (length - (transpositions ? edits * 2 : edits)) / (edits + 1.0f) + 1.0f;
        return (int)Math.ceil(res) - 1;
    }

    @Override
    public boolean isRequired(){
        return required;
    }

    @Override
    public boolean shouldTrim(){
        return trim;
    }

    @Override
    public StrView getName(){
        return name;
    }

    @Override
    public void getVars(Variables vars, Parameters params, PatternMatch m){
        if(name == null)
            return;

        vars.add(Utils.concatenate(name, new StrView(".length")), new StrView(String.valueOf(m.getLength())));

        int patternIdx = m.getPatternIdx();
        vars.add(Utils.concatenate(name, new StrView(".pattern_idx")), new StrView(String.valueOf(patternIdx)));
        vars.add(Utils.concatenate(name, new StrView(".pattern_name")),
                patternIdx == -1 ? new StrView("") : patternNameList.get(m.getPatternIdx()));
        vars.add(Utils.concatenate(name, new StrView(".pattern")),
                patternIdx == -1 ? new StrView("") : params.getStrList("patterns").get(m.getPatternIdx()));
    }
}
