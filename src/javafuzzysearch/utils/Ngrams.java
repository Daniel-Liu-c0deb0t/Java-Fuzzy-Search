import javafuzzysearch.utils;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

public class Ngrams{
    private Map<StrView, Set<Integer>> ngrams;
    private List<StrView> strings;
    private int n;

    public Ngrams(List<StrView> strings, int n){
        this.ngrams = new HashMap<StrView, Set<Integer>>();
        this.strings = strings;
        this.n = n;
        
        for(int i = 0; i < strings.size(); i++)
            add(strings.get(i), i);
    }

    public List<StrView> get(StrView s){
        List<StrView> res = new ArrayList<>();
        Set<Integer> idx = getIdx(s);

        for(int i : idx)
            res.add(strings.get(i));

        return res;
    }

    private Set<Integer> getIdx(StrView s){
        Set<Integer> idx = new HashSet<>();

        for(int i = 0; i <= s.length() - n; i++){
            StrView ngram = s.substring(i, i + n);
            idx.addAll(ngrams.get(ngram));
        }

        return idx;
    }

    private void add(StrView s, int idx){
        for(int i = 0; i <= s.length() - n; i++){
            StrView ngram = s.substring(i, i + n);

            if(!ngrams.containsKey(ngram))
                ngrams.put(ngram, new HashSet<Integer>());

            ngrams.get(ngram).add(idx);
        }
    }
}
