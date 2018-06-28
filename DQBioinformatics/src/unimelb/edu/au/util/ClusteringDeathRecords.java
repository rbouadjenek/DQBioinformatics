/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unimelb.edu.au.util;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import unimelb.edu.au.doc.MutualReferences;

/**
 *
 * @author mbouadjenek
 */
public class ClusteringDeathRecords {

    protected File queries;
    protected Map<String, Set<String>> dic = new HashMap<>();
    protected Map<String, Set<String>> clusters = new HashMap<>();

    public ClusteringDeathRecords(String queries) {
        this.queries = new File(queries);
    }

    public void Cluster() throws FileNotFoundException, ParseException {
        FileInputStream fstream;
        int i = 0;
        fstream = new FileInputStream(queries);
        // Get the object of DataInputStream
        DataInputStream in = new DataInputStream(fstream);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {

            String str;
            while ((str = br.readLine()) != null) {
                i++;
                if (str.startsWith("#")) {
                    continue;
                }
                if (str.trim().length() == 0) {
                    continue;
                }
                StringTokenizer st = new StringTokenizer(str, "\t");
                String queryid = st.nextToken();
                String article = st.nextToken();
                String pmc = st.nextToken();
                String acession = st.nextToken();
                int citationNbr = Integer.parseInt(st.nextToken());
                long creationDateseconds = Long.parseLong(st.nextToken());
                long updateDateseconds = Long.parseLong(st.nextToken());
                String Status = st.nextToken();
                String annotation = st.nextToken();
                String query_text = st.nextToken();
                if (!Status.equals("live")) {
                    final Set<String> set = new HashSet<>();
                    set.addAll(StringSimilarity.tokenizeToArrayList(query_text, new EnglishAnalyzer(null)));
                    dic.put(queryid, set);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(MutualReferences.class.getName()).log(Level.SEVERE, null, ex);
        }
        for (Map.Entry<String, Set<String>> e1 : dic.entrySet()) {
            String key1 = "c" + clusters.size();
            Set<String> s1 = new HashSet<>();
            s1.add(e1.getKey());
            clusters.put(key1, s1);
            for (Map.Entry<String, Set<String>> e2 : dic.entrySet()) {
                if (!e1.getKey().equals(e2.getKey())) {
                    double sim = StringSimilarity.getJaccardSimilarity(e1.getValue(), e2.getValue());
                    if (sim >= 0.65) {
                        String key2 = "c" + clusters.size();
                        Set<String> s2 = new HashSet<>();
                        s2.add(e1.getKey());
                        s2.add(e2.getKey());
                        clusters.put(key2, s2);
                    }
                }
            }

        }
        int size = clusters.size() + 1;
        outerloop:
        while (true) {
            if (size == clusters.size()) {
                break;
            } else {
                size = clusters.size();
            }
            for (Map.Entry<String, Set<String>> e1 : clusters.entrySet()) {
                for (Map.Entry<String, Set<String>> e2 : clusters.entrySet()) {
                    if (!e1.getKey().equals(e2.getKey())) {
                        for (String key : e1.getValue()) {
                            if (e2.getValue().contains(key)) {
                                mergeClusters(e1.getKey(), e2.getKey());
                                continue outerloop;
                            }
                        }
                    }
                }
            }
        }
//        for (Map.Entry<String, Set<String>> e : clusters.entrySet()) {
//            System.out.print(e.getKey() + ": ");
//            for (String key : e.getValue()) {
//                System.out.print(key+",");
//            }
//            System.out.println("");
//        }
        for (Map.Entry<String, Set<String>> e : clusters.entrySet()) {
            for (String key : e.getValue()) {
                System.out.println(key + "\t" + e.getKey());
            }
        }

    }

    public void mergeClusters(String c1, String c2) {
        Set<String> s1 = clusters.get(c1);
        Set<String> s2 = clusters.get(c2);
        s2.addAll(s1);
        clusters.remove(c1);
        clusters.remove(c2);
        clusters.put(c1, s2);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            // TODO code application logic here
            ClusteringDeathRecords cdr = new ClusteringDeathRecords("/Users/mbouadjenek/Documents/bioinformatics_data/queries_definition_v7.0.txt");
            cdr.Cluster();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ClusteringDeathRecords.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(ClusteringDeathRecords.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
