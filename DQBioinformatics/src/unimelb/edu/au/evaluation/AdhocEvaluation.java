/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unimelb.edu.au.evaluation;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import unimelb.edu.au.util.Functions;

/**
 *
 * @author mbouadjenek
 */
public class AdhocEvaluation extends Evaluation {

    protected static final int[] PRECISION_RECALL_RANKS = new int[]{1, 2, 3, 4, 5, 10, 15, 20, 30, 50, 100, 200, 500, 1000};

    DecimalFormat df = new DecimalFormat("#.####");

    /**
     * The maximum number of documents retrieved for a query.
     */
    protected int maxNumberRetrieved;
    /**
     * The number of effective queries. An effective query is a query that has
     * corresponding relevant documents in the qrels file.
     */
    protected int numberOfEffQuery;
    /**
     * The total number of documents retrieved in the task.
     */
    protected int totalNumberOfRetrieved;
    /**
     * The total number of relevant documents in the qrels file for the queries
     * processed in the task.
     */
    protected int totalNumberOfRelevant;
    /**
     * The total number of relevant documents retrieved in the task.
     */
    protected int totalNumberOfRelevantRetrieved;
    /**
     * Precision at rank number of documents
     */
    protected Map<Integer, Double> precisionAtRank = new HashMap<>();
    /**
     * Recall at rank number of documents
     */
    protected Map<Integer, Double> recallAtRank = new HashMap<>();

    protected Map<Integer, Double> precisionAtRecall = new HashMap<>();

    /**
     * Mean Average Precision (MAP).
     */
    protected double meanAveragePrecision;
    /**
     * Mean reciprocal rank (MRR).
     */
    protected double meanReciprocalRank;
    /**
     * Relevant Precision.
     */
    protected double meanRelevantPrecision;
    /**
     * The reciprocal precision of each query.
     */
    protected double[] reciprocalRankOfEachQuery;
    /**
     * The average precision of each query.
     */
    protected double[] averagePrecisionOfEachQuery;
    /**
     * The query number of each query.
     */
    protected String[] queryNo;
    /**
     * Set of queries to be excluded.
     */
    Set<String> consideredQueries;

    /**
     * Create adhoc evaluation
     *
     * @param qrelsFile
     */
    public AdhocEvaluation(String qrelsFile) {
        super(qrelsFile);
    }

    /**
     * Initialize variables.
     */
    public void initialise() {
        this.maxNumberRetrieved = 1000;
        this.precisionAtRank.clear();
        this.recallAtRank.clear();
        this.precisionAtRecall.clear();
        this.numberOfEffQuery = 0;
        this.totalNumberOfRetrieved = 0;
        this.totalNumberOfRelevant = 0;
        this.meanAveragePrecision = 0;
        this.meanReciprocalRank = 0;
        this.meanRelevantPrecision = 0;

    }

    public void loadConsideredQueries(String exlfile) {
        this.consideredQueries = new HashSet<>();
        try {
            FileInputStream fstream = new FileInputStream(exlfile);
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
                    this.consideredQueries.add(str);
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    /**
     * Evaluates the given result file.
     *
     * @param resultFilename String the filename of the result file to evaluate.
     */
    @Override
    public void evaluate(String resultFilename) {
        this.initialise();
        //int retrievedQueryCounter = 0;
        //int releventQueryCounter = 0; 
        int effQueryCounter = 0;
        int[] numberOfRelevantRetrieved;
        int[] numberOfRelevant = null;
//        Vector<Record[]> listOfRetrieved = new Vector<>();
        Vector<Record[]> listOfRelevantRetrieved = new Vector<>();
        Vector<Integer> vecNumberOfRelevant = new Vector<>();
        Vector<Integer> vecNumberOfRetrieved = new Vector<>();
        Vector<Integer> vecNumberOfRelevantRetrieved = new Vector<>();
        Vector<String> vecQueryNo = new Vector<>();
        /**
         * Read records from the result file
         */
        try {
            FileInputStream fstream = new FileInputStream(resultFilename);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
                String str;
                String previous = ""; // the previous query number
                int numberOfRetrievedCounter = 0;
                int numberOfRelevantRetrievedCounter = 0;
                Vector<Record> relevantRetrieved = new Vector<>();
//                Vector<Record> retrieved = new Vector<>();
                while ((str = br.readLine()) != null) {
                    if (str.startsWith("#")) {
                        continue;
                    }
                    if (str.trim().length() == 0) {
                        continue;
                    }
                    StringTokenizer stk = new StringTokenizer(str);
                    if (stk.countTokens() != 6) {
                        throw new Exception("Please check your results file. Six (6) columns are expected in the results file.");
                    }
                    String queryid = stk.nextToken();
                    if (!qrels.queryExistInQrels(queryid)) {
                        continue;
                    }
                    if (consideredQueries != null) {
                        if (!consideredQueries.contains(queryid)) {
                            continue;
                        }
                    }
                    stk.nextToken();
                    String docID = stk.nextToken();
                    int rank = Integer.parseInt(stk.nextToken());
                    if (!previous.equals(queryid)) {
                        if (effQueryCounter != 0) {
                            vecNumberOfRetrieved.addElement(numberOfRetrievedCounter);
                            vecNumberOfRelevantRetrieved.addElement(numberOfRelevantRetrievedCounter);
//                            listOfRetrieved.addElement((Record[]) retrieved.toArray(new Record[retrieved.size()]));
                            listOfRelevantRetrieved.addElement((Record[]) relevantRetrieved.toArray(new Record[relevantRetrieved.size()]));
                            numberOfRetrievedCounter = 0;
                            numberOfRelevantRetrievedCounter = 0;
//                            retrieved = new Vector<>();
                            relevantRetrieved = new Vector<>();
                        }
                        effQueryCounter++;
                        vecQueryNo.addElement(queryid);
                        vecNumberOfRelevant.addElement(qrels.getNumberOfRelevant(queryid));
                    }
                    previous = queryid;
                    numberOfRetrievedCounter++;
                    totalNumberOfRetrieved++;
//                    retrieved.addElement(new Record(queryid, docID, rank));
                    if (qrels.isRelevant(queryid, docID)) {
                        relevantRetrieved.addElement(new Record(queryid, docID, rank));
                        numberOfRelevantRetrievedCounter++;
                    }
                }
                listOfRelevantRetrieved.addElement(relevantRetrieved.toArray(new Record[relevantRetrieved.size()]));
//                listOfRetrieved.addElement(retrieved.toArray(new Record[retrieved.size()]));
                vecNumberOfRetrieved.addElement(numberOfRetrievedCounter);
                vecNumberOfRelevantRetrieved.addElement(numberOfRelevantRetrievedCounter);
                br.close();
                this.queryNo = vecQueryNo.toArray(new String[vecQueryNo.size()]);
                numberOfRelevantRetrieved = new int[effQueryCounter];
                numberOfRelevant = new int[effQueryCounter];
                this.totalNumberOfRelevant = 0;
                this.totalNumberOfRelevantRetrieved = 0;
                this.totalNumberOfRetrieved = 0;
                for (int i = 0; i < effQueryCounter; i++) {
                    numberOfRelevantRetrieved[i] = (vecNumberOfRelevantRetrieved.get(i));
                    numberOfRelevant[i] = (vecNumberOfRelevant.get(i));
                    this.totalNumberOfRetrieved += vecNumberOfRetrieved.get(i);
                    this.totalNumberOfRelevant += numberOfRelevant[i];
                    this.totalNumberOfRelevantRetrieved += numberOfRelevantRetrieved[i];
                }
            } catch (Exception ex) {
                Logger.getLogger(AdhocEvaluation.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.averagePrecisionOfEachQuery = new double[effQueryCounter];
        this.reciprocalRankOfEachQuery = new double[effQueryCounter];
        Map<Integer, Double>[] relevantAtRankByQuery = new HashMap[effQueryCounter];
        for (int i = 0; i < effQueryCounter; i++) {
            relevantAtRankByQuery[i] = new HashMap<>();
        }
        double[] ExactPrecision = new double[effQueryCounter];
        double[] ReciprocalRank = new double[effQueryCounter];
        double[] RPrecision = new double[effQueryCounter];
        Arrays.fill(ExactPrecision, 0.0d);
        Arrays.fill(ReciprocalRank, 0.0d);
        Arrays.fill(RPrecision, 0.0d);
        meanAveragePrecision = 0d;
        meanReciprocalRank = 0d;
        meanRelevantPrecision = 0d;
        numberOfEffQuery = effQueryCounter;
        for (int i = 0; i < effQueryCounter; i++) {
            Record[] relevantRetrieved = (Record[]) listOfRelevantRetrieved.get(i);
            for (int j = 0; j < relevantRetrieved.length; j++) {
                if (relevantRetrieved[j].rank < numberOfRelevant[i]) {
                    RPrecision[i] += 1d;
                }
                for (int precisionRank : PRECISION_RECALL_RANKS) {
                    if (relevantRetrieved[j].rank <= precisionRank) {
                        if (relevantAtRankByQuery[i].containsKey(precisionRank)) {
                            double v = relevantAtRankByQuery[i].get(precisionRank) + 1;
                            relevantAtRankByQuery[i].put(precisionRank, v);
                        } else {
                            relevantAtRankByQuery[i].put(precisionRank, 1.0d);
                        }
                    }
                }
                ExactPrecision[i] += (double) (j + 1)
                        / (relevantRetrieved[j].rank);
                if (ReciprocalRank[i] == 0) {
                    ReciprocalRank[i] = (double) 1 / relevantRetrieved[j].rank;
                }
                relevantRetrieved[j].precision
                        = (double) (j + 1)
                        / (1d + relevantRetrieved[j].rank);
                relevantRetrieved[j].recall
                        = (double) (j + 1) / numberOfRelevant[i];
            }
            //Modified by G.AMATI 7th May 2002
            if (numberOfRelevant[i] > 0) {
                ExactPrecision[i] /= ((double) numberOfRelevant[i]);
            } else {
                numberOfEffQuery--;
            }
            if (numberOfRelevant[i] > 0) {
                RPrecision[i] /= ((double) numberOfRelevant[i]);
            }
            meanAveragePrecision += ExactPrecision[i];
            meanReciprocalRank += ReciprocalRank[i];
            this.averagePrecisionOfEachQuery[i] = ExactPrecision[i];
            this.reciprocalRankOfEachQuery[i] = ReciprocalRank[i];
            meanRelevantPrecision += RPrecision[i];
            for (int precision_RecallRank : PRECISION_RECALL_RANKS) {
                double v = 0;
                if (precisionAtRank.containsKey(precision_RecallRank)) {
                    v = precisionAtRank.get(precision_RecallRank);
                }
                if (relevantAtRankByQuery[i].containsKey(precision_RecallRank)) {
                    v += (relevantAtRankByQuery[i].get(precision_RecallRank) / (double) precision_RecallRank);
                }
                precisionAtRank.put(precision_RecallRank, v);
                if (recallAtRank.containsKey(precision_RecallRank)) {
                    v = recallAtRank.get(precision_RecallRank);
                }
                if (relevantAtRankByQuery[i].containsKey(precision_RecallRank)) {
                    v += (relevantAtRankByQuery[i].get(precision_RecallRank) / (double) numberOfRelevant[i]);
                }
                recallAtRank.put(precision_RecallRank, v);
            }
        }
        for (int precisionRank : PRECISION_RECALL_RANKS) {
            double v = precisionAtRank.get(precisionRank);
            precisionAtRank.put(precisionRank, v / numberOfEffQuery);

            v = recallAtRank.get(precisionRank);
            recallAtRank.put(precisionRank, v / numberOfEffQuery);
        }
        meanAveragePrecision /= (double) numberOfEffQuery;
        meanReciprocalRank /= (double) numberOfEffQuery;
        meanRelevantPrecision /= (double) numberOfEffQuery;
    }

    /**
     * Output the evaluation result of each query to the specific file.
     *
     * @param out java.io.PrintWriter the stream to which the results are
     * printed.
     */
    @Override
    public void writeEvaluationResultOfEachQuery(PrintWriter out) {
        final StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < this.queryNo.length; i++) {
//            sb.append(queryNo[i]).append("\t").append(this.averagePrecisionOfEachQuery[i]).append("\n");
//        }
//        out.print(sb.toString());
        for (int i = 0; i < this.queryNo.length; i++) {
            sb.append(queryNo[i]).append("\t").append(df.format(this.reciprocalRankOfEachQuery[i])).append("\n");
        }
        out.print(sb.toString());
        out.flush();
    }

    /**
     * Output the evaluation result to the specific file.
     *
     * @param out java.io.PrintWriter the stream to which the results are
     * printed.
     */
    @Override
    public void writeEvaluationResult(PrintWriter out) {
        out.println("-------------------------------------------------------------------------");
        out.println("Number of queries  = " + numberOfEffQuery);
        out.println("Retrieved          = " + totalNumberOfRetrieved);
        out.println("Relevant           = " + totalNumberOfRelevant);
        out.println("Relevant retrieved = " + totalNumberOfRelevantRetrieved);
        out.println("-------------------------------------------------------------------------");
        out.println("Mean Average Precision (MAP): " + df.format(meanAveragePrecision));
        out.println("Mean Reciprocal Rank (MRR): " + df.format(meanReciprocalRank));
//        System.out.println("R Precision      : " + meanRelevantPrecision);
//        out.println("____________________________________");
//        for (int precisionRank : PRECISION_RECALL_RANKS) {
//            out.printf("Precision at   %d : %s\n", precisionRank, df.format(precisionAtRank.get(precisionRank)));
//        }
//        out.println("____________________________________");
//        for (int precisionRank : PRECISION_RECALL_RANKS) {
//            out.printf("Recall at   %d : %s\n", precisionRank, df.format(recallAtRank.get(precisionRank)));
//        }
        out.println("-------------------------------------------------------------------------");
        out.println("#Precision\tRecall");
        for (int precisionRank : PRECISION_RECALL_RANKS) {
            out.println(df.format(precisionAtRank.get(precisionRank)) + "\t" + df.format(recallAtRank.get(precisionRank)));
        }
        out.flush();

    }

    /**
     * @param args the command line arguments
     * @throws java.lang.Exception
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        String qrels;
        String results;
        if (args.length == 0) {
            qrels = "/Volumes/Macintosh HD/Users/mbouadjenek/Documents/bioinformatics_data/qrels_definition_v5.0.txt";
            results = "/Volumes/Macintosh HD/Users/mbouadjenek/Documents/bioinformatics_data/test.txt";
        } else {
            qrels = args[0];
            results = args[1];
        }
        long start = System.currentTimeMillis();
        AdhocEvaluation e = new AdhocEvaluation(qrels);
        if (args.length == 3) {
            e.loadConsideredQueries(args[2]);
        }
        e.evaluate(results);
        PrintWriter writer = new PrintWriter(System.out);
        e.writeEvaluationResultOfEachQuery(writer);
        e.writeEvaluationResult(writer);
        long end = System.currentTimeMillis();
        writer.println("-------------------------------------------------------------------------");
        long millis = (end - start);
        writer.println("The evaluation took " + Functions.getTimer(millis) + ".");
        writer.println("-------------------------------------------------------------------------");
        writer.flush();
    }

}
