package tests;

import javafuzzysearch.searchers.KMPSearcher;

public class KMPTest{
    public static void main(String[] args){
        Tester t = new Tester("KMP Test");
        
        KMPSearcher s = new KMPSearcher();
        
        t.testStrEquals(s.search("hello world", "hello"),
                        "[ExactMatch(index = 4)]");
        
        t.testStrEquals(s.search("hello hello", "hello"),
                        "[ExactMatch(index = 4), ExactMatch(index = 10)]");
        
        t.testStrEquals(s.search("world world", "hello"),
                        "[]");
        
        int[] lsp = s.preprocessPattern("hello");
        
        t.testStrEquals(s.search("hello world", "hello", lsp),
                        "[ExactMatch(index = 4)]");
    }
}
