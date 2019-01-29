java -jar ../fuzzysplit.jar \
text.fastq \
--pattern pattern.txt \
--matched demux/fuzzysplit_demux/matched_%b.pattern_name%.fastq \
--threads 2
