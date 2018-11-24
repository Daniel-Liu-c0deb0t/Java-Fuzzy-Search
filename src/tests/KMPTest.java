package tests;

import javafuzzysearch.searchers.KMPSearcher;
import javafuzzysearch.utils.StrView;

public class KMPTest{
    public static void main(String[] args){
        Tester t = new Tester("KMP Test");
        
        KMPSearcher s = new KMPSearcher();
        
        t.testStrEquals(s.search(new StrView("hello world"), new StrView("hello")),
                        "[ExactMatch(index = 4)]");
        
        t.testStrEquals(s.search(new StrView("hello hello"), new StrView("hello")),
                        "[ExactMatch(index = 4), ExactMatch(index = 10)]");
        
        t.testStrEquals(s.search(new StrView("world world"), new StrView("hello")),
                        "[]");
        
        int[] lsp = s.preprocessPattern(new StrView("hello"));
        
        t.testStrEquals(s.search(new StrView("hello world"), new StrView("hello"), lsp),
                        "[ExactMatch(index = 4)]");
    }
}
