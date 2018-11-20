package tests;

import javafuzzysearch.searchers.CutoffSearcher;
import javafuzzysearch.utils.LengthParam;
import javafuzzysearch.utils.FuzzyMatch;
import javafuzzysearch.utils.EditWeights;

import java.util.List;

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

        CutoffSearcher s2 = new CutoffSearcher().minOverlap(new LengthParam(1, false, true));

        t.testStrEquals(s2.search("ello world", "hello", false),
                        "[FuzzyMatch(index = 3, length = 5, overlap = 4, score = 0)]");

        t.testStrEquals(s2.search("hello worl", "world", false),
                        "[FuzzyMatch(index = 10, length = 5, overlap = 4, score = 0)]");

        CutoffSearcher s3 = new CutoffSearcher().scoreThreshold(new LengthParam(1)).minOverlap(new LengthParam(1, false, true));

        t.testStrEquals(s3.search("ello world", "hello", false),
                        "[FuzzyMatch(index = 2, length = 4, overlap = 4, score = 1), FuzzyMatch(index = 3, length = 5, overlap = 4, score = 0), FuzzyMatch(index = 4, length = 6, overlap = 4, score = 1)]");

        t.testStrEquals(s3.search("hello worl", "world", false),
                        "[FuzzyMatch(index = 9, length = 4, overlap = 5, score = 1), FuzzyMatch(index = 10, length = 5, overlap = 4, score = 0)]");

        EditWeights w = new EditWeights().setDefault(0, 1, Integer.MAX_VALUE, 2);

        CutoffSearcher s4 = new CutoffSearcher().scoreThreshold(new LengthParam(2)).editWeights(w);

        t.testStrEquals(s4.search("helo world", "hello", false),
                        "[FuzzyMatch(index = 3, length = 4, overlap = 5, score = 2), FuzzyMatch(index = 4, length = 5, overlap = 5, score = 2)]");
    }
}
