/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unimelb.edu.au.indexing;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import unimelb.edu.au.util.Functions;

/**
 *
 * @author mbouadjenek
 */
public class IndexesMerge {

    /**
     * An index writer.
     */
    private final IndexWriter writer;

    private final File INDEXES_DIR;

    public IndexesMerge(String indexDist, String indexesDir) throws IOException {
        File indexDirFile = new File(indexDist);
//        analyzer = new MyEnglishAnalyzer(null);
        Analyzer analyzer = new EnglishAnalyzer(StandardAnalyzer.STOP_WORDS_SET);
        IndexWriterConfig conf = new IndexWriterConfig(analyzer);
        conf.setUseCompoundFile(false);
//        conf.setCodec(new SimpleTextCodec()); // set a simple text codec.
        writer = new IndexWriter(FSDirectory.open(indexDirFile.toPath()), conf);
        INDEXES_DIR = new File(indexesDir);
    }

    public int merge() throws IOException {
//        writer.setMergeFactor(1000);
//        writer.setRAMBufferSizeMB(50);

        Directory indexes[] = new Directory[INDEXES_DIR.list().length];

        for (int i = 0; i < INDEXES_DIR.list().length; i++) {
            System.out.println("Adding: " + INDEXES_DIR.list()[i]);
            indexes[i] = FSDirectory.open(new File(INDEXES_DIR.getAbsolutePath() + "/" + INDEXES_DIR.list()[i]).toPath());
//            System.out.println(indexes[i]);
        }

        System.out.println("Merging added indexes...");
        writer.addIndexes(indexes);
        System.out.println("done");

//        System.out.print("Optimizing index...");
//        writer.optimize();
//        writer.close();
        System.out.println("done");
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
    public static void main(String[] args) {
        // TODO code application logic here
        String indexDist;
        String indexesDir;
        if (args.length == 0) {
            indexDist = "/Users/mbouadjenek/Documents/bioFactChecking/test_data/";
            indexesDir = "/Users/mbouadjenek/Documents/bioFactChecking/index_test/";
        } else {
            indexDist = args[0];
            indexesDir = args[1];
        }
        try {
            long start = System.currentTimeMillis();
            IndexesMerge indexer = new IndexesMerge(indexDist, indexesDir);
            int numIndexed;
            numIndexed = indexer.merge();
            long end = System.currentTimeMillis();
            System.out.println("-------------------------------------------------------------------------");
            long millis = (end - start);
            System.out.println("Indexing " + numIndexed + " files took " + Functions.getTimer(millis) + ".");
            System.out.println("Indexed " + indexer.getNumberofDocs() + " files.");
            System.out.println("-------------------------------------------------------------------------");
            indexer.close();
        } catch (IOException ex) {
            Logger.getLogger(IndexesMerge.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
