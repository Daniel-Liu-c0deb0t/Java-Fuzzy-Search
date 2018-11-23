package tests;

import javafuzzysearch.searchers.CutoffSearcher;
import javafuzzysearch.utils.LengthParam;
import javafuzzysearch.utils.FuzzyMatch;
import javafuzzysearch.utils.EditWeights;
import javafuzzysearch.utils.Location;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

public class CutoffTest{
    public static void main(String[] args){
        Tester t = new Tester("Cutoff Test");
        
        CutoffSearcher s = new CutoffSearcher().scoreThreshold(new LengthParam(1)).allowTranspositions();

        t.testStrEquals(s.search("hlelo world", "hello", false),
                        "[FuzzyMatch(index = 4, length = 5, overlap = 5, score = 1)]");

        t.testStrEquals(s.search("heello world", "hello", false),
                        "[FuzzyMatch(index = 5, length = 5, overlap = 5, score = 1)]");

        t.testStrEquals(s.search("hllo world", "hello", false),
                        "[FuzzyMatch(index = 3, length = 4, overlap = 5, score = 1)]");

        List<FuzzyMatch> res = s.search("hllo world", "hello", true);
        t.testStrEquals(res,
                        "[FuzzyMatch(index = 3, length = 4, overlap = 5, score = 1)]");
        t.testStrEquals(res.get(0).getPath(),
                        "['h', - 'e', 'l', 'l', 'o']");

        CutoffSearcher s2 = new CutoffSearcher().minOverlap(new LengthParam(1, false, true), Location.ANY);

        t.testStrEquals(s2.search("ello world", "hello", false),
                        "[FuzzyMatch(index = 3, length = 5, overlap = 4, score = 0)]");

        t.testStrEquals(s2.search("hello worl", "world", false),
                        "[FuzzyMatch(index = 10, length = 5, overlap = 4, score = 0)]");

        CutoffSearcher s3 = new CutoffSearcher().scoreThreshold(new LengthParam(1)).minOverlap(new LengthParam(1, false, true), Location.ANY);

        t.testStrEquals(s3.search("ello world", "hello", false),
                        "[FuzzyMatch(index = 2, length = 4, overlap = 4, score = 1), FuzzyMatch(index = 3, length = 5, overlap = 4, score = 0), FuzzyMatch(index = 4, length = 6, overlap = 4, score = 1)]");

        t.testStrEquals(s3.search("hello worl", "world", false),
                        "[FuzzyMatch(index = 9, length = 4, overlap = 5, score = 1), FuzzyMatch(index = 10, length = 5, overlap = 4, score = 0)]");

        EditWeights w = new EditWeights().setDefault(0, 1, Integer.MAX_VALUE, 2);

        CutoffSearcher s4 = new CutoffSearcher().scoreThreshold(new LengthParam(2)).editWeights(w);

        t.testStrEquals(s4.search("helo world", "hello", false),
                        "[FuzzyMatch(index = 3, length = 4, overlap = 5, score = 2), FuzzyMatch(index = 4, length = 5, overlap = 5, score = 2)]");

        Map<Character, Set<Character>> wildcards = new HashMap<>();
        Set<Character> set = new HashSet<>();
        set.add('e');
        wildcards.put('*', set);

        CutoffSearcher s5 = new CutoffSearcher().wildcardChars(wildcards);

        t.testStrEquals(s5.search("h*llo world", "hello", false),
                        "[FuzzyMatch(index = 4, length = 5, overlap = 5, score = 0)]");

        t.testStrEquals(s5.search("h**lo world", "hello", false),
                        "[]");

        Map<Character, Set<Character>> wildcards2 = new HashMap<>();
        wildcards2.put('*', null);

        CutoffSearcher s6 = new CutoffSearcher().wildcardChars(wildcards2);

        t.testStrEquals(s6.search("h**lo world", "hello", false),
                        "[FuzzyMatch(index = 4, length = 5, overlap = 5, score = 0)]");

        Set<Integer> idx = new HashSet<>();
        idx.add(2);

        t.testStrEquals(s6.search("h**lo world", "hello", false, idx, new HashSet<Integer>()),
                        "[]");

        EditWeights w2 = new EditWeights().setDefault(1, -1, -1, -1);

        CutoffSearcher s7 = new CutoffSearcher().scoreThreshold(new LengthParam(2, false, true)).editWeights(w2).maximizeScore();

        t.testStrEquals(s7.search("hlllo world", "hello", false),
                        "[FuzzyMatch(index = 4, length = 5, overlap = 5, score = 3)]");
    }
}
