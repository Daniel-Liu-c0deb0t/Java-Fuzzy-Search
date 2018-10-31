package tests;

import javafuzzysearch.searchers.BitapSearcher;

public class BitapTest{
    public static void main(String[] args){
        BitapSearcher s1 = new BitapSearcher(0, Integer.MAX_VALUE);
        
        System.out.println("[FuzzyMatch(index = 4, length = 5, value = 0)]".equals(
            String.valueOf(s1.search("hello world", "hello"))));
        
        System.out.println("[FuzzyMatch(index = 4, length = 5, value = 0), FuzzyMatch(index = 10, length = 5, value = 0)]".equals(
            String.valueOf(s1.search("hello hello", "hello"))));
        
        BitapSearcher s2 = new BitapSearcher(1, Integer.MAX_VALUE);
        
        System.out.println("[FuzzyMatch(index = 4, length = 5, value = 1)]".equals(
            String.valueOf(s2.search("?ello world", "hello"))));
        
        System.out.println("[]".equals(
            String.valueOf(s2.search("what the", "hello"))));
    }
}
