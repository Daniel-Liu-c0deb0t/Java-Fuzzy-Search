package fuzzysplit.parameters;

import java.util.List;
import java.util.ArrayList;

import javafuzzysearch.utils.StrView;
import javafuzzysearch.utils.Utils;

import fuzzysplit.references.Reference;
import fuzzysplit.references.StrReference;

import fuzzysplit.utils.Variables;

public class IntParameter{
    private List<Reference> references;
    private int val;

    public IntParameter(List<Reference> references){
        if(references.size() == 1 && references.get(0) instanceof StrReference)
            this.val = Integer.parseInt(references.get(0).get(null).toString());
        else
            this.references = references;
    }

    public IntParameter(int val){
        this.val = val;
    }

    public int get(Variables vars){
        if(references == null){
            return val;
        }else{
            StringBuilder b = new StringBuilder();

            for(Reference r : references){
                StrView s = r.get(vars);

                for(int i = 0; i < s.length(); i++)
                    b.append(s.charAt(i));
            }

            return Integer.parseInt(b.toString());
        }
    }
}
