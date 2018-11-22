package tests;

import javafuzzysearch.searchers.MultipleCutoffSearcher;

import java.util.List;
import java.util.ArrayList;

public class MultipleCutoffTest{
    public static void main(String[] args){
        Tester t = new Tester("Multiple Pattern Cutoff Test");

        MultipleCutoffSearcher s = new MultipleCutoffSearcher();

        List<String> p = new ArrayList<>();
        p.add("hello");
        p.add("what");
        p.add("code");
        p.add("coding");
        p.add("wha");

        t.testStrEquals(s.search("hello what code", p, false),
                "[[FuzzyMatch(index = 4, length = 5, overlap = 5, score = 0)], [FuzzyMatch(index = 9, length = 4, overlap = 4, score = 0)], [FuzzyMatch(index = 14, length = 4, overlap = 4, score = 0)], [], [FuzzyMatch(index = 8, length = 3, overlap = 3, score = 0)]]");
    }
}
