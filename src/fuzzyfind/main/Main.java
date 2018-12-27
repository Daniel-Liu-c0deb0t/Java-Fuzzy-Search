package fuzzyfind.main;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class Main{
    public static void main(String[] args){
        Map<String, List<String>> argMap = new HashMap<>();
        String currArg = null;
        argMap.put(null, new ArrayList<String>());

        for(int i = 0; i < args.length; i++){
            if(args[i].startsWith("-")){
                currArg = args[i];
                argMap.put(args[i], new ArrayList<String>());
            }else{
                argMap.get(currArg).add(args[i]);
            }
        }

        List<String> inputPaths = argMap.get(null);
        List<String> matchedOutputPaths = null;
        List<String> unmatchedOutputPaths = null;
        List<String> patternPaths = null;
        Character delimiter = '\n';
        boolean gzipInput = false;
        boolean gzipOutput = false;

        String s = "--pattern";

        if(argMap.containsKey(s))
            patternPaths = argMap.get(s);
        else
            throw new IllegalArgumentException("Must specify pattern file(s)!");

        s = "--matched";

        if(argMap.containsKey(s))
            matchedOutputPaths = argMap.get(s);
        else
            matchedOutputPaths = ParsingUtils.appendBeforePathNames(inputPaths, "matched_");

        s = "--unmatched";

        if(argMap.containsKey(s))
            unmatchedOutputPaths = argMap.get(s);

        s = "--delimiter";

        if(argMap.containsKey(s)){
            if(argMap.get(s).isEmpty())
                delimiter = null;
            else
                delimiter = ParsingUtils.parseChar(argMap.get(s).get(0));
        }

        s = "--in-gz";

        if(argMap.containsKey(s))
            gzipInput = true;

        s = "--out-gz";

        if(argMap.containsKey(s))
            gzipOutput = true;

        PatternMatcher patternMatcher = new PatternMatcher(patternPaths);
        patternMatcher.match(inputPaths, gzipInput, matchedOutputPaths, unmatchedOutputPaths, gzipOutput, delimiter);
    }
}
