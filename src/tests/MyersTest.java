package tests;

import javafuzzysearch.searchers.MyersSearcher;
import javafuzzysearch.utils.LengthParam;

public class MyersTest{
    public static void main(String[] args){
        Tester t = new Tester("Myer's Algorithm Test");
        
        MyersSearcher s1 = new MyersSearcher(new LengthParam(0, false, false), false);
        
        t.testStrEquals(s1.search("hello world", "hello"),
                        "[FuzzyMatch(index = 4, length = 5, edits = 0)]");
        
        t.testStrEquals(s1.search("hello hello", "hello"),
                        "[FuzzyMatch(index = 4, length = 5, edits = 0), FuzzyMatch(index = 10, length = 5, edits = 0)]");
        
        MyersSearcher s2 = new MyersSearcher(new LengthParam(1, false, false), false);
        
        t.testStrEquals(s2.search("?ello world", "hello"),
                        "[FuzzyMatch(index = 4, length = 5, edits = 1)]");
        
        t.testStrEquals(s2.search("helo world", "hello"),
                        "[FuzzyMatch(index = 3, length = 5, edits = 1)]");
        
        t.testStrEquals(s2.search("helllo world", "hello"),
                        "[FuzzyMatch(index = 3, length = 5, edits = 1), FuzzyMatch(index = 4, length = 5, edits = 1), FuzzyMatch(index = 5, length = 5, edits = 1)]");
        
        t.testStrEquals(s2.search("what the", "hello"),
                        "[]");
        
        MyersSearcher s3 = new MyersSearcher(new LengthParam(1, false, false), true);

        t.testStrEquals(s3.search("ehllo world", "hello"),
                        "[FuzzyMatch(index = 4, length = 5, edits = 1)]");
    }
}
