package javafuzzysearch.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Utils{
    public static String repeatChar(char c, int n){
        char[] res = new char[n];
        Arrays.fill(res, c);
        return new String(res);
    }
    
    public static Set<Character> uniqueChars(String... strings){
        Set<Character> res = new HashSet<>();
        
        for(String s : strings){
            for(int i = 0; i < s.length(); i++){
                res.add(s.charAt(i));
            }
        }
        
        return res;
    }

    public static int addInt(int a, int b){
        long c = (long)a + (long)b;
        return (int)Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, c));
    }

    public static int mulInt(int a, int b){
        long c = (long)a * (long)b;
        return (int)Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, c));
    }
}
