package tests;

import javafuzzysearch.searchers.CutoffSearcher;
import javafuzzysearch.utils.LengthParam;
import javafuzzysearch.utils.FuzzyMatch;

import java.util.List;

public class CutoffTest{
    public static void main(String[] args){
        Tester t = new Tester("Cutoff Test");
        
        CutoffSearcher s = new CutoffSearcher(new LengthParam(1, false, false), true);

        t.testStrEquals(s.search("hlelo world", "hello", false),
                        "[FuzzyMatch(index = 4, length = 5, edits = 1)]");

        t.testStrEquals(s.search("heello world", "hello", false),
                        "[FuzzyMatch(index = 5, length = 5, edits = 1)]");

        t.testStrEquals(s.search("hllo world", "hello", false),
                        "[FuzzyMatch(index = 3, length = 4, edits = 1)]");

        List<FuzzyMatch> res = s.search("hllo world", "hello", true);
        t.testStrEquals(res,
                        "[FuzzyMatch(index = 3, length = 4, edits = 1)]");
        t.testStrEquals(res.get(0).getPath(),
                        "[h, - e, l, l, o]");

        CutoffSearcher s2 = new CutoffSearcher(new LengthParam(0, false, false), new LengthParam(1, false, true), true);

        t.testStrEquals(s2.search("ello world", "hello", false),
                        "[FuzzyMatch(index = 3, length = 4, edits = 0)]");

        t.testStrEquals(s2.search("hello worl", "world", false),
                        "[FuzzyMatch(index = 10, length = 4, edits = 0)]");

        CutoffSearcher s3 = new CutoffSearcher(new LengthParam(1, false, false), new LengthParam(1, false, true), true);

        t.testStrEquals(s3.search("ello world", "hello", false),
                        "[FuzzyMatch(index = 3, length = 4, edits = 0), FuzzyMatch(index = 4, length = 5, edits = 1)]");

        t.testStrEquals(s3.search("hello worl", "world", false),
                        "[FuzzyMatch(index = 9, length = 4, edits = 1), FuzzyMatch(index = 10, length = 4, edits = 0)]");
    }
}
