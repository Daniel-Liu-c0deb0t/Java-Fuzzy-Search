package javafuzzysearch.searchers;

import javafuzzysearch.utils.FuzzyMatch;
import javafuzzysearch.utils.Utils;
import javafuzzysearch.utils.BitVector;
import javafuzzysearch.utils.LengthParam;
import javafuzzysearch.utils.Location;
import javafuzzysearch.utils.StrView;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

/**
 * Implementation of the Bitap fuzzy searching algorithm for Hamming distance.
 */
public class BitapSearcher{
    private LengthParam maxEdits = new LengthParam(0, false, false);
    private LengthParam minOverlap = new LengthParam(0, false, true);
    private Map<Character, Set<Character>> wildcardChars = new HashMap<>();
    private Location nonOverlapLocation = Location.ANY;

    public BitapSearcher maxEdits(LengthParam maxEdits){
        this.maxEdits = maxEdits;
        return this;
    }

    public BitapSearcher minOverlap(LengthParam minOverlap, Location nonOverlapLocation){
        this.minOverlap = minOverlap;
        this.nonOverlapLocation = nonOverlapLocation;
        return this;
    }

    public BitapSearcher wildcardChars(Map<Character, Set<Character>> wildcardChars){
        this.wildcardChars = wildcardChars;
        return this;
    }

    public Map<Boolean, Map<Character, BitVector>> preprocessPattern(StrView pattern, Set<Character> alphabet, Set<Integer> patternEscapeIdx){
        Map<Boolean, Map<Character, BitVector>> res = new HashMap<>();

        Map<Character, BitVector> normalMasks = new HashMap<>();
        Map<Character, BitVector> wildcardMasks = new HashMap<>();

        for(char c : alphabet){
            normalMasks.put(c, new BitVector(pattern.length() + 1));
            if(wildcardChars.containsKey(c))
                wildcardMasks.put(c, new BitVector(pattern.length() + 1));
        }

        for(int i = 0; i < pattern.length(); i++){
            char c = pattern.charAt(i);
            normalMasks.get(c).set(i);

            if(wildcardChars.containsKey(c) && !patternEscapeIdx.contains(i)){
                Set<Character> set;

                if(wildcardChars.get(c) == null)
                    set = alphabet;
                else
                    set = wildcardChars.get(c);

                for(char d : set){
                    normalMasks.get(d).set(i);
                }
            }

            for(char wildcard : wildcardChars.keySet()){
                Set<Character> set = wildcardChars.get(wildcard);
                if(set == null || set.contains(c))
                    wildcardMasks.get(wildcard).set(i);
            }
        }

        res.put(false, normalMasks);
        res.put(true, wildcardMasks);

        return res;
    }

    public List<FuzzyMatch> search(StrView text, StrView pattern){
        return search(text, pattern, preprocessPattern(pattern, Utils.uniqueChars(text, pattern), new HashSet<Integer>()), new HashSet<Integer>());
    }

    public List<FuzzyMatch> search(StrView text, StrView pattern, Map<Boolean, Map<Character, BitVector>> patternMask, Set<Integer> textEscapeIdx){
        if(pattern.isEmpty())
            return new ArrayList<FuzzyMatch>();
        
        int currMinOverlap = minOverlap.get(pattern.length());
        int currMaxNonOverlap = pattern.length() - currMinOverlap;

        int startMaxNonOverlap = 0;
        if(nonOverlapLocation != Location.END)
            startMaxNonOverlap = currMaxNonOverlap;

        int endMaxNonOverlap = 0;
        if(nonOverlapLocation != Location.START)
            endMaxNonOverlap = currMaxNonOverlap;

        // max edits allowed assuming full overlap between pattern and text
        int currFullMaxEdits = maxEdits.get(pattern.length());

        BitVector[] r = new BitVector[currFullMaxEdits + 1];
        List<FuzzyMatch> matches = new ArrayList<>();
        
        for(int i = 0; i <= currFullMaxEdits; i++){
            r[i] = new BitVector(pattern.length() + 1).set(0);
        }
        
        for(int i = 0; i < startMaxNonOverlap + endMaxNonOverlap + text.length(); i++){
            BitVector old = new BitVector(pattern.length() + 1).or(r[0]);
            boolean found = false;
            
            for(int j = 0; j <= currFullMaxEdits; j++){
                if(j == 0){
                    if(i >= startMaxNonOverlap && i < startMaxNonOverlap + text.length()){
                        int idx = i - startMaxNonOverlap;
                        r[0].and(patternMask.get(wildcardChars.containsKey(text.charAt(idx)) &&
                                    !textEscapeIdx.contains(idx)).get(text.charAt(idx)));
                    }
                }else{
                    BitVector temp = new BitVector(pattern.length() + 1).or(r[j]);
                    
                    if(i >= startMaxNonOverlap && i < startMaxNonOverlap + text.length()){
                        int idx = i - startMaxNonOverlap;
                        r[j].and(patternMask.get(wildcardChars.containsKey(text.charAt(idx)) &&
                                    !textEscapeIdx.contains(idx)).get(text.charAt(idx)));
                    }
                    
                    r[j].or(old);
                    
                    old = temp;
                }
                
                r[j].leftShift().set(0);
                
                if(!found && r[j].get(pattern.length())){
                    int index = i - startMaxNonOverlap;
                    int nonOverlapLength = Math.max(pattern.length() - index - 1, 0) + Math.max(index + 1 - text.length(), 0);
                    int overlapLength = pattern.length() - nonOverlapLength;
                    int currPartialMaxEdits = maxEdits.get(overlapLength);

                    if(j <= currPartialMaxEdits && overlapLength >= currMinOverlap){
                        matches.add(new FuzzyMatch(index, pattern.length(), overlapLength, j));
                    }
                    
                    found = true;
                }
            }
        }
        
        return matches;
    }
}
