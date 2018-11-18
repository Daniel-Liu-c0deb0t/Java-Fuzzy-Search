package javafuzzysearch.utils;

import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;

public class EditWeights{
    private static final int defaultSame = 0, defaultSub = 1, defaultIns = 1, defaultDel = 1, defaultTra = 1;

    private Map<Edit.Type, Map<Character, Integer>> mapSingle;
    private Map<Edit.Type, Map<Character, Map<Character, Integer>>> mapPair;

    public EditWeights(){
        mapSingle = new EnumMap<>(Edit.Type.class);

        mapSingle.put(Edit.Type.INS, new HashMap<Character, Integer>());
        mapSingle.get(Edit.Type.INS).put(null, defaultIns);

        mapSingle.put(Edit.Type.DEL, new HashMap<Character, Integer>());
        mapSingle.get(Edit.Type.DEL).put(null, defaultDel);

        mapSingle.put(Edit.Type.SAME, new HashMap<Character, Integer>());
        mapSingle.get(Edit.Type.SAME).put(null, defaultSame);

        mapPair = new EnumMap<>(Edit.Type.class);

        mapPair.put(Edit.Type.SUB, new HashMap<Character, Map<Character, Integer>>());
        mapPair.get(Edit.Type.SUB).put(null, new HashMap<Character, Integer>());
        mapPair.get(Edit.Type.SUB).get(null).put(null, defaultSub);

        mapPair.put(Edit.Type.TRA, new HashMap<Character, Map<Character, Integer>>());
        mapPair.get(Edit.Type.TRA).put(null, new HashMap<Character, Integer>());
        mapPair.get(Edit.Type.TRA).get(null).put(null, defaultTra);
    }

    public void setDefault(int same, int sub, int ins, int del, int tra){
        mapSingle.get(Edit.Type.INS).put(null, ins);
        mapSingle.get(Edit.Type.DEL).put(null, del);
        mapSingle.get(Edit.Type.SAME).put(null, same);

        mapPair.get(Edit.Type.SUB).get(null).put(null, sub);
        mapPair.get(Edit.Type.TRA).get(null).put(null, tra);
    }

    public void setDefault(int same, int sub, int ins, int del){
        setDefault(same, sub, ins, del, defaultTra);
    }

    public void set(char c, int same, int ins, int del){
        mapSingle.get(Edit.Type.INS).put(c, ins);
        mapSingle.get(Edit.Type.DEL).put(c, del);
        mapSingle.get(Edit.Type.SAME).put(c, same);
    }

    public void set(Character a, Character b, int sub, int tra){
        if(!mapPair.get(Edit.Type.SUB).containsKey(a))
            mapPair.get(Edit.Type.SUB).put(a, new HashMap<Character, Integer>());
        mapPair.get(Edit.Type.SUB).get(a).put(b, sub);

        if(!mapPair.get(Edit.Type.TRA).containsKey(a))
            mapPair.get(Edit.Type.TRA).put(a, new HashMap<Character, Integer>());
        mapPair.get(Edit.Type.TRA).get(a).put(b, tra);
    }

    public void set(Character a, Character b, int sub){
        set(a, b, sub, defaultTra);
    }

    public int get(char c, Edit.Type type){
        if(mapSingle.get(type).containsKey(c))
            return mapSingle.get(type).get(c);

        return mapSingle.get(type).get(null);
    }

    public int get(char a, char b, Edit.Type type){
        if(mapPair.get(type).containsKey(a)){
            if(mapPair.get(type).get(a).containsKey(b))
                return mapPair.get(type).get(a).get(b);
            else
                return mapPair.get(type).get(a).get(null);
        }else{
            if(mapPair.get(type).get(null).containsKey(b))
                return mapPair.get(type).get(null).get(b);
            else
                return mapPair.get(type).get(null).get(null);
        }
    }
}
