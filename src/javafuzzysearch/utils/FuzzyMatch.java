package javafuzzysearch.utils;

import java.util.List;

public class FuzzyMatch{
    private int index, length;
    private int edits;
    private List<Edit> path;

    public FuzzyMatch(int index, int length, int edits){
        this.index = index;
        this.length = length;
        this.edits = edits;
    }

    public void withPath(List<Edit> path){
        this.path = path;
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
    
    public List<Edit> getPath(){
        return path;
    }

    @Override
    public String toString(){
        return String.format("FuzzyMatch(index = %d, length = %d, edits = %d)", index, length, edits);
    }

    public static enum Edit{
        SUB(-1, -1), INS(-1, 0), DEL(0, -1), TRA(-2, -2), SAME(-1, -1);

        public int x, y;

        Edit(int x, int y){
            this.x = x;
            this.y = y;
        }
    }
}
