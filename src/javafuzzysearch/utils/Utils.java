package javafuzzysearch.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;

public class Utils{
    public static String repeatChar(char c, int n){
        char[] res = new char[n];
        Arrays.fill(res, c);
        return new String(res);
    }
}
