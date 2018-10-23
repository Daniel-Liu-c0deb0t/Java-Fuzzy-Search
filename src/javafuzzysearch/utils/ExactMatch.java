package javafuzzysearch.utils;

public class ExactMatch{
    private int index;

    public ExactMatch(int index){
        this.index = index;
    }

    public int getIndex(){
        return index;
    }

    @Override
    public String toString(){
        return String.format("ExactMatch(index = %d)", index);
    }
}