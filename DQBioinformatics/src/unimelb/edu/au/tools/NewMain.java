/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unimelb.edu.au.tools;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.symbol.SymbolList;
import org.biojavax.Namespace;
import org.biojavax.RichObjectFactory;
import org.biojavax.SimpleRankedDocRef;
import org.biojavax.bio.db.RichSequenceDB;
import org.biojavax.bio.db.ncbi.GenbankRichSequenceDB;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;
import org.biojavax.ontology.SimpleComparableTerm;
import unimelb.edu.au.lucene.analysis.en.MyEnglishAnalyzer;
import unimelb.edu.au.lucene.analysis.en.PubMedSpecialWords;

/**
 *
 * @author mbouadjenek
 */
public class NewMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // TODO code application logic here

//        MyEnglishAnalyzer a = new MyEnglishAnalyzer(PubMedStopWords.DEFINITION_STOP_WORDS_SET);
//        TokenStream ts = a.tokenStream("", "this is a test of a new method developed by reda cds");
//        CharTermAttribute charTermAttribute = ts.addAttribute(CharTermAttribute.class);
//        ts.reset();
//        String t;
//        while (ts.incrementToken()) {
//            t = charTermAttribute.toString();
//            System.out.println(t);
//        }
//        System.out.println(EnglishAnalyzer.getDefaultStopSet());
//
        RichSequenceDB rsDB;
        RichSequence rs;

        Set<String> l = new HashSet<>();
//        l.add("CP007004");
        l.add("AB008754");

        GenbankRichSequenceDB grsdb = new GenbankRichSequenceDB();
        try {
            // Demonstration of use with GenBank accession number

            Namespace ns = RichObjectFactory.getDefaultNamespace();
//            rsDB = grsdb.getRichSequences(l);
//            RichSequenceIterator it =rsDB.getRichSequenceIterator();
//            while (it.hasNext()) {
//                 rs=it.nextRichSequence();
//                 RichSequence.IOTools.writeGenbank(System.out, rs, ns);
//                 System.out.println(rs.getAccession());
//                 System.out.println("***********");
//            }
            rs = grsdb.getRichSequence("AF196567");
//            System.out.println("************************************************");
//            System.out.println(rs.getSource().getSourceName());
//            System.out.println(rs.getSource().getOrganismName());
//            System.out.println("************************************************");
//            
//            System.out.println(rs.getTaxon().getNCBITaxID());
            
            
            
//            grsdb.writeFastRichSequence("AB361052", new PrintWriter(System.out));
            
//            RichSequence.IOTools.writeGenbank(System.out, rs, ns);

//            System.out.println(rs.getName() + " | " + rs.getDescription());
//            System.out.println(rs.getDescription().toLowerCase());
//            System.out.println(rs.getSource().sizeLineage());
//            System.out.println("*"+rs.getSource().getSourceName());
//            for (int i = 0; i < rs.getSource().sizeLineage(); i++) {
//                System.out.println(rs.getSource().getLineage(i));
//            }

            //**************** REFERENCES****************
            Iterator it = rs.getRankedDocRefs().iterator();
//
            while (it.hasNext()) {
                SimpleRankedDocRef s = (SimpleRankedDocRef) it.next();
                System.out.println("Rank: " + s.getRank());
                System.out.println("Authors: " + s.getDocumentReference().getAuthors());
                System.out.println("Title: " + s.getDocumentReference().getTitle());
                System.out.println("Journal: " + s.getDocumentReference().getLocation());
                if (s.getDocumentReference().getCrossref() != null) {
                    System.out.println(s.getDocumentReference().getCrossref().getDbname() + ": " + s.getDocumentReference().getCrossref().getAccession());

                }
                System.out.println("------------");
            }
//            ********************************************
//            System.out.println("**************************");
//            System.out.println("*********SOURCES**********");
//            System.out.println("**************************");
//            System.out.println("------------");
//            for (Feature f : rs.getFeatureSet()) {
//                System.out.println("Type: " + f.getType()+"\t"+f.getLocation().toString());
//
//                Map<SimpleComparableTerm, String> m = f.getAnnotation().asMap();
////                System.out.println("m = " + m);
//                for (SimpleComparableTerm key : m.keySet()) {
////                    System.out.println(key.getName());
//                    System.out.println("Key = " + key.toString().replace("biojavax:", "") + " -> value = " + m.get(key));
//                }
//
//                System.out.println("------------");
//            }
//
//            SymbolList sl = rs.getInternalSymbolList();
//            System.out.println(sl.seqString());
        } catch (Exception be) {
            be.printStackTrace();
        }
    }

}
