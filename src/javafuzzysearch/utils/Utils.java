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

    public static boolean equalsWildcard(String a, int aIdx, Set<Integer> aEscapeIdx, String b, int bIdx, Set<Integer> bEscapeIdx, Map<Character, Set<Character>> wildcardChars){
        char c = a.charAt(aIdx);
        char d = b.charAt(bIdx);

        return c == d ||
            (!aEscapeIdx.contains(aIdx) && wildcardChars.containsKey(c) && (wildcardChars.get(c) == null || wildcardChars.get(c).contains(d))) ||
            (!bEscapeIdx.contains(bIdx) && wildcardChars.containsKey(d) && (wildcardChars.get(d) == null || wildcardChars.get(d).contains(c)));
    }

    public static boolean equalsWildcard(String s, Set<Integer> escapeIdx, int aIdx, int bIdx, Map<Character, Set<Character>> wildcardChars){
        char a = s.charAt(aIdx);
        char b = s.charAt(bIdx);

        return a == b ||
            (!escapeIdx.contains(aIdx) && wildcardChars.containsKey(a) && (wildcardChars.get(a) == null || wildcardChars.get(a).contains(b))) ||
            (!escapeIdx.contains(bIdx) && wildcardChars.containsKey(b) && (wildcardChars.get(b) == null || wildcardChars.get(b).contains(a)));
    }
}
