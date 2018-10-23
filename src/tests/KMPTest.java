package tests;

import javafuzzysearch.searchers.KMPSearcher;

public class KMPTest{
    public static void main(String[] args){
        KMPSearcher s = new KMPSearcher();
        System.out.println("[ExactMatch(index = 0)]".equals(
            String.valueOf(s.search("hello world", "hello"))));
        
        System.out.println("[ExactMatch(index = 0), ExactMatch(index = 6)]".equals(
            String.valueOf(s.search("hello hello", "hello"))));

        System.out.println("[]".equals(
            String.valueOf(s.search("world world", "hello"))));
        
        int[] lsp = s.preprocessPattern("hello");
        System.out.println("[ExactMatch(index = 0)]".equals(
            String.valueOf(s.search("hello world", "hello", lsp))));
    }
}