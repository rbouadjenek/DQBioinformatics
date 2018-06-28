/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unimelb.edu.au.evaluation;

/**
 *
 * @author mbouadjenek
 */
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class QrelsInMemory {

    /**
     * Each element in the array contains the docids of the relevant documents
     * with respect to a query.
     */
    public Map<String, Map<String, Integer>> qrelsPerQuery = new HashMap<>();
    /**
     * The qrels file.
     */
    protected String qrelsFilename;

    /**
     * A constructor that creates an instance of the class and loads in memory
     * the relevance assessments from the given file.
     *
     * @param qrelsFilename String The full path of the qrels file to load.
     */
    public QrelsInMemory(String qrelsFilename) {
        try {
            this.qrelsFilename = qrelsFilename;
            this.loadQrelsFile();
        } catch (Exception ex) {
            Logger.getLogger(QrelsInMemory.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Get ids of the queries that appear in the pool.
     *
     * @return The ids of queries in the pool.
     */
    public Set<String> getQueryids() {
        return qrelsPerQuery.keySet();
    }

    /**
     * Returns the total number of queries contained in the loaded relevance
     * assessments.
     *
     * @return int The number of unique queries in the loaded relevance
     * assessments.
     */
    public int getNumberOfQueries() {
        return this.qrelsPerQuery.size();
    }

    /**
     * Get the pooled relevant documents for the given query.
     *
     * @param queryid The id of the given query.
     * @return A hashset containing the docnos of the pooled relevant documents
     * for the given query.
     */
    public Map<String, Integer> getRelevant(String queryid) {
        return qrelsPerQuery.get(queryid);
    }

    /**
     * Returns the numbe of relevant documents for a given query.
     *
     * @param queryid String The identifier of a query.
     * @return int The number of relevant documents for the given query.
     */
    public int getNumberOfRelevant(String queryid) {
        return getRelevant(queryid).size();
    }

    /**
     * Check if the given document is relevant for a given query.
     *
     * @param queryid String a query identifier.
     * @param docid String a document identifier.
     * @return boolean true if the given document is relevant for the given
     * query, or otherwise false.
     */
    public boolean isRelevant(String queryid, String docid) {
        return qrelsPerQuery.get(queryid).containsKey(docid);
    }

    /**
     * Checks whether there is a query with a given identifier in the relevance
     * assessments.
     *
     * @param queryid String the identifier of a query.
     * @return boolean true if the given query exists in the qrels file,
     * otherwise it returns false.
     */
    public boolean queryExistInQrels(String queryid) {
        return qrelsPerQuery.containsKey(queryid);
    }

    /**
     * Load in memory the relevance assessment files that are specified in the
     * array fqrels.
     */
    protected void loadQrelsFile() throws Exception {
        try {
            FileInputStream fstream = new FileInputStream(qrelsFilename);
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

                    StringTokenizer st = new StringTokenizer(str);
                    if (st.countTokens() != 4) {
                        throw new Exception("Please check your qrels file. Four (4) columns are expected in the qrels file.");
                    }
                    String queryid = st.nextToken();
                    String v = st.nextToken();
                    String docid = st.nextToken();
                    String relevance = st.nextToken();
                    Map<String, Integer> doc;
                    if (qrelsPerQuery.containsKey(queryid)) {
                        doc = qrelsPerQuery.get(queryid);
                        if (doc.containsKey(docid)) {
                            int val = doc.get(docid);
                            if (val < Integer.parseInt(relevance)) {
                                doc.put(docid, Integer.parseInt(relevance));
                            }
                        } else {
                            doc.put(docid, Integer.parseInt(relevance));
                        }

                    } else {
                        doc = new HashMap<>();
                        doc.put(docid, Integer.parseInt(relevance));
                    }
                    qrelsPerQuery.put(queryid, doc);
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }

    }

    /**
     * @param args the command line arguments
     * @throws java.lang.Exception
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        QrelsInMemory qrels = new QrelsInMemory("/Volumes/Macintosh HD/Users/mbouadjenek/Documents/bioinformatics_data/qrels_definition_v5.0.txt");
        System.out.println(qrels.qrelsPerQuery.size());
        System.out.println(qrels.getNumberOfRelevant("Query-1"));
        System.out.println(qrels.queryExistInQrels("Query-1"));
        System.out.println(qrels.queryExistInQrels("Query-reda"));

    }
}
