package javafuzzysearch.utils;

public class StrView implements Comparable{
    private char[] arr;
    private int start, length;
    private boolean reversed;

    public StrView(String s){
        this.arr = s.toCharArray();
        this.start = 0;
        this.length = arr.length;
        this.reversed = false;
    }

    public StrView(char[] arr, int start, int length, boolean reversed){
        this.arr = arr;
        this.start = start;
        this.length = length;
        this.reversed = reversed;
    }

    public char charAt(int i){
        return arr[reversed ? (start + length - 1 - i) : (start + i)];
    }

    public StrView substring(int i, int j){
        int s = reversed ? (start + length - 1 - j) : (start + i);
        return new StrView(arr, s, j - i, reversed);
    }

    public int length(){
        return length;
    }

    public boolean isEmpty(){
        return length == 0;
    }

    public StrView reverse(){
        return new StrView(arr, start, length, !reversed);
    }

    @Override
    public String toString(){
        char[] res = new char[length];

        for(int i = 0; i < length; i++)
            res[i] = charAt(i);

        return new String(res);
    }

    @Override
    public int compareTo(Object o){
        StrView s = (StrView)o;

        int min = Math.min(length, s.length());

        for(int i = 0; i < min; i++){
            int a = charAt(i);
            int b = s.charAt(i);

            if(a != b)
                return a - b;
        }

        if(length != s.length())
            return length - s.length();

        return 0;
    }

    @Override
    public boolean equals(Object o){
        if(o != null && o instanceof StrView){
            StrView s = (StrView)o;
            
            if(length != s.length())
                return false;

            for(int i = 0; i < length; i++){
                if(charAt(i) != s.charAt(i))
                    return false;
            }

            return true;
        }

        return false;
    }
}
