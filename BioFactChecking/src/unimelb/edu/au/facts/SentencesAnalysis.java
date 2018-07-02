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
import java.io.OutputStreamWriter;
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
import opennlp.tools.sentdetect.SentenceModel;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import unimelb.edu.au.doc.PubMedDoc;
import unimelb.edu.au.indexing.EntityMention;
import unimelb.edu.au.lucene.analysis.en.PubMedSpecialWords;
import unimelb.edu.au.util.Functions;

/**
 *
 * @author mbouadjenek
 */
public class SentencesAnalysis {

    RecordManager recman;
    String dir_articles;
    SentenceDetectorME detector;
    PrimaryHashMap<String, EntityMention> pmcid2diseases;
    PrimaryHashMap<String, EntityMention> pmcid2genes;
    Map<String, Integer> dict = new HashMap<>();
    OutputStreamWriter out;

    public SentencesAnalysis(String dir_articles, String mapFile, String nlpmodel, String outputtopterms) throws IOException {
        this.dir_articles = dir_articles;

        recman = RecordManagerFactory.createRecordManager(mapFile);
        pmcid2diseases = recman.hashMap("pmcid2diseases");
        pmcid2genes = recman.hashMap("pmcid2genes");
        InputStream inputStream = new FileInputStream(nlpmodel);
        SentenceModel model = new SentenceModel(inputStream);
        detector = new SentenceDetectorME(model);
        out = new OutputStreamWriter(new FileOutputStream(outputtopterms));
    }

    public void processFile(String f, int y) {
        try {
            FileInputStream fstream = new FileInputStream(f);
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
                    StringTokenizer st = new StringTokenizer(str);
                    String id = st.nextToken();
                    String object1 = st.nextToken();
                    String object2 = st.nextToken();
                    int y_ = Integer.valueOf(st.nextToken());
                    String pmc = st.nextToken();
                    int rank = Integer.valueOf(st.nextToken());
                    if (y == y_ && rank <= 20) {
                        System.err.println(i + "- " + str);
                        analyseSentences(pmc, object1, object2);
                    }
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }

    }

    public void analyseSentences(String pmc, String object1, String object2) throws FileNotFoundException, IOException {
        String path = PubMedDoc.getAbsoluteFile(pmc, dir_articles);
        PubMedDoc doc = new PubMedDoc(path);
        doc.replaceConcepts(object1, PubMedDoc.SUFFIX_GENE_A, pmcid2genes.get(pmc), object2, PubMedDoc.SUFFIX_GENE_B, pmcid2genes.get(pmc));
        /**
         * Detecting the sentence in title.
         */
        System.out.println(pmc + "\t" + PubMedDoc.TITLE + "\t" + object1 + "\t" + object2 + "\t" + getSentenceJaccad(doc.getTitle(), pmc));

        /**
         * Detecting the sentence in abstract.
         */
        System.out.println(pmc + "\t" + PubMedDoc.ABSTRACT + "\t" + object1 + "\t" + object2 + "\t" + getSentenceJaccad(doc.getAbstract(), pmc));
        /**
         * Detecting the sentence in body.
         */
        System.out.println(pmc + "\t" + PubMedDoc.BODY + "\t" + object1 + "\t" + object2 + "\t" + getSentenceJaccad(doc.getBody(), pmc));

        /**
         * Detecting the sentence in all.
         */
//        System.out.println(pmc + "\t" + "all" + "\t" + object1 + "\t" + object2 + "\t" + getSentenceJaccad(doc.getTitle() + ". " + doc.getAbstract() + ". " + doc.getBody(), pmc));
    }

    public double getSentenceJaccad(String text, String pmc) throws IOException {
        double total = 0, common = 0;
        if (text != null) {
            String[] sentences = detector.sentDetect(text.replace("\n", " "));
            for (String sentence : sentences) {
                if (sentence.contains(PubMedDoc.SUFFIX_GENE_A) && sentence.contains(PubMedDoc.SUFFIX_GENE_B)) {
                    common++;
                    String pre_sentence = Functions.processString(sentence, new EnglishAnalyzer(PubMedSpecialWords.ENGLISH_STOP_WORDS_SET));
                    /**
                     * My code to remove later
                     */
                    String z = "";
                    if (pre_sentence.contains("complex")) {
                        z += "complex*";
                    }
                    if (pre_sentence.contains("interact")) {
                        z += "interact*";
                    }
                    if (pre_sentence.contains("activ")) {
                        z += "activ*";
                    }
                    if (pre_sentence.contains("express")) {
                        z += "express*";
                    }
                    if (pre_sentence.contains("bind")) {
                        z += "bind*";
                    }
                    if (pre_sentence.contains("figur")) {
                        z += "figur*";
                    }
                    if (pre_sentence.contains("anti")) {
                        z += "anti*";
                    }
                    if (pre_sentence.contains("signal")) {
                        z += "signal*";
                    }
                    if (pre_sentence.contains("regul")) {
                        z += "regul*";
                    }
                    if (pre_sentence.contains("domain")) {
                        z += "domain*";
                    }
                    if (pre_sentence.contains("subunit")) {
                        z += "subunit*";
                    }
                    if (pre_sentence.contains("function")) {
                        z += "function*";
                    }
                    if (pre_sentence.contains("level")) {
                        z += "level*";
                    }
                    if (pre_sentence.contains("antibodi")) {
                        z += "antibodi*";
                    }
                    if (pre_sentence.contains("result")) {
                        z += "result*";
                    }
                    if (pre_sentence.contains("factor")) {
                        z += "factor*";
                    }
                    if (pre_sentence.contains("phosphoryl")) {
                        z += "phosphoryl*";
                    }
                    if (pre_sentence.contains("kinas")) {
                        z += "kinas*";
                    }
                    if (pre_sentence.contains("mediat")) {
                        z += "mediat*";
                    }
                    if (pre_sentence.contains("receptor")) {
                        z += "receptor*";
                    }
                    if (!z.equals("")) {
                        out.write("******************************\n");
                        out.write(pmc + " " + z + ": " + sentence.replace("\n", " ") + "\n");
//                        out.write(pre_sentence+"\n");
                        out.write("******************************\n");
                        out.flush();
                    }
                    //*****************************************************
                    //*****************************************************
                    StringTokenizer st = new StringTokenizer(pre_sentence);
                    while (st.hasMoreTokens()) {
                        String t = st.nextToken();
                        if (t.startsWith(PubMedDoc.SUFFIX_GENE_A.toLowerCase()) || t.startsWith(PubMedDoc.SUFFIX_GENE_B.toLowerCase())) {
                            continue;
                        }
                        if (dict.containsKey(t)) {
                            int val = dict.get(t) + 1;
                            dict.put(t, val);
                        } else {
                            dict.put(t, 1);
                        }
                    }
                }
                if (sentence.contains(PubMedDoc.SUFFIX_GENE_A) || sentence.contains(PubMedDoc.SUFFIX_GENE_B)) {
                    total++;
                }
            }
            if (total == 0) {
                return 0;
            }
            return common / total;
        } else {
            return 0;
        }
    }

    /**
     * This class prints top terms.
     *
     * @param k
     * @throws java.io.FileNotFoundException
     */
    public void printTopTerms(int k) throws IOException {
        dict = MapUtil.sortByValue(dict);
        int i = 0;
        for (Map.Entry<String, Integer> e : dict.entrySet()) {
            out.write(e.getKey() + "\t" + e.getValue() + "\n");
            i++;
            if (i > k) {
                break;
            }
        }
        out.flush();
        out.close();
    }

    /**
     * Class used to sort a map according to its values.
     */
    static class MapUtil {

        static <K, V extends Comparable<? super V>> Map<K, V>
                sortByValue(Map<K, V> map) {
            List<Map.Entry<K, V>> list
                    = new LinkedList<>(map.entrySet());
            Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
                @Override
                public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                    return (o2.getValue()).compareTo(o1.getValue());
                }
            });

            Map<K, V> result = new LinkedHashMap<>();
            for (Map.Entry<K, V> entry : list) {
                result.put(entry.getKey(), entry.getValue());
            }
            return result;
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        long start = System.currentTimeMillis();
        String input;
        int y = 0;
        String dir_articles = "/Users/mbouadjenek/Documents/bioinformatics_data/PubMed_Central/dataset/";
        String mapFile = "/Users/mbouadjenek/Documents/bioFactChecking/index_JDBM_v3.0/db_index";
        String nlpmodel = "en-sent.bin";
        String outputtopterms = "output.txt";
        input = args[0];
        y = Integer.valueOf(args[1]);
        dir_articles = args[2];
        mapFile = args[3];
        nlpmodel = args[4];
        outputtopterms = args[5];
        SentencesAnalysis ef = new SentencesAnalysis(dir_articles, mapFile, nlpmodel, outputtopterms);
        ef.processFile(input, y);
//        ef.analyseSentences("PMC1609179", "8788", "55384");
        ef.printTopTerms(500);
        long end = System.currentTimeMillis();
        System.err.println("-------------------------------------------------------------------------");
        long millis = (end - start);
        System.err.println("The processing time took: " + Functions.getTimer(millis) + ".");
        System.err.println("-------------------------------------------------------------------------");

    }

}
