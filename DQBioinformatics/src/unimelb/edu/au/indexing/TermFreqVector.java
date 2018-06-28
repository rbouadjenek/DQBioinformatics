/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unimelb.edu.au.indexing;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;

/**
 * This class provide a support to represent a document as vector of unique
 * terms and their frequencies.
 *
 * @author mbouadjenek
 */
public class TermFreqVector {

    /*
     * The vector representatation.
     */
    private final Map<String, Integer> TermFreqVector;

    public TermFreqVector(Terms terms) throws IOException {
        TermFreqVector = new HashMap<>();
        if (terms != null && terms.size() > 0) {
            TermsEnum termsEnum = terms.iterator(); // access the terms for this field
            BytesRef term;
            while ((term = termsEnum.next()) != null) {// explore the terms for this field                
                PostingsEnum p = termsEnum.postings(null);
                p.nextDoc();
                TermFreqVector.put(term.utf8ToString(), p.freq());
            }
        }
    }

    /**
     * Return the number of distinct terms.
     *
     * @return
     */
    public int size() {
        return TermFreqVector.size();
    }

    /**
     * This method return the frequency of a term in this vector representation.
     *
     * @param term
     * @return The frequency of the term.
     */
    public Integer getFreq(String term) {
        try {
            EnglishAnalyzer e = new EnglishAnalyzer();
            TokenStream ts = e.tokenStream("", term);
            CharTermAttribute charTermAttribute = ts.addAttribute(CharTermAttribute.class);
            ts.reset();
            String t = null;
            while (ts.incrementToken()) {
                t = charTermAttribute.toString().replace(":", "\\:");
            }
            if (TermFreqVector.containsKey(t)) {
                return TermFreqVector.get(t);
            } else {
                return 0;
            }
        } catch (IOException ex) {
            return null;
        }
    }

    /**
     * This method computes the dot product between this vector representation
     * and the map given.
     *
     * @param vec The map which is used to compute the dot product.
     * @return The dot product value.
     */
    public double getDotProduct(Map<String, Integer> vec) {
        double dot = 0;
        dot = vec.keySet().stream().filter((t) -> (TermFreqVector.containsKey(t))).map((t) -> TermFreqVector.get(t) * Math.pow(vec.get(t), 2)).reduce(dot, (accumulator, _item) -> accumulator + _item);
        return dot;
    }

    /**
     * This method return the list of unique terms.
     *
     * @return The list of unique terms.
     */
    public Set<String> getTerms() {
        return TermFreqVector.keySet();
    }

    public Collection<Integer> termFreqs() {
        return TermFreqVector.values();
    }

    /**
     * Return the total number of terms in this vector representation.
     *
     * @return
     */
    public int numberOfTerms() {
        if (TermFreqVector.isEmpty()) {
            return 0;
        }
        int sum = 0;
        sum = TermFreqVector.values().stream().map((i) -> i).reduce(sum, Integer::sum);
        return sum;
    }
}
