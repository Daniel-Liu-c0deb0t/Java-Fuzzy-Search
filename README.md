# Java-Fuzzy-Search
A fast and flexible Java fuzzy **search** (not match!) library that supports bit parallel algorithms, wildcard characters, different scoring schemes, and other features. The goal is to focus on doing one thing (string search) and have tons of options and optimizations for different use cases.

Also includes a [fuzzy search tool](#fuzzyfind-tool), called fuzzyfind, that uses a simple language for describing patterns, similar to the `grep` Unix command. Since the tool is very general, it can be applied to bioinformatic tasks like demultiplexing DNA sequences and trimming adapters.

## Overview of fuzzy search features

### Knuth-Morris-Pratt exact string search
A very standard implementation of the [KMP algorithm](https://en.wikipedia.org/wiki/Knuth%E2%80%93Morris%E2%80%93Pratt_algorithm). The runtime complexity is `O(n + m)` for a text of size `n` and a pattern of size `m`.

### Bitap fuzzy string search with the Hamming distance metric
Implementation of the [Bitap algorithm](https://www.cs.helsinki.fi/u/tpkarkka/opetus/11s/spa/lecture04.pdf) for searching for matches that may contain substitutions between the pattern and the text. The runtime complexity is `O(n * (m / w))` for a text of size `n`, a pattern of size `m`, and a word size of `w`. It takes advantage of bit operations on short bit sets having essentially constant time. We use a Java `long` that is 63 bits (not using sign bit) as one word, and we partition the pattern into many chunks (words) for faster searching.

#### Other features
- Partial overlap between the text and the pattern
- Allows some wildcard characters to match a set of other characters

### Myer's fuzzy string search with the Damerau-Levenshtein distance metric
Implementation of [Myer's algorithm](https://pdfs.semanticscholar.org/ec02/dc32220d26f6a84c89f49ecc42fc91e4a592.pdf) that allows matches to have insertions, deletions, and transpositions in addition to substitutions. The runtime complexity is `O(n * (m / w))` for a text of size `n`, a pattern of size `m`, and a word size of `w`. This algorithm also takes advantage of bit parallel operations to improve the theoretical bound.

#### Other features
- Partial overlap between the text and the pattern (only at the end of the text)
- Allows some wildcard characters to match a set of other characters

### Vanilla DP and Ukkonen's cutoff heuristic with the Damerau-Levenshtein distance metric
Implementation of the classical DP technique to search for a pattern, while allowing insertions, deletions, substitutions, and transpositions. To speed it up, [Ukkonen's cutoff heuristic](https://mycourses.aalto.fi/pluginfile.php/192362/mod_resource/content/2/lecture04.pdf) is used to eliminate diagonals in the DP matrix that exceed the maximum edit distance. The average run time complexity is `O(k * n)` for a text of size `n` and allowing up to `k` edits.

#### Other features
- Partial overlap between the text and the pattern
- Allows some wildcard characters to match a set of other characters
- Custom edit weights for different scoring schemes (some schemes may automatically disable using Ukkonen's cutoff heuristic)

### Stateful DP and Ukkonen's cutoff heuristic with the Damerau-Levenshtein distance metric
Same as vanilla DP with Ukkonen's cutoff heuristic, but it is stateful across multiple patterns. The patterns are lexicographically sorted, so patterns with similar prefixes are closer to each other. This allows part of the DP matrix to be kept across multiple patterns, speeding up the search time. It has the same features as the vanilla version, except it is slightly more limited in the amount of options for the partial overlap between the text and the pattern.

### N-grams
Splits each pattern into contiguous, overlapping segments of length `N`, and when matching a piece of text, do the same for the text. This allows fast checks for overlaps of length `N` between the pattern and the text. It can be used as a preliminary check before using a more costly searching algorithm. A practical value for `N` is either `N = 3` or `N = 2`.

### StrView
`StrView` is the class used to represent strings in the library. It acts as an immutable view on a character array, allowing substring, reverse, and upper/lower case operations to be constant time.

---

## fuzzyfind tool
This is a general tool for matching multiple fuzzy patterns and other types of patterns that occur in a user-defined format. It was built for preprocessing DNA sequences in `.fastq` format by trimming and demultiplexing, but the tool can be used in other ways due to its flexibility.

### Introduction
The tool is very simple. It takes template files that describes the patterns for each of the input text files. Matched regions in the text files can be trimmed, and lines that contain those regions can be transfered to different files.

As an example of how the tool functions, let's use a sample `.fastq` file that contains 4 lines, or 1 "read" describing a DNA sequence:

**sample.fastq**
```
@sample dummy sequence
AAATTTCCCCCCCCCC
+
BBBBBBCCCCCCCCCC
```
The 2nd line and the 4th line contain the information that we care about. The 2nd line is the actual DNA sequence, and the last line describes the quality score for each base pair (character) in the 2nd line. Let's say we want to match and trim the barcode (`AAATTT`), while also trimming the corresponding region in the 4th line. In practice, there may be more regions to match and trim, but we will keep it simple.

The tool allows multiple input files and multiple template files, where each template file corresponds to an input file. For the sample `.fastq` file, we will use the following template file:

**template.txt**
```

```
