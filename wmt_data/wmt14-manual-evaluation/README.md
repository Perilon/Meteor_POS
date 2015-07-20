This directory contains the results of WMT14 manual (human) evaluation
in the file "wmt14-judgments-anonymized.csv". For more information
about how the evaluation was conducted, see
[the WMT14 overview paper](http://www.statmt.org/wmt14/pdf/WMT01.pdf). That
and this data can be found on the
[WMT14 results page](statmt.org/wmt14/results.html).

The first line of this file is a header defining the following
fields. This is the "WMT" format. Many of the fields are no longer
used, but we have kept the same format to keep things consistent
across the past few years of the manual evaluation. The fields are:

- {src,trg}lang: the source and target language
- srcIndex: the sentence number (1-indexed)
- documentId: not used (set to -1)
- segmentId: the sentence number (same as srcIndex)
- judgeId: the anonymized judge name (e.g., "annotator17")
- system[12345]Number: not used (set to -1)
- system[12345]Id: the system name (e.g., newstest2014.uedin-wmt14.3023)
- system[1234]rank: the rank the judge assigned to this task 
  (1 is best, 5 is worst)

You might also be interested in
[the code used to produce the rankings](https://github.com/keisks/mt_trueskill_scripts).
