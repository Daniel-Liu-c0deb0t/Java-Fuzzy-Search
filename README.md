# Java-Fuzzy-Search
A simple library with Java implementations of fuzzy search algorithms.

## Features
- Exact string search using the KMP algorithm
- Fuzzy string search using the Bitap algorithm (for Hamming disance)
- Fuzzy string search using Myer's algorithm (for Levenshtein distance + transpositions)
- Fuzzy string search using regular DP + Ukkonen's cutoff algorithm (for Levenshtein distance + transpositions)

## Planned Features
- Support wilcard characters and different DP weights in Ukkonen's cutoff algorithm
- Trie speedup for Ukkonen's cutoff
- Benchmarks
