package tests;

import javafuzzysearch.searchers.MyersSearcher;
import javafuzzysearch.utils.LengthParam;
import javafuzzysearch.utils.StrView;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

public class MyersTest{
    public static void main(String[] args){
        Tester t = new Tester("Myer's Algorithm Test");
        
        MyersSearcher s1 = new MyersSearcher();
        
        t.testStrEquals(s1.search(new StrView("hello world"), new StrView("hello")),
                        "[FuzzyMatch(index = 4, length = 5, overlap = 5, score = 0)]");
        
        t.testStrEquals(s1.search(new StrView("hello hello"), new StrView("hello")),
                        "[FuzzyMatch(index = 4, length = 5, overlap = 5, score = 0), FuzzyMatch(index = 10, length = 5, overlap = 5, score = 0)]");
        
        MyersSearcher s2 = new MyersSearcher().maxEdits(new LengthParam(1));
        
        t.testStrEquals(s2.search(new StrView("?ello world"), new StrView("hello")),
                        "[FuzzyMatch(index = 4, length = 5, overlap = 5, score = 1)]");
        
        t.testStrEquals(s2.search(new StrView("helo world"), new StrView("hello")),
                        "[FuzzyMatch(index = 3, length = 5, overlap = 5, score = 1)]");
        
        t.testStrEquals(s2.search(new StrView("helllo world"), new StrView("hello")),
                        "[FuzzyMatch(index = 3, length = 5, overlap = 5, score = 1), FuzzyMatch(index = 4, length = 5, overlap = 5, score = 1), FuzzyMatch(index = 5, length = 5, overlap = 5, score = 1)]");
        
        t.testStrEquals(s2.search(new StrView("what the"), new StrView("hello")),
                        "[]");
        
        MyersSearcher s3 = new MyersSearcher().maxEdits(new LengthParam(1)).allowTranspositions();

        t.testStrEquals(s3.search(new StrView("ehllo world"), new StrView("hello")),
                        "[FuzzyMatch(index = 4, length = 5, overlap = 5, score = 1)]");

        MyersSearcher s4 = new MyersSearcher().maxEdits(new LengthParam(1)).minOverlap(new LengthParam(1, false, true));

        t.testStrEquals(s4.search(new StrView("hello woorl"), new StrView("world")),
                        "[FuzzyMatch(index = 11, length = 5, overlap = 4, score = 1)]");

        Map<Character, Set<Character>> wildcards = new HashMap<>();
        Set<Character> set = new HashSet<>();
        set.add('e');
        wildcards.put('*', set);

        MyersSearcher s5 = new MyersSearcher().wildcardChars(wildcards);

        t.testStrEquals(s5.search(new StrView("h*llo world"), new StrView("hello")),
                        "[FuzzyMatch(index = 4, length = 5, overlap = 5, score = 0)]");

        t.testStrEquals(s5.search(new StrView("h**lo world"), new StrView("hello")),
                        "[]");
    }
}
