package javafuzzysearch.utils;

public class FuzzyMatch<T>{
    private int index, length;
    private T value;

    public FuzzyMatch(int index, int length, T value){
        this.index = index;
        this.length = length;
        this.value = value;
    }

    public int getIndex(){
        return index;
    }
    
    public int getLength(){
        return length;
    }

    public T getValue(){
        return value;
    }
    
    @Override
    public String toString(){
        return String.format("FuzzyMatch(index = %d, length = %d, value = %s)", index, length, value);
    }
}
