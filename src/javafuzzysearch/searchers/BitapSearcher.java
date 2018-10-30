package javafuzzysearch.searchers;

import javafuzzysearch.utils.FuzzyMatch;
import javafuzzysearch.utils.Utils;
import javafuzzysearch.utils.BitVector;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

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
    
    private char[] bp = {'A', 'T', 'C', 'G', 'N'};
    
    public Map<Character, BitVector> preprocessPattern(String pattern){
        Map<Character, BitVector> res = new HashMap<>();
        
        for(int i = 0; i < bp.length; i++){
            res.put(bp[i], new BitVector(pattern.length()));
        }
        
        for(int i = 0; i < pattern.length(); i++){
            char c = Character.toUpperCase(pattern.charAt(i));
            res.get(c).set(i);
        }
        
        return res;
    }
    
    public List<FuzzyMatch<Integer>> search(String text, String pattern){
        if(pattern.isEmpty())
            return new ArrayList<FuzzyMatch<Integer>>();
        
        int currMinOverlap = Math.min(pattern.length(), minOverlap);
        text = Utils.repeatChar('#', pattern.length() - currMinOverlap) + text;
        List<FuzzyMatch<Integer>> matches = new ArrayList<>();
        
        // max edits allowed assuming full overlap between pattern and text
        int fullMaxEdits = (int)(maxEdits < 1.0 ? (maxEdits * pattern.length()) : maxEdits);
        Map<Character, BitVector> pm = preprocessPattern(pattern);
        BitVector[] r = new BitVector[fullMaxEdits + 1];
        
        for(int i = 0; i <= fullMaxEdits; i++){
            r[i] = new BitVector(pattern.length() + 1).set(0);
        }
        
        for(int i = 0; i < text.length(); i++){
            BitVector old = new BitVector(pattern.length() + 1).or(r[0]);
            boolean found = false;
            
            for(int j = 0; j <= fullMaxEdits; j++){
                if(j == 0){
                    if(text.charAt(i) != '#')
                        r[0].and(pm.get(Character.toUpperCase(text.charAt(i))));
                }else{
                    BitVector temp = new BitVector(pattern.length() + 1).or(r[j]);
                    (text.charAt(i) == '#' ? r[j] : r[j].and(pm.get(Character.toUpperCase(text.charAt(i))))).or(old);
                    old = temp;
                }
                
                r[j].leftShift().set(0);
                
                if(!found && r[j].get(pattern.length())){
                    int index = i - (pattern.length() - currMinOverlap);
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
