package fuzzyfind.utils;

import java.util.List;

import javafuzzysearch.utils.Edit;
import javafuzzysearch.utils.FuzzyMatch;

public class PatternMatch{
    private int index, overlap, length, score, patternIdx;
    private List<Edit> path;

    public PatternMatch(int index, int length, int overlap, int score, int patternIdx){
        this.index = index;
        this.length = length;
        this.overlap = overlap;
        this.score = score;
        this.patternIdx = patternIdx;
    }

    public PatternMatch(int index, int length, int overlap, int score){
        this.index = index;
        this.length = length;
        this.overlap = overlap;
        this.score = score;
    }

    public PatternMatch(FuzzyMatch m, int patternIdx){
        this.index = m.getIndex();
        this.length = m.getLength();
        this.overlap = m.getOverlap();
        this.score = m.getScore();
        this.patternIdx = patternIdx;
    }

    public void setPath(List<Edit> path){
        this.path = path;
    }

    public int getPatternIdx(){
        return patternIdx;
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

    public void setLength(int length){
        this.length = length;
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
        return String.format("PatternMatch(index = %d, length = %d, score = %d)", index, length, score);
    }
}
