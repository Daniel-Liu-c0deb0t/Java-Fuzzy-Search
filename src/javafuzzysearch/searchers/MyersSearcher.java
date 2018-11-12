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
 * Implementation of Myer's fuzzy searching algorithm for Levenshtein distance.
 */
public class MyersSearcher{
    private LengthParam maxEdits;
    private LengthParam minOverlap;
    private boolean allowTranspositions, useMinOverlap;
    
    public MyersSearcher(LengthParam maxEdits, LengthParam minOverlap, boolean allowTranspositions){
        this.maxEdits = maxEdits;
        this.minOverlap = minOverlap;
        this.allowTranspositions = allowTranspositions;
        this.useMinOverlap = true;
    }
    
    public MyersSearcher(LengthParam maxEdits, boolean allowTranspositions){
        this.maxEdits = maxEdits;
        this.minOverlap = new LengthParam(0, false, true);
        this.allowTranspositions = allowTranspositions;
        this.useMinOverlap = false;
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
    
    public List<FuzzyMatch> search(String text, String pattern){
        return search(text, pattern, preprocessPattern(pattern, Utils.uniqueChars(text, pattern)));
    }
    
    public List<FuzzyMatch> search(String text, String pattern, Map<Character, BitVector> patternIdx){
        if(pattern.isEmpty())
            return new ArrayList<FuzzyMatch>();
        
        int currMinOverlap = minOverlap.get(pattern.length());
        int currMaxNonOverlap = pattern.length() - currMinOverlap;
        
        BitVector vn = new BitVector(pattern.length());
        BitVector vp = new BitVector(pattern.length()).set(0, pattern.length());
        BitVector allSet = new BitVector(pattern.length()).set(0, pattern.length());
        BitVector prevM = null;
        BitVector prevD0 = null;

        int dist = pattern.length();
        
        List<FuzzyMatch> matches = new ArrayList<>();
        
        for(int i = 0; i < currMaxNonOverlap * 2 + text.length(); i++){
            BitVector m;
            if(i >= currMaxNonOverlap && i < currMaxNonOverlap + text.length())
                m = patternIdx.get(text.charAt(i - currMaxNonOverlap));
            else
                m = allSet;
            
            BitVector tr = null;
            if(allowTranspositions){
                tr = new BitVector(pattern.length());
                if(i != 0)
                    tr = tr.or(prevD0).not().and(m).leftShift().and(prevM);
            }
            BitVector d0 = new BitVector(pattern.length()).or(m).and(vp).add(vp).xor(vp).or(m).or(vn);
            if(allowTranspositions)
                d0 = d0.or(tr);

            BitVector hp = new BitVector(pattern.length()).or(d0).or(vp).not().or(vn);
            BitVector hn = new BitVector(pattern.length()).or(vp).and(d0);
            
            vp = new BitVector(pattern.length()).or(d0).orLShift(hp).not().orLShift(hn);
            vn = new BitVector(pattern.length()).or(d0).andLShift(hp);

            if(allowTranspositions){
                prevM = m;
                prevD0 = d0;
            }

            if(hp.get(pattern.length() - 1)){
                dist++;
            }else if(hn.get(pattern.length() - 1)){
                dist--;
            }
            
            int index = i - currMaxNonOverlap;
            int length = Math.min(index + 1, Math.min(pattern.length(), text.length() - (index + 1 - pattern.length())));
            int currPartialMaxEdits = maxEdits.get(length);
            
            if(dist <= currPartialMaxEdits && (!useMinOverlap || length >= currMinOverlap)){
                matches.add(new FuzzyMatch(index, useMinOverlap ? length : pattern.length(), dist));
            }
        }
        
        return matches;
    }
}
