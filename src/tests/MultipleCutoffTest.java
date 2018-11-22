package tests;

import javafuzzysearch.searchers.MultipleCutoffSearcher;
import javafuzzysearch.utils.LengthParam;
import javafuzzysearch.utils.FuzzyMatch;
import javafuzzysearch.utils.EditWeights;
import javafuzzysearch.utils.Location;

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

        System.out.println(s.search("hello what code", p, true));
    }
}
