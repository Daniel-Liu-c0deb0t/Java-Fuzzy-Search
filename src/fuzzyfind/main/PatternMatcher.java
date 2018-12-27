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
        patternList = new ArrayList<>();
        List<List<Integer>> idxList = new ArrayList<>();

        for(String path : paths){
            List<List<Pattern>> currPatternList = new ArrayList<>();
            List<Integer> currIdxList = new ArrayList<>();
            BufferedReader r = null;

            try{
                r = Files.newBufferedReader(Paths.get(path));
            }catch(Exception e){
                e.printStackTrace();

                if(r != null)
                    r.close();

                System.exit(1);
            }

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
    }

    public void match(List<String> inputPaths, List<String> outputPaths, char delimiter){
        List<BufferedReader> inputReaders = new ArrayList<>();
        List<StrParameter> outputWriterPaths = new ArrayList<>();

        for(int i = 0; i < inputPaths.size(); i++){
            try{
                BufferedReader inputReader = Files.newBufferedReader(Paths.get(inputPaths.get(i)));
            }catch(Exception e){
                e.printStackTrace();

                if(inputReader != null)
                    inputReader.close();

                for(BufferedReader r : inputReaders)
                    r.close();

                System.exit(1);
            }

            StrParameter outputPathParam = new StrParameter(ParsingUtils.splitByVars(new StrView(outputPaths.get(i))));

            inputReaders.add(inputReader);
            outputWriterPaths.add(outputPathParam);
        }

        Map<StrView, BufferedWriter> cachedWriters = new HashMap<>();

        while_loop:
        while(true){
            List<List<StrView>> texts = new ArrayList<>();

            for(int i = 0; i < inputReaders.size(); i++){
                List<StrView> currTexts = null;

                try{
                    currTexts = ParsingUtils.read(inputReaders.get(i), delimiter, patterns.getLength(i));
                }catch(Exception e){
                    e.printStackTrace();

                    for(BufferedReader r : inputReaders)
                        r.close();

                    for(BufferedWriter w : cachedWriters.values())
                        w.close();

                    System.exit(1);
                }

                if(currTexts == null)
                    break while_loop;

                texts.add(currTexts);
            }

            List<List<List<PatternMatch>>> matches = patterns.search(texts);

            if(matches == null)
                continue;

            for(int i = 0; i < matches.size(); i++){
                StrView path = outputWriterPaths.get(i).get();
                BufferedWriter w = null;

                if(cachedWriters.containsKey(path)){
                    w = cachedWriters.get(path);
                }else{
                    try{
                        w = Files.newBufferedWriter(Paths.get(path.toString()));
                    }catch(Exception e){
                        e.printStackTrace();

                        if(w != null)
                            w.close();

                        for(BufferedReader r : inputReaders)
                            r.close();

                        for(BufferedWriter writer : cachedWriters.values())
                            writer.close();

                        System.exit(1);
                    }

                    cachedWriters.put(path, w);
                }

                List<StrView> currTexts = texts.get(i);
                List<List<PatternMatch>> currMatches = matches.get(i);
                List<List<Pattern>> currPatterns = patternList.get(i);

                for(int j = 0; j < currTexts.size(); j++){
                    StringBuilder b = trimText(currTexts.get(j), currMatches.get(j), currPatterns.get(j));
                    w.append(b);

                    if(j < currTexts.size() - 1)
                        w.write(delimiter);
                }
            }
        }

        for(BufferedReader r : inputReaders)
            r.close();

        for(BufferedWriter w : cachedWriters.values())
            w.close();
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

        for(int i = 0; i < text.length(); i++){
            Boolean b = intervals.get(i);

            if(b == true)
                count++;

            if(count == 0)
                res.append(text.charAt(i));

            if(b == false)
                count--;
        }

        return res;
    }
}
