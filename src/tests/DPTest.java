package tests;

import javafuzzysearch.searchers.DPSearcher;
import javafuzzysearch.utils.LengthParam;

public class DPTest{
    public static void main(String[] args){
        Tester t = new Tester("DP Test");
        
        DPSearcher s = new DPSearcher(new LengthParam(0, false, false), new LengthParam(0, false, false), false);
        s.search("hello world", "hello");
    }
}
