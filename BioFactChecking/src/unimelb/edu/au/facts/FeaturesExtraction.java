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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
import unimelb.edu.au.util.Statistics;
import unimelb.edu.au.util.StringSimilarity;

/**
 *
 * @author mbouadjenek
 */
public class FeaturesExtraction {

    RecordManager recman;
    String dir_articles;
    PrimaryHashMap<String, EntityMention> pmcid2diseases;
    PrimaryHashMap<String, EntityMention> pmcid2genes;
    List<String> vocabulary = new ArrayList<>();
    SentenceDetectorME detector;
    int vsize = 100;
    int topK;

    public FeaturesExtraction(String dir_articles, String mapFile, String nlpmodel, String vocabulary, int topK) throws IOException {
        this.dir_articles = dir_articles;
        recman = RecordManagerFactory.createRecordManager(mapFile);
        pmcid2diseases = recman.hashMap("pmcid2diseases");
        pmcid2genes = recman.hashMap("pmcid2genes");
        pmcid2genes.get("PMC3509075").title_map.get("326").remove("12_32");
        InputStream inputStream = new FileInputStream(nlpmodel);
        SentenceModel model = new SentenceModel(inputStream);
        detector = new SentenceDetectorME(model);
        this.topK = topK;
        try {
            FileInputStream fstream = new FileInputStream(vocabulary);
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
                    if (i == vsize) {
                        break;
                    }
                    i++;
                    StringTokenizer st = new StringTokenizer(str);
                    String term = st.nextToken();
                    this.vocabulary.add(term);
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    public void extract(String f, int i, int j) {
        try {
            FileInputStream fstream = new FileInputStream(f);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
                String str;
                Set<String> pmcids = new HashSet<>();
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
                    String pmc = st.nextToken();
                    int rank = Integer.valueOf(st.nextToken());
                    if (Integer.valueOf(id) < i || Integer.valueOf(id) > j) {
                        continue;
                    }
                    if (rank <= topK) {
                        if (!pmc.toLowerCase().equals("x")) {
                            pmcids.add(pmc);
                        }
                        if (rank == topK) {
                            System.err.println(id + "\t" + object1 + "\t" + object2 + "\ttopK = " + pmcids.size());
                            String vec_features = extract(object1, object2, pmcids);
                            System.out.println(y + " " + vec_features);
                            pmcids.clear();
                        }
                    }
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }

    }

    public String extract(String object1, String object2, Set<String> pmcids) throws FileNotFoundException, IOException {
        String vec_features = "";
        int f_count = 1;
        Set<PubMedDoc> docs = new HashSet<>();
        for (String pmcid : pmcids) {
            String path = PubMedDoc.getAbsoluteFile(pmcid, dir_articles);
            PubMedDoc doc = new PubMedDoc(path);
            doc.replaceConcepts(object1, PubMedDoc.SUFFIX_GENE_A, pmcid2genes.get(pmcid), object2, PubMedDoc.SUFFIX_DISEASE, pmcid2diseases.get(pmcid)); // for GD
//            doc.replaceConcepts(object1, PubMedDoc.SUFFIX_GENE_A, pmcid2genes.get(pmcid), object2, PubMedDoc.SUFFIX_GENE_B, pmcid2genes.get(pmcid));// for PPI

            docs.add(doc);
        }
        /**
         * ************************************
         * Extracting the vocabulary features
         * ************************************
         */
        // processing title
        List<String> titles = new ArrayList<>();
        for (PubMedDoc doc : docs) {
            if (doc.getTitle() != null) {
                titles.add(doc.getTitle());
            }
        }
        List<String> common_sentences_titles = getCommonSentences(titles);
        List<String> union_sentences_titles = getUnionOfSentences(titles);
        List<String> object1_sentences_titles = getSentences(titles, PubMedDoc.SUFFIX_GENE_A);
//        List<String> object2_sentences_titles = getSentences(titles, PubMedDoc.SUFFIX_GENE_B);
        List<String> object2_sentences_titles = getSentences(titles, PubMedDoc.SUFFIX_DISEASE);
        for (String term : vocabulary) {
            int occ = 0;
            for (String sentence : common_sentences_titles) {
                if (sentence.contains("_" + term + "_")) {
                    occ++;
                }
            }
            vec_features += (f_count++) + ":" + occ + " ";
        }
        // processing abstract
        List<String> abstracts = new ArrayList<>();
        for (PubMedDoc doc : docs) {
            if (doc.getAbstract() != null) {
                abstracts.add(doc.getAbstract());
            }
        }
        List<String> common_sentences_abstracts = getCommonSentences(abstracts);
        List<String> union_sentences_abstracts = getUnionOfSentences(abstracts);
        List<String> object1_sentences_abstracts = getSentences(abstracts, PubMedDoc.SUFFIX_GENE_A);
//        List<String> object2_sentences_abstracts = getSentences(abstracts, PubMedDoc.SUFFIX_GENE_B);
        List<String> object2_sentences_abstracts = getSentences(abstracts, PubMedDoc.SUFFIX_DISEASE);
        for (String term : vocabulary) {
            int occ = 0;
            for (String sentence : common_sentences_abstracts) {
                if (sentence.contains("_" + term + "_")) {
                    occ++;
                }
            }
            vec_features += (f_count++) + ":" + occ + " ";
        }
        // processing body
        List<String> bodies = new ArrayList<>();
        for (PubMedDoc doc : docs) {
            if (doc.getBody() != null) {
                bodies.add(doc.getBody());
            }
        }
        List<String> common_sentences_bodies = getCommonSentences(bodies);
        List<String> union_sentences_bodies = getUnionOfSentences(bodies);
        List<String> object1_sentences_bodies = getSentences(bodies, PubMedDoc.SUFFIX_GENE_A);
//        List<String> object2_sentences_bodies = getSentences(bodies, PubMedDoc.SUFFIX_GENE_B);
        List<String> object2_sentences_bodies = getSentences(bodies, PubMedDoc.SUFFIX_DISEASE);
        for (String term : vocabulary) {
            int occ = 0;
            for (String sentence : common_sentences_bodies) {
                if (sentence.contains("_" + term + "_")) {
                    occ++;
                }
            }
            vec_features += (f_count++) + ":" + occ + " ";
        }
        /**
         * ************************************
         * Extracting statistic-based features
         * ************************************
         */
        // statistics on titles
        vec_features += (f_count++) + ":" + common_sentences_titles.size() + " "; // matching        
        vec_features += (f_count++) + ":" + (((double) common_sentences_titles.size()) / ((double) union_sentences_titles.size())) + " "; // jaccard 
        vec_features += (f_count++) + ":" + (((double) 2 * common_sentences_titles.size()) / ((double) object1_sentences_titles.size() + object2_sentences_titles.size())) + " "; // dice
        vec_features += (f_count++) + ":" + (((double) common_sentences_titles.size()) / Math.sqrt((double) object1_sentences_titles.size() * object2_sentences_titles.size())) + " "; // cosine
        // statistics on abstracts
        vec_features += (f_count++) + ":" + common_sentences_abstracts.size() + " "; // matching        
        vec_features += (f_count++) + ":" + (((double) common_sentences_abstracts.size()) / ((double) union_sentences_abstracts.size())) + " "; // jaccard 
        vec_features += (f_count++) + ":" + (((double) 2 * common_sentences_abstracts.size()) / ((double) object1_sentences_abstracts.size() + object2_sentences_abstracts.size())) + " "; // dice
        vec_features += (f_count++) + ":" + (((double) common_sentences_abstracts.size()) / Math.sqrt((double) object1_sentences_abstracts.size() * object2_sentences_abstracts.size())) + " "; // cosine
        // statistics on bodies
        vec_features += (f_count++) + ":" + common_sentences_bodies.size() + " "; // matching        
        vec_features += (f_count++) + ":" + (((double) common_sentences_bodies.size()) / ((double) union_sentences_bodies.size())) + " "; // jaccard 
        vec_features += (f_count++) + ":" + (((double) 2 * common_sentences_bodies.size()) / ((double) object1_sentences_bodies.size() + object2_sentences_bodies.size())) + " "; // dice
        vec_features += (f_count++) + ":" + (((double) common_sentences_bodies.size()) / Math.sqrt((double) object1_sentences_bodies.size() * object2_sentences_bodies.size())) + " "; // cosine        
        /**
         * **********************************************
         * Extracting sentence similarity-based features
         * **********************************************
         */
        //**********************************************
        //**********  Similarities on titles  **********
        //**********************************************
        // Compute Overlap
        List<Double> list = new ArrayList<>();
        for (String object1_sentence : object1_sentences_titles) {
            for (String object2_sentence : object2_sentences_titles) {
                list.add((double) StringSimilarity.getOverlapSimilarity(object1_sentence.replace("_", " "), object2_sentence.replace("_", " "), Functions.analyzer));
            }
        }
        double[] arr = list.stream().mapToDouble(i -> i).toArray();
        vec_features += (f_count++) + ":" + Statistics.getSumArray(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getStdDev(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getMinArray(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getMaxArray(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getArithmeticMean(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getGeometricMean(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getHarmonicMean(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getCoefficientVariation(arr) + " ";
        // Compute Jaccard
        list = new ArrayList<>();
        for (String object1_sentence : object1_sentences_titles) {
            for (String object2_sentence : object2_sentences_titles) {
                list.add((double) StringSimilarity.getJaccardSimilarity(object1_sentence.replace("_", " "), object2_sentence.replace("_", " "), Functions.analyzer));
            }
        }
        arr = list.stream().mapToDouble(i -> i).toArray();
        vec_features += (f_count++) + ":" + Statistics.getSumArray(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getStdDev(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getMinArray(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getMaxArray(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getArithmeticMean(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getGeometricMean(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getHarmonicMean(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getCoefficientVariation(arr) + " ";
        // Compute Dice
        list = new ArrayList<>();
        for (String object1_sentence : object1_sentences_titles) {
            for (String object2_sentence : object2_sentences_titles) {
                list.add((double) StringSimilarity.getDiceSimilarity(object1_sentence.replace("_", " "), object2_sentence.replace("_", " "), Functions.analyzer));
            }
        }
        arr = list.stream().mapToDouble(i -> i).toArray();
        vec_features += (f_count++) + ":" + Statistics.getSumArray(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getStdDev(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getMinArray(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getMaxArray(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getArithmeticMean(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getGeometricMean(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getHarmonicMean(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getCoefficientVariation(arr) + " ";
        // Compute Matching
        list = new ArrayList<>();
        for (String object1_sentence : object1_sentences_titles) {
            for (String object2_sentence : object2_sentences_titles) {
                list.add((double) StringSimilarity.getMatchingSimilarity(object1_sentence.replace("_", " "), object2_sentence.replace("_", " "), Functions.analyzer));
            }
        }
        arr = list.stream().mapToDouble(i -> i).toArray();
        vec_features += (f_count++) + ":" + Statistics.getSumArray(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getStdDev(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getMinArray(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getMaxArray(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getArithmeticMean(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getGeometricMean(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getHarmonicMean(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getCoefficientVariation(arr) + " ";
        // Compute cosine
        list = new ArrayList<>();
        for (String object1_sentence : object1_sentences_titles) {
            for (String object2_sentence : object2_sentences_titles) {
                list.add((double) StringSimilarity.getCosineSimilarity(object1_sentence.replace("_", " "), object2_sentence.replace("_", " "), Functions.analyzer));
            }
        }
        arr = list.stream().mapToDouble(i -> i).toArray();
        vec_features += (f_count++) + ":" + Statistics.getSumArray(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getStdDev(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getMinArray(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getMaxArray(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getArithmeticMean(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getGeometricMean(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getHarmonicMean(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getCoefficientVariation(arr) + " ";

        //**********************************************
        //**********  Similarities on abstracts  **********
        //**********************************************
        // Compute Overlap
        list = new ArrayList<>();
        for (String object1_sentence : object1_sentences_abstracts) {
            for (String object2_sentence : object2_sentences_abstracts) {
                list.add((double) StringSimilarity.getOverlapSimilarity(object1_sentence.replace("_", " "), object2_sentence.replace("_", " "), Functions.analyzer));
            }
        }
        arr = list.stream().mapToDouble(i -> i).toArray();
        vec_features += (f_count++) + ":" + Statistics.getSumArray(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getStdDev(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getMinArray(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getMaxArray(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getArithmeticMean(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getGeometricMean(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getHarmonicMean(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getCoefficientVariation(arr) + " ";
        // Compute Jaccard
        list = new ArrayList<>();
        for (String object1_sentence : object1_sentences_abstracts) {
            for (String object2_sentence : object2_sentences_abstracts) {
                list.add((double) StringSimilarity.getJaccardSimilarity(object1_sentence.replace("_", " "), object2_sentence.replace("_", " "), Functions.analyzer));
            }
        }
        arr = list.stream().mapToDouble(i -> i).toArray();
        vec_features += (f_count++) + ":" + Statistics.getSumArray(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getStdDev(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getMinArray(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getMaxArray(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getArithmeticMean(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getGeometricMean(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getHarmonicMean(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getCoefficientVariation(arr) + " ";
        // Compute Dice
        list = new ArrayList<>();
        for (String object1_sentence : object1_sentences_abstracts) {
            for (String object2_sentence : object2_sentences_abstracts) {
                list.add((double) StringSimilarity.getDiceSimilarity(object1_sentence.replace("_", " "), object2_sentence.replace("_", " "), Functions.analyzer));
            }
        }
        arr = list.stream().mapToDouble(i -> i).toArray();
        vec_features += (f_count++) + ":" + Statistics.getSumArray(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getStdDev(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getMinArray(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getMaxArray(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getArithmeticMean(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getGeometricMean(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getHarmonicMean(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getCoefficientVariation(arr) + " ";
        // Compute Matching
        list = new ArrayList<>();
        for (String object1_sentence : object1_sentences_abstracts) {
            for (String object2_sentence : object2_sentences_abstracts) {
                list.add((double) StringSimilarity.getMatchingSimilarity(object1_sentence.replace("_", " "), object2_sentence.replace("_", " "), Functions.analyzer));
            }
        }
        arr = list.stream().mapToDouble(i -> i).toArray();
        vec_features += (f_count++) + ":" + Statistics.getSumArray(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getStdDev(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getMinArray(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getMaxArray(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getArithmeticMean(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getGeometricMean(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getHarmonicMean(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getCoefficientVariation(arr) + " ";
        // Compute cosine
        list = new ArrayList<>();
        for (String object1_sentence : object1_sentences_abstracts) {
            for (String object2_sentence : object2_sentences_abstracts) {
                list.add((double) StringSimilarity.getCosineSimilarity(object1_sentence.replace("_", " "), object2_sentence.replace("_", " "), Functions.analyzer));
            }
        }
        arr = list.stream().mapToDouble(i -> i).toArray();
        vec_features += (f_count++) + ":" + Statistics.getSumArray(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getStdDev(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getMinArray(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getMaxArray(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getArithmeticMean(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getGeometricMean(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getHarmonicMean(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getCoefficientVariation(arr) + " ";
        //**********************************************
        //**********  Similarities on bodies  **********
        //**********************************************
        // Compute Overlap
        list = new ArrayList<>();
        for (String object1_sentence : object1_sentences_bodies) {
            for (String object2_sentence : object2_sentences_bodies) {
                list.add((double) StringSimilarity.getOverlapSimilarity(object1_sentence.replace("_", " "), object2_sentence.replace("_", " "), Functions.analyzer));
            }
        }
        arr = list.stream().mapToDouble(i -> i).toArray();
        vec_features += (f_count++) + ":" + Statistics.getSumArray(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getStdDev(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getMinArray(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getMaxArray(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getArithmeticMean(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getGeometricMean(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getHarmonicMean(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getCoefficientVariation(arr) + " ";
        // Compute Jaccard
        list = new ArrayList<>();
        for (String object1_sentence : object1_sentences_bodies) {
            for (String object2_sentence : object2_sentences_bodies) {
                list.add((double) StringSimilarity.getJaccardSimilarity(object1_sentence.replace("_", " "), object2_sentence.replace("_", " "), Functions.analyzer));
            }
        }
        arr = list.stream().mapToDouble(i -> i).toArray();
        vec_features += (f_count++) + ":" + Statistics.getSumArray(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getStdDev(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getMinArray(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getMaxArray(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getArithmeticMean(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getGeometricMean(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getHarmonicMean(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getCoefficientVariation(arr) + " ";
        // Compute Dice
        list = new ArrayList<>();
        for (String object1_sentence : object1_sentences_bodies) {
            for (String object2_sentence : object2_sentences_bodies) {
                list.add((double) StringSimilarity.getDiceSimilarity(object1_sentence.replace("_", " "), object2_sentence.replace("_", " "), Functions.analyzer));
            }
        }
        arr = list.stream().mapToDouble(i -> i).toArray();
        vec_features += (f_count++) + ":" + Statistics.getSumArray(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getStdDev(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getMinArray(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getMaxArray(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getArithmeticMean(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getGeometricMean(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getHarmonicMean(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getCoefficientVariation(arr) + " ";
        // Compute Matching
        list = new ArrayList<>();
        for (String object1_sentence : object1_sentences_bodies) {
            for (String object2_sentence : object2_sentences_bodies) {
                list.add((double) StringSimilarity.getMatchingSimilarity(object1_sentence.replace("_", " "), object2_sentence.replace("_", " "), Functions.analyzer));
            }
        }
        arr = list.stream().mapToDouble(i -> i).toArray();
        vec_features += (f_count++) + ":" + Statistics.getSumArray(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getStdDev(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getMinArray(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getMaxArray(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getArithmeticMean(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getGeometricMean(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getHarmonicMean(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getCoefficientVariation(arr) + " ";
        // Compute cosine
        list = new ArrayList<>();
        for (String object1_sentence : object1_sentences_bodies) {
            for (String object2_sentence : object2_sentences_bodies) {
                list.add((double) StringSimilarity.getCosineSimilarity(object1_sentence.replace("_", " "), object2_sentence.replace("_", " "), Functions.analyzer));
            }
        }
        arr = list.stream().mapToDouble(i -> i).toArray();
        vec_features += (f_count++) + ":" + Statistics.getSumArray(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getStdDev(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getMinArray(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getMaxArray(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getArithmeticMean(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getGeometricMean(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getHarmonicMean(arr) + " ";
        vec_features += (f_count++) + ":" + Statistics.getCoefficientVariation(arr) + " ";

        return vec_features.replace("NaN", "0.0");
    }

    /**
     * This method return common sentences from the text (top documents).
     *
     * @param texts
     * @return List of common sentences.
     */
    public List<String> getCommonSentences(List<String> texts) {
        List<String> out = new ArrayList<>();
        for (String text : texts) {
            String[] sentences = detector.sentDetect(text.replace("\n", " "));
            for (String sentence : sentences) {
                if (sentence.contains(PubMedDoc.SUFFIX_GENE_A) && sentence.contains(PubMedDoc.SUFFIX_DISEASE)) {
                    String pre_sentence = Functions.processString(sentence, new EnglishAnalyzer(PubMedSpecialWords.ENGLISH_STOP_WORDS_SET));
                    pre_sentence = pre_sentence.replaceAll(PubMedDoc.SUFFIX_GENE_A.toLowerCase() + "[a-z0-9]*", "");
//                    pre_sentence = pre_sentence.replaceAll(PubMedDoc.SUFFIX_GENE_B.toLowerCase() + "[a-z0-9]*", "");
                    pre_sentence = pre_sentence.replaceAll(PubMedDoc.SUFFIX_DISEASE.toLowerCase() + "[a-z0-9_]*", "");
                    pre_sentence = "_" + pre_sentence.replace("  ", " ").replace(" ", "_") + "_";
                    out.add(pre_sentence);
                }
            }
        }
        return out;
    }

    /**
     * This method return the union of sentences from the text (top documents).
     *
     * @param texts
     * @return List of common sentences.
     */
    public List<String> getUnionOfSentences(List<String> texts) {
        List<String> out = new ArrayList<>();
        for (String text : texts) {
            String[] sentences = detector.sentDetect(text.replace("\n", " "));
            for (String sentence : sentences) {
                if (sentence.contains(PubMedDoc.SUFFIX_GENE_A) || sentence.contains(PubMedDoc.SUFFIX_DISEASE)) {
                    String pre_sentence = Functions.processString(sentence, new EnglishAnalyzer(PubMedSpecialWords.ENGLISH_STOP_WORDS_SET));
                    pre_sentence = pre_sentence.replaceAll(PubMedDoc.SUFFIX_GENE_A.toLowerCase() + "[a-z0-9]*", "");
//                    pre_sentence = pre_sentence.replaceAll(PubMedDoc.SUFFIX_GENE_B.toLowerCase() + "[a-z0-9]*", "");
                    pre_sentence = pre_sentence.replaceAll(PubMedDoc.SUFFIX_DISEASE.toLowerCase() + "[a-z0-9_]*", "");
                    pre_sentence = "_" + pre_sentence.replace("  ", " ").replace(" ", "_") + "_";
                    out.add(pre_sentence);
                }
            }
        }
        return out;
    }

    /**
     * This method return the sentences of mention from the text (top
     * documents).
     *
     * @param texts
     * @param suffix
     * @return List of common sentences.
     */
    public List<String> getSentences(List<String> texts, String suffix) {
        List<String> out = new ArrayList<>();
        for (String text : texts) {
            String[] sentences = detector.sentDetect(text.replace("\n", " "));
            for (String sentence : sentences) {
                if (sentence.contains(suffix)) {
                    String pre_sentence = Functions.processString(sentence, new EnglishAnalyzer(PubMedSpecialWords.ENGLISH_STOP_WORDS_SET));
                    pre_sentence = pre_sentence.replaceAll(suffix.toLowerCase() + "[a-z0-9]*", "");
                    pre_sentence = "_" + pre_sentence.replace("  ", " ").replace(" ", "_") + "_";
                    out.add(pre_sentence);
                }
            }
        }
        return out;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        long start = System.currentTimeMillis();
        String input = "/Users/mbouadjenek/Documents/bioFactChecking/search_resultsAlgo2.txt";
        String dir_articles = "/Users/mbouadjenek/Documents/bioinformatics_data/PubMed_Central/dataset/";
        String mapFile = "/Users/mbouadjenek/Documents/bioFactChecking/index_JDBM_v3.0/db_index";
        String nlpmodel = "en-sent.bin";
        String vocabulary = "/Users/mbouadjenek/Documents/bioFactChecking/pos_topterms.txt";
        int i = 1, j = 4;
        int topK = 3;
        input = args[0];
        dir_articles = args[1];
        mapFile = args[2];
        nlpmodel = args[3];
        vocabulary = args[4];
        topK = Integer.valueOf(args[5]);
        i = Integer.valueOf(args[6]);
        j = Integer.valueOf(args[7]);
        FeaturesExtraction ef = new FeaturesExtraction(dir_articles, mapFile, nlpmodel, vocabulary, topK);
        ef.extract(input, i, j);
        long end = System.currentTimeMillis();
        System.err.println("-------------------------------------------------------------------------");
        long millis = (end - start);
        System.err.println("The processing time took: " + Functions.getTimer(millis) + ".");
        System.err.println("-------------------------------------------------------------------------");
    }

}
