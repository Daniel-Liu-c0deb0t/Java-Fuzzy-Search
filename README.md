# Java-Fuzzy-Search
A fast and flexible Java fuzzy search library. The goal is to focus on doing one thing (string search) and have a ton of options and speedups for different use cases.

## Features
- Very basic exact string search using the KMP algorithm
- Fuzzy string search using the Bitap algorithm (for Hamming disance)
- Fuzzy string search using Myer's algorithm (for Levenshtein distance + transpositions)
- Fuzzy string search using regular DP + Ukkonen's cutoff algorithm (for Levenshtein distance + transpositions)
- Support for wildcard characters for all fuzzy string matching algorithms
- Support for different edit weights/scores with Ukkonen's cutoff algorithm
- Partial overlap between text and search pattern with fuzzy search
- Stateful DP with Ukkonen's cutoff algorithm for multiple patterns (works well with patterns that share prefixes)

## Planned Features
- Fuzzy grep command line tool
