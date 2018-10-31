package tests;

import javafuzzysearch.searchers.BitapSearcher;

public class BitapTest{
    public static void main(String[] args){
        Tester t = new Tester("Bitap Test");
        
        BitapSearcher s1 = new BitapSearcher(0, Integer.MAX_VALUE);
        
        t.testStrEquals(s1.search("hello world", "hello"),
                        "[FuzzyMatch(index = 4, length = 5, value = 0)]");
        
        t.testStrEquals(s1.search("hello hello", "hello"),
                        "[FuzzyMatch(index = 4, length = 5, value = 0), FuzzyMatch(index = 10, length = 5, value = 0)]");
        
        BitapSearcher s2 = new BitapSearcher(1, Integer.MAX_VALUE);
        
        t.testStrEquals(s2.search("?ello world", "hello"),
                        "[FuzzyMatch(index = 4, length = 5, value = 1)]");
        
        t.testStrEquals(s2.search("what the", "hello"),
                        "[]");
        
        BitapSearcher s3 = new BitapSearcher(0, 4);
        
        t.testStrEquals(s3.search("ello world", "hello"),
                        "[FuzzyMatch(index = 3, length = 4, value = 0)]");
        
        t.testStrEquals(s3.search("llo world", "hello"),
                        "[]");
    }
}
