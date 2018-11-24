package tests;

import javafuzzysearch.searchers.BitapSearcher;
import javafuzzysearch.utils.LengthParam;
import javafuzzysearch.utils.Location;
import javafuzzysearch.utils.StrView;

public class BitapTest{
    public static void main(String[] args){
        Tester t = new Tester("Bitap Test");
        
        BitapSearcher s1 = new BitapSearcher();

        t.testStrEquals(s1.search(new StrView("hello world"), new StrView("hello")),
                        "[FuzzyMatch(index = 4, length = 5, overlap = 5, score = 0)]");
        
        t.testStrEquals(s1.search(new StrView("hello hello"), new StrView("hello")),
                        "[FuzzyMatch(index = 4, length = 5, overlap = 5, score = 0), FuzzyMatch(index = 10, length = 5, overlap = 5, score = 0)]");
        
        BitapSearcher s2 = new BitapSearcher().maxEdits(new LengthParam(1));
        
        t.testStrEquals(s2.search(new StrView("?ello world"), new StrView("hello")),
                        "[FuzzyMatch(index = 4, length = 5, overlap = 5, score = 1)]");
        
        t.testStrEquals(s2.search(new StrView("what the"), new StrView("hello")),
                        "[]");
        
        BitapSearcher s3 = new BitapSearcher().minOverlap(new LengthParam(4, false, false), Location.ANY);
        
        t.testStrEquals(s3.search(new StrView("ello world"), new StrView("hello")),
                        "[FuzzyMatch(index = 3, length = 5, overlap = 4, score = 0)]");
        
        t.testStrEquals(s3.search(new StrView("llo world"), new StrView("hello")),
                        "[]");

        t.testStrEquals(s3.search(new StrView("hello worl"), new StrView("world")),
                        "[FuzzyMatch(index = 10, length = 5, overlap = 4, score = 0)]");
    }
}
