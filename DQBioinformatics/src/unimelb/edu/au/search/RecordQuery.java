/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unimelb.edu.au.search;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.biojavax.bio.seq.io.GenbankFormat;
import org.biojavax.bio.taxa.NCBITaxon;
import unimelb.edu.au.doc.PubMedDoc;
import unimelb.edu.au.lucene.analysis.en.MyEnglishAnalyzer;
import unimelb.edu.au.lucene.analysis.en.PubMedSpecialWords;
import unimelb.edu.au.search.similarities.MySimilarity;
import unimelb.edu.au.util.Functions;

/**
 *
 * @author mbouadjenek
 */
public class RecordQuery {
    
    public static Analyzer analyzer = new Analyzer() {
            @Override
            protected Analyzer.TokenStreamComponents createComponents(String fieldName) {
                final Tokenizer source = new StandardTokenizer();
                TokenStream result = new StandardFilter(source);
                return new Analyzer.TokenStreamComponents(source, result);
            }
        };

//    protected static final String[] ORGANISMS = new String[]{"homo sapiens", "Homo sapiens",
//        "Homo Sapiens", "Arabidopsis thaliana", "Bos taurus", "Caenorhabditis elegans", "Chlamydomonas reinhardtii",
//        "Danio rerio (zebrafish)", "Dictyostelium discoideum", "Drosophila melanogaster", "Escherichia coli",
//        "Hepatitis C virus", "Mus musculus", "Mycoplasma pneumoniae", "Oryza sativa", "Plasmodium falciparum", "Pneumocystis carinii",
//        "Rattus norvegicus", "Saccharomyces cerevisiae", "Schizosaccharomyces pombe", "Takifugu rubripes", "Xenopus laevis", "Zea mays"};
    public static Query getRecDefinitionQueryWithBigramsQE(String query_text, IndexSearcher is, List<String> tax_ids, String name_class, String[] fields) throws ParseException, IOException {

        BooleanQuery.setMaxClauseCount(Integer.MAX_VALUE);
        
        String query_text_tokenized = Functions.processString(QueryParser.escape(query_text), analyzer);
        for (String com_qualifier : PubMedSpecialWords.KEYWORDS_COMPLETENESS_QUALIFIERS) {
            query_text_tokenized = query_text_tokenized.replace(com_qualifier, ""); // This remove the completeness qualifiers
        }
        //----------------------------------------------------------------
        //-------------- GENERATE BIGRAMS IN THE QUERY --------------
        //----------------------------------------------------------------
        String[] tokens = query_text_tokenized.split(" ");
        String[] tokens_copy = query_text_tokenized.split(" ");
        for (int i = 0; i < tokens.length; i++) {
            if (PubMedSpecialWords.KEYWORDS_REC_DEFINITION.contains(tokens[i].toLowerCase()) && i > 0) {
                tokens_copy[i] = "\"" + tokens[i - 1] + " " + tokens[i] + "\"";
            }
        }
        query_text_tokenized = java.util.Arrays.toString(tokens_copy).replaceAll("\\[|\\]|,", "");
        //----------------------------------------------------------------
        //----------------------------------------------------------------
////        System.out.println(query_text_tokenized);

//        query_text_tokenized = query_text_tokenized.replaceAll("(?i)" + organism, "\"" + organism + "\"");
        BooleanQuery.Builder bqBuilder = new BooleanQuery.Builder();
        BooleanQuery.Builder bqBuilder1 = new BooleanQuery.Builder();
        tax_ids.stream().map((tax_id) -> new TermQuery(new Term("tax_id", tax_id))).forEach((tq) -> {
            bqBuilder1.add(tq, BooleanClause.Occur.SHOULD);
        });
        Query q = bqBuilder1.build();
        bqBuilder.add(q, BooleanClause.Occur.MUST);
        PhraseQuery.Builder pqBuilder = new PhraseQuery.Builder();
        pqBuilder.add(new Term("name_class", name_class));
        PhraseQuery pq = pqBuilder.build();
        bqBuilder.add(pq, BooleanClause.Occur.FILTER);
//        QueryParser qp = new QueryParser("tax_id", analyzer);
        TopDocs hits = is.search(bqBuilder.build(), 1000);
        for (ScoreDoc scoreDoc : hits.scoreDocs) {
            Document doc = is.doc(scoreDoc.doc);
            String syn = Functions.processString(doc.get("name_txt"), analyzer);
            String[] synArray = syn.split(" ");
            if (synArray.length < -1) {
                for (int i = 0; i < synArray.length - 1; i++) {
                    query_text_tokenized += "\"" + synArray[i] + " " + synArray[i + 1] + "\"";
                }
            } else {
                query_text_tokenized += " " + syn;
            }
//            for (String synArray1: synArray) {
//                if (!query_text_tokenized.toLowerCase().contains(synArray1.toLowerCase())) {
//                    query_text_tokenized += " " + synArray1;
//                }
//            }
        }
//        for (String ORGANISM : ORGANISMS) {
//            query_text_tokenized = query_text_tokenized.replace(organism, "\"" + organism + "\"");
//        }
//        System.out.println(query_text_tokenized);
        //----------------------------------------------------------------
        //----------------------------------------------------------------
//        System.out.println(query_text_tokenized);
        String[] queries_lucene = new String[fields.length];
        BooleanClause.Occur[] allflags = new BooleanClause.Occur[fields.length];
        for (int i = 0; i < fields.length; i++) {
            queries_lucene[i] = query_text_tokenized;
            allflags[i] = BooleanClause.Occur.SHOULD;
        }
        return MultiFieldQueryParser.parse(queries_lucene, fields, allflags, new MyEnglishAnalyzer(PubMedSpecialWords.ENGLISH_STOP_WORDS_SET2, PubMedSpecialWords.KEYWORDS_SET));
    }

    public static Query getRecDefinitionQueryWithBigramsNoQE(String query_text, String[] fields) throws ParseException, IOException {        
        String query_text_tokenized = Functions.processString(QueryParser.escape(query_text), analyzer);
        for (String com_qualifier : PubMedSpecialWords.KEYWORDS_COMPLETENESS_QUALIFIERS) {
            query_text_tokenized = query_text_tokenized.replace(com_qualifier, ""); // This remove the completeness qualifiers
        }
////        System.out.println(query_text_tokenized);
        //----------------------------------------------------------------
        //-------------- GENERATE BIGRAMS IN THE QUERY --------------
        //----------------------------------------------------------------
        String[] tokens = query_text_tokenized.split(" ");
        String[] tokens_copy = query_text_tokenized.split(" ");
        for (int i = 0; i < tokens.length; i++) {
            if (PubMedSpecialWords.KEYWORDS_REC_DEFINITION.contains(tokens[i].toLowerCase()) && i > 0) {
                tokens_copy[i] = "\"" + tokens[i - 1] + " " + tokens[i] + "\"";
            }
        }
        query_text_tokenized = java.util.Arrays.toString(tokens_copy).replaceAll("\\[|\\]|,", "");
//        for (String ORGANISM : ORGANISMS) {
//            query_text_tokenized = query_text_tokenized.replace(organism, "\"" + organism + "\"");
//        }
//        System.out.println(query_text_tokenized);
        //----------------------------------------------------------------
        //----------------------------------------------------------------
//        System.out.println(query_text_tokenized);
        String[] queries_lucene = new String[fields.length];
        BooleanClause.Occur[] allflags = new BooleanClause.Occur[fields.length];
        for (int i = 0; i < fields.length; i++) {
            queries_lucene[i] = query_text_tokenized;
            allflags[i] = BooleanClause.Occur.SHOULD;
        }
        return MultiFieldQueryParser.parse(queries_lucene, fields, allflags, new MyEnglishAnalyzer(PubMedSpecialWords.ENGLISH_STOP_WORDS_SET2, PubMedSpecialWords.KEYWORDS_SET));
    }

    public static Query getRecDefinitionQuery(String query_text, String[] fields) throws ParseException, IOException {
        String[] queries_lucene = new String[fields.length];
        BooleanClause.Occur[] allflags = new BooleanClause.Occur[fields.length];
        for (int i = 0; i < fields.length; i++) {
            queries_lucene[i] = QueryParser.escape(query_text);
            allflags[i] = BooleanClause.Occur.SHOULD;
        }
        return MultiFieldQueryParser.parse(queries_lucene, fields, allflags, new EnglishAnalyzer(PubMedSpecialWords.ENGLISH_STOP_WORDS_SET));
    }

    public static Query getPubMedQuery(PubMedDoc pmdoc, String field) throws ParseException, IOException {
        BooleanQuery.setMaxClauseCount(Integer.MAX_VALUE);
        QueryParser qp = new QueryParser(GenbankFormat.DEFINITION_TAG, new MyEnglishAnalyzer(PubMedSpecialWords.ENGLISH_STOP_WORDS_SET2, PubMedSpecialWords.KEYWORDS_SET));
        if (field.equals(PubMedDoc.TITLE)) {
            return qp.parse(QueryParser.escape(pmdoc.getTitle()));
        } else if (field.equals(PubMedDoc.ABSTRACT)) {
            return qp.parse(QueryParser.escape(pmdoc.getAbstract()));
        } else {
            return qp.parse(QueryParser.escape(pmdoc.getBody()));
        }

    }

    public static Query getRecDefinitionQueryWithBigramsQE2(String query_text, IndexSearcher is, List<String> tax_ids, String name_class, String[] fields) throws ParseException, IOException {

        BooleanQuery.setMaxClauseCount(Integer.MAX_VALUE);
        BooleanQuery.Builder bqBuilder = new BooleanQuery.Builder();
        BooleanQuery.Builder bqBuilder1 = new BooleanQuery.Builder();
        tax_ids.stream().map((tax_id) -> new TermQuery(new Term("tax_id", tax_id))).forEach((tq) -> {
            bqBuilder1.add(tq, BooleanClause.Occur.SHOULD);
        });
        Query q = bqBuilder1.build();
        bqBuilder.add(q, BooleanClause.Occur.MUST);
        PhraseQuery.Builder pqBuilder = new PhraseQuery.Builder();
        pqBuilder.add(new Term("name_class", name_class));
        PhraseQuery pq = pqBuilder.build();
        bqBuilder.add(pq, BooleanClause.Occur.FILTER);
//        QueryParser qp = new QueryParser("tax_id", analyzer);
        TopDocs hits = is.search(bqBuilder.build(), 1000);
        for (ScoreDoc scoreDoc : hits.scoreDocs) {
            Document doc = is.doc(scoreDoc.doc);
            String syn = Functions.processString(doc.get("name_txt"), analyzer);
            query_text += " " + syn;
        }
        String query_text_tokenized = Functions.processString(QueryParser.escape(query_text), analyzer);

        for (String com_qualifier : PubMedSpecialWords.KEYWORDS_COMPLETENESS_QUALIFIERS) {
            query_text_tokenized = query_text_tokenized.replace(com_qualifier, ""); // This remove the completeness qualifiers
        }
        //----------------------------------------------------------------
        //-------------- GENERATE BIGRAMS IN THE QUERY --------------
        //----------------------------------------------------------------
        String[] tokens = query_text_tokenized.split(" ");
        String[] tokens_copy = query_text_tokenized.split(" ");
        for (int i = 0; i < tokens.length; i++) {
            if (PubMedSpecialWords.KEYWORDS_REC_DEFINITION.contains(tokens[i].toLowerCase()) && i > 0) {
                tokens_copy[i] = "\"" + tokens[i - 1] + " " + tokens[i] + "\"";
            }
        }
        query_text_tokenized = java.util.Arrays.toString(tokens_copy).replaceAll("\\[|\\]|,", "");
        //----------------------------------------------------------------
        //----------------------------------------------------------------
//        System.out.println(query_text_tokenized);
//        query_text_tokenized = query_text_tokenized.replaceAll("(?i)" + organism, "\"" + organism + "\"");
        //----------------------------------------------------------------
        //----------------------------------------------------------------
//        System.out.println(query_text_tokenized);
        String[] queries_lucene = new String[fields.length];
        BooleanClause.Occur[] allflags = new BooleanClause.Occur[fields.length];
        for (int i = 0; i < fields.length; i++) {
            queries_lucene[i] = query_text_tokenized;
            allflags[i] = BooleanClause.Occur.SHOULD;
        }
        return MultiFieldQueryParser.parse(queries_lucene, fields, allflags, new MyEnglishAnalyzer(PubMedSpecialWords.ENGLISH_STOP_WORDS_SET2, PubMedSpecialWords.KEYWORDS_SET));
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        PubMedDoc doc = new PubMedDoc("/Users/mbouadjenek/Documents/bioinformatics_data/PubMed_Central/dataset/02/71/10/PMC02711072.nxml");
        String[] fields = new String[]{PubMedDoc.TITLE};
        Directory dir = FSDirectory.open(new File("/Volumes/Macintosh HD/Users/mbouadjenek/Documents/bioinformatics_data/index_Organism_Names_v3.0/").toPath());
        IndexSearcher is = new IndexSearcher(DirectoryReader.open(dir));
        is.setSimilarity(new MySimilarity());
        try {
            String query = "Afrotis afra myoglobin gene, intron 2 and partial cds.";
            System.out.println(query);

            List<String> tax_ids = new ArrayList<>();
            tax_ids.add("100");
            tax_ids.add("4929");

            System.out.println(getRecDefinitionQueryWithBigramsQE2(query, is, tax_ids, NCBITaxon.MISSPELLING, fields));

//            System.out.println(getRecDefinitionQueryWithBigramsNoQE(query, fields));
//            System.out.println(getPubMedQuery(doc, PubMedDoc.BODY));
        } catch (ParseException | IOException ex) {
            Logger.getLogger(RecordQuery.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
