package fuzzyfind.references;

import javafuzzysearch.utils.StrView;

import fuzzyfind.utils.Variables;

public class StrReference implements Reference{
    private StrView val;

    public StrReference(StrView val){
        this.val = val;
    }

    @Override
    public StrView get(Variables vars){
        return val;
    }
}
