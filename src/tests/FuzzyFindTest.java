package tests;

import fuzzyfind.WholePattern;
import fuzzyfind.FuzzyPattern;
import fuzzyfind.RepeatingFixedPattern;
import fuzzyfind.RepeatingIntervalPattern;
import fuzzyfind.Pattern;
import javafuzzysearch.utils.StrView;
import javafuzzysearch.utils.FuzzyMatch;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class FuzzyFindTest{
    public static void main(String[] args){
        FuzzyPattern p = new FuzzyPattern();
        Map<StrView, StrView> m = new HashMap<>();
        m.put(new StrView("length"), new StrView("2"));
        m.put(new StrView("pattern"), new StrView("*"));
        m.put(new StrView("required"), new StrView(""));
        RepeatingFixedPattern p2 = new RepeatingFixedPattern(m);

        Map<StrView, StrView> m2 = new HashMap<>();
        m.put(new StrView("length"), new StrView("0-3"));
        m.put(new StrView("pattern"), new StrView("a"));
        RepeatingIntervalPattern p3 = new RepeatingIntervalPattern(m);

        List<List<List<Pattern>>> l = Arrays.asList(Arrays.asList(Arrays.asList(p, p3, p2)));
        List<List<List<Boolean>>> l2 = Arrays.asList(Arrays.asList(Arrays.asList(true, true)));
        WholePattern w = new WholePattern(l, l2);
        List<List<List<FuzzyMatch>>> res = w.search(Arrays.asList(Arrays.asList(new StrView("helo**"))));
        System.out.println(res.toString());
    }
}
