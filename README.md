# Java-Fuzzy-Search
A fast and flexible Java fuzzy search library that supports bit parallel algorithms, wildcard characters, different scoring schemes, and other features. The goal is to focus on doing one thing (string search) and have tons of options and speedups for different use cases.

## Overview of features
- Very basic exact string search using the KMP algorithm (use Bitap for wildcard characters and other features)
- Fuzzy string search using the Bitap algorithm (Hamming disance)
- Fuzzy string search using Myer's bit parallel algorithm (Levenshtein distance + optional transpositions)
- Fuzzy string search using regular DP + Ukkonen's cutoff heuristic (Levenshtein distance + optional transpositions + different scoring schemes)
- Stateful DP across multiple patterns + Ukkonen's cutoff heuristic (works well with patterns that share prefixes)
- Support for wildcard characters with all fuzzy string matching algorithms
- Support for partial overlaps between the text and the search pattern with fuzzy search algorithms (allows the pattern to hang off the ends of the text)

## Future work
- Generalize searching to work on any list of objects, instead of just strings
    - An example use case would be fuzzy searching with whole words instead of just characters
