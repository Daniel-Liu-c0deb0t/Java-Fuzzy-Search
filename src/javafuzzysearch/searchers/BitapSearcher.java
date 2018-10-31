package javafuzzysearch.searchers;

import javafuzzysearch.utils.FuzzyMatch;
import javafuzzysearch.utils.Utils;
import javafuzzysearch.utils.BitVector;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

/**
 * Implementation of the Bitap fuzzy matching algorithm for Hamming distance.
 */
public class BitapSearcher{
    private double maxEdits;
    private int minOverlap;
    
    public BitapSearcher(double maxEdits, int minOverlap){
        this.maxEdits = maxEdits;
        this.minOverlap = minOverlap;
    }
    
    public Map<Character, BitVector> preprocessPattern(String pattern, Set<Character> alphabet){
        Map<Character, BitVector> res = new HashMap<>();
        
        for(char a : alphabet){
            res.put(a, new BitVector(pattern.length()));
        }
        
        for(int i = 0; i < pattern.length(); i++){
            res.get(pattern.charAt(i)).set(i);
        }
        
        return res;
    }
    
    public List<FuzzyMatch<Integer>> search(String text, String pattern){
        return search(text, pattern, preprocessPattern(pattern, Utils.uniqueChars(text, pattern)));
    }
    
    public List<FuzzyMatch<Integer>> search(String text, String pattern, Map<Character, BitVector> patternIdx){
        if(pattern.isEmpty())
            return new ArrayList<FuzzyMatch<Integer>>();
        
        int currMinOverlap = Math.min(pattern.length(), minOverlap);
        int maxNonOverlap = pattern.length() - currMinOverlap;
        
        // max edits allowed assuming full overlap between pattern and text
        int fullMaxEdits = (int)(maxEdits < 1.0 ? (maxEdits * pattern.length()) : maxEdits);
        
        BitVector[] r = new BitVector[fullMaxEdits + 1];
        List<FuzzyMatch<Integer>> matches = new ArrayList<>();
        
        for(int i = 0; i <= fullMaxEdits; i++){
            r[i] = new BitVector(pattern.length() + 1).set(0);
        }
        
        for(int i = 0; i < maxNonOverlap * 2 + text.length(); i++){
            BitVector old = new BitVector(pattern.length() + 1).or(r[0]);
            boolean found = false;
            
            for(int j = 0; j <= fullMaxEdits; j++){
                if(j == 0){
                    if(i >= maxNonOverlap && i < maxNonOverlap + text.length())
                        r[0].and(patternIdx.get(text.charAt(i - maxNonOverlap)));
                }else{
                    BitVector temp = new BitVector(pattern.length() + 1).or(r[j]);
                    
                    if(i >= maxNonOverlap && i < maxNonOverlap + text.length())
                        r[j].and(patternIdx.get(text.charAt(i - maxNonOverlap))).or(old);
                    else
                        r[j].or(old);
                    
                    old = temp;
                }
                
                r[j].leftShift().set(0);
                
                if(!found && r[j].get(pattern.length())){
                    int index = i - maxNonOverlap;
                    int length = Math.min(index + 1, pattern.length());
                    int partialMaxEdits = (int)(maxEdits < 1.0 ? (maxEdits * length) : maxEdits);
                    
                    if(j <= partialMaxEdits && length >= currMinOverlap){
                        matches.add(new FuzzyMatch<Integer>(index, length, j));
                    }
                    
                    found = true;
                }
            }
        }
        
        return matches;
    }
}
