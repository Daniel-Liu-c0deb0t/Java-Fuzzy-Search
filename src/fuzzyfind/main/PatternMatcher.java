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

    public PatternMatcher(List<String> paths){
        List<List<List<Pattern>>> patternList = new ArrayList<>();
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

                System.exit(0);
            }

            String line = null;

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
            BufferedReader inputReader = Files.newBufferedReader(Paths.get(inputPaths.get(i)));
            StrParameter outputPathParam = new StrParameter(ParsingUtils.splitByVars(new StrView(outputPaths.get(i))));

            inputReaders.add(inputReader);
            outputWriterPaths.add(outputPathParam);
        }

        Map<StrView, BufferedWriter> cachedWriters = new HashMap<>();
        boolean done = false;

        while(!done){
            List<List<StrView>> texts = new ArrayList<>();

            for(int i = 0; i < inputReaders.size(); i++){
                List<StrView> currTexts = new ArrayList<>();
                done |= ParsingUtils.read(inputReaders.get(i), delimiter, patterns.getLength(i), currTexts);
                texts.add(currTexts);
            }

            List<List<List<PatternMatch>>> matches = patterns.search(texts);

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

                        for(BufferedWriter writer : cachedWriters.values())
                            writer.close();

                        System.exit(0);
                    }

                    cachedWriters.put(path, w);
                }

                List<StrView> currTexts = texts.get(i);
                List<List<PatternMatch>> currMatches = matches.get(i);

                for(int j = 0; j < currTexts.size(); j++){
                    StringBuilder b = trimText(currTexts.get(j), currMatches.get(j));
                    w.append(b);

                    if(j < currTexts.size() - 1)
                        w.write(delimiter);
                }
            }
        }

        for(BufferedWriter w : cachedWriters.values())
            w.close();
    }
}
