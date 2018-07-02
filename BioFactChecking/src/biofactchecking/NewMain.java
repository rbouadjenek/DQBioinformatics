/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package biofactchecking;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import jdbm.PrimaryHashMap;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import org.apache.commons.configuration.ConfigurationException;
import unimelb.edu.au.doc.PubMedDoc;
import static unimelb.edu.au.doc.PubMedDoc.getAbsoluteFile;
import unimelb.edu.au.indexing.EntityMention;
import unimelb.edu.au.run.ApplyDNorm;

/**
 *
 * @author mbouadjenek
 */
public class NewMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        RecordManager recman;
        recman = RecordManagerFactory.createRecordManager("/Volumes/TOSHIBA EXT/Documents/bioFactChecking/index_JDBM_v3.0/db_index");
        PrimaryHashMap<String, EntityMention> pmcid2diseases = recman.hashMap("pmcid2diseases");
        PrimaryHashMap<String, EntityMention> pmcid2genes = recman.hashMap("pmcid2genes");

        pmcid2genes.get("PMC3509075").title_map.get("326").remove("12_32");
        recman.commit();
        for (String offset : pmcid2diseases.get("PMC3017948").body_map.keySet()) {
            System.out.println(offset);
        }

//        
//        String pmc = getAbsoluteFile("PMC3509075", "/Users/mbouadjenek/Documents/bioinformatics_data/PubMed_Central/dataset/");
//        PubMedDoc doc = new PubMedDoc(pmc);
//        System.out.println(doc.getId_pmc());
//        doc.replaceConcepts("326", PubMedDoc.SUFFIX_GENE_A, pmcid2genes.get("PMC3509075"), "8743", PubMedDoc.SUFFIX_GENE_B, pmcid2genes.get("PMC3509075"));
    }

}
