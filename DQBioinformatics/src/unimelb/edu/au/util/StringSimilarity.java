/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unimelb.edu.au.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import org.apache.lucene.analysis.Analyzer;

/**
 *
 * @author mbouadjenek
 */
public class StringSimilarity {

    public static String OVERLAP = "overlap";

    public static String JACCARD = "jaccard";

    public static String DICE = "dice";

    public static String MATCHING = "matching";
    
    public static String COSINE = "cosine";

    /**
     * The overlap coefficient (or, Szymkiewicz-Simpson coefficient) is a
     * similarity measure related to the Jaccard index that measures the overlap
     * between two sets, and is defined as the size of the intersection divided
     * by the smaller of the size of the two sets: Overlap(X,Y)= (X∩Y)/Min(X,Y)
     *
     * @param string1
     * @param string2
     * @param analyzer
     * @return The Overlap similarity
     */
    public static float getOverlapSimilarity(final String string1, final String string2, Analyzer analyzer) {
        final ArrayList<String> str1Tokens = tokenizeToArrayList(string1, analyzer);
        final Set<String> firstStringTokens = new HashSet<>();
        firstStringTokens.addAll(str1Tokens);
        final ArrayList<String> str2Tokens = tokenizeToArrayList(string2, analyzer);
        final Set<String> secondStringTokens = new HashSet<>();
        secondStringTokens.addAll(str2Tokens);
        return getOverlapSimilarity(firstStringTokens, secondStringTokens);
    }

    /**
     * The overlap coefficient (or, Szymkiewicz-Simpson coefficient) is a
     * similarity measure related to the Jaccard index that measures the overlap
     * between two sets, and is defined as the size of the intersection divided
     * by the smaller of the size of the two sets: Overlap(X,Y)= (X∩Y)/Min(X,Y)
     *
     * @param firstStringTokens
     * @param secondStringTokens
     * @return The Overlap similarity
     */
    public static float getOverlapSimilarity(final Set<String> firstStringTokens, final Set<String> secondStringTokens) {
        if (firstStringTokens.isEmpty() || secondStringTokens.isEmpty()) {
            return 0;
        }
        final int termsInString1 = firstStringTokens.size();
        final int termsInString2 = secondStringTokens.size();
        final Set<String> allTokens = new HashSet<>();
        allTokens.addAll(firstStringTokens);
        allTokens.addAll(secondStringTokens);
        final int commonTerms = (termsInString1 + termsInString2) - allTokens.size();
        return (float) (commonTerms) / (float) (Math.min(firstStringTokens.size(), secondStringTokens.size()));
    }

    /**
     * The Jaccard index, also known as the Jaccard similarity coefficient, is a
     * statistic used for comparing the similarity and diversity of sample sets.
     * The Jaccard coefficient measures similarity between finite sample sets,
     * and is defined as the size of the intersection divided by the size of the
     * union of the sample sets: Jaccard(X,Y)= |X∩Y|/|X|+|Y|-|X∩Y|
     *
     * @param string1
     * @param string2
     * @param analyzer
     * @return The Jaccard similarity
     */
    public static float getJaccardSimilarity(final String string1, final String string2, Analyzer analyzer) {
        final ArrayList<String> str1Tokens = tokenizeToArrayList(string1, analyzer);
        final ArrayList<String> str2Tokens = tokenizeToArrayList(string2, analyzer);
        final Set<String> firstStringTokens = new HashSet<>();
        firstStringTokens.addAll(str1Tokens);
        final Set<String> secondStringTokens = new HashSet<>();
        secondStringTokens.addAll(str2Tokens);
        return getJaccardSimilarity(firstStringTokens, secondStringTokens);
    }

    /**
     * The Jaccard index, also known as the Jaccard similarity coefficient, is a
     * statistic used for comparing the similarity and diversity of sample sets.
     * The Jaccard coefficient measures similarity between finite sample sets,
     * and is defined as the size of the intersection divided by the size of the
     * union of the sample sets: Jaccard(X,Y)= |X∩Y|/|X|+|Y|-|X∩Y|
     *
     * @param firstStringTokens
     * @param secondStringTokens
     * @return The Jaccard similarity
     */
    public static float getJaccardSimilarity(final Set<String> firstStringTokens, final Set<String> secondStringTokens) {
        if (firstStringTokens.isEmpty() || secondStringTokens.isEmpty()) {
            return 0;
        }
        final int termsInString1 = firstStringTokens.size();
        final int termsInString2 = secondStringTokens.size();
        final Set<String> allTokens = new HashSet<>();
        allTokens.addAll(firstStringTokens);
        allTokens.addAll(secondStringTokens);
        final int commonTerms = (termsInString1 + termsInString2) - allTokens.size();
        return (float) (commonTerms) / (float) (allTokens.size());
    }

    /**
     * The Sørensen–Dice index, also known by other names (see Names, below), is
     * a statistic used for comparing the similarity of two samples. It was
     * independently developed by the botanists Thorvald Sørensen[1] and Lee
     * Raymond Dice,[2] who published in 1948 and 1945 respectively. This dice
     * is computed as: Dice(X,Y)= 2*|X∩Y|/|X|+|Y|
     *
     * @param string1
     * @param string2
     * @param analyzer
     * @return The Jaccard similarity
     */
    public static float getDiceSimilarity(final String string1, final String string2, Analyzer analyzer) {
        final ArrayList<String> str1Tokens = tokenizeToArrayList(string1, analyzer);
        final ArrayList<String> str2Tokens = tokenizeToArrayList(string2, analyzer);
        final Set<String> firstStringTokens = new HashSet<>();
        firstStringTokens.addAll(str1Tokens);
        final Set<String> secondStringTokens = new HashSet<>();
        secondStringTokens.addAll(str2Tokens);
        return getDiceSimilarity(firstStringTokens, secondStringTokens);
    }

    /**
     * The Sørensen–Dice index, also known by other names (see Names, below), is
     * a statistic used for comparing the similarity of two samples. It was
     * independently developed by the botanists Thorvald Sørensen[1] and Lee
     * Raymond Dice,[2] who published in 1948 and 1945 respectively. This dice
     * is computed as: Dice(X,Y)= 2*|X∩Y|/|X|+|Y|
     *
     * @param firstStringTokens
     * @param secondStringTokens
     * @return The Jaccard similarity
     */
    public static float getDiceSimilarity(final Set<String> firstStringTokens, final Set<String> secondStringTokens) {
        if (firstStringTokens.isEmpty() || secondStringTokens.isEmpty()) {
            return 0;
        }
        final int termsInString1 = firstStringTokens.size();
        final int termsInString2 = secondStringTokens.size();
        final Set<String> allTokens = new HashSet<>();
        allTokens.addAll(firstStringTokens);
        allTokens.addAll(secondStringTokens);
        final int commonTerms = (termsInString1 + termsInString2) - allTokens.size();
        return (float) (commonTerms) / (float) (termsInString1 + termsInString2);
    }

    /**
     * This matching similarity is computed as: matching(X,Y)= |X∩Y|
     *
     * @param string1
     * @param string2
     * @param analyzer
     * @return The Jaccard similarity
     */
    public static float getMatchingSimilarity(final String string1, final String string2, Analyzer analyzer) {
        final ArrayList<String> str1Tokens = tokenizeToArrayList(string1, analyzer);
        final ArrayList<String> str2Tokens = tokenizeToArrayList(string2, analyzer);
        final Set<String> firstStringTokens = new HashSet<>();
        firstStringTokens.addAll(str1Tokens);
        final Set<String> secondStringTokens = new HashSet<>();
        secondStringTokens.addAll(str2Tokens);
        return getMatchingSimilarity(firstStringTokens, secondStringTokens);
    }

    /**
     * This matching similarity is computed as: matching(X,Y)= |X∩Y|
     *
     * @param firstStringTokens
     * @param secondStringTokens
     * @return The Jaccard similarity
     */
    public static float getMatchingSimilarity(final Set<String> firstStringTokens, final Set<String> secondStringTokens) {
        if (firstStringTokens.isEmpty() || secondStringTokens.isEmpty()) {
            return 0;
        }
        final int termsInString1 = firstStringTokens.size();
        final int termsInString2 = secondStringTokens.size();
        final Set<String> allTokens = new HashSet<>();
        allTokens.addAll(firstStringTokens);
        allTokens.addAll(secondStringTokens);
        final int commonTerms = (termsInString1 + termsInString2) - allTokens.size();
        return (float) (commonTerms);
    }

    /**
     * This cosine similarity is computed as: cosine(X,Y)= |X∩Y|/sqrt(|X|*|Y|)
     *
     * @param string1
     * @param string2
     * @param analyzer
     * @return The Jaccard similarity
     */
    public static float getCosineSimilarity(final String string1, final String string2, Analyzer analyzer) {
        final ArrayList<String> str1Tokens = tokenizeToArrayList(string1, analyzer);
        final ArrayList<String> str2Tokens = tokenizeToArrayList(string2, analyzer);
        final Set<String> firstStringTokens = new HashSet<>();
        firstStringTokens.addAll(str1Tokens);
        final Set<String> secondStringTokens = new HashSet<>();
        secondStringTokens.addAll(str2Tokens);
        return getCosineSimilarity(firstStringTokens, secondStringTokens);
    }

    /**
     * This cosine similarity is computed as: cosine(X,Y)= |X∩Y|/sqrt(|X|*|Y|)
     *
     * @param firstStringTokens
     * @param secondStringTokens
     * @return The Jaccard similarity
     */
    public static float getCosineSimilarity(final Set<String> firstStringTokens, final Set<String> secondStringTokens) {
        if (firstStringTokens.isEmpty() || secondStringTokens.isEmpty()) {
            return 0;
        }
        final int termsInString1 = firstStringTokens.size();
        final int termsInString2 = secondStringTokens.size();
        final Set<String> allTokens = new HashSet<>();
        allTokens.addAll(firstStringTokens);
        allTokens.addAll(secondStringTokens);
        final int commonTerms = (termsInString1 + termsInString2) - allTokens.size();
        return (float) (commonTerms) / (float) (Math.sqrt(termsInString1 * termsInString2));
    }

    public static ArrayList<String> tokenizeToArrayList(final String input, Analyzer analyzer) {
        final ArrayList<String> returnVect = new ArrayList<>();
        String str = Functions.processString(input, analyzer);
        StringTokenizer st = new StringTokenizer(str, " ");
        while (st.hasMoreTokens()) {
            returnVect.add(st.nextToken());
        }
        return returnVect;
    }

}
