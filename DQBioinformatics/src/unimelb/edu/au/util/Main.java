/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unimelb.edu.au.util;

import java.io.IOException;
import java.text.BreakIterator;
import java.util.Locale;
import java.util.Map;
import linnaeus.Linnaeus;
import unimelb.edu.au.doc.PubMedDoc;
import unimelb.edu.au.lucene.analysis.en.PubMedSpecialWords;

/**
 *
 * @author mbouadjenek
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        String dir_articles = "/Users/mbouadjenek/Documents/bioinformatics_data/PubMed_Central/dataset/";
        PubMedDoc pmdoc = new PubMedDoc(PubMedDoc.getAbsoluteFile("PMC56901", dir_articles));
//        Linnaeus l = new Linnaeus();
//        Map<String, String> orgs = l.getOrganisms(pmdoc.getBody());
//        for (String o : orgs.keySet()) {
//            System.out.println(o + " -> " + orgs.get(o));
//        }

        BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.US);
        String source = "Sp100 is a nuclear protein that displays a number of alternative splice variants. "
                + "In old world monkeys, apes and humans one of these variants is extended by a retroprocessed pseudogene, hmg1l3, whose antecedent gene is a member of the family of high-mobility-group proteins, hmg1. "
                + "This is one of only a few documented cases of a retropseudogene being incorporated into another gene as a functional exon. "
                + "In addition to the hmg1l3 insertion, old world monkey genomes also contain an alu sequence within the last sp100-hmg intron.";
        iterator.setText(source);

        int start = iterator.first();
        for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
            System.out.println(source.substring(start, end));
        }

    }

}
