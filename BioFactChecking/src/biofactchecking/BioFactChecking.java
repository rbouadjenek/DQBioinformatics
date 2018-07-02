/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package biofactchecking;

import java.io.IOException;
import java.util.List;
import org.apache.commons.configuration.ConfigurationException;
import unimelb.edu.au.doc.PubMedDoc;
import static unimelb.edu.au.doc.PubMedDoc.getAbsoluteFile;
import unimelb.edu.au.run.ApplyDNorm;
import unimelb.edu.au.util.Functions;

/**
 *
 * @author mbouadjenek
 */
public class BioFactChecking {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, ConfigurationException {
        // TODO code application logic here
        String pmc = PubMedDoc.getAbsoluteFile("PMC2885306", "/Users/mbouadjenek/Documents/bioinformatics_data/PubMed_Central/dataset/");
        PubMedDoc doc = new PubMedDoc(pmc);
        ApplyDNorm dnorm = new ApplyDNorm("/Users/mbouadjenek/Documents/bioFactChecking/DNormData/config/banner_NCBIDisease_UMLS2013AA_TEST.xml", "/Users/mbouadjenek/Documents/bioFactChecking/DNormData/data/CTD_diseases_old.tsv", "/Users/mbouadjenek/Documents/bioFactChecking/DNormData/output/simmatrix_NCBIDisease_e4.bin", "/Users/mbouadjenek/Documents/bioFactChecking/DNormData/AB3P_DIR/", "/Users/mbouadjenek/Documents/bioFactChecking/DNormData/tmp/");
        long start = System.currentTimeMillis();

        List<ApplyDNorm.DNormResult> results = dnorm.process(doc.getBody());
        for (ApplyDNorm.DNormResult r : results) {
            System.out.print(r.getStartChar() + "\t" + r.getEndChar() + "\t" + r.getMentionText() + "\tDisease");
            if (r.getConceptID() != null) {
                System.out.print("\t" + r.getConceptID());
            }
            System.out.println("");
        }
        long end = System.currentTimeMillis();
        System.out.println("-------It took " + Functions.getTimer(end - start) + " ---------------");

//        start = System.currentTimeMillis();
//        String text=doc.getBody();
//        results = dnorm.process(text);
//        for (ApplyDNorm.DNormResult r : results) {
//            System.out.print(r.getStartChar() + "\t" + r.getEndChar() + "\t" + r.getMentionText() + "\tDisease");
//            if (r.getConceptID() != null) {
//                System.out.print("\t" + r.getConceptID());
//            }
//            System.out.println("");
//        }
//        end = System.currentTimeMillis();
//        System.out.println("-------It took " + Functions.getTimer(end - start) + " ---------------");

    }

}
