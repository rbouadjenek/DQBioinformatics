package unimelb.edu.au.indexing;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import jdbm.PrimaryHashMap;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import unimelb.edu.au.util.Functions;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author mbouadjenek
 */
public class IndexMentionsHashMap {

    public void index(String f1, String mapname) throws IOException {
        RecordManager recman = RecordManagerFactory.createRecordManager("db_index");
        PrimaryHashMap<String, EntityMention> m1 = recman.hashMap(mapname);
//        PrimaryHashMap<String, EntityMention> m2 = recman.hashMap("pmcid2genes");
        try {
            FileInputStream fstream = new FileInputStream(f1);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
                String str;
                int i = 0;
                long start = System.currentTimeMillis();
                while ((str = br.readLine()) != null) {
                    if (str.startsWith("#")) {
                        continue;
                    }
                    if (str.trim().length() == 0) {
                        continue;
                    }
                    i++;
                    StringTokenizer st = new StringTokenizer(str);
                    String pmcid = st.nextToken();
                    String field = st.nextToken();
                    String entityid = st.nextToken();
                    String char_start = st.nextToken();
                    String char_end = st.nextToken();

                    if (m1.containsKey(pmcid)) {
                        EntityMention em = m1.get(pmcid);
                        em.insert(entityid, char_start + "_" + char_end, field);
                        m1.put(pmcid, em);
                    } else {
                        EntityMention em = new EntityMention();
                        em.insert(entityid, char_start + "_" + char_end, field);
                        m1.put(pmcid, em);
                    }
//                    if (m2.containsKey(diseaseid)) {
//                        Set<String> s = m2.get(diseaseid);
//                        s.add(pmcid);
//                        m2.put(diseaseid, s);
//                    } else {
//                        Set<String> s = new HashSet<>();
//                        s.add(pmcid);
//                        m2.put(diseaseid, s);
//                    }
                    if (i % 1e5 == 0) {
                        /**
                         * Commit periodically, otherwise program would run out
                         * of memory
                         */
                        recman.commit();
                        long end = System.currentTimeMillis();
                        long millis = (end - start);
                        System.out.println(i + " mentions have been indexed from " + f1 + " in " + mapname + ". The process took: " + Functions.getTimer(millis) + ".");
                        start = System.currentTimeMillis();
//                        break;
                    }

                }
                recman.commit();
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
        IndexMentionsHashMap index = new IndexMentionsHashMap();
        index.index(args[0], "pmcid2diseases");
        index.index(args[1], "pmcid2genes");
        long end = System.currentTimeMillis();
        System.err.println("-------------------------------------------------------------------------");
        long millis = (end - start);
        System.err.println("The indexing files took " + Functions.getTimer(millis) + ".");
        System.err.println("-------------------------------------------------------------------------");
    }

}
