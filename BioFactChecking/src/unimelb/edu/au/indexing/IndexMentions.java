/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unimelb.edu.au.indexing;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import unimelb.edu.au.doc.MedlineDoc;
import unimelb.edu.au.util.Functions;
import org.apache.lucene.codecs.simpletext.SimpleTextCodec;

/**
 *
 * @author rbouadjenek
 */
public class IndexMentions {

    /**
     * An index writer.
     */
    private final IndexWriter writer;
    /**
     * An analyzer for text.
     */
    private final Analyzer analyzer;

    /**
     * Build an indexer object to index the PubMed collection.
     *
     * @param indexDir The path to the folder where the PubMed articles are
     * located.
     * @throws java.io.IOException
     */
    public IndexMentions(String indexDir) throws IOException {
        File indexDirFile = new File(indexDir);
//        analyzer = new MyEnglishAnalyzer(null);
        analyzer = new StandardAnalyzer();
        IndexWriterConfig conf = new IndexWriterConfig(analyzer);
        conf.setUseCompoundFile(false);
        conf.setCodec(new SimpleTextCodec()); // set a simple text codec.
        writer = new IndexWriter(FSDirectory.open(indexDirFile.toPath()), conf);
    }

    public int index(String file) throws IOException {
        try {
            FileInputStream fstream = new FileInputStream(file);
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
                    StringTokenizer st = new StringTokenizer(str, "|");
                    String id = st.nextToken();
                    String text = st.nextToken().replaceAll(":", "_");
                    System.out.println(i + "- " + id);
                    Document doc = new Document();
                    doc.add(new StringField(MedlineDoc.ID_PMID, id, Field.Store.YES));//Index the pmid 
                    doc.add(new TextField(MedlineDoc.ABSTRACT, text, Field.Store.NO));
                    writer.addDocument(doc);                    
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
        return writer.numDocs();
    }

    private void close() throws IOException {
        writer.close();
    }

    /**
     * @return The number of articles indexed.
     */
    public int getNumberofDocs() {
        return writer.numDocs();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        String dataFile;
        String indexDir;
        if (args.length == 0) {
            dataFile = "/Users/rbouadjenek/Desktop/test.txt";
            indexDir = "/Users/rbouadjenek/Desktop/index_test/";
        } else {
            dataFile = args[0];
            indexDir = args[1];
        }
        long start = System.currentTimeMillis();
        IndexMentions indexer = new IndexMentions(indexDir);
        int numIndexed;
        try {
            numIndexed = indexer.index(dataFile);
            long end = System.currentTimeMillis();
            System.out.println("-------------------------------------------------------------------------");
            long millis = (end - start);
            System.out.println("Indexing " + numIndexed + " files took " + Functions.getTimer(millis) + ".");
            System.out.println("Indexed " + indexer.getNumberofDocs() + " files.");
            System.out.println("-------------------------------------------------------------------------");
        } finally {
            indexer.close();
        }
    }

}
