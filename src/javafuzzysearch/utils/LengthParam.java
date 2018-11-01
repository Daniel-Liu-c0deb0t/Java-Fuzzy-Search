package javafuzzysearch.utils;

public class LengthParam{
    private double value;
    private boolean percentage;
    private boolean lengthMinus;
    
    public LengthParam(double value, boolean percentage, boolean lengthMinus){
        this.value = value;
        this.percentage = percentage;
        this.lengthMinus = lengthMinus;
    }
    
    public int get(int length){
        int res;
        
        if(percentage)
            res = (int)(length * value);
        else
            res = (int)value;
        
        res = Math.min(res, length);
        
        if(lengthMinus)
            return length - res;
        else
            return res;
    }
}
