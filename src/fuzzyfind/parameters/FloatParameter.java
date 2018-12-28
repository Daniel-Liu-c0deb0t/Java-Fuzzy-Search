package fuzzyfind.parameters;

import java.util.List;
import java.util.ArrayList;

import javafuzzysearch.utils.StrView;
import javafuzzysearch.utils.Utils;

import fuzzyfind.references.Reference;
import fuzzyfind.references.StrReference;

import fuzzyfind.utils.Variables;

public class FloatParameter{
    private List<Reference> references;
    private float val;

    public FloatParameter(List<Reference> references){
        if(references.size() == 1 && references.get(0) instanceof StrReference)
            this.val = Float.parseFloat(references.get(0).get(null).toString());
        else
            this.references = references;
    }

    public FloatParameter(float val){
        this.val = val;
    }

    public float get(Variables vars){
        if(references == null){
            return val;
        }else{
            StringBuilder b = new StringBuilder();

            for(Reference r : references){
                StrView s = r.get(vars);

                for(int i = 0; i < s.length(); i++)
                    b.append(s.charAt(i));
            }

            return Float.parseFloat(b.toString());
        }
    }
}
