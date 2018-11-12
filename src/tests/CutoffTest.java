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
                        "[SAME, DEL, SAME, SAME, SAME]");
    }
}
