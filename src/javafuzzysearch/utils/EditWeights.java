package javafuzzysearch.utils;

import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;

public class EditWeights{
    private Map<Character, Weights> map;

    public EditWeights(){
        map = new HashMap<>();
        map.put(null, new Weights(1, 1, 1, 1));
    }

    public void setDefault(int sub, int ins, int del, int tra){
        map.put(null, new Weights(sub, ins, del, tra));
    }

    public void setDefault(int sub, int ins, int del){
        map.put(null, new Weights(sub, ins, del, 1));
    }

    public void set(char c, int sub, int ins, int del, int tra){
        map.put(c, new Weights(sub, ins, del, tra));
    }

    public void set(char c, int sub, int ins, int del){
        map.put(c, new Weights(sub, ins, del, null));
    }

    public void set(char c, Edit.Type type, int val){
        if(!map.containsKey(c))
            map.put(c, new Weights(null, null, null, null));

        map.get(c).set(type, val);
    }

    public int get(char c, Edit.Type type){
        if(!map.containsKey(c))
            return map.get(null).get(type);

        Weights w = map.get(c);

        if(w.get(type) == null)
            return map.get(null).get(type);

        return w.get(type);
    }

    private static class Weights{
        Map<Edit.Type, Integer> weights;

        public Weights(Integer sub, Integer ins, Integer del, Integer tra){
            weights = new EnumMap<>(Edit.Type.class);
            weights.put(Edit.Type.SUB, sub);
            weights.put(Edit.Type.INS, ins);
            weights.put(Edit.Type.DEL, del);
            weights.put(Edit.Type.TRA, tra);
        }

        public void set(Edit.Type type, Integer val){
            weights.put(type, val);
        }

        public Integer get(Edit.Type type){
            return weights.get(type);
        }
    }
}
