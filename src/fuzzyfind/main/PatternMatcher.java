package fuzzyfind.main;

import fuzzyfind.patterns.WholePattern;
import fuzzyfind.patterns.Pattern;

import fuzzyfind.parameters.StrParameter;

import fuzzyfind.utils.ParsingUtils;
import fuzzyfind.utils.PatternMatch;
import fuzzyfind.utils.Variables;

import javafuzzysearch.utils.StrView;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import java.nio.file.Files;
import java.nio.file.Paths;

import java.io.BufferedReader;
import java.io.BufferedWriter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

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

    private boolean gzipOutput;
    private Character delimiter;
    private List<StrParameter> matchedOutputParams;
    private List<BufferedWriter> unmatchedWriters;
    private Map<StrView, BufferedWriter> cachedWriters;
    private Semaphore semaphore;
    private Object writeLock;

    public void match(List<String> inputPaths, boolean gzipInput, List<String> matchedOutputPaths, List<String> unmatchedOutputPaths, boolean gzipOutput, Character delimiter, int threadCount, int batchSize){
        this.gzipOutput = gzipOutput;
        this.delimiter = delimiter;

        try{
            List<BufferedReader> inputReaders = new ArrayList<>();
            matchedOutputParams = new ArrayList<StrParameter>();
            unmatchedWriters = new ArrayList<BufferedWriter>();
            cachedWriters = new HashMap<StrView, BufferedWriter>();

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

            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            semaphore = new Semaphore(threadCount * 2);
            writeLock = new Object();

            while_loop:
            while(true){
                semaphore.acquire();

                List<List<List<StrView>>> batchTexts = new ArrayList<>();

                for(int i = 0; i < batchSize; i++){
                    List<List<StrView>> texts = new ArrayList<>();

                    for(int j = 0; j < inputReaders.size(); j++){
                        List<StrView> currTexts = ParsingUtils.read(inputReaders.get(j), delimiter, patterns.getLength(j));

                        if(currTexts == null){
                            if(!batchTexts.isEmpty())
                                executor.execute(new BatchTask(batchTexts));

                            break while_loop;
                        }

                        texts.add(currTexts);
                    }

                    batchTexts.add(texts);
                }

                executor.execute(new BatchTask(batchTexts));
            }

            executor.shutdown();
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);

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

    private class BatchTask implements Runnable{
        private List<List<List<StrView>>> batchTexts;

        BatchTask(List<List<List<StrView>>> batchTexts){
            this.batchTexts = batchTexts;
        }

        @Override
        public void run(){
            try{
                for(List<List<StrView>> texts : batchTexts){
                    Variables vars = new Variables();
                    List<List<List<PatternMatch>>> matches = patterns.search(texts, vars);

                    synchronized(writeLock){
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
                            StrView path = matchedOutputParams.get(i).get(vars);
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
                }
            }catch(Exception e){
                e.printStackTrace();
            }finally{
                semaphore.release();
            }
        }
    }
}
