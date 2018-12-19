package fuzzyfind.parameters;

import java.util.List;
import java.util.ArrayList;
import java.util.StringBuilder;

import javafuzzysearch.utils.StrView;
import javafuzzysearch.utils.Utils;

public class FloatParameter{
    private List<Reference> references;
    private float val;

    public FloatParameter(List<Reference> references){
        if(references.size() == 1 && references.get(0) instanceof StrReference)
            this.val = Float.parseFloat(referenes.get(0).get().toString());
        else
            this.references = references;
    }

    public FloatParameter(float val){
        this.val = val;
    }

    public float get(){
        if(references == null){
            return val;
        }else{
            StringBuilder b = new StringBuilder();

            for(Reference r : references)
                b.append(r.get().toString());

            return Float.parseFloat(b.toString());
        }
    }
}
