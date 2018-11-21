package javafuzzysearch.utils;

import java.util.ArrayList;
import java.util.List;

public class Array2D<T>{
    private List<List<T>> arr;
    private boolean isEmpty;

    public Array2D(int length){
        this.arr = new ArrayList<List<T>>(length);

        for(int i = 0; i < length; i++){
            this.arr.add(new ArrayList<T>());
        }

        this.isEmpty = true;
    }

    public T get(int i, int j){
        return arr.get(i).get(j);
    }

    public void set(int i, int j, T val){
        if(isEmpty)
            isEmpty = false;

        List<T> a = arr.get(i);

        if(j > a.size())
            throw new IllegalArgumentException("Index out of bounds when modifying an element!");

        if(j == a.size())
            a.add(val);
        else
            a.set(j, val);
    }

    public int getLength(int i){
        return arr.get(i).size();
    }

    public boolean isEmpty(){
        return isEmpty;
    }
}
