package tests;

import javafuzzysearch.searchers.BitapSearcher;

public class BitapTest{
    public static void main(String[] args){
        BitapSearcher s = new BitapSearcher(0, 1000);
        System.out.println("[FuzzyMatch(index = 2, length = 3, value = 0)]".equals(
            String.valueOf(s.search("AAATTTT", "AAA"))));
    }
}
