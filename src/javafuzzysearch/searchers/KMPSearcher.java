package javafuzzysearch.searchers;

import javafuzzysearch.utils.ExactMatch;
import javafuzzysearch.utils.StrView;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the KMP exact string search algorithm.
 */
public class KMPSearcher{
    /**
     * Returns the longest suffix-prefix array for a pattern.
     */
    public int[] preprocessPattern(StrView pattern){
        int length = 0;
        int i = 1;
        int[] lsp = new int[pattern.length()];

        while(i < pattern.length()){
            if(pattern.charAt(i) == pattern.charAt(length)){
                length++;
                lsp[i] = length;
                i++;
            }else{
                if(length == 0){
                    lsp[i] = 0;
                    i++;
                }else{
                    length = lsp[length - 1];
                }
            }
        }

        return lsp;
    }

    /**
     * Searches for a String pattern in the String text.
     * Returns a list of ExactMatch objects that represents each match location.
     */
    public List<ExactMatch> search(StrView text, StrView pattern){
        return search(text, pattern, preprocessPattern(pattern));
    }

    /**
     * Searches for a String pattern in the String text.
     * Takes a longest prefix-suffix array for the pattern.
     */
    public List<ExactMatch> search(StrView text, StrView pattern, int[] lsp){
        int i = 0, j = 0;
        List<ExactMatch> matches = new ArrayList<>();

        while(i < text.length()){
            if(text.charAt(i) == pattern.charAt(j)){
                i++;
                j++;
            }
            if(j == pattern.length()){
                matches.add(new ExactMatch(i - 1));
                j = lsp[j - 1];
            }else if(i < text.length() && text.charAt(i) != pattern.charAt(j)){
                if(j == 0)
                    i++;
                else
                    j = lsp[j - 1];
            }
        }

        return matches;
    }
}
