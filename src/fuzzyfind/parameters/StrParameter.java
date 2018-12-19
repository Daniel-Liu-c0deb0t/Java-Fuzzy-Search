package fuzzyfind.parameters;

import java.util.List;
import java.util.ArrayList;

import javafuzzysearch.utils.StrView;
import javafuzzysearch.utils.Utils;

public class StrParameter{
    private List<Reference> references;
    private StrView str;

    public StrParameter(List<Reference> references){
        if(references.size() == 1 && references.get(0) instanceof StrReference)
            this.str = references.get(0).get();
        else
            this.references = references;
    }

    public StrView get(){
        if(str == null){
            List<StrView> list = new ArrayList<>();

            for(Reference r : references)
                list.add(r.get());

            return Utils.concatenate(list);
        }else{
            return str;
        }
    }
}
