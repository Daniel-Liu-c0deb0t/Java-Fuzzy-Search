package javafuzzysearch.utils;

import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;

public class EditWeights{
    private Map<Edit.Type, Map<Character, Integer>> mapSingle;
    private Map<Edit.Type, Map<Character, Map<Character, Integer>>> mapPair;

    public EditWeights(){
        mapSingle = new EnumMap<>(Edit.Type.class);

        mapSingle.put(Edit.Type.INS, new HashMap<Character, Integer>());
        mapSingle.get(Edit.Type.INS).put(null, 1);

        mapSingle.put(Edit.Type.DEL, new HashMap<Character, Integer>());
        mapSingle.get(Edit.Type.DEL).put(null, 1);

        mapSingle.put(Edit.Type.SAME, new HashMap<Character, Integer>());
        mapSingle.get(Edit.Type.SAME).put(null, 0);

        mapPair = new EnumMap<>(Edit.Type.class);

        mapPair.put(Edit.Type.SUB, new HashMap<Character, HashMap<Character, Integer>>());
        mapPair.get(Edit.Type.SUB).put(null, new HashMap<Character, Integer>());
        mapPair.get(Edit.Type.SUB).get(null).put(null, 1);

        mapPair.put(Edit.Type.TRA, new HashMap<Character, Integer>());
        mapPair.get(Edit.Type.TRA).put(null, new HashMap<Character, Integer>());
        mapPair.get(Edit.Type.TRA).get(null).put(null, 1);
    }

    public void setDefault(int same, int sub, int ins, int del, int tra){
        mapSingle.get(Edit.Type.INS).put(null, ins);
        mapSingle.get(Edit.Type.DEL).put(null, del);
        mapSingle.get(Edit.Type.SAME).put(null, same);

        mapPair.get(Edit.Type.SUB).get(null).put(null, sub);
        mapPair.get(Edit.Type.TRA).get(null).put(null, tra);
    }

    public void setDefault(int same, int sub, int ins, int del){
        setDefault(same, sub, ins, del, 1);
    }

    public void set(char c, int same, int sub, int ins, int del, int tra){
        map.put(c, new Weights(same, sub, ins, del, tra));
    }

    public void set(char c, int same, int sub, int ins, int del){
        map.put(c, new Weights(same, sub, ins, del, null));
    }

    public void set(char c, Edit.Type type, int val){
        if(!map.containsKey(c))
            map.put(c, new Weights(null, null, null, null, null));

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
}
