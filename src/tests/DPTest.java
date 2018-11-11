package tests;

import javafuzzysearch.searchers.DPSearcher;
import javafuzzysearch.utils.LengthParam;

public class DPTest{
    public static void main(String[] args){
        Tester t = new Tester("DP Test");
        
        DPSearcher s = new DPSearcher(new LengthParam(0, false, false), new LengthParam(0, false, false), false);
        t.testStrEquals(s.search("hello world", "hello"),
                        "[FuzzyMatch(index = 4, length = 5, edits = 0)]");
    }
}
