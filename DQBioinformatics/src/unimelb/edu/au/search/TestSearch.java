/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unimelb.edu.au.search;

import java.io.File;
import java.io.IOException;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.biojavax.bio.seq.io.GenbankFormat;
import unimelb.edu.au.doc.PubMedDoc;
import unimelb.edu.au.lucene.analysis.en.MyEnglishAnalyzer;
import unimelb.edu.au.lucene.analysis.en.PubMedSpecialWords;
import unimelb.edu.au.search.similarities.MySimilarity;
import unimelb.edu.au.util.Functions;

/**
 *
 * @author mbouadjenek
 */
public class TestSearch {

    private final IndexSearcher is;
    private final int topK;

    public TestSearch(String indexDir, int topK) throws IOException {
        Directory dir = FSDirectory.open(new File(indexDir).toPath());
        is = new IndexSearcher(DirectoryReader.open(dir));
        is.setSimilarity(new MySimilarity());
        this.topK = topK;
//        LeafReader a = (LeafReader) is.getIndexReader();
//        int i = 1;
//        for (LeafReaderContext lrc : is.getIndexReader().leaves()) {
//            System.out.println(i + "- DEFINITION_TAG: " + lrc.reader().terms(GenbankFormat.DEFINITION_TAG).size());
//
//            System.out.println(i + "- ACCESSION_TAG: " + lrc.reader().terms(GenbankFormat.ACCESSION_TAG).size());
//            i++;
//        }

//        System.out.println("Size: " + a.terms(PubMedDoc.BODY).size());
    }

    public void search(String disease, String gene) throws Exception {
        QueryParser parser = new QueryParser(PubMedDoc.ABSTRACT, new MyEnglishAnalyzer(PubMedSpecialWords.ENGLISH_STOP_WORDS_SET));
        Query query = parser.parse(disease);
        System.out.println(query);
        long start = System.currentTimeMillis();
        TopDocs hits = is.search(query, topK);
        long end = System.currentTimeMillis();
        System.err.println("Found " + hits.totalHits
                + " document(s) has matched query. Processed in " + Functions.getTimer(end - start) + ".");

        int i = 0;
        for (ScoreDoc scoreDoc : hits.scoreDocs) {
            i++;
//            System.out.println(new PatentQuery(patent, titleBoost, abstractBoost, descriptionBoost, descriptionP5Boost, claimsBoost, claims1Boost, filter, stopWords).parse().toString());
//            Explanation explanation = is.explain(query, scoreDoc.doc);
            Document doc = is.doc(scoreDoc.doc);
            System.out.println(i + "\t" + doc.get(PubMedDoc.ID_PMC) + "\t" + scoreDoc.score);
//            System.out.println(explanation.toString());

        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        String indexDir;
        String disease;
        String gene;
        if (args.length == 0) {
            indexDir = "/Volumes/Macintosh HD/Users/mbouadjenek/Documents/bioinformatics_data/index_PubMedCentral_v10.0/";
            disease = "Wolfram syndrome";
            gene = "WFS1";
        } else {
            indexDir = args[0];
            disease = args[1];
            gene = args[2];
        }
        try {
            TestSearch searcher = new TestSearch(indexDir, 10);
            searcher.search(disease, gene);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
