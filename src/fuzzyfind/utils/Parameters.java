package fuzzyfind.utils;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import javafuzzysearch.utils.StrView;

import javafuzzysearch.searchers.CutoffSearcher;

public class Parameters{
    private Map<String, Object> params;

    public Parameters(){
        params = new HashMap<String, Object>();
    }

    public void add(String key, Object val){
        params.put(key, val);
    }

    public StrView getStr(String key){
        return (StrView)params.get(key);
    }

    public int getInt(String key){
        return ((Integer)params.get(key)).intValue();
    }

    public float getFloat(String key){
        return ((Float)params.get(key)).floatValue();
    }

    public CutoffSearcher getSearcher(String key){
        return (CutoffSearcher)params.get(key);
    }

    @SuppressWarnings("unchecked")
    public List<Integer> getIntList(String key){
        return (List<Integer>)params.get(key);
    }

    @SuppressWarnings("unchecked")
    public List<StrView> getStrList(String key){
        return (List<StrView>)params.get(key);
    }
}
