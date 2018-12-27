package fuzzyfind.main;

import fuzzyfind.patterns.WholePattern;
import fuzzyfind.patterns.Pattern;

import fuzzyfind.parameters.StrParameter;

import fuzzyfind.utils.ParsingUtils;
import fuzzyfind.utils.PatternMatch;

import javafuzzysearch.utils.StrView;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import java.nio.file.Files;
import java.nio.file.Paths;

import java.io.BufferedReader;
import java.io.BufferedWriter;

public class PatternMatcher{
    private WholePattern patterns;
    private List<List<List<Pattern>>> patternList;

    public PatternMatcher(List<String> paths){
        try{
            patternList = new ArrayList<>();
            List<List<Integer>> idxList = new ArrayList<>();

            for(String path : paths){
                List<List<Pattern>> currPatternList = new ArrayList<>();
                List<Integer> currIdxList = new ArrayList<>();
                BufferedReader r = Files.newBufferedReader(Paths.get(path));

                String line;

                while((line = r.readLine()) != null){
                    line = line.trim();

                    if(line.isEmpty() || line.startsWith("#"))
                        continue;

                    int startIdx = line.indexOf('{');

                    if(startIdx == -1)
                        startIdx = line.length();

                    Integer idx = startIdx == 0 ? null : Integer.parseInt(line.substring(0, startIdx).trim());
                    line = line.substring(startIdx);

                    currPatternList.add(ParsingUtils.parsePatterns(ParsingUtils.removeWhitespace(new StrView(line))));
                    currIdxList.add(idx);
                }

                patternList.add(currPatternList);
                idxList.add(currIdxList);

                r.close();
            }

            patterns = new WholePattern(patternList, idxList);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void match(List<String> inputPaths, boolean gzipInput, List<String> matchedOutputPaths, List<String> unmatchedOutputPaths, boolean gzipOutput, Character delimiter){
        try{
            List<BufferedReader> inputReaders = new ArrayList<>();
            List<StrParameter> matchedOutputParams = new ArrayList<>();
            List<BufferedWriter> unmatchedWriters = new ArrayList<>();
            Map<StrView, BufferedWriter> cachedWriters = new HashMap<>();

            for(int i = 0; i < inputPaths.size(); i++){
                BufferedReader inputReader = ParsingUtils.getReader(inputPaths.get(i), gzipInput);
                StrParameter matchedOutputParam = new StrParameter(ParsingUtils.splitByVars(new StrView(matchedOutputPaths.get(i))));

                inputReaders.add(inputReader);
                matchedOutputParams.add(matchedOutputParam);

                if(unmatchedOutputPaths != null){
                    BufferedWriter unmatchedWriter = ParsingUtils.getWriter(unmatchedOutputPaths.get(i), gzipOutput);
                    unmatchedWriters.add(unmatchedWriter);
                }
            }

            while_loop:
            while(true){
                List<List<StrView>> texts = new ArrayList<>();

                for(int i = 0; i < inputReaders.size(); i++){
                    List<StrView> currTexts = ParsingUtils.read(inputReaders.get(i), delimiter, patterns.getLength(i));

                    if(currTexts == null)
                        break while_loop;

                    texts.add(currTexts);
                }

                List<List<List<PatternMatch>>> matches = patterns.search(texts);

                if(matches == null){
                    for(int i = 0; i < unmatchedWriters.size(); i++){
                        BufferedWriter w = unmatchedWriters.get(i);
                        List<StrView> currTexts = texts.get(i);

                        for(StrView text : currTexts){
                            w.write(text.toString());

                            if(delimiter != null)
                                w.write(delimiter);
                        }
                    }

                    continue;
                }

                for(int i = 0; i < matches.size(); i++){
                    StrView path = matchedOutputParams.get(i).get();
                    BufferedWriter w = null;

                    if(cachedWriters.containsKey(path)){
                        w = cachedWriters.get(path);
                    }else{
                        w = ParsingUtils.getWriter(path.toString(), gzipOutput);
                        cachedWriters.put(path, w);
                    }

                    List<StrView> currTexts = texts.get(i);
                    List<List<PatternMatch>> currMatches = matches.get(i);
                    List<List<Pattern>> currPatterns = patternList.get(i);

                    for(int j = 0; j < currTexts.size(); j++){
                        StringBuilder b = trimText(currTexts.get(j), currMatches.get(j), currPatterns.get(j));
                        w.append(b);

                        if(delimiter != null)
                            w.write(delimiter);
                    }
                }
            }

            for(BufferedReader r : inputReaders){
                if(r != null)
                    r.close();
            }

            for(BufferedWriter w : cachedWriters.values()){
                if(w != null)
                    w.close();
            }

            for(BufferedWriter w : unmatchedWriters){
                if(w != null)
                    w.close();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private StringBuilder trimText(StrView text, List<PatternMatch> matches, List<Pattern> currPatterns){
        Map<Integer, Boolean> intervals = new HashMap<>();

        for(int i = 0; i < matches.size(); i++){
            PatternMatch m = matches.get(i);

            if(m.getLength() <= 0 || !currPatterns.get(i).shouldTrim())
                continue;

            intervals.put(m.getIndex() - m.getLength() + 1, true);
            intervals.put(m.getIndex(), false);
        }

        StringBuilder res = new StringBuilder();
        int count = 0;

        for(int i = 0; i < text.length(); i++){
            Boolean b = intervals.get(i);

            if(b != null && b == true)
                count++;

            if(count == 0)
                res.append(text.charAt(i));

            if(b != null && b == false)
                count--;
        }

        return res;
    }
}
