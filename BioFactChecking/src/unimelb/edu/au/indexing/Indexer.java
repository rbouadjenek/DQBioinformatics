/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unimelb.edu.au.indexing;

import java.io.File;
import java.io.IOException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
//import org.apache.lucene.codecs.simpletext.SimpleTextCodec;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import unimelb.edu.au.doc.BioinformaticFilter;
import unimelb.edu.au.doc.MedlineDoc;
import unimelb.edu.au.util.Functions;

/**
 * This class is designed to index a collection of PubMed articles.
 *
 * @author mbouadjenek
 */
public class Indexer {

    /**
     * An index writer.
     */
    private final IndexWriter writer;
    /**
     * An analyzer for text.
     */
    private final Analyzer analyzer;
    /**
     * A filter for dataset files.
     */
    private final BioinformaticFilter filter = new BioinformaticFilter(BioinformaticFilter.DATASET_FILES);

    /**
     * Build an indexer object to index the PubMed collection.
     *
     * @param indexDir The path to the folder where the PubMed articles are
     * located.
     * @throws java.io.IOException
     */
    public Indexer(String indexDir) throws IOException {
        File indexDirFile = new File(indexDir);
//        analyzer = new MyEnglishAnalyzer(null);
        analyzer = new EnglishAnalyzer(StandardAnalyzer.STOP_WORDS_SET);
        IndexWriterConfig conf = new IndexWriterConfig(analyzer);
        conf.setUseCompoundFile(false);
//        conf.setCodec(new SimpleTextCodec()); // set a simple text codec.
        writer = new IndexWriter(FSDirectory.open(indexDirFile.toPath()), conf);
    }

    public int indexPubMedCentral(String dataDir) throws Exception {
        File f = new File(dataDir);
        File[] listFiles = f.listFiles();
        for (File listFile : listFiles) {
            if (listFile.isDirectory()) {
                indexPubMedCentral(listFile.toString());
            } else {
                if (!listFile.isHidden() && listFile.exists() && listFile.canRead() && filter.accept(listFile)) {
                    indexPubMedArticle(listFile);
                }
            }
        }
        return writer.numDocs();
    }

    private void indexPubMedArticle(File f) throws Exception {
        System.out.println(writer.numDocs() + 1 + "- Indexing " + f.getCanonicalPath());
        MedlineDoc article = new MedlineDoc(f);
        writer.addDocument(this.getDocument(article));
        writer.commit();
    }

    private Document getDocument(MedlineDoc medlinedoc) throws IOException {
        Document doc = new Document();
        doc.add(new StringField(MedlineDoc.ID_PMID, medlinedoc.getPmid(), Field.Store.YES));//Index the pmid 
        doc.add(new TextField(MedlineDoc.TITLE, medlinedoc.getTitle(), Field.Store.NO));//Index the title 
        if (medlinedoc.getAbstract() != null) {
            doc.add(new TextField(MedlineDoc.ABSTRACT, medlinedoc.getAbstract(), Field.Store.NO));//Index the abstract 
        }
        return doc;
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
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException, Exception {
        // TODO code application logic here
        String dataDir;
        String indexDir;
        if (args.length == 0) {
            dataDir = "/Users/mbouadjenek/Documents/bioFactChecking/test_data/";
            indexDir = "/Users/mbouadjenek/Documents/bioFactChecking/index_test/";
        } else {
            dataDir = args[0];
            indexDir = args[1];
        }
        long start = System.currentTimeMillis();
        Indexer indexer = new Indexer(indexDir);
        int numIndexed;
        try {
            numIndexed = indexer.indexPubMedCentral(dataDir);
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
