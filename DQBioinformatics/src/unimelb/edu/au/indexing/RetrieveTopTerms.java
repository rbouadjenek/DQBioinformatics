/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unimelb.edu.au.indexing;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.misc.HighFreqTerms;
import org.apache.lucene.misc.TermStats;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import unimelb.edu.au.doc.PubMedDoc;
import unimelb.edu.au.util.Functions;

/**
 *
 * @author mbouadjenek
 */
public class RetrieveTopTerms {

    IndexReader ir;
    DecimalFormat df = new DecimalFormat("#.####");

    public RetrieveTopTerms(File INDEXES_DIR) throws IOException {
        Directory dir = FSDirectory.open(INDEXES_DIR.toPath());
        ir = new IndexSearcher(DirectoryReader.open(dir)).getIndexReader();
    }

    public RetrieveTopTerms(String INDEXES_DIR) throws IOException {
        Directory dir = FSDirectory.open(new File(INDEXES_DIR).toPath());
        ir = new IndexSearcher(DirectoryReader.open(dir)).getIndexReader();

//        Fields fields = MultiFields.getFields(ir);
//        Terms terms = fields.terms("main");
//        TermsEnum iterator = terms.iterator();
//        System.out.println("Vocabulary size: "+terms.size());
    }

    public void showTopTerms(int k) throws IOException, Exception {
        System.out.println(ir.numDocs());
//        System.out.println(ir.);
//        System.out.println("-------------------------------------");
//        System.out.println("TOP TERMS in Title (" + "Total doc= " + ir.getDocCount(PubMedDoc.TITLE) + ")");
//        System.out.println("-------------------------------------");
        TermStats[] listTermStats = HighFreqTerms.getHighFreqTerms(ir, k, PubMedDoc.TITLE, new HighFreqTerms.DocFreqComparator());
//        int i = 1;
//        for (TermStats termStats : listTermStats) {
//            System.out.println(i + "\t" + termStats.termtext.utf8ToString() + "\t" + Math.log10((double) ir.getDocCount(PubMedDoc.TITLE) / termStats.docFreq));
//            i++;
////            System.out.println(termStats.termtext.utf8ToString() + " -> DocFreq: " + termStats.docFreq + ", idf= " + Math.log10((double) ir.getDocCount(PubMedDoc.TITLE) / termStats.docFreq));
//        }
//        System.out.println("-------------------------------------");
//        System.out.println("TOP TERMS in Abstract (" + "Total doc= " + ir.getDocCount(PubMedDoc.ABSTRACT) + ")");
//        System.out.println("-------------------------------------");
//        listTermStats = HighFreqTerms.getHighFreqTerms(ir, k, PubMedDoc.ABSTRACT, new HighFreqTerms.DocFreqComparator());
//        for (TermStats termStats : listTermStats) {
//            System.out.println(i + "\t" + termStats.termtext.utf8ToString() + "\t" + Math.log10((double) ir.getDocCount(PubMedDoc.ABSTRACT) / termStats.docFreq));
//            i++;
////            System.out.println(termStats.termtext.utf8ToString() + " -> DocFreq: " + termStats.docFreq + ", idf= " + Math.log10((double) ir.getDocCount(PubMedDoc.ABSTRACT) / termStats.docFreq));
//        }
//        System.out.println("-------------------------------------");
//        System.out.println("TOP TERMS in Body (" + "Total doc= " + ir.getDocCount("main") + ")");
//        System.out.println("-------------------------------------");
//        TermStats[] listTermStats = HighFreqTerms.getHighFreqTerms(ir, k, "body", new HighFreqTerms.DocFreqComparator());
//        System.out.println("Rank\tTerm\tDocFreq\tidf");
//        System.out.println("-----------------------------");
//        for (TermStats termStats : listTermStats) {
//
//            System.out.println(i + "\t" + termStats.termtext.utf8ToString() + "\t" + termStats.docFreq + "\t" + df.format(Math.log10((double) ir.getDocCount("body") / termStats.docFreq)));
//
////            System.out.print("\"" + termStats.termtext.utf8ToString() + "\",");
////            if (i % 10 == 0) {
////                System.out.println("");
////            }
////            System.out.println(i + "\t" + termStats.termtext.utf8ToString() + "\t" + termStats.docFreq);
//            i++;
////            System.out.println(termStats.termtext.utf8ToString() + " -> DocFreq: " + termStats.docFreq + ", idf= " + Math.log10((double) ir.getDocCount(PubMedDoc.BODY) / termStats.docFreq));
//        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here  
//        System.out.println(StopAnalyzer.ENGLISH_STOP_WORDS_SET);
        int k;
        if (args.length == 0) {
            k = 100;
        } else {
            k = Integer.parseInt(args[0]);
        }
        try {
//            RetrieveTopTerms top = new RetrieveTopTerms("/Volumes/Macintosh HD/Users/mbouadjenek/Documents/bioinformatics_data/index_Definitions_v2.0/");
            RetrieveTopTerms top = new RetrieveTopTerms("/Volumes/Macintosh HD/Users/mbouadjenek/Documents/bioinformatics_data/index_PubMedCentral_v10.0/");
            top.showTopTerms(k);
        } catch (Exception ex) {
            Logger.getLogger(RetrieveTopTerms.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
