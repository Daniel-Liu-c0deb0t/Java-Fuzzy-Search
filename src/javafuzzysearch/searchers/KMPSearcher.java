package javafuzzysearch.searchers;

import javafuzzysearch.utils.ExactMatch;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the KMP exact string search algorithm.
 */
public class KMPSearcher{
    /**
     * Returns a longest suffix-prefix array.
     * @param pattern The pattern to be preprocessed.
     * @return The preprocessed longest suffix-prefix array.
     */
    public int[] preprocessPattern(String pattern){
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
     * @param text String to be searched.
     * @param pattern String to be searched for.
     * @return A List of ExactMatches that have the indexes of match start locations.
     */
    public List<ExactMatch> search(String text, String pattern){
        return search(text, pattern, preprocessPattern(pattern));
    }

    /**
     * Searches for a String pattern in the String text.
     * @param text String to be searched.
     * @param pattern String to be searched for.
     * @param lsp A preprocessed longest suffix-prefix array for the pattern.
     * @return A List of ExactMatches that have the indexes of the match start locations.
     */
    public List<ExactMatch> search(String text, String pattern, int[] lsp){
        int i = 0, j = 0;
        List<ExactMatch> matches = new ArrayList<>();

        while(i < text.length()){
            if(text.charAt(i) == pattern.charAt(j)){
                i++;
                j++;
            }
            if(j == pattern.length()){
                matches.add(new ExactMatch(i - j));
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