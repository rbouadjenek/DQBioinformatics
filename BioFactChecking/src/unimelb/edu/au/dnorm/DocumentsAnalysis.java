/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unimelb.edu.au.dnorm;


import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import org.apache.commons.configuration.ConfigurationException;
import unimelb.edu.au.doc.PubMedDoc;
import unimelb.edu.au.run.ApplyDNorm;
import unimelb.edu.au.util.Functions;

/**
 *
 * @author mbouadjenek
 */
public class DocumentsAnalysis {

    ApplyDNorm dnorm;

    public DocumentsAnalysis(String configurationFilename, String lexiconFilename, String matrixFilename, String abbreviationDirectory, String tempDirectory) throws ConfigurationException, IOException {
        dnorm = new ApplyDNorm(configurationFilename, lexiconFilename, matrixFilename, abbreviationDirectory, tempDirectory);
    }

    public void analyze(String doc2analyze, String dir_articles, int line) {
        try {
            FileInputStream fstream = new FileInputStream(doc2analyze);
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
                    if (i <= line) {
                        continue;
                    }
                    String pmc = PubMedDoc.getAbsoluteFile(str, dir_articles);
                    PubMedDoc doc = new PubMedDoc(pmc);
                    long start = System.currentTimeMillis();
                    List<ApplyDNorm.DNormResult> results;
                    if (doc.getTitle() != null) {
                        results = dnorm.process(doc.getTitle());
                        for (ApplyDNorm.DNormResult r : results) {
                            if (r.getConceptID() != null) {
                                System.out.println(str + "\tTitle\t" + r.getStartChar() + "\t" + r.getEndChar() + "\t" + r.getConceptID());
                            }
                        }
                    }
                    if (doc.getAbstract() != null) {
                        results = dnorm.process(doc.getAbstract());
                        for (ApplyDNorm.DNormResult r : results) {
                            if (r.getConceptID() != null) {
                                System.out.println(str + "\tAbstract\t" + r.getStartChar() + "\t" + r.getEndChar() + "\t" + r.getConceptID());
                            }
                        }
                    }
                    if (doc.getBody() != null) {
                        results = dnorm.process(doc.getBody());
                        for (ApplyDNorm.DNormResult r : results) {
                            if (r.getConceptID() != null) {
                                System.out.println(str + "\tBody\t" + r.getStartChar() + "\t" + r.getEndChar() + "\t" + r.getConceptID());
                            }
                        }
                    }
                    long end = System.currentTimeMillis();
                    System.err.println(i + "- It took " + Functions.getTimer(end - start) + " to process " + str + ".");
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, ConfigurationException {
        // TODO code application logic here
        String configurationFilename, lexiconFilename, matrixFilename, abbreviationDirectory, tempDirectory;
        String doc2analyze, dir_articles;
        int line = 0;

        if (args.length == 0) {
            configurationFilename = "config/banner_NCBIDisease_UMLS2013AA_TEST.xml";
            lexiconFilename = "data/CTD_diseases_old.tsv";
            matrixFilename = "output/simmatrix_NCBIDisease_e4.bin";
            abbreviationDirectory = "AB3P_DIR/";
            tempDirectory = "tmp/";
            doc2analyze = "/Users/mbouadjenek/Documents/bioFactChecking/doc2analyze.txt";
            dir_articles = "/Users/mbouadjenek/Documents/bioinformatics_data/PubMed_Central/dataset/";
        } else {
            configurationFilename = args[0];
            lexiconFilename = args[1];
            matrixFilename = args[2];
            abbreviationDirectory = args[3];
            tempDirectory = args[4];
            doc2analyze = args[5];
            dir_articles = args[6];
            if (args[7] != null) {
                line = Integer.parseInt(args[7]);
            }
        }

        long start = System.currentTimeMillis();
        DocumentsAnalysis dA = new DocumentsAnalysis(configurationFilename, lexiconFilename, matrixFilename, abbreviationDirectory, tempDirectory);
        dA.analyze(doc2analyze, dir_articles, line);
        long end = System.currentTimeMillis();
        System.out.println("-------It took " + Functions.getTimer(end - start) + " ---------------");

    }

}
