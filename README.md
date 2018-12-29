# Java-Fuzzy-Search
A fast and flexible Java fuzzy **search** (not match!) library that supports bit parallel algorithms, wildcard characters, different scoring schemes, and other features. The goal is to focus on doing one thing (string search) and have tons of options and optimizations for different use cases.

Also includes a fuzzy search tool that uses a simple language for describing patterns, which is similar to the `grep` Unix command. Since the tool is very general, it can be applied to bioinformatic tasks like demultiplexing DNA sequences and trimming adapters.

## Overview of features
- Very basic exact string search using the KMP algorithm (use Bitap for wildcard characters and other features)
- Fuzzy string search using the Bitap algorithm (Hamming disance)
- Fuzzy string search using Myer's bit parallel algorithm (Levenshtein distance + optional transpositions)
- Fuzzy string search using regular DP + Ukkonen's cutoff heuristic (Levenshtein distance + optional transpositions + different scoring schemes)
- Stateful DP across multiple patterns + Ukkonen's cutoff heuristic (works well with patterns that share prefixes)
- Support for wildcard characters with all fuzzy string matching algorithms
- Support for partial overlaps between the text and the search pattern with fuzzy search algorithms (allows the pattern to hang off the ends of the text)

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

### Vanilla DP and Ukkonen's cutoff heuristic


### Stateful DP and Ukkonen's cutoff heuristic
