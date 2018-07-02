/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unimelb.edu.au.search;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import unimelb.edu.au.doc.MedlineDoc;
import unimelb.edu.au.doc.MutualReferences;
import unimelb.edu.au.doc.PubMedDoc;
import unimelb.edu.au.lucene.analysis.en.MyEnglishAnalyzer;
import unimelb.edu.au.lucene.analysis.en.PubMedSpecialWords;
import unimelb.edu.au.search.similarities.MySimilarity;
import unimelb.edu.au.util.Functions;

/**
 *
 * @author mbouadjenek
 */
public class SearchTREC {

    private final IndexSearcher is;
    private final int topK;
    protected File queries;

    public SearchTREC(String indexDir, String queries, int topK) throws IOException {
        Directory dir = FSDirectory.open(new File(indexDir).toPath());
        is = new IndexSearcher(DirectoryReader.open(dir));
        this.queries = new File(queries);
//        is.setSimilarity(new MySimilarity());
        this.topK = topK;
    }

    public void search() throws Exception {
        FileInputStream fstream;
        int i = 0;
        fstream = new FileInputStream(queries);
        // Get the object of DataInputStream
        DataInputStream in = new DataInputStream(fstream);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            String str;
            while ((str = br.readLine()) != null) {

                if (str.startsWith("#")) {
                    continue;
                }
                if (str.trim().length() == 0) {
                    continue;
                }
                i++;
                if (i <= 819) {
                    continue;
                }

                StringTokenizer st = new StringTokenizer(str, "\t");
                String queryid = st.nextToken();
                String gene = st.nextToken().toLowerCase();
                String diseaseid = st.nextToken().toLowerCase();
                String diseasename = st.nextToken().toLowerCase();
                BooleanQuery.Builder builder = new BooleanQuery.Builder();
//                QueryParser parser = new QueryParser(MedlineDoc.ABSTRACT, new MyEnglishAnalyzer(PubMedSpecialWords.ENGLISH_STOP_WORDS_SET));
                QueryParser parser = new QueryParser(PubMedDoc.BODY, new MyEnglishAnalyzer(PubMedSpecialWords.ENGLISH_STOP_WORDS_SET));
                Query query = parser.parse(QueryParser.escape(diseasename));
                builder.add(query, BooleanClause.Occur.MUST);
//                FuzzyQuery fq=new FuzzyQuery(term)
//                builder.add(new FuzzyQuery(new Term(MedlineDoc.ABSTRACT, gene.toLowerCase())), BooleanClause.Occur.MUST);
                builder.add(new FuzzyQuery(new Term(PubMedDoc.BODY, gene.toLowerCase())), BooleanClause.Occur.MUST);
                System.err.println(builder.build());
                long start = System.currentTimeMillis();
                TopDocs hits = is.search(builder.build(), topK);
                long end = System.currentTimeMillis();
                System.err.println(i + "- Found " + hits.totalHits
                        + " document(s) has matched query. Processed in " + Functions.getTimer(end - start) + ".");

                int j = 0;
                for (ScoreDoc scoreDoc : hits.scoreDocs) {
                    j++;
//            System.out.println(new PatentQuery(patent, titleBoost, abstractBoost, descriptionBoost, descriptionP5Boost, claimsBoost, claims1Boost, filter, stopWords).parse().toString());
//            Explanation explanation = is.explain(query, scoreDoc.doc);
                    Document doc = is.doc(scoreDoc.doc);
//                    System.out.println(queryid + "\tQ0\t" + doc.get(MedlineDoc.ID_PMID).toUpperCase() + "\t" + j + "\t" + scoreDoc.score + "\tSTANDARD");
                    System.out.println(queryid + "\tQ0\t" + doc.get(PubMedDoc.ID_PMC).toUpperCase() + "\t" + j + "\t" + scoreDoc.score + "\tSTANDARD");

//            System.out.println(explanation.toString());
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(MutualReferences.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        String indexDir;
        String queries;
        int topk;
        if (args.length == 0) {
//            indexDir = "/Volumes/Macintosh HD/Users/mbouadjenek/Documents/bioFactChecking/medline_index_v1.0/";
            indexDir = "/Volumes/Macintosh HD/Users/mbouadjenek/Documents/bioinformatics_data/index_PubMedCentral_v10.0/";
            queries = "/Volumes/Macintosh HD/Users/mbouadjenek/Documents/bioFactChecking/queries.txt";
            topk = 1000;
        } else {
            indexDir = args[0];
            queries = args[1];
            topk = Integer.parseInt(args[2]);
        }
        try {
            SearchTREC searcher = new SearchTREC(indexDir, queries, topk);
            searcher.search();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
