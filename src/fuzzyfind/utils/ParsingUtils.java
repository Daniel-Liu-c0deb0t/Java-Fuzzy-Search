package fuzzyfind.utils;

import javafuzzysearch.utils.LengthParam;
import javafuzzysearch.utils.StrView;
import javafuzzysearch.utils.Utils;

import fuzzyfind.references.Reference;
import fuzzyfind.references.StrReference;
import fuzzyfind.references.VarReference;

import fuzzyfind.patterns.Pattern;
import fuzzyfind.patterns.FuzzyPattern;
import fuzzyfind.patterns.RepeatingIntervalPattern;
import fuzzyfind.patterns.RepeatingFixedPattern;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

import java.nio.file.Paths;
import java.nio.file.Files;

import java.io.BufferedReader;

public class ParsingUtils{
    public static LengthParam toLengthParam(float n){
        boolean percentage = false;
        boolean lengthMinus = false;

        if(Math.abs(n) < 1.0f)
            percentage = true;

        if(Float.floatToIntBits(n) < 0)
            lengthMinus = true;

        return new LengthParam(n, percentage, lengthMinus);
    }

    public static StrView[] resolveStrWithSelector(StrView s){
        if(s.charAt(s.length() - 1) == ']'){
            int idx = s.lastIndexOf('[');
            return new StrView[]{resolveStr(s.substring(0, idx)), s.substring(idx + 1, s.length() - 1)};
        }else{
            return new StrView[]{resolveStr(s), null};
        }
    }

    public static StrView resolveStr(StrView s){
        if(s.charAt(0) == 'f'){
            String path = removeOuterQuotes(s.substring(1)).toString();
            String data = null;

            try{
                data = new String(Files.readAllBytes(Paths.get(path)));
            }catch(Exception e){
                e.printStackTrace();
                System.exit(1);
            }

            return new StrView(data);
        }else{
            return removeOuterQuotes(s);
        }
    }

    public static StrView removeOuterQuotes(StrView s){
        s = s.substring(1, s.length() - 1);

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

        return new StrView(b);
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
                        res.add(new StrReference(new StrView(curr)));

                    inVar = !inVar;
                    curr.setLength(0);
                    curr.append(c);
                }

                escaped = false;
            }else{
                if(c == '%'){
                    if(inVar){
                        if(curr.length() > 0)
                            res.add(new VarReference(new StrView(curr)));

                        inVar = false;
                        curr.setLength(0);
                    }else{
                        escaped = true;
                    }
                }else{
                    curr.append(c);
                }
            }
        }

        if(curr.length() > 0){
            res.add(new StrReference(new StrView(curr)));
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

    public static List<StrView> splitOutsideStr(StrView s, boolean removeEmpty, char... d){
        Set<Character> set = new HashSet<>();

        for(char c : d)
            set.add(c);

        List<StrView> res = new ArrayList<>();
        boolean inStr = false;
        boolean inBrackets = false;
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
                }else if(!inStr && ((!inBrackets && c == '[') || (inBrackets && c == ']'))){
                    inBrackets = !inBrackets;
                }else if(!inStr && !inBrackets && set.contains(c)){
                    if(!removeEmpty || prev < i)
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
        List<StrView> lines = splitOutsideStr(s, true, ';', '\n');
        List<List<StrView>> res = new ArrayList<>();

        for(StrView line : lines){
            res.add(splitOutsideStr(line, false, ':'));
        }

        return res;
    }

    public static StrView removeWhitespace(StrView s){
        return Utils.concatenate(splitOutsideStr(s, true, ' ', '\t'));
    }

    public static int nextIdx(StrView s, int idx, char nextChar){
        boolean inStr = false;
        boolean inBrackets = false;
        boolean escaped = false;

        for(int i = idx; i < s.length(); i++){
            char c = s.charAt(i);

            if(escaped){
                escaped = false;
            }else{
                if(c == '\\'){
                    escaped = true;
                }else if(c == '"'){
                    inStr = !inStr;
                }else if(!inStr && ((!inBrackets && c == '[') || (inBrackets && c == ']'))){
                    inBrackets = !inBrackets;
                }else if(!inStr && !inBrackets && c == nextChar){
                    return i;
                }
            }
        }

        return -1;
    }

    public static List<Pattern> parsePatterns(StrView s){
        List<Pattern> res = new ArrayList<>();
        int startIdx = 1;

        while(startIdx < s.length()){
            int endIdx = nextIdx(s, startIdx, '}');

            if(endIdx == -1)
                throw new IllegalArgumentException("Ending '}' not found!");

            char type = s.charAt(startIdx);
            Map<StrView, StrView> paramMap = new HashMap<>();

            List<StrView> params = splitOutsideStr(s.substring(startIdx + 1, endIdx), false, ',');

            for(StrView param : params){
                int idx = param.indexOf('=');

                if(idx == -1)
                    paramMap.put(param, null);
                else
                    paramMap.put(param.substring(0, idx), param.substring(idx + 1));
            }

            if(type == 'f'){
                res.add(new FuzzyPattern(paramMap));
            }else if(type == 'r'){
                res.add(new RepeatingFixedPattern(paramMap));
            }else if(type == 'i'){
                res.add(new RepeatingIntervalPattern(paramMap));
            }else{
                throw new IllegalArgumentException(type + " is not a valid pattern type!");
            }

            startIdx = endIdx + 2;
        }

        return res;
    }

    public static List<StrView> read(BufferedReader r, Character d, int n) throws Exception{
        List<StrView> lines = new ArrayList<>();

        StringBuilder b = new StringBuilder();
        int i;

        while((i = r.read()) != -1){
            char c = (char)i;

            if(d != null && c == d){
                lines.add(new StrView(b));
                b.setLength(0);

                if(lines.size() >= n)
                    return lines;
            }else{
                b.append(c);
            }
        }

        if(b.length() > 0)
            lines.add(new StrView(b));

        if(lines.size() < n)
            return null;
        else
            return lines;
    }
}
