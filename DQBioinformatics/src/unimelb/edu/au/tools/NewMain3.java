/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unimelb.edu.au.tools;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.queryparser.classic.ParseException;
import unimelb.edu.au.lucene.analysis.en.MyEnglishAnalyzer;
import unimelb.edu.au.doc.PubMedDoc;
import unimelb.edu.au.lucene.analysis.en.PubMedSpecialWords;
import unimelb.edu.au.search.RecordQuery;

/**
 *
 * @author mbouadjenek
 */
public class NewMain3 {

    public static Analyzer createAnalyzer(final int shingles) {
        return new Analyzer() {
            @Override
            protected Analyzer.TokenStreamComponents createComponents(String fieldName) {
                final Tokenizer source = new StandardTokenizer();
                TokenStream result = new StandardFilter(source);
                result = new LowerCaseFilter(result);
                result = new ShingleFilter(result, 2, 2);
//                result = new StopFilter(result, PubMedStopWords.DEFINITION_STOP_WORDS_SET2);
                return new Analyzer.TokenStreamComponents(source, result);
            }

        };
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, ParseException {
        // TODO code application logic here
        MyEnglishAnalyzer analyzer = new MyEnglishAnalyzer(PubMedSpecialWords.ENGLISH_STOP_WORDS_SET2, PubMedSpecialWords.KEYWORDS_SET);

        FileInputStream fstream;
        fstream = new FileInputStream(new File("/Volumes/Macintosh HD/Users/mbouadjenek/Documents/bioinformatics_data/queries_definition_v3.0.txt"));
        // Get the object of DataInputStream
        DataInputStream in = new DataInputStream(fstream);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            String str;
            int i = 0;
            while ((str = br.readLine()) != null) {
                if (str.startsWith("#")) {
                    continue;
                }
                if (str.trim().length() == 0) {
                    continue;
                }
                i++;
                if (i < 0) {
                    continue;
                }
                if (i == 1000) {
                    break;
                }
                StringTokenizer st = new StringTokenizer(str, "\t");
                String queryid = st.nextToken();
                String article = st.nextToken();
//                if (!article.equals(article.toLowerCase())) {
//                    System.out.print("\"" + article + "\"," + "\"" + article.toLowerCase() + "\",");
//                } else {
//                    System.out.print("\"" + article + "\",");
//                }
//                if (i % 5 == 0) {
//                    System.out.println("");
//                }

                String pmc = st.nextToken();
                String feature_type = st.nextToken();
                String annotation = st.nextToken();
                String query_text = st.nextToken();
                String tax_id = st.nextToken();
//                query_text="Cercopithecus aethiops in IN sp100-hmg (sp100) gene, partial cds; alternatively spliced RNAs. bass is Is IS Is The the THE";
                System.out.println( query_text+"\t"+tax_id);
//                System.out.println(RecordQuery.getRecDefinitionQueryWithBigrams(query_text,tax_id, new String[]{PubMedDoc.TITLE}));
                
//                System.out.println(RecordQuery.getRecDefinitionQuery(query_text, new String[]{PubMedDoc.TITLE}));
                System.out.println("------------------------------------------------------------------------------------------");
//                TokenStream stream = analyzer.tokenStream(null, new StringReader(query_text));
//
////        stream = new ShingleFilter(stream,2,2);
////        stream = new StopFilter(stream, PubMedStopWords.DEFINITION_STOP_WORDS_SET2);
//                CharTermAttribute cattr = stream.addAttribute(CharTermAttribute.class);
//                stream.reset();
//                System.out.print(i + "- ");
//                while (stream.incrementToken()) {
//                    System.out.print(cattr.toString() + " ");
//                }
//                System.out.println("");
//                System.out.println("---------------");
//                stream.end();
//                stream.close();
            }
        }

    }

}
