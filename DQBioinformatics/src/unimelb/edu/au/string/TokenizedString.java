/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unimelb.edu.au.string;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import unimelb.edu.au.doc.PubMedDoc;
import unimelb.edu.au.util.Functions;
import unimelb.edu.au.util.Statistics;

/**
 *
 * @author mbouadjenek
 */
public class TokenizedString {

    /*The original text*/
    String original_text;

    /*The tokenized text*/
    String tokenized_text = "";

    /*This structure stores the offsets of words of the original text*/
    int[] offsets;

    /*Separations*/
    String sep = "\n|.|,|;|:|-| |(|)|'|\"|‘|’|<|>|?|/|{|}|[|]|=|+|%|!|~|&|*|#|@|≥";

    public TokenizedString(String text) {
        this.original_text = text.toLowerCase();
        String tmp = Functions.processString(text, Functions.analyzer);
        StringTokenizer st = new StringTokenizer(tmp, sep);
        List<Integer> offsets_ = new ArrayList<>();
        while (st.hasMoreTokens()) {
            offsets_.add(tokenized_text.length());
            tokenized_text += st.nextToken();
        }
//        offsets_.add(tokenized_text.length());
        offsets = offsets_.stream().mapToInt(i -> i).toArray();
//        System.out.println(tokenized_text);
    }

    /**
     * Returns true if and only if this string contains the specified sequence
     * of char values.
     *
     * @param s the sequence to search for
     * @return true if this string contains s, false otherwise
     */
    public boolean contains(CharSequence s) {
        String sub = "";
        String tmp = Functions.processString(s.toString(), Functions.analyzer);
        StringTokenizer st = new StringTokenizer(tmp, sep);
        while (st.hasMoreTokens()) {
            sub += st.nextToken();
        }
        for (int i = 0; i < offsets.length - 1; i++) {
            String word = tokenized_text.substring(offsets[i]);
            if (word.startsWith(sub)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the index within this string of the first occurrence of the
     * specified substring. The returned index is the smallest value k for
     * which:
     *
     * this.startsWith(str, k)
     *
     * If no such value of k exists, then -1 is returned.
     *
     * @param str the substring to search for.
     * @return the index of the first occurrence of the specified substring, or
     * -1 if there is no such occurrence.
     */
    public int indexOf(String str) {
        String sub = "";
        String tmp = Functions.processString(str, Functions.analyzer);
        StringTokenizer st = new StringTokenizer(tmp, sep);
        while (st.hasMoreTokens()) {
            sub += st.nextToken();
        }
        for (int i = 0; i < offsets.length; i++) {
            String word = tokenized_text.substring(offsets[i]);
            if (word.startsWith(sub)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the indexes within this string of all occurrences of the
     * specified substring. If no such substring exists, then return empty set.
     *
     * @param str the substring to search for.
     * @return the array of all indexes of the occurrences of the specified
     * substring, or empty array if there is no such occurrence.
     */
    public int[] indexesOf(String str) {
        String sub = "";
        List<Integer> offsets_ = new ArrayList<>();
        String tmp = Functions.processString(str, Functions.analyzer);
        StringTokenizer st = new StringTokenizer(tmp, sep);
        while (st.hasMoreTokens()) {
            sub += st.nextToken();
        }
        for (int i = 0; i < offsets.length ; i++) {
            String word = tokenized_text.substring(offsets[i]);
            if (word.startsWith(sub)) {
                offsets_.add(i);
            }
        }
        return offsets_.stream().mapToInt(i -> i).toArray();
    }

    /**
     * Returns the
     *
     * @param str1
     * @param str2
     * @return
     */
    public int[][] distanceMatrix(String str1, String str2) {
        int[] occ1 = indexesOf(str1);
        int[] occ2 = indexesOf(str2);
        int[][] out = new int[occ1.length][occ2.length];
        for (int i = 0; i < occ1.length; i++) {
            for (int j = 0; j < occ2.length; j++) {
                out[i][j] = Math.abs(occ1[i] - occ2[j]);
            }
        }
        return out;
    }

    /**
     * Returns the
     *
     * @param str1
     * @param str2
     * @return
     */
    public double[] distanceArray(String str1, String str2) {
        int[] occ1 = indexesOf(str1);
        int[] occ2 = indexesOf(str2);
        double[] out = new double[occ1.length * occ2.length];
        for (int i = 0; i < occ1.length; i++) {
            for (int j = 0; j < occ2.length; j++) {
                out[(j * occ1.length) + i] = Math.abs(occ1[i] - occ2[j]);
            }
        }
        return out;
    }

    public List<Map<String, Double>> getContext(String str, int size) {
        int[] occ = indexesOf(str);
        return null;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        String dir_articles = "/Users/mbouadjenek/Documents/bioinformatics_data/PubMed_Central/dataset/";

        PubMedDoc pmdoc = new PubMedDoc(PubMedDoc.getAbsoluteFile("pmc4566133", dir_articles));
        TokenizedString n = new TokenizedString(pmdoc.getBody());
        String gene = "wfs1";
        String org = "wolfram syndrome";

        int[] indx = n.indexesOf(org);
        for (int j = 0; j < indx.length; j++) {
            System.out.print(indx[j] + ", ");
        }
        System.out.println("");
        indx = n.indexesOf(gene);
        for (int j = 0; j < indx.length; j++) {
            System.out.print(indx[j] + ", ");
        }
        System.out.println("");
        System.out.println("***************************");
        int[][] distMat = n.distanceMatrix(org, gene);
        for (int i = 0; i < distMat.length; i++) {
            for (int j = 0; j < distMat[i].length; j++) {
                System.out.print(distMat[i][j] + "\t");
            }
            System.out.println("");
        }
        System.out.println("-------");
        double[] distArray = n.distanceArray(gene, org);
        for (int j = 0; j < distArray.length; j++) {
            System.out.print(distArray[j] + ", ");
        }
        System.out.println("");
        System.out.println("-------");
        System.out.println(Statistics.getArithmeticMean((double[]) distArray));
//        System.out.println(Statistics.getCoefficientVariation((double[])distArray));
//        System.out.println(Statistics.getGeometricMean((double[])distArray));
//        System.out.println(Statistics.getHarmonicMean((double[])distArray));
        System.out.println(Statistics.getMaxArray((double[]) distArray));
//        System.out.println(Statistics.getMedian((double[])distArray));
        System.out.println(Statistics.getMinArray((double[]) distArray));
//        System.out.println(Statistics.getStdDev((double[])distArray));
//        System.out.println(Statistics.getSumArray((double[])distArray));        
//        System.out.println(Statistics.getVariance((double[])distArray));
    }

}
