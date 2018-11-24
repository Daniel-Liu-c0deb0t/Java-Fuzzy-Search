package tests;

import javafuzzysearch.searchers.CutoffSearcher;
import javafuzzysearch.utils.LengthParam;
import javafuzzysearch.utils.FuzzyMatch;
import javafuzzysearch.utils.EditWeights;
import javafuzzysearch.utils.Location;
import javafuzzysearch.utils.StrView;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

public class CutoffTest{
    public static void main(String[] args){
        Tester t = new Tester("Cutoff Test");
        
        CutoffSearcher s = new CutoffSearcher().scoreThreshold(new LengthParam(1)).allowTranspositions();

        t.testStrEquals(s.search(new StrView("hlelo world"), new StrView("hello"), false),
                        "[FuzzyMatch(index = 4, length = 5, overlap = 5, score = 1)]");

        t.testStrEquals(s.search(new StrView("heello world"), new StrView("hello"), false),
                        "[FuzzyMatch(index = 5, length = 5, overlap = 5, score = 1)]");

        t.testStrEquals(s.search(new StrView("hllo world"), new StrView("hello"), false),
                        "[FuzzyMatch(index = 3, length = 4, overlap = 5, score = 1)]");

        List<FuzzyMatch> res = s.search(new StrView("hllo world"), new StrView("hello"), true);
        t.testStrEquals(res,
                        "[FuzzyMatch(index = 3, length = 4, overlap = 5, score = 1)]");
        t.testStrEquals(res.get(0).getPath(),
                        "['h', - 'e', 'l', 'l', 'o']");

        CutoffSearcher s2 = new CutoffSearcher().minOverlap(new LengthParam(1, false, true), Location.ANY);

        t.testStrEquals(s2.search(new StrView("ello world"), new StrView("hello"), false),
                        "[FuzzyMatch(index = 3, length = 5, overlap = 4, score = 0)]");

        t.testStrEquals(s2.search(new StrView("hello worl"), new StrView("world"), false),
                        "[FuzzyMatch(index = 10, length = 5, overlap = 4, score = 0)]");

        CutoffSearcher s3 = new CutoffSearcher().scoreThreshold(new LengthParam(1)).minOverlap(new LengthParam(1, false, true), Location.ANY);

        t.testStrEquals(s3.search(new StrView("ello world"), new StrView("hello"), false),
                        "[FuzzyMatch(index = 2, length = 4, overlap = 4, score = 1), FuzzyMatch(index = 3, length = 5, overlap = 4, score = 0), FuzzyMatch(index = 4, length = 6, overlap = 4, score = 1)]");

        t.testStrEquals(s3.search(new StrView("hello worl"), new StrView("world"), false),
                        "[FuzzyMatch(index = 9, length = 4, overlap = 5, score = 1), FuzzyMatch(index = 10, length = 5, overlap = 4, score = 0)]");

        EditWeights w = new EditWeights().setDefault(0, 1, Integer.MAX_VALUE, 2);

        CutoffSearcher s4 = new CutoffSearcher().scoreThreshold(new LengthParam(2)).editWeights(w);

        t.testStrEquals(s4.search(new StrView("helo world"), new StrView("hello"), false),
                        "[FuzzyMatch(index = 3, length = 4, overlap = 5, score = 2), FuzzyMatch(index = 4, length = 5, overlap = 5, score = 2)]");

        Map<Character, Set<Character>> wildcards = new HashMap<>();
        Set<Character> set = new HashSet<>();
        set.add('e');
        wildcards.put('*', set);

        CutoffSearcher s5 = new CutoffSearcher().wildcardChars(wildcards);

        t.testStrEquals(s5.search(new StrView("h*llo world"), new StrView("hello"), false),
                        "[FuzzyMatch(index = 4, length = 5, overlap = 5, score = 0)]");

        t.testStrEquals(s5.search(new StrView("h**lo world"), new StrView("hello"), false),
                        "[]");

        Map<Character, Set<Character>> wildcards2 = new HashMap<>();
        wildcards2.put('*', null);

        CutoffSearcher s6 = new CutoffSearcher().wildcardChars(wildcards2);

        t.testStrEquals(s6.search(new StrView("h**lo world"), new StrView("hello"), false),
                        "[FuzzyMatch(index = 4, length = 5, overlap = 5, score = 0)]");

        Set<Integer> idx = new HashSet<>();
        idx.add(2);

        t.testStrEquals(s6.search(new StrView("h**lo world"), new StrView("hello"), false, idx, new HashSet<Integer>()),
                        "[]");

        EditWeights w2 = new EditWeights().setDefault(1, -1, -1, -1);

        CutoffSearcher s7 = new CutoffSearcher().scoreThreshold(new LengthParam(2, false, true)).editWeights(w2).maximizeScore();

        t.testStrEquals(s7.search(new StrView("hlllo world"), new StrView("hello"), false),
                        "[FuzzyMatch(index = 4, length = 5, overlap = 5, score = 3)]");
    }
}
