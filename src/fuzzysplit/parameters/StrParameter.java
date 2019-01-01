package fuzzysplit.parameters;

import java.util.List;
import java.util.ArrayList;

import javafuzzysearch.utils.StrView;
import javafuzzysearch.utils.Utils;

import fuzzysplit.references.Reference;
import fuzzysplit.references.StrReference;

import fuzzysplit.utils.Variables;

public class StrParameter{
    private List<Reference> references;
    private StrView str;

    public StrParameter(List<Reference> references){
        if(references.size() == 1 && references.get(0) instanceof StrReference)
            this.str = references.get(0).get(null);
        else
            this.references = references;
    }

    public StrParameter(StrView str){
        this.str = str;
    }

    public StrView get(Variables vars){
        if(str == null){
            List<StrView> list = new ArrayList<>();

            for(Reference r : references)
                list.add(r.get(vars));

            return Utils.concatenate(list);
        }else{
            return str;
        }
    }
}
