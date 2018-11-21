package tests;

import javafuzzysearch.searchers.TrieCutoffSearcher;
import javafuzzysearch.utils.LengthParam;
import javafuzzysearch.utils.FuzzyMatch;
import javafuzzysearch.utils.EditWeights;
import javafuzzysearch.utils.Location;

public class TrieCutoffTest{
    public static void main(String[] args){
        Tester t = new Tester("Trie Cutoff Test");
        TrieCutoffSearcher s = new TrieCutoffSearcher();
    }
}
