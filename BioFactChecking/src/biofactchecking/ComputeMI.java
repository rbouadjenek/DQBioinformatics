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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import unimelb.edu.au.tools.Mathematics;

/**
 *
 * @author mbouadjenek
 */
public class ComputeMI {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here

        Map<String, Set<String>> set1 = new HashMap<>();
        Map<String, Set<String>> set2 = new HashMap<>();

        try {
            FileInputStream fstream = new FileInputStream("/Users/mbouadjenek/Documents/bioFactChecking/ppi_pmc_genes_body.txt");
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
                String str;
                while ((str = br.readLine()) != null) {
                    if (str.startsWith("#")) {
                        continue;
                    }
                    if (str.trim().length() == 0) {
                        continue;
                    }
//                    i++;
                    StringTokenizer st = new StringTokenizer(str);
                    String pmcid = st.nextToken();
                    String geneid = st.nextToken();
                    if (set1.containsKey(geneid)) {
                        Set<String> pmcids = set1.get(geneid);
                        pmcids.add(pmcid);
                        set1.put(geneid, pmcids);
                    } else {
                        Set<String> pmcids = new HashSet<>();
                        pmcids.add(pmcid);
                        set1.put(geneid, pmcids);
                    }

                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
        
        set2=new HashMap<>(set1);

//        try {
//            FileInputStream fstream = new FileInputStream("/Users/mbouadjenek/Documents/bioFactChecking/pmc_diseases_body.txt");
//            // Get the object of DataInputStream
//            DataInputStream in = new DataInputStream(fstream);
//            try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
//                String str;
//                while ((str = br.readLine()) != null) {
//                    if (str.startsWith("#")) {
//                        continue;
//                    }
//                    if (str.trim().length() == 0) {
//                        continue;
//                    }
////                    i++;
//                    StringTokenizer st = new StringTokenizer(str);
//                    String pmcid = st.nextToken();
//                    String diseaseid = st.nextToken();
//                    if (set2.containsKey(diseaseid)) {
//                        Set<String> pmcids = set2.get(diseaseid);
//                        pmcids.add(pmcid);
//                        set2.put(diseaseid, pmcids);
//                    } else {
//                        Set<String> pmcids = new HashSet<>();
//                        pmcids.add(pmcid);
//                        set2.put(diseaseid, pmcids);
//                    }
//
//                }
//            }
//        } catch (IOException | NumberFormatException e) {
//            e.printStackTrace();
//        }

        try {
            FileInputStream fstream = new FileInputStream("/Users/mbouadjenek/Documents/bioFactChecking/ppi_dataset.txt");
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
                String str;
                while ((str = br.readLine()) != null) {
                    if (str.startsWith("#")) {
                        continue;
                    }
                    if (str.trim().length() == 0) {
                        continue;
                    }
//                    i++;
                    StringTokenizer st = new StringTokenizer(str);
                    String id = st.nextToken();
                    String object1 = st.nextToken();
                    String object2 = st.nextToken();
                    String y = st.nextToken();

                    double posObject1 = 0;
                    if (set1.containsKey(object1)) {
                        posObject1 = (double) (set1.get(object1).size()) / (double) 1135611;
                    }
                    double negObject1 = 1 - posObject1;

                    double posObject2 = 0;
                    if (set2.containsKey(object2)) {
                        posObject2 = (double) (set2.get(object2).size()) / (double) 1135611;
                    }
                    double negObject2 = 1 - posObject2;
                    /**
                     * posObject1_posObject2
                     */
                    Set<String> s = new HashSet();
                    if (set1.containsKey(object1)) {
                        s.addAll(set1.get(object1));
                    }
                    if (set2.containsKey(object2)) {
                        s.retainAll(set2.get(object2));
                    }
                    double posObject1_posObject2 = 0;
                    if (!s.isEmpty()) {
                        posObject1_posObject2 = (double) (s.size()) / (double) 1135611;
                    }
                    /**
                     * posObject1_negObject2
                     */
                    s = new HashSet();
                    if (set1.containsKey(object1)) {
                        s.addAll(set1.get(object1));
                    }
                    if (set2.containsKey(object2)) {
                        s.removeAll(set2.get(object2));
                    }
                    double posObject1_negObject2 = 0;
                    if (!s.isEmpty()) {
                        posObject1_negObject2 = (double) (s.size()) / (double) 1135611;
                    }
                    /**
                     * negObject1_posObject2
                     */
                    s = new HashSet();
                    if (set2.containsKey(object2)) {
                        s.addAll(set2.get(object2));
                    }
                    if (set1.containsKey(object1)) {
                        s.removeAll(set1.get(object1));
                    }
                    double negObject1_posObject2 = 0;
                    if (!s.isEmpty()) {
                        negObject1_posObject2 = (double) (s.size()) / (double) 1135611;
                    }
                    /**
                     * negObject1_negObject2
                     */
                    s = new HashSet();

                    if (set1.containsKey(object1)) {
                        s.addAll(set1.get(object1));
                    }
                    if (set2.containsKey(object2)) {
                        s.addAll(set2.get(object2));
                    }
                    double negObject1_negObject2 = 1;
                    if (!s.isEmpty()) {
                        negObject1_negObject2 = (double) (1135611 - s.size()) / (double) 1135611;
                    }

//                    System.out.println(gene1 + "\t" + gene2 + "\t" + posGene1 + "\t" + negGene1 + "\t" + posGene2 + "\t" + negGene2 + "\t" + posGene1_posGene2 + "\t" + posGene1_negGene2 + "\t" + negGene1_posGene2 + "\t" + negGene1_negGene2);
                    double mi = 0;
                    if (posObject1_posObject2 != 0 && posObject1 != 0 && posObject2 != 0
                            && posObject1_negObject2 != 0 && negObject2 != 0 && negObject1_posObject2 != 0
                            && negObject1 != 0 && negObject1_negObject2 != 0) {
                        mi = posObject1_posObject2 * Mathematics.log2(posObject1_posObject2 / (posObject1 * posObject2))
                                + posObject1_negObject2 * Mathematics.log2(posObject1_negObject2 / (posObject1 * negObject2))
                                + negObject1_posObject2 * Mathematics.log2(negObject1_posObject2 / (negObject1 * posObject2))
                                + negObject1_negObject2 * Mathematics.log2(negObject1_negObject2 / (negObject1 * negObject2));
                    }
                    System.out.printf(y + "\t%.20f\n", mi);

//                    s = new HashSet();
//                    if (set1.containsKey(object1)) {
//                        s.addAll(set1.get(object1));
//                    }
//                    if (set2.containsKey(object2)) {
//                        s.retainAll(set2.get(object2));
//                    }
//                    System.out.println(y + "\t" +s.size());
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }

    }

}
