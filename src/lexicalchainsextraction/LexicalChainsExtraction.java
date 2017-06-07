/**
 * Copyright 2017 George Giannakopoulos
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package lexicalchainsextraction;

import gr.demokritos.iit.conceptualIndex.structs.Distribution;
import gr.demokritos.iit.jinsect.structs.CategorizedFileEntry;
import gr.demokritos.iit.jinsect.structs.DocumentSet;
import gr.demokritos.iit.jinsect.utils;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author ggianna
 */
public class LexicalChainsExtraction {
    protected int N_GRAM_SIZE = 2;
    Distribution<Integer> dStringCounts = new Distribution<>();
    Set<Integer> sAlphabet = new TreeSet<>();
    List<String> lsFullText = new ArrayList<>();
    protected String DOC_DELIMITER = String.valueOf('\u0000');
    Set<String> ssStopwords = new HashSet<>(Arrays.asList(
            utils.loadFileToStringWithNewlines("english.list").split("\\s*\n\\s*")));
        

    public void analyzeDocuments(int iMinSize, int iMaxSize) {
        double dTotal = getDocumentCount();
        double dCnt = 0;
        
        Iterator<CategorizedFileEntry> icfe = getDocumentIterator();
        // For every document in the set
        while (icfe.hasNext()) {
            String sText = utils.loadFileToString(icfe.next().getFileName());
            
            // Perform the analysis
            analyzeDocument(iMinSize, iMaxSize, sText);
            
            dCnt += 1;
            // DEBUG LINES
//            System.err.println(dPrefixSuffixCounts.asTreeMap().toString());
            //
            appendToLog(String.format("Analyzed %1.0f out of %1.0f (%5.2f%%)", dCnt, dTotal,
                    100.0 * dCnt / dTotal));
        } // End for every document
    }
    
    protected ComparablePair<Integer, Integer> toPair(List<String> lsToConvert) {
        if (lsToConvert.size() == 1) {
            // TODO: Check if I should put the string second (as suffix)
            return new ComparablePair(lsToConvert.toString().hashCode(),
                    "".hashCode());
        }
        else
        return new ComparablePair(
            lsToConvert.subList(0, N_GRAM_SIZE).toString().hashCode(), 
            lsToConvert.subList(N_GRAM_SIZE - 1, 
                lsToConvert.size()).toString().hashCode());
    }
    
    /**
     * Splits a text, taking into account only letters (and digits).
     * @param sText The text to split
     * @return A list of the tokenized/split sentence.
     */
    protected List<String> split(String sText) {
        List<String> lsRes = new ArrayList<>(Arrays.asList(
                sText.trim().replaceAll("[^a-zA-Z0-9' ]", " ").replaceAll(
                    "[\"]|([']){2,}", " ").replaceAll(
                            "'s", " ").toLowerCase().split("\\s+")));
        
        return lsRes.stream().filter(new Predicate<String>() {

            @Override
            public boolean test(String t) {
                return !ssStopwords.contains(t);
            }
        }).collect(Collectors.toList());
    }
    
    
    protected DocumentSet dsSet = null;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        LexicalChainsExtraction le = new LexicalChainsExtraction();
        
        Hashtable<String,String> hSwitches = utils.parseCommandLineSwitches(args);
        
        String sTextDir = utils.getSwitch(hSwitches, "textDir", "../data/OriginalTexts/");
        int iMinSize = Integer.valueOf(utils.getSwitch(hSwitches, "minSize", "2"));
        int iMaxSize = Integer.valueOf(utils.getSwitch(hSwitches, "maxSize", "5"));
        // Read documents
        if (!le.loadDocuments(sTextDir))
            return;
        
        // Analyze documents
        le.analyzeDocuments(iMinSize, iMaxSize);
        
        // Now extract most probable sequences
        List<ComparableGradedText> dBestSequences = 
                le.getBestSequences(iMinSize,iMaxSize, Integer.MAX_VALUE);
        
        try {
            // Save them
            File fTmp = File.createTempFile(new Date().getTime() + "out", ".txt");
            {
                // Do the writing
                new BufferedWriter(new FileWriter(fTmp)).write(
                        le.sequencesToString(dBestSequences));
            }
            System.out.println("Most promising sequences saved to " + fTmp.getAbsolutePath());
            
        } catch (IOException ex) {
            Logger.getLogger(LexicalChainsExtraction.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Most promising sequences output, because saving failed. " +
                le.sequencesToString(dBestSequences));
        }
        
    }
    
    protected Iterator<CategorizedFileEntry> getDocumentIterator() {
        Iterator<CategorizedFileEntry> iIter = dsSet.getTrainingSet().iterator();
        return iIter;
    }
    
    protected int getDocumentCount() {
        return dsSet.getTrainingSet().size();
    }
    
    protected boolean loadDocuments(String sBaseDirPath) {
            // Get categories and files from disk
            dsSet = new DocumentSet(sBaseDirPath, 1.0);
            dsSet.createSets(true, (double)100 / 100);
            
            int iTotal = dsSet.getTrainingSet().size();
            if (iTotal == 0) // No documents to import
            {
                appendToLog("No input documents.\n");
                appendToLog("======DONE=====\n");
                return false;
            }
        
            return true; // Success
    }

    protected void appendToLog(String sMsg) {
        System.err.println(sMsg);
    }

    /** Returns the probability of generation of an exact given string, based on the alphabet of the 
     * Symbolic Graph, and given that there the generation process is random.
     * @param lsFullText The list of strings/tokens that make up the full text.
     * @param lsString The list of strings/tokens that make up the string of interest.
     *@return The probability of appearence of the given string.
     */
    protected double getProbabilityOfStringInRandomText(final List<String> lsFullText, 
            final List<String> lsString) {
        
        // Get prefix info
        int iPrefixCount = (int)dStringCounts.getValue(
                toHash(lsString.subList(0, lsString.size() - 1)));
        
        int iPrefixLen = lsString.size() - 1;
        int iFullStringLen = lsString.size() - (getDocumentCount() - 1); // Reduce by doc delimiters
        // Get additional info
        int iAlphabetSize = sAlphabet.size();
        int iSuffixLen = 1;
        int iFullTextLen = lsFullText.size() - (getDocumentCount() - 1); // Reduce by doc delimiters
        
        // Consider that the occurence of a random suffix is a proportion of the prefix occurences,
        // as indicated by the probability of the given suffix, as a random selection process of n
        // symbols from the symbol set.
        double dFullStringCount = (double)iPrefixCount * 
                Math.pow(1.0 / iAlphabetSize, iSuffixLen);
        // Consider p(sPrefix) = N(sPrefix) / (length(Text) - length(sPrefix))
        // Again allowing overlapping n-grams
        double pPrefix = (double) iPrefixCount / 
                (iFullTextLen - (iPrefixLen - 1));
        
        
        double pJoined = dFullStringCount / 
                (iFullTextLen - iFullStringLen);
        // Return p(sPrefix) * p(sPrefix, sSuffix)
        double dRes = (iPrefixCount == 0) ? 0.0 : pPrefix * pJoined;
        return dRes;
    }
    
    /** Returns the probability of occurence of a string within the corpus.
     * @param lsFullText The (string list) text of the corpus.
     * @param lsSeq The sequence of interest.
     *@return The probability of occurence of the suffix, given the prefix.
     */
    protected double getProbabilityOfStringInText(final List<String> lsFullText,
            final List<String> lsSeq) {
        int iStringOccurences = (int) dStringCounts.getValue(toHash(lsSeq)); // TODO: Fix
        int iStringLen = lsSeq.size();
        int iFullTextLen = lsFullText.size() - (getDocumentCount() - 1); // Reduce by doc delimiters
        
        //Consider p(sPrefix) = N(sPrefix) / (length(Text) - length(sPrefix))
        // Here I allow overlapping occurences of the n-gram
        double pString = (double) iStringOccurences / 
                (iFullTextLen - (iStringLen - 1));
        
        return pString;
        
    }

    /**
     * Maps the prefix of (string list) text snippet to an integer hash.
     * The prefix (or head) is essentially all the list's items, except 
     * the final one (which we call the suffix/tail).
     * @param lsToConvert The string list to hash.
     * @return The hash Integer of the prefix.
     */
    protected Integer toPrefixHash(List<String> lsToConvert) {
        return lsToConvert.subList(0, N_GRAM_SIZE).toString().hashCode();
    }

    /**
     * Maps a (string list) text to a single integer hash.
     * @param lsToConvert The text to hash.
     * @return The hash Integer.
     */
    protected Integer toHash(List<String> lsToConvert) {
        return lsToConvert.toString().hashCode();
    }

    /**
     * Given a minimum and maximum length of n-grams (n-tokens), returns the most
     * promising n-grams in terms of their probability ratio, as follows:
     * (Actual frequency of appearance) / (Prob. of appearence).
     * @param iMinLen The minimum n of interest (e.g. 2 for bi-grams)
     * @param iMaxLen The maximum n of interest (e.g. 3 for tri-grams)
     * @param iNumToReturn The number of top performers to return
     * @return An ordered list of mappings [n-gram, probability ratio]
     */
    private List<ComparableGradedText> getBestSequences(int iMinLen, int iMaxLen,
            int iNumToReturn) {
        // Avoid invalid lengths
        if (iMinLen < 2) {
            throw new ArithmeticException("Minimum length should be at least 2.");
        }
            
        Distribution<ComparableList<String>> dTmpRes = new Distribution<>();
                
        // For every size
        for (int iCurLen = iMinLen; iCurLen <= iMaxLen; iCurLen++) {
            // DEBUG LINES
            appendToLog(String.format("Analyzing %d out of %d (%5.2f%%)", 
                    iCurLen - iMinLen + 1, iMaxLen - iMinLen + 1,
                    100.0 * (iCurLen - iMinLen) / (iMaxLen - iMinLen)));
            //////////////
            
            // Move to start of overall document
            ListIterator<String> sCurIter = lsFullText.listIterator();
            // Initialize current token list
            ComparableList<String> lsCurTokens = new ComparableList<>();

            // For all the overall document
            while (sCurIter.hasNext()) {
                // Get next token
                String sCur = sCurIter.next();
                // On new document
                if (sCur.equals(DOC_DELIMITER)) {
                    // Clear current token list
                    lsCurTokens.clear();
                    // Continue to next token
                    continue;
                }
                
                // Add to current list
                lsCurTokens.add(sCur);
                // Keep only appropriate size
                if (lsCurTokens.size() > iCurLen) {
                    // Get rid of oldest in list
                    lsCurTokens.remove(0);
                }

                if (lsCurTokens.size() == iCurLen) {
                // Update list performance
                // TODO: Update
                    dTmpRes.setValue(new ComparableList(lsCurTokens), 
                        (getProbabilityOfStringInText(lsFullText, lsCurTokens) + 10e-5) /
                        (getProbabilityOfStringInRandomText(lsFullText, 
                                lsCurTokens) + 10e-5));
                }            
            }
            // DEBUG LINES
            appendToLog(String.format("Analyzed %d out of %d (%5.2f%%)", 
                    iCurLen - iMinLen + 1, iMaxLen - iMinLen + 1,
                    100.0 * (iCurLen - iMinLen) / (iMaxLen - iMinLen)));
            //////////////
        }
        
        // DEBUG LINES
        appendToLog(String.format("Keeping top performers..."));
        //////////////
            
        // Init result list
        List<ComparableGradedText> lToReturn = new ArrayList<>();
        // For each entry in the results
        for (Entry<ComparableList<String>, Double> eCur: dTmpRes.asTreeMap().entrySet()) {
            // Add it as a pair to the list
            lToReturn.add(new ComparableGradedText(eCur.getValue(), eCur.getKey()));
        }
        // Now sort list, based on value
        Collections.sort(lToReturn, new DescendingComparatorImpl());
        
        
        // Keep sublist
        lToReturn = lToReturn.subList(0, Math.min(lToReturn.size(), iNumToReturn));

        // DEBUG LINES
        appendToLog(String.format("Keeping top performers... Done."));
        //////////////
        return lToReturn;
    }

    protected void analyzeDocument(int iMinSize, int iMaxSize, final String sText) {
        // Split in tokens
        final List<String> lsTokens = split(sText);
        // Add to overall document
        lsFullText.addAll(lsTokens);
        // TODO: Add document delimiter
        lsFullText.add(DOC_DELIMITER);
        
        // Analyze single tokens
        analyzeSingleTokens(lsTokens);
        
        // For every n-gram size
        for (int iCurSize = iMinSize; iCurSize <= iMaxSize; iCurSize++) {
            analyzeNGramsOfSize(lsTokens, iCurSize);
        } // end for every n-gram size
    }

    protected void analyzeNGramsOfSize(List<String> lsTokens, int iCurSize) {
        List<String> lsCurTokens = new ArrayList<>();
        
        ListIterator<String> liCur = lsTokens.listIterator();
        
        // For every remaining token
        while (liCur.hasNext()) {
            String sCur = liCur.next();
            // On new document
            if (sCur.equals(DOC_DELIMITER)) {
                // Clear current token list
                lsCurTokens.clear();
                // Continue to next token
                continue;
            }
            lsCurTokens.add(sCur);
            
            // If we exceeded the n-gram size
            if (lsCurTokens.size() > iCurSize) {
                // remove the oldest token from the prefix
                lsCurTokens.remove(0);
            }
            if (lsCurTokens.size() == iCurSize) {
                // Update the occurrences of the prefix-suffix pair
                    dStringCounts.increaseValue(
                            toHash(lsCurTokens),
                            1.0);
            }
        }
        
    }

    protected void analyzeSingleTokens(List<String> lsTokens) {
        for (String sCur : lsTokens) {
            // On new document
            if (sCur.equals(DOC_DELIMITER)) {
                // Continue to next token
                continue;
            }
            
            // Update single token counts
            dStringCounts.increaseValue(
                    toHash(Arrays.asList(new String[] {sCur})), 1.0);
            
            // Update alphabet
            sAlphabet.add(sCur.hashCode());

        }
    }

    private String sequencesToString(List<ComparableGradedText> dBestSequences) {
        StringBuffer sb = new StringBuffer();
        // For every graded text
        for (ComparableGradedText cgtCur : dBestSequences) {
            sb.append(cgtCur.getFirst()).append("\t").append(utils.printIterable(
                    cgtCur.getSecond(), " ")).append("\n");
        }
        return sb.toString();
    }

    private static class DescendingComparatorImpl implements Comparator<ComparableGradedText> {

        public DescendingComparatorImpl() {
        }

        @Override
        public int compare(ComparableGradedText o1, ComparableGradedText o2) {
            int iRes = -(int)(Math.signum(o1.getFirst() - o2.getFirst()));
            // If identical in terms of value
            // use string ordering
            if (iRes == 0)
                return -(o1.getSecond().toString().compareTo(
                        o2.getSecond().toString()));
            return iRes;
        }
    }

}
