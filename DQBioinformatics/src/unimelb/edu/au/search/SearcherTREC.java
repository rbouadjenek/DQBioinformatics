/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unimelb.edu.au.search;

import unimelb.edu.au.search.similarities.MySimilarity;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.biojavax.Namespace;
import org.biojavax.RichObjectFactory;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;
import org.biojavax.bio.seq.io.GenbankFormat;
import org.biojavax.bio.taxa.NCBITaxon;
import unimelb.edu.au.lucene.analysis.en.MyEnglishAnalyzer;
import unimelb.edu.au.doc.PubMedDoc;
import unimelb.edu.au.lucene.analysis.en.PubMedSpecialWords;
import unimelb.edu.au.doc.MutualReferences;
import unimelb.edu.au.indexing.TermFreqVector;
import unimelb.edu.au.util.Functions;

/**
 *
 * @author mbouadjenek
 */
public class SearcherTREC {

    private final IndexSearcher is;
    protected File queries;
    private final String field;
    Pattern article_patern = Pattern.compile("([\\d]{8})");
//    Map<String, Integer> bigrams = new HashMap<>();

    public SearcherTREC(String indexDir, String queries, String field) throws IOException {
        Directory dir = FSDirectory.open(new File(indexDir).toPath());
        is = new IndexSearcher(DirectoryReader.open(dir));
        is.setSimilarity(new MySimilarity());
        this.queries = new File(queries);
        this.field = field;
    }

    void searchGenBank(String dir_articles, int k) throws FileNotFoundException, ParseException {
        FileInputStream fstream;
        int i = 0;
        fstream = new FileInputStream(queries);
        // Get the object of DataInputStream
        DataInputStream in = new DataInputStream(fstream);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {

            String str;
            while ((str = br.readLine()) != null) {
                i++;
                if (str.startsWith("#")) {
                    continue;
                }
                if (str.trim().length() == 0) {
                    continue;
                }
                if (i > k) {
                    break;
                }
                StringTokenizer st = new StringTokenizer(str);
                String article = st.nextToken();
                String pmc = st.nextToken().toLowerCase();
                //****************************************
                //****** Build path to the Article *******
                //****************************************
                Matcher m = article_patern.matcher(article);
                String id = "";
                while (m.find()) {
                    id = m.group();
                }
                File file_article = new File(new File(dir_articles).getAbsolutePath() + "/"
                        + id.substring(0, 2) + "/"
                        + id.substring(2, 4) + "/"
                        + id.substring(4, 6) + "/" + article);
                PubMedDoc pubmeddoc = new PubMedDoc(file_article);
                //*****************************************
                Query finalQuery = RecordQuery.getPubMedQuery(pubmeddoc, this.field);
                long start = System.currentTimeMillis();
                TopDocs hits = is.search(finalQuery, 1000);
                long end = System.currentTimeMillis();
                int j = 0;
                System.err.println(i + "- Found " + hits.totalHits
                        + " document(s) has matched " + pmc + ". Processed in " + Functions.getTimer(end - start) + ".");
//                System.err.println(finalQuery);
//                System.err.println("------------------------------------------------------------------------------------------------------");
                if (hits.totalHits == 0) {
                    System.out.println(pmc + "\tQ0\tXXXXXXXXXX\t1\t0.0\tSTANDARD");
                }
                for (ScoreDoc scoreDoc : hits.scoreDocs) {
                    j++;
//                    TermFreqVector docTerms = new TermFreqVector(is.getIndexReader().getTermVector(scoreDoc.doc, PubMedDoc.BODY));
//                    docTerms.getTerms().stream().forEach((term) -> {
//                        System.out.println(term);
//                    });
//                    Explanation explanation = is.explain(finalQuery, scoreDoc.doc);
//                    System.out.println(explanation.toString());
//                    System.out.println("------------------------------------------------------------------------------------------------------");
                    Document doc = is.doc(scoreDoc.doc);
                    System.out.println(pmc + "\tQ0\t" + doc.get(GenbankFormat.ACCESSION_TAG) + "\t" + j + "\t" + scoreDoc.score + "\t" + this.field);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(MutualReferences.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    void searchPubMed(Boolean qe, String index_organism, String class_name, int k) throws FileNotFoundException, ParseException, IOException, Exception {
        int i = 0;
//        boolean test = true;
        FileInputStream fstream;
        fstream = new FileInputStream(queries);
        // Get the object of DataInputStream
        DataInputStream in = new DataInputStream(fstream);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            String str;
            IndexSearcher is2 = null;
            if (qe) {
                Directory dir = FSDirectory.open(new File(index_organism).toPath());
                is2 = new IndexSearcher(DirectoryReader.open(dir));
                is2.setSimilarity(new MySimilarity());
                if (!class_name.equals(NCBITaxon.AUTHORITY)
                        && !class_name.equals(NCBITaxon.COMMON_NAME)
                        && !class_name.equals(NCBITaxon.EQUIVALENT_NAME)
                        && !class_name.equals(NCBITaxon.GENBANK_COMMON_NAME)
                        && !class_name.equals(NCBITaxon.INCLUDES)
                        && !class_name.equals(NCBITaxon.MISNOMER)
                        && !class_name.equals(NCBITaxon.MISSPELLING)
                        && !class_name.equals(NCBITaxon.SCIENTIFIC_NAME)
                        && !class_name.equals(NCBITaxon.SYNONYM)
                        && !class_name.equals(NCBITaxon.TYPE_MATERIAL)
                        && !class_name.equals(NCBITaxon.ACRONYM)) {
                    throw new Exception("Please check class name.");
                }
            }
            while ((str = br.readLine()) != null) {
                i++;
                if (str.startsWith("#")) {
                    continue;
                }
                if (str.trim().length() == 0) {
                    continue;
                }
//                if (i < 2085) {
//                    continue;
//                }
                if (i > k) {
                    break;
                }
//                System.err.println(i + "- " + str);
                StringTokenizer st = new StringTokenizer(str, "\t");
                String queryid = st.nextToken();
                String article = st.nextToken();
                String pmc = st.nextToken();
                String feature_type = st.nextToken();
                String annotation = st.nextToken();
                String query_text = st.nextToken();
                List<String> tax_ids = new ArrayList<>();
                while (st.hasMoreTokens()) {
                    tax_ids.add(st.nextToken());
                }
//                query_text = "Isopora togianensis mitochondrion, complete genome.";
//                query_text = "Isopora togianensis mitochondrion, complete genome.";
//                Query q = (new QueryParser(PubMedDoc.ID_PMC, new StandardAnalyzer())).parse("PMC3989238");
                String[] fields;
                switch (field) {
                    case "title":
                        fields = new String[]{PubMedDoc.TITLE};
                        break;
                    case "abstract":
                        fields = new String[]{PubMedDoc.ABSTRACT};
                        break;
                    case "body":
                        fields = new String[]{PubMedDoc.BODY};
                        break;
                    case "all":
                        fields = new String[]{PubMedDoc.TITLE, PubMedDoc.ABSTRACT, PubMedDoc.BODY};
                        break;
                    default:
                        System.err.println("Error on field.");
                        continue;
                }
//                System.out.println(query_text);
                Query finalQuery;
                if (qe) {
                    finalQuery = RecordQuery.getRecDefinitionQueryWithBigramsQE(query_text, is2, tax_ids, class_name, fields);//query_text, is2, tax_id, fields);
                } else {
                    finalQuery = RecordQuery.getRecDefinitionQueryWithBigramsNoQE(query_text, fields);
                }
//                BooleanQuery.Builder builder = new BooleanQuery.Builder();
//                builder.add(q, BooleanClause.Occur.FILTER);
//                builder.add(finalQuery, BooleanClause.Occur.MUST);
//                finalQuery = builder.build();
                long start = System.currentTimeMillis();
                TopDocs hits = is.search(finalQuery, 1000);
                long end = System.currentTimeMillis();
                int j = 0;
                System.err.println(i + "- Found " + hits.totalHits
                        + " document(s) has matched " + queryid + ". Processed in " + Functions.getTimer(end - start) + ".");
                System.err.println(query_text);
                System.err.println(finalQuery);
                System.err.println("------------------------------------------------------------------------------------------------------");
                if (hits.totalHits == 0) {
                    System.out.println(queryid + "\tQ0\tXXXXXXXXXX\t1\t0.0\tSTANDARD");
                }
                for (ScoreDoc scoreDoc : hits.scoreDocs) {
                    j++;
//                    TermFreqVector docTerms = new TermFreqVector(is.getIndexReader().getTermVector(scoreDoc.doc, PubMedDoc.BODY));
//                    docTerms.getTerms().stream().forEach((term) -> {
//                        System.out.println(term);
//                    });
//                    Explanation explanation = is.explain(finalQuery, scoreDoc.doc);
//                    System.out.println(explanation.toString());
//                    System.out.println("------------------------------------------------------------------------------------------------------");
                    Document doc = is.doc(scoreDoc.doc);
                    System.out.println(queryid + "\tQ0\t" + doc.get(PubMedDoc.ID_PMC) + "\t" + j + "\t" + scoreDoc.score + "\tSTANDARD");
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(MutualReferences.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     * @throws java.io.FileNotFoundException
     * @throws org.apache.lucene.queryparser.classic.ParseException
     */
    public static void main(String[] args) throws IOException, FileNotFoundException, ParseException, Exception {
        // TODO code application logic here
        String indexDir;
        String queries;
        String field;
        int k;
        boolean qe;
        String index_organism = null;
        String class_name = null;
        if (args.length == 0) {
//            indexDir = "/Volumes/Macintosh HD/Users/mbouadjenek/Documents/bioinformatics_data/index_PubMedCentral_v10.0/";
//            indexDir = "/Volumes/Macintosh HD/Users/mbouadjenek/Documents/bioinformatics_data/index_test/";
            queries = "/Users/mbouadjenek/Documents/bioinformatics_data/queries_definition_v5.0.txt";
            indexDir = "/Volumes/Macintosh HD/Users/mbouadjenek/Documents/bioinformatics_data/index_GenBankRecords_v2.0/";
//            queries = "/Users/mbouadjenek/Documents/bioinformatics_data/queries_pubmed.txt";
//            dir_articles = "/Users/mbouadjenek/Documents/bioinformatics_data/PubMed_Central/dataset/";            
            field = "title";
            k = 1;
            qe = true;
            index_organism = "/Users/mbouadjenek/Documents/bioinformatics_data/index_Organism_Names_v4.0/";
            class_name = "scientific name";
        } else {
            indexDir = args[0];
            queries = args[1];
            field = args[2];
            k = Integer.parseInt(args[3]);
            qe = Boolean.valueOf(args[4]);
            if (qe) {
                index_organism = args[5];
                class_name = args[6].replaceAll("_", " ");
            }
        }
        long start = System.currentTimeMillis();
        SearcherTREC s = new SearcherTREC(indexDir, queries, field);
        s.searchPubMed(qe, index_organism, class_name, k);
//        s.searchGenBank(dir_articles, k);
        long end = System.currentTimeMillis();
        long millis = (end - start);
        System.err.println("-------------------------------------------------------------------------");
        System.err.println("The process tooks " + Functions.getTimer(millis) + ".");
        System.err.println("-------------------------------------------------------------------------");
    }
}
