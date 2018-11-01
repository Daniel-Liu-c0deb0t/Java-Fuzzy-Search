package javafuzzysearch.utils;

public class FuzzyMatch{
    private int index, length;
    private int edits;

    public FuzzyMatch(int index, int length, int edits){
        this.index = index;
        this.length = length;
        this.edits = edits;
    }

    public int getIndex(){
        return index;
    }
    
    public int getLength(){
        return length;
    }

    public int getEdits(){
        return edits;
    }
    
    @Override
    public String toString(){
        return String.format("FuzzyMatch(index = %d, length = %d, edits = %d)", index, length, edits);
    }
}
