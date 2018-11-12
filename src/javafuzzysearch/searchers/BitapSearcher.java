package javafuzzysearch.searchers;

import javafuzzysearch.utils.FuzzyMatch;
import javafuzzysearch.utils.Utils;
import javafuzzysearch.utils.BitVector;
import javafuzzysearch.utils.LengthParam;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

/**
 * Implementation of the Bitap fuzzy searching algorithm for Hamming distance.
 */
public class BitapSearcher{
    private LengthParam maxEdits;
    private LengthParam minOverlap;
    
    public BitapSearcher(LengthParam maxEdits){
        this.maxEdits = maxEdits;
        this.minOverlap = new LengthParam(0, false, true);
    }

    public BitapSearcher(LengthParam maxEdits, LengthParam minOverlap){
        this.maxEdits = maxEdits;
        this.minOverlap = minOverlap;
    }
    
    public Map<Character, BitVector> preprocessPattern(String pattern, Set<Character> alphabet){
        Map<Character, BitVector> res = new HashMap<>();
        
        for(char a : alphabet){
            res.put(a, new BitVector(pattern.length() + 1));
        }
        
        for(int i = 0; i < pattern.length(); i++){
            res.get(pattern.charAt(i)).set(i);
        }
        
        return res;
    }
    
    public List<FuzzyMatch> search(String text, String pattern){
        return search(text, pattern, preprocessPattern(pattern, Utils.uniqueChars(text, pattern)));
    }
    
    public List<FuzzyMatch> search(String text, String pattern, Map<Character, BitVector> patternIdx){
        if(pattern.isEmpty())
            return new ArrayList<FuzzyMatch>();
        
        int currMinOverlap = minOverlap.get(pattern.length());
        int currMaxNonOverlap = pattern.length() - currMinOverlap;
        
        // max edits allowed assuming full overlap between pattern and text
        int currFullMaxEdits = maxEdits.get(pattern.length());
        
        BitVector[] r = new BitVector[currFullMaxEdits + 1];
        List<FuzzyMatch> matches = new ArrayList<>();
        
        for(int i = 0; i <= currFullMaxEdits; i++){
            r[i] = new BitVector(pattern.length() + 1).set(0);
        }
        
        for(int i = 0; i < currMaxNonOverlap * 2 + text.length(); i++){
            BitVector old = new BitVector(pattern.length() + 1).or(r[0]);
            boolean found = false;
            
            for(int j = 0; j <= currFullMaxEdits; j++){
                if(j == 0){
                    if(i >= currMaxNonOverlap && i < currMaxNonOverlap + text.length())
                        r[0].and(patternIdx.get(text.charAt(i - currMaxNonOverlap)));
                }else{
                    BitVector temp = new BitVector(pattern.length() + 1).or(r[j]);
                    
                    if(i >= currMaxNonOverlap && i < currMaxNonOverlap + text.length())
                        r[j].and(patternIdx.get(text.charAt(i - currMaxNonOverlap)));
                    
                    r[j].or(old);
                    
                    old = temp;
                }
                
                r[j].leftShift().set(0);
                
                if(!found && r[j].get(pattern.length())){
                    int index = i - currMaxNonOverlap;
                    int length = Math.min(index + 1, Math.min(pattern.length(), text.length() - (index + 1 - pattern.length())));
                    int currPartialMaxEdits = maxEdits.get(length);
                    
                    if(j <= currPartialMaxEdits && length >= currMinOverlap){
                        matches.add(new FuzzyMatch(index, length, j));
                    }
                    
                    found = true;
                }
            }
        }
        
        return matches;
    }
}
