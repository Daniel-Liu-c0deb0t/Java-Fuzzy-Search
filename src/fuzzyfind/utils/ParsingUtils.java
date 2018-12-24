package fuzzyfind.utils;

import javafuzzysearch.utils.LengthParam;
import javafuzzysearch.utils.StrView;

import fuzzyfind.references.Reference;
import fuzzyfind.references.StrReference;
import fuzzyfind.references.VarReference;

import java.util.List;
import java.util.ArrayList;
import java.util.StringBuilder;
import java.util.Set;
import java.util.HashSet;

public class ParsingUtils{
    public static LengthParam toLengthParam(float n){
        boolean percentage = false;
        boolean lengthMinus = false;

        if(Math.abs(n) < 1.0f)
            percentage = true;

        if(Float.floatToIntBits(n) < 0.0f)
            lengthMinus = true;

        return new LengthParam(n, percentage, lengthMinus);
    }

    public static StrView removeOuterQuotes(StrView s){
        s = s.subtring(1, s.length() - 1);

        StringBuilder b = new StringBuilder();
        boolean escaped = false;

        for(int i = 0; i < s.length(); i++){
            char c = s.charAt(i);

            if(escaped){
                if(c == '\\')
                    b.append('\\');
                else if(c == 'n')
                    b.append('\n');
                else if(c == 't')
                    b.append('\t');
                else if(c == 'r')
                    b.append('\r');
                else if(c == '"')
                    b.append('"');
                else
                    throw new IllegalArgumentException(c + " cannot be escaped!");

                escaped = false;
            }else{
                if(c == '\\')
                    escaped = true;
                else
                    b.append(c);
            }
        }

        return new StrView(b.toCharArray());
    }

    public static List<Reference> splitByVars(StrView s){
        List<Reference> res = new ArrayList<>();
        StringBuilder curr = new StringBuilder();
        boolean escaped = false;
        boolean inVar = false;

        for(int i = 0; i < s.length(); i++){
            char c = s.charAt(i);

            if(escaped){
                if(c == '%'){
                    curr.append(c);
                }else{
                    if(curr.length() > 0)
                        res.add(new StrReference(new StrView(curr.toCharArray())));

                    inVar = !inVar;
                    curr.clear();
                    curr.append(c);
                }

                escaped = false;
            }else{
                if(c == '%'){
                    if(inVar){
                        if(curr.length() > 0)
                            res.add(new VarReference(new StrView(curr.toCharArray())));

                        inVar = false;
                        curr.clear();
                    }else{
                        escaped = true;
                    }
                }else{
                    curr.append(c);
                }
            }
        }

        if(curr.length() > 0){
            res.add(new StrReference(new StrView(curr.toCharArray())));
        }

        return res;
    }

    public static Set<Character> parseCharRanges(StrView s){
        Set<Character> res = new HashSet<>();
        boolean escaped = false;
        char prev = ' ';

        for(int i = 0; i < s.length(); i++){
            char c = s.charAt(i);

            if(escaped){
                if(c == '-'){
                    res.add(c);
                    prev = c;
                }else{
                    for(int j = prev; j <= c; j++)
                        res.add((char)j);
                    prev = c;
                }

                escaped = false;
            }else{
                if(c == '-'){
                    escaped = true;
                }else{
                    res.add(c);
                    prev = c;
                }
            }
        }

        return res;
    }

    public static List<StrView> splitOutsideStr(StrView s, char... d){
        Set<Character> set = new HashSet<>();

        for(char c : d)
            set.add(c);

        List<StrView> res = new ArrayList<>();
        boolean inStr = false;
        boolean escaped = false;
        int prev = 0;

        for(int i = 0; i < s.length(); i++){
            char c = s.charAt(i);

            if(escaped){
                escaped = false;
            }else{
                if(c == '\\'){
                    escaped = true;
                }else if(c == '"'){
                    inStr = !inStr;
                }else if(!inStr && set.contains(c)){
                    res.add(s.substring(prev, i));
                    prev = i + 1;
                }
            }
        }

        if(prev < s.length())
            res.add(s.substring(prev));

        return res;
    }

    public static List<List<StrView>> splitKeyValuePairs(StrView s){
        List<StrView> lines = splitOutsideStr(s, ';', '\n');
        List<List<StrView>> res = new ArrayList<>();

        for(StrView line : lines){
            res.add(splitOutsideStr(line, ':'));
        }

        return res;
    }
}
