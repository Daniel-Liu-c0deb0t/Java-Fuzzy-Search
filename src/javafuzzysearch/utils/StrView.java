package javafuzzysearch.utils;

public class StrView implements Comparable<StrView>{
    private char[] arr;
    private int start, length;
    private boolean reversed;
    private int hash;

    public StrView(String s){
        this.arr = s.toCharArray();
        this.start = 0;
        this.length = arr.length;
        this.reversed = false;
    }

    public StrView(StringBuilder b){
        this.arr = new char[b.length()];
        b.getChars(0, b.length(), this.arr, 0);
        this.start = 0;
        this.length = arr.length;
        this.reversed = false;
    }

    public StrView(char[] arr){
        this.arr = arr;
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
        int s = reversed ? (start + length - j) : (start + i);
        return new StrView(arr, s, j - i, reversed);
    }

    public StrView substring(int i){
        return substring(i, length);
    }

    public int indexOf(char c){
        for(int i = 0; i < length; i++){
            if(charAt(i) == c)
                return i;
        }

        return -1;
    }

    public int lastIndexOf(char c){
        for(int i = length - 1; i >= 0; i--){
            if(charAt(i) == c)
                return i;
        }

        return -1;
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
    public int compareTo(StrView s){
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

    @Override
    public int hashCode(){
        if(length == 0)
            return 0;

        if(hash == 0){
            for(int i = 0; i < length; i++){
                hash = hash * 31 + charAt(i);
            }
        }

        return hash;
    }
}
