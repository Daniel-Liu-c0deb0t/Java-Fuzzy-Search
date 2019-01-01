package tests;

import fuzzysplit.patterns.WholePattern;
import fuzzysplit.patterns.FuzzyPattern;
import fuzzysplit.patterns.RepeatingFixedPattern;
import fuzzysplit.patterns.RepeatingIntervalPattern;
import fuzzysplit.patterns.Pattern;
import fuzzysplit.utils.PatternMatch;

import javafuzzysearch.utils.StrView;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class FuzzySplitTest{
    public static void main(String[] args){
        Map<StrView, StrView> m = new HashMap<>();
        m.put(new StrView("edits"), new StrView("1"));
        m.put(new StrView("required"), new StrView(""));
        m.put(new StrView("name"), new StrView("\"f1\""));
        m.put(new StrView("pattern"), new StrView("f\"test_patterns.txt\""));
        FuzzyPattern p = new FuzzyPattern(m);

        Map<StrView, StrView> m3 = new HashMap<>();
        m3.put(new StrView("edits"), new StrView("0"));
        m3.put(new StrView("pattern"), new StrView("f\"test_patterns.txt\"[%f1.pattern_name%]"));
        FuzzyPattern p4 = new FuzzyPattern(m3);

        Map<StrView, StrView> m1 = new HashMap<>();
        m1.put(new StrView("length"), new StrView("2"));
        m1.put(new StrView("pattern"), new StrView("\"*\""));
        m1.put(new StrView("required"), new StrView(""));
        RepeatingFixedPattern p2 = new RepeatingFixedPattern(m1);

        Map<StrView, StrView> m2 = new HashMap<>();
        m2.put(new StrView("length"), new StrView("0-10"));
        m2.put(new StrView("pattern"), new StrView("\"a-z*\""));
        RepeatingIntervalPattern p3 = new RepeatingIntervalPattern(m2);

        List<List<List<Pattern>>> l = Arrays.asList(Arrays.asList(Arrays.asList(p, p3), Arrays.asList(p4)));
        List<List<Integer>> idx = Arrays.asList(Arrays.asList(1, 2));
        WholePattern w = new WholePattern(l, idx);
        List<List<List<PatternMatch>>> res = w.search(Arrays.asList(Arrays.asList(new StrView("worl**"), new StrView("world"))));
        System.out.println(res.toString());
    }
}