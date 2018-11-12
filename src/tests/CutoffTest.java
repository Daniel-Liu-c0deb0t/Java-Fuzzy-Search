package tests;

import javafuzzysearch.searchers.CutoffSearcher;
import javafuzzysearch.utils.LengthParam;

public class CutoffTest{
    public static void main(String[] args){
        Tester t = new Tester("Cutoff Test");
        
        CutoffSearcher s = new CutoffSearcher(new LengthParam(1, false, false), true);

        t.testStrEquals(s.search("hlelo world", "hello"),
                        "[FuzzyMatch(index = 4, length = 5, edits = 1)]");

        t.testStrEquals(s.search("heello world", "hello"),
                        "[FuzzyMatch(index = 5, length = 5, edits = 1)]");

        t.testStrEquals(s.search("hllo world", "hello"),
                        "[FuzzyMatch(index = 3, length = 4, edits = 1)]");
    }
}
