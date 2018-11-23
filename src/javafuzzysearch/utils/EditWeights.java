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
        mapSingle.get(Edit.Type.INS).put(null, Edit.Type.INS.defaultWeight);

        mapSingle.put(Edit.Type.DEL, new HashMap<Character, Integer>());
        mapSingle.get(Edit.Type.DEL).put(null, Edit.Type.DEL.defaultWeight);

        mapSingle.put(Edit.Type.SAME, new HashMap<Character, Integer>());
        mapSingle.get(Edit.Type.SAME).put(null, Edit.Type.SAME.defaultWeight);

        mapPair = new EnumMap<>(Edit.Type.class);

        mapPair.put(Edit.Type.SUB, new HashMap<Character, Map<Character, Integer>>());
        mapPair.get(Edit.Type.SUB).put(null, new HashMap<Character, Integer>());
        mapPair.get(Edit.Type.SUB).get(null).put(null, Edit.Type.SUB.defaultWeight);

        mapPair.put(Edit.Type.TRA, new HashMap<Character, Map<Character, Integer>>());
        mapPair.get(Edit.Type.TRA).put(null, new HashMap<Character, Integer>());
        mapPair.get(Edit.Type.TRA).get(null).put(null, Edit.Type.TRA.defaultWeight);
    }

    public EditWeights setDefault(int same, int sub, int ins, int del, int tra){
        mapSingle.get(Edit.Type.INS).put(null, ins);
        mapSingle.get(Edit.Type.DEL).put(null, del);
        mapSingle.get(Edit.Type.SAME).put(null, same);

        mapPair.get(Edit.Type.SUB).get(null).put(null, sub);
        mapPair.get(Edit.Type.TRA).get(null).put(null, tra);

        return this;
    }

    public EditWeights setDefault(int same, int sub, int ins, int del){
        setDefault(same, sub, ins, del, Edit.Type.TRA.defaultWeight);

        return this;
    }

    public EditWeights set(char c, int same, int ins, int del){
        mapSingle.get(Edit.Type.INS).put(c, ins);
        mapSingle.get(Edit.Type.DEL).put(c, del);
        mapSingle.get(Edit.Type.SAME).put(c, same);

        return this;
    }

    public EditWeights set(Character a, Character b, int sub, int tra){
        if(!mapPair.get(Edit.Type.SUB).containsKey(a))
            mapPair.get(Edit.Type.SUB).put(a, new HashMap<Character, Integer>());
        mapPair.get(Edit.Type.SUB).get(a).put(b, sub);

        if(!mapPair.get(Edit.Type.TRA).containsKey(a))
            mapPair.get(Edit.Type.TRA).put(a, new HashMap<Character, Integer>());
        mapPair.get(Edit.Type.TRA).get(a).put(b, tra);

        return this;
    }

    public EditWeights set(Character a, Character b, int sub){
        set(a, b, sub, Edit.Type.TRA.defaultWeight);

        return this;
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

    public boolean isDiagonalMonotonic(){
        boolean[] signs = new boolean[3];

        for(Edit.Type key : mapSingle.keySet()){
            Map<Character, Integer> map = mapSingle.get(key);

            for(Character c : map.keySet())
                signs[Integer.signum(map.get(c)) + 1] = true;
        }

        for(Edit.Type key : mapPair.keySet()){
            Map<Character, Map<Character, Integer>> map = mapPair.get(key);

            for(Character a : map.keySet()){
                Map<Character, Integer> m = map.get(a);

                for(Character b : m.keySet())
                    signs[Integer.signum(m.get(b)) + 1] = true;
            }
        }

        return !signs[0] || !signs[2];
    }
}
