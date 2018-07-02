/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unimelb.edu.au.facts;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import jdbm.PrimaryHashMap;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import opennlp.tools.sentdetect.SentenceDetectorME;
import unimelb.edu.au.indexing.EntityMention;
import unimelb.edu.au.tools.Mathematics;
import unimelb.edu.au.util.Functions;

/**
 *
 * @author mbouadjenek
 */
public class DiversityAnalysis {

    RecordManager recman;
    SentenceDetectorME detector;
    PrimaryHashMap<String, EntityMention> pmcid2diseases;
    PrimaryHashMap<String, EntityMention> pmcid2genes;
    Map<String, Integer> dict = new HashMap<>();

    public DiversityAnalysis(String mapFile) throws IOException {
        recman = RecordManagerFactory.createRecordManager(mapFile);
        pmcid2diseases = recman.hashMap("pmcid2diseases");
        pmcid2genes = recman.hashMap("pmcid2genes");
    }

    public void processFile(String f) {
        try {
            FileInputStream fstream = new FileInputStream(f);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
                String str;
                int i = 0;
                int nbr_object2 = 0, nbr_object1 = 0;
                String current_id = "";

                while ((str = br.readLine()) != null) {
                    if (str.startsWith("#")) {
                        continue;
                    }
                    if (str.trim().length() == 0) {
                        continue;
                    }
                    i++;
                    if (str.contains(";")) {
                        continue;
                    }
                    StringTokenizer st = new StringTokenizer(str, "\t");
                    String id = st.nextToken();
                    if (current_id.equals("")) {
                        current_id = id;
                    }
                    String object1 = st.nextToken();
                    String object2 = st.nextToken();
                    int y_ = Integer.valueOf(st.nextToken());
                    String pmc = st.nextToken();
                    int rank = Integer.valueOf(st.nextToken());

                    if (rank == 21 || !current_id.equals(id)) {
                        double p1 = (double) nbr_object1 / (double) (nbr_object1 + nbr_object2);
                        double p2 = (double) nbr_object2 / (double) (nbr_object1 + nbr_object2);

                        double a = 0, b = 0;
                        if (p1 != 0) {
                            a = -p1 * Mathematics.log2(p1);
                        }
                        if (p2 != 0) {
                            b = -p2 * Mathematics.log2(p2);
                        }

                        System.out.println(id + "\t" + nbr_object1 + "\t" + nbr_object2 + "\t" + (a + b));

                        nbr_object1 = 0;
                        nbr_object2 = 0;
                        current_id = id;
                    } else {
                        current_id = id;
                    }

                    if (rank <= 20 && current_id.equals(id)) {
                        System.err.println(i + "- " + str);
                        if (pmcid2genes.get(pmc) != null) {
                            if (pmcid2genes.get(pmc).title_map.get(object1) != null) {
                                nbr_object1 += pmcid2genes.get(pmc).title_map.get(object1).size();
                            }
                            if (pmcid2genes.get(pmc).abstract_map.get(object1) != null) {
                                nbr_object1 += pmcid2genes.get(pmc).abstract_map.get(object1).size();
                            }
                            if (pmcid2genes.get(pmc).body_map.get(object1) != null) {
                                nbr_object1 += pmcid2genes.get(pmc).body_map.get(object1).size();
                            }
                        }
                        if (pmcid2genes.get(pmc) != null) {
                            if (pmcid2genes.get(pmc).title_map.get(object2) != null) {
                                nbr_object2 += pmcid2genes.get(pmc).title_map.get(object2).size();
                            }
                            if (pmcid2genes.get(pmc).abstract_map.get(object2) != null) {
                                nbr_object2 += pmcid2genes.get(pmc).abstract_map.get(object2).size();
                            }
                            if (pmcid2genes.get(pmc).body_map.get(object2) != null) {
                                nbr_object2 += pmcid2genes.get(pmc).body_map.get(object2).size();
                            }
                        }
                    }

                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        long start = System.currentTimeMillis();
        String input = "/Users/mbouadjenek/Downloads/BIOGRID-MV-Physical-3.4.151.tab2_2.txt";
        String mapFile = "/Users/mbouadjenek/Documents/bioFactChecking/index_JDBM_v3.0/db_index";
        input = args[0];
        mapFile = args[1];
        DiversityAnalysis ef = new DiversityAnalysis(mapFile);
        ef.processFile(input);
        long end = System.currentTimeMillis();
        System.err.println("-------------------------------------------------------------------------");
        long millis = (end - start);
        System.err.println("The processing time took: " + Functions.getTimer(millis) + ".");
        System.err.println("-------------------------------------------------------------------------");

    }

}
