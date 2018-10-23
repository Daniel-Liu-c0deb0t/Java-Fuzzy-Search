package javafuzzysearch.utils;

public class FuzzyMatch<T>{
    private int index;
    private T value;

    public FuzzyMatch(int index, T value){
        this.index = index;
        this.value = value;
    }

    public FuzzyMatch(int index){
        this.index = index;
    }

    public int getIndex(){
        return index;
    }

    public T getValue(){
        return value;
    }
}