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
    
    public MyersSearcher(LengthParam maxEdits, LengthParam minOverlap){
        this.maxEdits = maxEdits;
        this.minOverlap = minOverlap;
    }
    
    public MyersSearcher(LengthParam maxEdits){
        this.maxEdits = maxEdits;
        this.minOverlap = new LengthParam(0, false, true);
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
        int dist = pattern.length();
        
        List<FuzzyMatch> matches = new ArrayList<>();
        
        for(int i = 0; i < currMaxNonOverlap * 2 + text.length(); i++){
            BitVector m;
            if(i >= currMaxNonOverlap && i < currMaxNonOverlap + text.length())
                m = patternIdx.get(text.charAt(i - currMaxNonOverlap));
            else
                m = allSet;
            
            BitVector d0 = new BitVector(pattern.length()).or(m).and(vp).add(vp).xor(vp).or(m).or(vn);
            BitVector hp = new BitVector(pattern.length()).or(d0).or(vp).not().or(vn);
            BitVector hn = new BitVector(pattern.length()).or(vp).and(d0);
            
            vp = new BitVector(pattern.length()).or(d0).orLShift(hp).not().orLShift(hn);
            vn = new BitVector(pattern.length()).or(d0).andLShift(hp);
            
            if(hp.get(pattern.length() - 1)){
                dist++;
            }else if(hn.get(pattern.length() - 1)){
                dist--;
            }
            
            int index = i - currMaxNonOverlap;
            int length = Math.min(index + 1, pattern.length());
            int currPartialMaxEdits = maxEdits.get(length);
            
            // note that using min overlap may produce inaccurate lengths and indexes!
            // to fix the problem, the algorithm must be able to count the number of insertions and deletions
            if(dist <= currPartialMaxEdits && (currMaxNonOverlap == 0 || length >= currMinOverlap)){
                matches.add(new FuzzyMatch(index, currMaxNonOverlap == 0 ? pattern.length() : length, dist));
            }
        }
        
        return matches;
    }
}
