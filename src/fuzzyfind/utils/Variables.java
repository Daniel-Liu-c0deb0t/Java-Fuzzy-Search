package fuzzyfind.utils;

import java.util.Map;
import java.util.HashMap;

public class Variables{
    private Map<StrView, StrView> vars;
    private static Variables global = null;

    public Variables(){
        vars = new HashMap<StrView, StrView>();
    }

    public void add(StrView key, StrView val){
        vars.put(key, val);
    }

    public StrView get(StrView key){
        return vars.get(key);
    }

    public void clear(){
        vars.clear();
    }

    public static Variables getGlobal(){
        if(global == null)
            global = new Variables();

        return global;
    }
}
