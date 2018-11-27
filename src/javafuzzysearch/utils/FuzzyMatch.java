package javafuzzysearch.utils;

import java.util.List;

public class FuzzyMatch{
    private int index, overlap, length, score;
    private List<Edit> path;

    public FuzzyMatch(int index, int length, int overlap, int score){
        this.index = index;
        this.length = length;
        this.overlap = overlap;
        this.score = score;
    }

    public void setPath(List<Edit> path){
        this.path = path;
    }

    public int getIndex(){
        return index;
    }
    
    public int getLength(){
        return length;
    }

    public void setIndex(int index){
        this.index = index;
    }

    public int getOverlap(){
        return overlap;
    }

    public int getScore(){
        return score;
    }
    
    public List<Edit> getPath(){
        return path;
    }

    @Override
    public String toString(){
        return String.format("FuzzyMatch(index = %d, length = %d, overlap = %d, score = %d)", index, length, overlap, score);
    }
}
