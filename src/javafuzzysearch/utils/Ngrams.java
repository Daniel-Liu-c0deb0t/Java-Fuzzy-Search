package javafuzzysearch.utils;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.BitSet;

public class Ngrams{
    private Map<StrView, BitSet> ngrams;
    private List<StrView> strings;
    private int n;
    private int hashPow = 1;

    public Ngrams(List<StrView> strings, int n){
        this.ngrams = new HashMap<StrView, BitSet>();
        this.strings = strings;
        this.n = n;

        for(int i = 0; i < n - 1; i++)
            hashPow *= StrView.HASH_CONST;

        for(int i = 0; i < strings.size(); i++)
            add(strings.get(i), i);
    }

    public int getN(){
        return n;
    }

    public List<StrView> get(StrView s){
        List<StrView> res = new ArrayList<>();
        BitSet set = getSet(s);

        for(int i = set.nextSetBit(0); i >= 0; i = set.nextSetBit(i + 1))
            res.add(strings.get(i));

        return res;
    }

    public Set<Integer> getIdx(StrView s){
        Set<Integer> res = new HashSet<>();
        BitSet set = getSet(s);

        for(int i = set.nextSetBit(0); i >= 0; i = set.nextSetBit(i + 1))
            res.add(i);

        return res;
    }

    private BitSet getSet(StrView s){
        BitSet set = new BitSet();

        int hash = 0;

        for(int i = 0; i <= s.length() - n; i++){
            StrView ngram = s.substring(i, i + n);

            if(i == 0){
                hash = ngram.hashCode();
            }else{
                hash = (hash - s.charAt(i - 1) * hashPow) * StrView.HASH_CONST + s.charAt(i + n - 1);
                ngram.setHash(hash);
            }

            BitSet curr = ngrams.get(ngram);
            if(curr != null)
                set.or(curr);
        }

        return set;
    }

    private void add(StrView s, int idx){
        int hash = 0;

        for(int i = 0; i <= s.length() - n; i++){
            StrView ngram = s.substring(i, i + n);

            if(i == 0){
                hash = ngram.hashCode();
            }else{
                hash = (hash - s.charAt(i - 1) * hashPow) * StrView.HASH_CONST + s.charAt(i + n - 1);
                ngram.setHash(hash);
            }

            if(!ngrams.containsKey(ngram))
                ngrams.put(ngram, new BitSet());

            ngrams.get(ngram).set(idx);
        }
    }
}
