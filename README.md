# LexicalChainsExtraction
A probabilistic word chain extraction tool, ranking word n-grams by their probability to form (a sort of) collocation.

## Overview
The tool:
- reads a set of texts from a given directory structure (with each subdirectory containing a single file);
- keeps only characters, digits and single quotes, replacing all else with whitespace;
- splits all remaining "words" on the spaces;
- removes stopwords from a wordlist file (expected to be found in the running directory, by the name "english.list" and 
one stopword per line);
- removes double quotes (either '' or ");
- removes genitives ("'s" forms).

After the above tokenization and cleaning process the system estimates a ratio for each n-gram within a length span (default: 2 to 5).
The ratio expresses the probability of occurrence of the n-gram in the corpus input to the random probability of seeing the n-gram, 
if the text generation process was a random generation procedure.

## Related reading
This implementation is close - but not identical - to the description of Symbols and Non-symbols in 
[my thesis](http://users.iit.demokritos.gr/~ggianna/Publications/thesis.pdf)
and in the following paper:

Giannakopoulos, George, Vangelis Karkaletsis, George Vouros, and Panagiotis Stamatopoulos 2008; Summarization System Evaluation Revisited: N-Gram Graphs. ACM Trans. Speech Lang. Process. 5(3): 1–39.


## License
This work is under the Apache License v2.
