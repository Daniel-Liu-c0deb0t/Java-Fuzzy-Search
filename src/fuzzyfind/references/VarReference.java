package fuzzyfind.references;

import javafuzzysearch.utils.StrView;

import fuzzyfind.utils.Variables;

public class VarReference implements Reference{
    private StrView name;

    public VarReference(StrView name){
        this.name = name;
    }

    @Override
    public StrView get(){
        return Variables.getGlobal().get(name);
    }
}
