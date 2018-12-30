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

There are three types of patterns that serve as building blocks in the template file, with each occurrance and their parameters enclosed in `{}`s. The `r` type and the `i` type represents a repeating, fixed length pattern and a repeating, interval length pattern, respectively. Their patterns are unordered sets of characters, and they match contiguous sequences of characters in the text that only contain characters in the set. The pattern can be omitted to match any character. Also, `i` type patterns can match sequences of any length if the length parameter is omitted. `f` type patterns are patterns that require fuzzy searching. In this case, it allows a maximum of 1 edit between the text and the pattern string to count as a match, and the pattern strings are from the `barcodes.txt` file:

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
- `--batch-size`: How many sets of lines to batch for each thread. Defaults to 1000.

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
- Hyphens in strings that represent a set of characters can be escaped like `--`

### How it works

