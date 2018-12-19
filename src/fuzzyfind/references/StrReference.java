package fuzzyfind.references;

import javafuzzysearch.utils.StrView;

public class StrReference implements Reference{
    private StrView val;

    public StrReference(StrView val){
        this.val = val;
    }

    @Override
    public StrView get(){
        return val;
    }
}
