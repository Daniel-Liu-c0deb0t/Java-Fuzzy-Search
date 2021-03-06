package fuzzysplit.references;

import javafuzzysearch.utils.StrView;

import fuzzysplit.utils.Variables;

public class VarReference implements Reference{
    private StrView name;

    public VarReference(StrView name){
        this.name = name;
    }

    @Override
    public StrView get(Variables vars){
        return vars.get(name);
    }
}
