# Java-Fuzzy-Search
A fast and flexible Java fuzzy **search** (not match!) library that supports bit parallel algorithms, wildcard characters, different scoring schemes, and other features. The goal is to focus on doing one thing (string search) and have tons of options and optimizations for different use cases.

Also includes a [fuzzy search tool](#fuzzyfind-tool), called fuzzyfind, that uses a simple language for describing patterns, similar to the `grep` Unix command. Since the tool is very general, it can be applied to bioinformatic tasks like demultiplexing DNA sequences and trimming adapters.

## Overview of fuzzy search features

### Knuth-Morris-Pratt exact string search
A very standard implementation of the [KMP algorithm](https://en.wikipedia.org/wiki/Knuth%E2%80%93Morris%E2%80%93Pratt_algorithm). The runtime complexity is `O(n + m)` for a text of size `n` and a pattern of size `m`.

### Bitap fuzzy string search with the Hamming distance metric
Implementation of the [Bitap algorithm](https://www.cs.helsinki.fi/u/tpkarkka/opetus/11s/spa/lecture04.pdf) for searching for matches that may contain substitutions between the pattern and the text. The runtime complexity is `O(k * n * (m / w))` for a text of size `n`, a pattern of size `m`, a word size of `w`, and `k` edits allowed (should be small). It takes advantage of bit operations on short bit sets having essentially constant time. We use a Java `long` that is 63 bits (not using sign bit) as one word, and we partition the pattern into many chunks (words) for faster searching.

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
AAATTTCCCCC
+
BBBBBBCCCCC
```
The 2nd line and the 4th line contain the information that we care about. The 2nd line is the actual DNA sequence, and the last line describes the quality score for each base pair (character) in the 2nd line. Let's say we want to match and trim the barcode (`AAATTT`), while also trimming the corresponding region in the 4th line. In practice, there may be more regions to match and trim, but we will keep it simple.

The tool allows multiple input files and multiple template files, where each template file corresponds to an input file. For the sample `.fastq` file, we will use the following template file:

**template.txt**
```
{i}
{f required, trim, name = "barcode", edits = 1, pattern = f"barcodes.txt"}{i pattern = "ATCG", length = 0-100}
{i}
{r trim, length = %barcode.length%}{i}
```
The template file contains 4 lines, each corresponding to a line in a read. It represents how each read should be partitioned by specifying patterns that should match a corresponding location in the read. If the input file contains more that four lines, then the template file will be repeatedly applied for every four lines in the input file.

There are three types of patterns that serve as building blocks in the template file, with each occurrance and their parameters enclosed in `{}`s. The `r` type and the `i` type represents a repeating, fixed-length pattern and a repeating, interval-length pattern, respectively. Their patterns are unordered sets of characters, and they match contiguous sequences of characters in the text that only contain characters in the set. The pattern can be omitted to match any character. Also, `i` type patterns can match sequences of any length if the length parameter is omitted. `f` type patterns are patterns that require fuzzy searching. In this case, it allows a maximum of 1 edit between the text and the pattern string to count as a match, and the pattern strings are from the `barcodes.txt` file:

**barcodes.txt**
```
"barcode_1": "AAATTT"
"barcode_2": "AAATTTT"
```
Each of these barcodes have a name. These names will be used to name the output file. `barcode_1` will eventually be chosen as a matching pattern because it is the best match (lowest number of edits) for the text.

To finally run the tool, we use the following shell command:
```
java -jar fuzzyfind.jar \
sample.fastq \
--pattern template.txt \
--matched matched_%barcode.pattern_name%.fastq
```
This will produce a file named `matched_barcode_1.fastq`, that contains the following:

**matched_barcode_1.fastq**
```
@sample dummy sequence
CCCCC
+
CCCCC
```

#### Variables
fuzzyfind makes extensive use of variables that are generated while matching. They allow match data, such as the length of the match, the pattern matched when fuzzy searching, and the name of the pattern matched when fuzzy searching to be referenced in parameters of other patterns. In the example above, we use a variable reference to sync the barcode's match length and the length of the pattern that matches any character in the 4th line. They are both trimmed due to the `trim` parameter.

Variables created in one pattern file can be referenced in other files. The command for running the tool can also reference variables that were created while matching the template files. This may cause some patterns to require other patterns to be matched first. By default, each template file is matched from the first to the last line. The template files are handled in the order they are specified in the command that runs the tool. To match lines out of order, place a non-negative integer before that line in the template file. Lines that are not numbered are always matched first, and then the numbered lines are matched in increasing order. Note that a pattern within a line cannot reference variables from another pattern in the same line.

#### Selector (paired-end demultiplexing)
Variables can be used alongside the selection operator to easily handle paired-end demultiplexing. We will have two input `.fastq` files, two template files, and two output files (that may reference some variables). We also have two lists of barcodes that are in the same format as the `barcodes.txt` which we will call `barcodes_forwards.txt` and `barcodes_reversed.txt`. Our goal is to match a barcode with some name for the forwards read, and then match the corresponding reversed barcode that should have the same name to the reversed read. To do that, we can specify the following as a parameter to the fuzzy `f` type pattern for the reversed read:
```
pattern = f"barcodes_reversed.txt"[%barcode_forwards.pattern_name%]
```
This assumes that the name of the forwards barcode is `barcode_forwards`. The selector is defined as the stuff in the `[]`s. It chooses a reversed barcode with the same name as the forwards barcode and uses that as the pattern.

### Commands and file formats
#### Template files
A template file may match multiple delimited areas in the text file. Each new line in the template file matches one delimited region in the text file. Empty lines and comment lines that begin with `#` are ignored. Each pattern is surrounded by `{}`, with the first character in the brackets indicating the pattern type, and all comma-separated parameters coming after it.

#### List files
These data files can be referenced through `f"<file name>"` strings or their contents can be directly placed in the template file as a normal string (though, the quotes need to be properly escaped using a `\`). List files are made up of key-value pairs that are separated by newlines or semicolons. If they specify fuzzy patterns, then the key is the name of that pattern value. If they specify wildcard characters, then each character in the key maps to the set of characters indicated by the value.

Each key-value pair in the list file must be in the following format:
```
"<key>": "<value>"
```

#### Command-line arguments
Positional arguments must come before all optional ones!

**Positional**
- All input files.

**Optional**
- `--pattern`: Template files.
- `--matched`: Output paths for text that matched the template file. Can reference variables from the template files.
- `--unmatched`: Output paths for text that did not match all of the required patterns in the template file.
- `--delimiter`: The character to split the text by. Defaults to `\n`.
- `--in-gz`: Force the input files to be processed as Gzip files. By default, the tool autodetects the file extension.
- `--out-gz`: Force the output as Gzip files. By default, the tool autodetects the file extension.
- `--threads`: Number of threads. By default, only one thread is used.
- `--batch-size`: How many sets of lines to batch for each thread. Defaults to 100.

#### Pattern types and their parameters
**f**
- `required`
- `trim`
- `transpose`: enable transpositions
- `no_case`: case insensitive
- `hamming`: use hamming distance
- `name = <string>`: name of the pattern, used to reference the match as a variable
- `edits = <number>`: maximum number of edits allowed
    - real value < 1 = percentage of pattern length, negative value = pattern length - value
- `min_overlap = <number>`: minimum overlap between pattern and the text, defaults to full overlap
    - real value < 1 = percentage of pattern length, negative value = pattern length - value
- `wildcard = <list file>`, `pattern_wildcard = <list file>`, `text_wildcard = <list file>`: list of wildcard characters and the set of characters they map to
- `pattern = <list file><selector>`: the fuzzy pattern(s) to search for

**r**
- `required`
- `trim`
- `name = <string>`
- `length = <integer>`: fixed length of the pattern
- `pattern = <set of characters>`: a set of characters like `"abcA-Z0-9 !?"`

**i**
- `trim`
- `name = <string>`
- `length = <integer> - <integer>`: two integers separated by a `-` that represents the lower and upper bounds of the length
- `pattern = <set of characters>`: a set of characters like `"abcA-Z0-9 !?"`

#### Note
- Characters like `"`, newline, tab, etc. can be escaped like `\"`, `\n`, `\t`, etc.
- Percent signs in strings that accept variables can be escaped like `%%`
- Hyphens in strings that represent sets of characters can be escaped like `--`

### How it works
#### Parallelizing the algorithm
Since handling a chunk of the input files that are specified by the template files does not require information from other chunks, the algorithm is a prime candidate for parallelization. The main thread essentially fills up a queue with large batches of input text, and worker threads process the batches in parallel. Each thread outputs the result of a whole batch at once, and synchronizes with other threads to ensure that only one thread is writing to the output files. Empirically, the speed benefit of multi-threading only shows on large input sizes.

#### Matching all 3 types of patterns
Each line in the template file is split into the patterns they represent. We will only examine the patterns in one line how they are matched.

First, the pattern is split into contiguous regions of either fuzzy patterns and repeating, fixed-length patterns, or repeating, interval-length patterns. Each of these regions will be handled separately. For simplicity, we will call the fuzzy patterns and repeating, fixed-length patterns the fixed-length region, even though it may not have a constant length.

Then, the algorithm keeps a starting index to track the "done" prefix of the text being searched. At first, the index is at the beginning of the text. To advance the done index, the algorithm goes through each fixed-length region, from left to right, and searches for the entire region in the not-done suffix of the text. Note that the region right before any fixed-length region must be an interval-length region. For every possible match within the edit threshold, the algorithm checks to see if the interval-length region before matches the region of the text after the done portion and before the match of the fixed-length region in the text. The algorithm chooses the first fixed-length match that satisfies all of the constraints to greedily leave space for patterns that may come later, and updates the done index up to the end of the fixed-length match. We choose to not try every possible match configuration, but instead use the gready approach that may result in suboptimal matches due to time-complexity concerns.

##### Searching/matching for the fixed-length region
`CutoffSearcher` from the searching library is used to match fuzzy patterns. The expected run time complexity is `O(f * k * n)` for each fuzzy pattern, where `f` is the number of fuzzy patterns, `n` is the length of the region of text, and the number of edits, `k` should be small. The match with the lowest number of edits is chosen. Matching fixed-length repeating patterns is trivial, and takes `O(n)` time.

##### Searching/matching for the interval-length region
We formulate the task of matching many contiguous interval-length patterns as a recurrence, and we solve it in `O(I * n)` time using DP, where `I` is the number of interval-length patterns and `n` is the length of the region of the text. The recurrence, `dp(i, j)`, calculates whether the first `i` characters of the text and the first `j` interval-length patterns match. Whether the whole region of the text and all of the contiguous interval-length patterns match will be `dp(n, I)`.

For all `0 <= i <= n` and `0 <= j <= I`, the following recurrence holds true:
```
max_j = maximum length of pattern j
min_j = minimum length of pattern j

dp(0, 0) = true

dp(i, j) = logical or of all dp(k, j - 1)
    where characters (k + 1) to i of text are all part of pattern j
        for k in {(i - max_j) to (i - min_j)}
```
Directly realizing this in code using a matrix to cache intermediate recursion values results in a `O(I * n^2)` upper bound for the time complexity. The key insight in improving the time complexity is that the contiguous region in the text that ends at a certain index `i`, where all characters in the region belong to the `j`th pattern, must form a contiguous overlap with the segment `i - max_j` to `i - min_j`, for `dp(i, j)` to possibly be true. This allows us to get rid of the innermost loop.

A prefix sum array of length `n` for each of the `I` patterns can be used to quickly query ranges in the `dp` matrix for whether it contains at least one true value. We define the prefix sum matrix as `pre(i, j)`, for `0 <= i <= n` and `0 <= j <= I`. We can also keep a matrix that holds the length of the longest contiguous substring of text that ends at an index, where all of that substring's characters are included by a certain pattern. The recursion can then be expressed as the following:
```
dp(0, 0) = true
longest(0, i) = 0
    for i in {0 to I}
pre(i, 0) = 1
    for i in {0 to n}

longest(i, j) = longest(i - 1, j) + 1
    if character i of text is part of pattern j
    else 0

dp(i, j) = (pre(i - min_j, j - 1) - pre(maximum(i - max_j, i - longest(i, j)) - 1, j - 1)) > 0
pre(i, j) = pre(i - 1, j) + dp(i, j)
```
To read out the corresponding segments of text where each pattern matched, jump pointers can be kept to back trace through the DP matrix.

It is easy to see why this recurrence is correct by induction.

The base case, `dp(0, 0) = true`, is correct because an empty text must match an empty list of patterns.

For a prefix of text up to index `i` and a prefix of an array of patterns up to index `j`, the last pattern must cover some suffix of the prefix of text. Furthermore, the segment it covers must have a length between `min_j` and `max_j`. That implies that the the requirements for `dp(i, j)` to be true must be that pattern `j - 1`, the second to last pattern has to end somewhere between `i - max_j` and `i - min_j`, and it must match, ie. `dp(k, j - 1)` is true for some ending index `k` of the second to last pattern. Also, pattern `j` must contain all of the characters in the substring from `k + 1` to `i` in the text. Those are the only requirements for `dp(i, j)` to be true. By plugging in `n` for `i` and `I` for `j`,  we get `dp(n, I)`, which is whether the whole text matches the all of the pattern.
