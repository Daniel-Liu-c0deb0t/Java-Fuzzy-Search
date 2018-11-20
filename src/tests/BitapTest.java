package tests;

import javafuzzysearch.searchers.BitapSearcher;
import javafuzzysearch.utils.LengthParam;

public class BitapTest{
    public static void main(String[] args){
        Tester t = new Tester("Bitap Test");
        
        BitapSearcher s1 = new BitapSearcher();

        t.testStrEquals(s1.search("hello world", "hello"),
                        "[FuzzyMatch(index = 4, length = 5, overlap = 5, score = 0)]");
        
        t.testStrEquals(s1.search("hello hello", "hello"),
                        "[FuzzyMatch(index = 4, length = 5, overlap = 5, score = 0), FuzzyMatch(index = 10, length = 5, overlap = 5, score = 0)]");
        
        BitapSearcher s2 = new BitapSearcher().maxEdits(new LengthParam(1));
        
        t.testStrEquals(s2.search("?ello world", "hello"),
                        "[FuzzyMatch(index = 4, length = 5, overlap = 5, score = 1)]");
        
        t.testStrEquals(s2.search("what the", "hello"),
                        "[]");
        
        BitapSearcher s3 = new BitapSearcher().minOverlap(new LengthParam(4, false, false));
        
        t.testStrEquals(s3.search("ello world", "hello"),
                        "[FuzzyMatch(index = 3, length = 5, overlap = 4, score = 0)]");
        
        t.testStrEquals(s3.search("llo world", "hello"),
                        "[]");

        t.testStrEquals(s3.search("hello worl", "world"),
                        "[FuzzyMatch(index = 10, length = 5, overlap = 4, score = 0)]");
    }
}
