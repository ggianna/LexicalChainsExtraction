/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lexicalchainsextraction;

import gr.demokritos.iit.conceptualIndex.structs.Distribution;
import gr.demokritos.iit.jinsect.structs.CategorizedFileEntry;
import gr.demokritos.iit.jinsect.structs.DocumentSet;
import gr.demokritos.iit.jinsect.structs.Pair;
import gr.demokritos.iit.jinsect.utils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

/**
 *
 * @author ggianna
 */
public class LexicalChainsExtraction {
    protected int N_GRAM_SIZE = 2;
    Distribution<Pair<Integer, Integer>> dPrefixSuffixCounts = new Distribution<>();
    Distribution<Integer> dPrefixCounts = new Distribution<>();
    Set<Integer> sAlphabet = new HashSet<>();
    List<String> lsFullText = new ArrayList<>();

    public void analyzeDocuments() {
        double dTotal = getDocumentCount();
        double dCnt = 0;
        
        Iterator<CategorizedFileEntry> icfe = getDocumentIterator();
        // For every document in the set
        while (icfe.hasNext()) {
            String sText = utils.loadFileToString(icfe.next().getFileName());
            
            // Split in tokens
            List<String> lsTokens = split(sText);
            // Add to overall document
            lsFullText.addAll(lsTokens);
            // TODO: Add document delimiter
            
            // Init current token
            List<String> sCurTokens = new ArrayList<>();
            
            ListIterator<String> liCur = lsTokens.listIterator();
            
            // For every remaining token
            while (liCur.hasNext()) {
                String sCur = liCur.next();
                sCurTokens.add(sCur);
                // Update single token counts
                dPrefixCounts.increaseValue(
                        toHash(Arrays.asList(new String[] {sCur})), 1.0);
                
                // Update alphabet
                sAlphabet.add(sCur.hashCode());
                
                // If we exceeded the n-gram size
                if (sCurTokens.size() >= N_GRAM_SIZE) {
                    // Update the occurrences of the prefix-suffix pair
                    dPrefixSuffixCounts.increaseValue(
                            toPair(sCurTokens),
                            1.0);
                    // Update prefix occurrences
                    dPrefixCounts.increaseValue(toPrefixHash(sCurTokens), 1.0);
                    // remove the oldest token from the prefix
                    sCurTokens.remove(0); 
                }
                
            }
            
            dCnt += 1;
            // DEBUG LINES
//            System.err.println(dPrefixSuffixCounts.asTreeMap().toString());
            //
            appendToLog(String.format("Analyzed %1.0f out of %1.0f (%5.2f%%)", dCnt, dTotal,
                    100.0 * dCnt / dTotal));
        }
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
    
    protected List<String> split(String sText) {
        return new ArrayList<>(Arrays.asList(
                sText.trim().replaceAll("[^a-zA-Z ]", " ").toLowerCase().split("\\s+")));
    }
    
    
    protected DocumentSet dsSet = null;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        LexicalChainsExtraction le = new LexicalChainsExtraction();
        // Read documents
        if (!le.loadDocuments("../data/tinyTest/"))
            return;
        
        // Analyze documents
        le.analyzeDocuments();
        
        // Now extract most probable sequences
        Distribution<ComparableList<String>> dBestSequences = 
                le.getBestSequences(2,4);
        
        // Show them
        System.out.println("Most promising sequences:\n" +
                utils.printIterable(dBestSequences.asTreeMap().entrySet(), "\n"));
        
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
     * @param lsPrefix The list of strings/tokens that make up the prefix
     * @param sSuffix The suffix to evaluate
     *@return The probability of appearence of the given string.
     */
    protected final double getProbabilityOfStringInRandomText(final List<String> lsFullText, 
            final List<String> lsPrefix, final String sSuffix) {
        double dRes = 0.0;
        
        int iPrefixCount = (int)dPrefixCounts.getValue(toHash(lsPrefix));
        int iPrefixLen = lsPrefix.size();
        int iAlphabetSize = sAlphabet.size();
        int iSuffixLen = 1;
        int iFullTextLen = lsFullText.size();
        
        // Consider that the occurence of a random suffix is a proportion of the prefix occurences,
        // as indicated by the probability of the given suffix, as a random selection process of n
        // symbols from the symbol set.
        double dFullStringCount = (double)iPrefixCount * 
                Math.pow(1.0 / iAlphabetSize, iSuffixLen);
        // Consider p(sPrefix) = N(sPrefix) / (length(Text) - length(sPrefix))
        // Again allowing overlapping n-grams
        double pPrefix = (double) iPrefixCount / 
                (iFullTextLen - (iPrefixLen - 1));
        
        List<String> lsFullString = new ArrayList<>();
        lsFullString.addAll(lsPrefix);
        lsFullString.add(sSuffix);
        int iFullStringLen = lsFullString.size();
        
        double pJoined = dFullStringCount / 
                (iFullTextLen / iFullStringLen);
        // Return p(sPrefix) * p(sPrefix, sSuffix)
        dRes = (iPrefixCount == 0) ? 0.0 : pPrefix * pJoined;
        return dRes;
    }
    
    /** Returns the probability of occurence of a given suffix, given a prefix, within the coprus
     *@param lsPrefix The prefix required.
     *@param lsSuffix The suffix for which the probability of occurence is to be calculated, given the
     * prefix.
     *@return The probability of occurence of the suffix, given the prefix.
     */
    private final double getProbabilityOfStringInText(final List<String> lsFullText,
            final List<String> lsSeq) {
        double dRes = 0.0;
        int iStringOccurences = (int) dPrefixCounts.getValue(toHash(lsSeq)); // TODO: Fix
        int iStringLen = lsSeq.size();
        int iFullTextLen = lsFullText.size();
        
        //Consider p(sPrefix) = N(sPrefix) / (length(Text) - length(sPrefix))
        // Here I allow overlapping occurences of the n-gram
        double pString = (double) iStringOccurences / 
                (iFullTextLen - (iStringLen - 1));
        
        return pString;
        
    }

    protected Integer toPrefixHash(List<String> lsToConvert) {
        return lsToConvert.subList(0, N_GRAM_SIZE).toString().hashCode();
    }

    protected Integer toHash(List<String> lsToConvert) {
        return lsToConvert.toString().hashCode();
    }

    private Distribution<ComparableList<String>> getBestSequences(int iMinLen, int iMaxLen) {
        // Avoid invalid lengths
        if (iMinLen < 2) {
            throw new ArithmeticException("Minimum length should be at least 2.");
        }
            
        Distribution<ComparableList<String>> dRes = new Distribution<>();
                
        // For every size
        for (int iCurLen = iMinLen; iCurLen <= iMaxLen; iCurLen++) {
            // Move to start of overall document
            ListIterator<String> sCurIter = lsFullText.listIterator();
            // Initialize current token list
            ComparableList<String> lsCurTokens = new ComparableList<>();

            // For all the overall document
            // TODO: Fix tomorrow!!!
            while (sCurIter.hasNext()) {
                // Get next token
                String sCur = sCurIter.next();
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
                    dRes.setValue(lsCurTokens, 
                        (getProbabilityOfStringInText(lsFullText, lsCurTokens) + 10e-5) /
                        (getProbabilityOfStringInRandomText(lsFullText, lsCurTokens, sCur) + 10e-5));
                }

            }
        }
        
        
        return dRes;
    }

}
