package javafuzzysearch.searchers;

import javafuzzysearch.utils.FuzzyMatch;
import javafuzzysearch.utils.Utils;
import javafuzzysearch.utils.BitVector;
import javafuzzysearch.utils.LengthParam;
import javafuzzysearch.utils.StrView;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

/**
 * Implementation of Myer's fuzzy searching algorithm for Levenshtein distance.
 */
public class MyersSearcher{
    private LengthParam maxEdits = new LengthParam(0, false, false);
    private LengthParam minOverlap = new LengthParam(0, false, true);
    private boolean allowTranspositions = false;
    private Map<Character, Set<Character>> patternWildcard = new HashMap<>();
    private Map<Character, Set<Character>> textWildcard = new HashMap<>();

    public MyersSearcher maxEdits(LengthParam maxEdits){
        this.maxEdits = maxEdits;
        return this;
    }

    public MyersSearcher minOverlap(LengthParam minOverlap){
        this.minOverlap = minOverlap;
        return this;
    }

    public MyersSearcher allowTranspositions(){
        this.allowTranspositions = true;
        return this;
    }

    public MyersSearcher wildcardChars(Map<Character, Set<Character>> textWildcard, Map<Character, Set<Character>> patternWildcard){
        this.patternWildcard = patternWildcard;
        this.textWildcard = textWildcard;
        return this;
    }

    public Map<Boolean, Map<Character, BitVector>> preprocessPattern(StrView pattern, Set<Character> alphabet, Set<Integer> patternEscapeIdx){
        Map<Boolean, Map<Character, BitVector>> res = new HashMap<>();

        Map<Character, BitVector> normalMasks = new HashMap<>();
        Map<Character, BitVector> wildcardMasks = new HashMap<>();

        for(char c : alphabet){
            normalMasks.put(c, new BitVector(pattern.length()));
            if(textWildcard.containsKey(c))
                wildcardMasks.put(c, new BitVector(pattern.length()));
        }

        for(int i = 0; i < pattern.length(); i++){
            char c = pattern.charAt(i);
            normalMasks.get(c).set(i);

            if(patternWildcard.containsKey(c) && !patternEscapeIdx.contains(i)){
                Set<Character> set;

                if(patternWildcard.get(c) == null)
                    set = alphabet;
                else
                    set = patternWildcard.get(c);

                for(char d : set){
                    normalMasks.get(d).set(i);
                }
            }

            for(char wildcard : textWildcard.keySet()){
                Set<Character> set = textWildcard.get(wildcard);
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
        
        BitVector vn = new BitVector(pattern.length());
        BitVector vp = new BitVector(pattern.length()).set(0, pattern.length());
        BitVector allSet = new BitVector(pattern.length()).set(0, pattern.length());
        BitVector prevM = null;
        BitVector prevD0 = null;

        int dist = pattern.length();
        
        List<FuzzyMatch> matches = new ArrayList<>();
        
        for(int i = 0; i < currMaxNonOverlap + text.length(); i++){
            BitVector m;
            if(i < text.length()){
                m = patternMask.get(textWildcard.containsKey(text.charAt(i)) &&
                        !textEscapeIdx.contains(i)).get(text.charAt(i));
            }else{
                m = allSet;
            }
            
            BitVector tr = null;
            if(allowTranspositions){
                tr = new BitVector(pattern.length());
                if(i > 0)
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
            
            int nonOverlapLength = Math.max(i + 1 - text.length(), 0);
            int overlapLength = pattern.length() - nonOverlapLength;
            int currPartialMaxEdits = maxEdits.get(overlapLength);
            
            if(dist <= currPartialMaxEdits && overlapLength >= currMinOverlap){
                matches.add(new FuzzyMatch(i, pattern.length(), overlapLength, dist));
            }
        }
        
        return matches;
    }
}
