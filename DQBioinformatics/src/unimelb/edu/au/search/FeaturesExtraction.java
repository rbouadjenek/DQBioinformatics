/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unimelb.edu.au.search;

import unimelb.edu.au.search.similarities.MySimilarity;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.DistributionLL;
import org.apache.lucene.search.similarities.IBSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.search.similarities.LambdaDF;
import org.apache.lucene.search.similarities.Normalization;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import unimelb.edu.au.doc.PubMedDoc;
import unimelb.edu.au.doc.MutualReferences;
import unimelb.edu.au.indexing.TermFreqVector;
import unimelb.edu.au.lucene.analysis.en.MyEnglishAnalyzer;
import unimelb.edu.au.lucene.analysis.en.PubMedSpecialWords;
import unimelb.edu.au.util.Functions;
import unimelb.edu.au.util.Statistics;
import unimelb.edu.au.util.StringSimilarity;

/**
 *
 * @author mbouadjenek
 */
public class FeaturesExtraction {

    private final IndexSearcher docIS;
    private final IndexSearcher orgIS;
    protected File queries;
    private static final BooleanClause.Occur[] flags = {BooleanClause.Occur.FILTER, BooleanClause.Occur.SHOULD};

    public FeaturesExtraction(String indexDir, String index_organism, String queries) throws IOException {
        Directory dir1 = FSDirectory.open(new File(indexDir).toPath());
        docIS = new IndexSearcher(DirectoryReader.open(dir1));
        docIS.setSimilarity(new MySimilarity());
        Directory dir2 = FSDirectory.open(new File(index_organism).toPath());
        orgIS = new IndexSearcher(DirectoryReader.open(dir2));
        this.queries = new File(queries);
    }

    void search(int a, int b) throws FileNotFoundException, ParseException, IOException {
        int i = 0;
        String[] GPXFILES1 = PubMedSpecialWords.KEYWORDS_COMPLETENESS_QUALIFIERS.toArray(new String[PubMedSpecialWords.KEYWORDS_COMPLETENESS_QUALIFIERS.size()]);
        FileInputStream fstream;
        fstream = new FileInputStream(queries);
        // Get the object of DataInputStream
        DataInputStream in = new DataInputStream(fstream);
//        System.out.println("queryid,acession,pmc,titleOverlapSimilarity,abstractOverlapSimilarity,bodyOverlapSimilarity,allOverlapSimilarity,titleJaccardSimilarity,abstractJaccardSimilarity,bodyJaccardSimilarity,allJaccardSimilarity,titleDiceSimilarity,abstractDiceSimilarity,bodyDiceSimilarity,allDiceSimilarity,titleMatchingSimilarity,abstractMatchingSimilarity,bodyMatchingSimilarity,allMatchingSimilarity,titleCosineSimilarity,abstractCosineSimilarity,bodyCosineSimilarity,allCosineSimilarity,titleDotProductSimilarity,abstractDotProductSimilarity,bodyDotProductSimilarity,titleSumTFIDFScore,abstractSumTFIDFScore,bodySumTFIDFScore,allSumTFIDFScore,titleLuceneVSMScore,abstractLuceneVSMScore,bodyLuceneVSMScore,allLuceneVSMScore,titleBM25Score,abstractBM25Score,bodyBM25Score,allBM25Score,titleLMJelinekMercerScore,abstractLMJelinekMercerScore,bodyLMJelinekMercerScore,allLMJelinekMercerScore,titleLMDirichletScore,abstractLMDirichletScore,bodyLMDirichletScore,allLMDirichletScore,titleIBSimilarityScore,abstractIBSimilarityScore,bodyIBSimilarityScore,allIBSimilarityScore,titleSumTF,titleSDTF,titleMinTF,titleMaxTF,titleArithmeticMeanTF,titleGeometricMeanTF,titleHarmonicMeanTF,titleCoefficientofVariationTF,abstractSumTF,abstractSDTF,abstractMinTF,abstractMaxTF,abstractArithmeticMeanTF,abstractGeometricMeanTF,abstractHarmonicMeanTF,abstractCoefficientofVariationTF,bodySumTF,bodySDTF,bodyMinTF,bodyMaxTF,bodyArithmeticMeanTF,bodyGeometricMeanTF,bodyHarmonicMeanTF,bodyCoefficientofVariationTF,titleRDCS,abstractRDCS,bodyRDCS,titleSumIDF,titleSDIDF,titleMinIDF,titleMaxIDF,titleArithmeticMeanIDF,titleGeometricMeanIDF,titleHarmonicMeanIDF,titleCoefficientofVariationIDF,abstractSumIDF,abstractSDIDF,abstractMinIDF,abstractMaxIDF,abstractArithmeticMeanIDF,abstractGeometricMeanIDF,abstractHarmonicMeanIDF,abstractCoefficientofVariationIDF,bodySumIDF,bodySDIDF,bodyMinIDF,bodyMaxIDF,bodyArithmeticMeanIDF,bodyGeometricMeanIDF,bodyHarmonicMeanIDF,bodyCoefficientofVariationIDF,titleSumICTF,titleSDICTF,titleMinICTF,titleMaxICTF,titleArithmeticMeanICTF,titleGeometricMeanICTF,titleHarmonicMeanICTF,titleCoefficientofVariationICTF,abstractSumICTF,abstractSDICTF,abstractMinICTF,abstractMaxICTF,abstractArithmeticMeanICTF,abstractGeometricMeanICTF,abstractHarmonicMeanICTF,abstractCoefficientofVariationICTF,bodySumICTF,bodySDICTF,bodyMinICTF,bodyMaxICTF,bodyArithmeticMeanICTF,bodyGeometricMeanICTF,bodyHarmonicMeanICTF,bodyCoefficientofVariationICTF,titleSumSCQ,titleSDSCQ,titleMinSCQ,titleMaxSCQ,titleArithmeticMeanSCQ,titleGeometricMeanSCQ,titleHarmonicMeanSCQ,titleCoefficientofVariationSCQ,abstractSumSCQ,abstractSDSCQ,abstractMinSCQ,abstractMaxSCQ,abstractArithmeticMeanSCQ,abstractGeometricMeanSCQ,abstractHarmonicMeanSCQ,abstractCoefficientofVariationSCQ,bodySumSCQ,bodySDSCQ,bodyMinSCQ,bodyMaxSCQ,bodyArithmeticMeanSCQ,bodyGeometricMeanSCQ,bodyHarmonicMeanSCQ,bodyCoefficientofVariationSCQ,titleSCS,abstractSCS,bodySCS,titleClarity1,abstractClarity1,bodyClarity1,titleClarity100,abstractClarity100,bodyClarity100,isMainOrgSciNameInTitle,isMainOrgSciNameInAbstract,isMainOrgSciNameInBody,isMainOrgSynonymInTitle,isMainOrgSynonymInAbstract,isMainOrgSynonymInBody,isMainOrgMisspellingInTitle,isMainOrgMisspellingInAbstract,isMainOrgMisspellingInBody,isMainOrgGenbankCommonNameInTitle,isMainOrgGenbankCommonNameInAbstract,isMainOrgGenbankCommonNameInBody,isMainOrgEquivalentNameInTitle,isMainOrgEquivalentNameInAbstract,isMainOrgEquivalentNameInBody,isMainOrgCommonNameInTitle,isMainOrgCommonNameInAbstract,isMainOrgCommonNameInBody,isMainOrgGenbankSynonymInTitle,isMainOrgGenbankSynonymInAbstract,isMainOrgGenbankSynonymInBody,isMainOrgMisnomerInTitle,isMainOrgMisnomerInAbstract,isMainOrgMisnomerInBody,isMainOrgAcronymInTitle,isMainOrgAcronymInAbstract,isMainOrgAcronymInBody,titleQS,abstractQS,bodyQS,allQS,QLen,citationNbr,logCitationNbr,nbrOrganisms,logAge,cat");
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
                if (i < a) {
                    continue;
                }
                if (i >= b) {
                    break;
                }
//                System.err.println(i + "- " + str);
                StringTokenizer st = new StringTokenizer(str, "\t");
                String queryid = st.nextToken();
                String article = st.nextToken();
                String pmc = st.nextToken();
                String accession = st.nextToken();
                int citationNbr = Integer.parseInt(st.nextToken());
                long creationDateseconds = Long.parseLong(st.nextToken());
                long updateDateseconds = Long.parseLong(st.nextToken());
                String Status = st.nextToken();
                String annotation = st.nextToken();
                String query_text = st.nextToken();
                List<String> organismList = new ArrayList<>();
                while (st.hasMoreTokens()) {
                    organismList.add(st.nextToken());
                }

                long start = System.currentTimeMillis();

                //***********************************************************************************************
                //*******************************      FEATURES     *********************************************
                //***********************************************************************************************
                double titleOverlapSimilarity, abstractOverlapSimilarity, bodyOverlapSimilarity, allOverlapSimilarity;
                double titleJaccardSimilarity, abstractJaccardSimilarity, bodyJaccardSimilarity, allJaccardSimilarity;
                double titleDiceSimilarity, abstractDiceSimilarity, bodyDiceSimilarity, allDiceSimilarity;
                double titleMatchingSimilarity, abstractMatchingSimilarity, bodyMatchingSimilarity, allMatchingSimilarity;
                double titleCosineSimilarity, abstractCosineSimilarity, bodyCosineSimilarity, allCosineSimilarity;
                double titleDotProductSimilarity, abstractDotProductSimilarity, bodyDotProductSimilarity;

                double titleSumTFIDFScore, abstractSumTFIDFScore, bodySumTFIDFScore, allSumTFIDFScore;
                double titleLuceneVSMScore, abstractLuceneVSMScore, bodyLuceneVSMScore, allLuceneVSMScore;
                double titleBM25Score, abstractBM25Score, bodyBM25Score, allBM25Score;
                double titleLMJelinekMercerScore, abstractLMJelinekMercerScore, bodyLMJelinekMercerScore, allLMJelinekMercerScore;
                double titleLMDirichletScore, abstractLMDirichletScore, bodyLMDirichletScore, allLMDirichletScore;
                double titleIBSimilarityScore, abstractIBSimilarityScore, bodyIBSimilarityScore, allIBSimilarityScore;

                double titleSumTF, titleSDTF, titleMinTF, titleMaxTF, titleArithmeticMeanTF, titleGeometricMeanTF, titleHarmonicMeanTF, titleCoefficientofVariationTF;
                double abstractSumTF, abstractSDTF, abstractMinTF, abstractMaxTF, abstractArithmeticMeanTF, abstractGeometricMeanTF, abstractHarmonicMeanTF, abstractCoefficientofVariationTF;
                double bodySumTF, bodySDTF, bodyMinTF, bodyMaxTF, bodyArithmeticMeanTF, bodyGeometricMeanTF, bodyHarmonicMeanTF, bodyCoefficientofVariationTF;

                double titleRDCS, abstractRDCS, bodyRDCS;

                double titleSumIDF, titleSDIDF, titleMinIDF, titleMaxIDF, titleArithmeticMeanIDF, titleGeometricMeanIDF, titleHarmonicMeanIDF, titleCoefficientofVariationIDF;
                double abstractSumIDF, abstractSDIDF, abstractMinIDF, abstractMaxIDF, abstractArithmeticMeanIDF, abstractGeometricMeanIDF, abstractHarmonicMeanIDF, abstractCoefficientofVariationIDF;
                double bodySumIDF, bodySDIDF, bodyMinIDF, bodyMaxIDF, bodyArithmeticMeanIDF, bodyGeometricMeanIDF, bodyHarmonicMeanIDF, bodyCoefficientofVariationIDF;

                double titleSumICTF, titleSDICTF, titleMinICTF, titleMaxICTF, titleArithmeticMeanICTF, titleGeometricMeanICTF, titleHarmonicMeanICTF, titleCoefficientofVariationICTF;
                double abstractSumICTF, abstractSDICTF, abstractMinICTF, abstractMaxICTF, abstractArithmeticMeanICTF, abstractGeometricMeanICTF, abstractHarmonicMeanICTF, abstractCoefficientofVariationICTF;
                double bodySumICTF, bodySDICTF, bodyMinICTF, bodyMaxICTF, bodyArithmeticMeanICTF, bodyGeometricMeanICTF, bodyHarmonicMeanICTF, bodyCoefficientofVariationICTF;

                double titleSumSCQ, titleSDSCQ, titleMinSCQ, titleMaxSCQ, titleArithmeticMeanSCQ, titleGeometricMeanSCQ, titleHarmonicMeanSCQ, titleCoefficientofVariationSCQ;
                double abstractSumSCQ, abstractSDSCQ, abstractMinSCQ, abstractMaxSCQ, abstractArithmeticMeanSCQ, abstractGeometricMeanSCQ, abstractHarmonicMeanSCQ, abstractCoefficientofVariationSCQ;
                double bodySumSCQ, bodySDSCQ, bodyMinSCQ, bodyMaxSCQ, bodyArithmeticMeanSCQ, bodyGeometricMeanSCQ, bodyHarmonicMeanSCQ, bodyCoefficientofVariationSCQ;

                double titleSCS, abstractSCS, bodySCS;

                //double titleKLDiv, abstractKLDiv, bodyKLDiv;

                double titleClarity1, abstractClarity1, bodyClarity1;
                double titleClarity5, abstractClarity5, bodyClarity5;
                double titleClarity10, abstractClarity10, bodyClarity10;
                double titleClarity20, abstractClarity20, bodyClarity20;
                double titleClarity50, abstractClarity50, bodyClarity50;
                double titleClarity100, abstractClarity100, bodyClarity100;

                boolean isMainOrgSciNameInTitle = false, isMainOrgSciNameInAbstract = false, isMainOrgSciNameInBody = false;
                boolean isMainOrgSynonymInTitle = false, isMainOrgSynonymInAbstract = false, isMainOrgSynonymInBody = false;
                boolean isMainOrgMisspellingInTitle = false, isMainOrgMisspellingInAbstract = false, isMainOrgMisspellingInBody = false;
                boolean isMainOrgGenbankCommonNameInTitle = false, isMainOrgGenbankCommonNameInAbstract = false, isMainOrgGenbankCommonNameInBody = false;
                boolean isMainOrgEquivalentNameInTitle = false, isMainOrgEquivalentNameInAbstract = false, isMainOrgEquivalentNameInBody = false;
                boolean isMainOrgCommonNameInTitle = false, isMainOrgCommonNameInAbstract = false, isMainOrgCommonNameInBody = false;
                boolean isMainOrgGenbankSynonymInTitle = false, isMainOrgGenbankSynonymInAbstract = false, isMainOrgGenbankSynonymInBody = false;
                boolean isMainOrgMisnomerInTitle = false, isMainOrgMisnomerInAbstract = false, isMainOrgMisnomerInBody = false;
                boolean isMainOrgAcronymInTitle = false, isMainOrgAcronymInAbstract = false, isMainOrgAcronymInBody = false;

                double titlePopularityMainOrganism, abstractPopularityMainOrganism, bodyPopularityMainOrganism, allPopularityMainOrganism;

                boolean isMainOrgSciNameInDefinition = false;
                double overlapMainOrgSciNameDefinition;

                double titleQS, abstractQS, bodyQS, allQS;
                int QLen;
                double logCitationNbr = Statistics.log2(citationNbr);
                int nbrOrganisms = organismList.size();
                long age;
                double logAge;
                String cat;

                String queryTokenized = Functions.processString(query_text, RecordQuery.analyzer);
                for (int h = 0; h < GPXFILES1.length; h++) {
                    String com_qualifier = GPXFILES1[h];
                    if (queryTokenized.toLowerCase().contains(com_qualifier)) {
//                        System.out.println(queryid + "\tCQ" + (h+1));
                    }
                    System.out.println(com_qualifier + "\tCQ" + (h+1));
                }
                for (String com_qualifier : PubMedSpecialWords.KEYWORDS_COMPLETENESS_QUALIFIERS) {
                    queryTokenized = queryTokenized.replaceAll("(?i)" + com_qualifier, ""); // This remove the completeness qualifiers
                }
                queryTokenized = Functions.processString(query_text, new MyEnglishAnalyzer(PubMedSpecialWords.ENGLISH_STOP_WORDS_SET2, PubMedSpecialWords.KEYWORDS_SET));

                String[] queryArray = queryTokenized.split(" ");
                Map<String, Integer> queryMap = new HashMap<>();
                for (String w : queryArray) {
                    if (queryMap.containsKey(w)) {
                        int occ = queryMap.get(w) + 1;
                        queryMap.put(w, occ);
                    } else {
                        queryMap.put(w, 1);
                    }
                }
                Set<String> querySet = new HashSet<>(Arrays.asList(queryArray));
                //***********************************************************************************************
                //*****************    Extract TermFreq vector for the relevant document   **********************
                //*********************************************************************************************** 
                QueryParser parser = new QueryParser(PubMedDoc.ID_PMC, new StandardAnalyzer());
                Query query = parser.parse(pmc);
                TopDocs hits = docIS.search(query, 1);
                TermFreqVector tfvTitle = new TermFreqVector(docIS.getIndexReader().getTermVector(hits.scoreDocs[0].doc, PubMedDoc.TITLE));
                TermFreqVector tfvAbstract = new TermFreqVector(docIS.getIndexReader().getTermVector(hits.scoreDocs[0].doc, PubMedDoc.ABSTRACT));
                TermFreqVector tfvBody = new TermFreqVector(docIS.getIndexReader().getTermVector(hits.scoreDocs[0].doc, PubMedDoc.BODY));
                Set<String> allSet = new HashSet<>();
                allSet.addAll(tfvTitle.getTerms());
                allSet.addAll(tfvAbstract.getTerms());
                allSet.addAll(tfvBody.getTerms());
                //***********************************************************************************************
                //*****************************    Set Similarities based features   ****************************
                //*********************************************************************************************** 
                // Compute Overlap
                titleOverlapSimilarity = StringSimilarity.getOverlapSimilarity(querySet, tfvTitle.getTerms());
                abstractOverlapSimilarity = StringSimilarity.getOverlapSimilarity(querySet, tfvAbstract.getTerms());
                bodyOverlapSimilarity = StringSimilarity.getOverlapSimilarity(querySet, tfvBody.getTerms());
                allOverlapSimilarity = StringSimilarity.getOverlapSimilarity(querySet, allSet);
                // Compute Jaccard
                titleJaccardSimilarity = StringSimilarity.getJaccardSimilarity(querySet, tfvTitle.getTerms());
                abstractJaccardSimilarity = StringSimilarity.getJaccardSimilarity(querySet, tfvAbstract.getTerms());
                bodyJaccardSimilarity = StringSimilarity.getJaccardSimilarity(querySet, tfvBody.getTerms());
                allJaccardSimilarity = StringSimilarity.getJaccardSimilarity(querySet, allSet);
                // Compute Dice
                titleDiceSimilarity = StringSimilarity.getDiceSimilarity(querySet, tfvTitle.getTerms());
                abstractDiceSimilarity = StringSimilarity.getDiceSimilarity(querySet, tfvAbstract.getTerms());
                bodyDiceSimilarity = StringSimilarity.getDiceSimilarity(querySet, tfvBody.getTerms());
                allDiceSimilarity = StringSimilarity.getDiceSimilarity(querySet, allSet);
                // Compute Matching
                titleMatchingSimilarity = StringSimilarity.getMatchingSimilarity(querySet, tfvTitle.getTerms());
                abstractMatchingSimilarity = StringSimilarity.getMatchingSimilarity(querySet, tfvAbstract.getTerms());
                bodyMatchingSimilarity = StringSimilarity.getMatchingSimilarity(querySet, tfvBody.getTerms());
                allMatchingSimilarity = StringSimilarity.getMatchingSimilarity(querySet, allSet);
                // Compute cosine
                titleCosineSimilarity = StringSimilarity.getCosineSimilarity(querySet, tfvTitle.getTerms());
                abstractCosineSimilarity = StringSimilarity.getCosineSimilarity(querySet, tfvAbstract.getTerms());
                bodyCosineSimilarity = StringSimilarity.getCosineSimilarity(querySet, tfvBody.getTerms());
                allCosineSimilarity = StringSimilarity.getCosineSimilarity(querySet, allSet);
                // Compute dot product
                titleDotProductSimilarity = tfvTitle.getDotProduct(queryMap);
                abstractDotProductSimilarity = tfvAbstract.getDotProduct(queryMap);
                bodyDotProductSimilarity = tfvBody.getDotProduct(queryMap);
                //*************************************************************************************************
                //**********************************TF-based features ************************************************
                //*************************************************************************************************
                double[] titleTFArray = QueryQualityFeatures.getTF_w(queryTokenized, tfvTitle);
                titleSumTF = Statistics.getSumArray(titleTFArray);
                titleSDTF = Statistics.getStdDev(titleTFArray);
                titleMinTF = Statistics.getMinArray(titleTFArray);
                titleMaxTF = Statistics.getMaxArray(titleTFArray);
                titleArithmeticMeanTF = Statistics.getArithmeticMean(titleTFArray);
                titleGeometricMeanTF = Statistics.getGeometricMean(titleTFArray);
                titleHarmonicMeanTF = Statistics.getHarmonicMean(titleTFArray);
                titleCoefficientofVariationTF = Statistics.getCoefficientVariation(titleTFArray);

                double[] abstractTFArray = QueryQualityFeatures.getTF_w(queryTokenized, tfvAbstract);
                abstractSumTF = Statistics.getSumArray(abstractTFArray);
                abstractSDTF = Statistics.getStdDev(abstractTFArray);
                abstractMinTF = Statistics.getMinArray(abstractTFArray);
                abstractMaxTF = Statistics.getMaxArray(abstractTFArray);
                abstractArithmeticMeanTF = Statistics.getArithmeticMean(abstractTFArray);
                abstractGeometricMeanTF = Statistics.getGeometricMean(abstractTFArray);
                abstractHarmonicMeanTF = Statistics.getHarmonicMean(abstractTFArray);
                abstractCoefficientofVariationTF = Statistics.getCoefficientVariation(abstractTFArray);

                double[] bodyTFArray = QueryQualityFeatures.getTF_w(queryTokenized, tfvBody);
                bodySumTF = Statistics.getSumArray(bodyTFArray);
                bodySDTF = Statistics.getStdDev(bodyTFArray);
                bodyMinTF = Statistics.getMinArray(bodyTFArray);
                bodyMaxTF = Statistics.getMaxArray(bodyTFArray);
                bodyArithmeticMeanTF = Statistics.getArithmeticMean(bodyTFArray);
                bodyGeometricMeanTF = Statistics.getGeometricMean(bodyTFArray);
                bodyHarmonicMeanTF = Statistics.getHarmonicMean(bodyTFArray);
                bodyCoefficientofVariationTF = Statistics.getCoefficientVariation(bodyTFArray); //***********************************************************************************************
                //********************Relevant documents based clarity score (RDCS)*************************
                //***********************************************************************************************
                titleRDCS = QueryQualityFeatures.getRelDocClarityScore(docIS, querySet, tfvTitle, PubMedDoc.TITLE);
                abstractRDCS = QueryQualityFeatures.getRelDocClarityScore(docIS, querySet, tfvAbstract, PubMedDoc.ABSTRACT);
                bodyRDCS = QueryQualityFeatures.getRelDocClarityScore(docIS, querySet, tfvBody, PubMedDoc.BODY);
                //***********************************************************************************************
                //*******************    Lucene Ranking function scores  ***********************
                //*********************************************************************************************** 
                //Title 
                String queryTokenized2 = Functions.processString(query_text, RecordQuery.analyzer);
                for (String com_qualifier : PubMedSpecialWords.KEYWORDS_COMPLETENESS_QUALIFIERS) {
                    queryTokenized2 = queryTokenized2.replaceAll("(?i)" + com_qualifier, ""); // This remove the completeness qualifiers
                }

                String[] fields = {PubMedDoc.ID_PMC, PubMedDoc.TITLE};
                String[] queries_lucene = {pmc, QueryParser.escape(queryTokenized2)};
                Query finalQuery = MultiFieldQueryParser.parse(queries_lucene, fields, flags, new MyEnglishAnalyzer(PubMedSpecialWords.ENGLISH_STOP_WORDS_SET2, PubMedSpecialWords.KEYWORDS_SET));

                titleSumTFIDFScore = QueryQualityFeatures.getSimilarityScore(docIS, finalQuery, new MySimilarity());
                titleLuceneVSMScore = QueryQualityFeatures.getSimilarityScore(docIS, finalQuery, new ClassicSimilarity());
                titleBM25Score = QueryQualityFeatures.getSimilarityScore(docIS, finalQuery, new BM25Similarity());
                titleLMJelinekMercerScore = QueryQualityFeatures.getSimilarityScore(docIS, finalQuery, new LMJelinekMercerSimilarity((float) 0.1));
                titleLMDirichletScore = QueryQualityFeatures.getSimilarityScore(docIS, finalQuery, new LMDirichletSimilarity());
                titleIBSimilarityScore = QueryQualityFeatures.getSimilarityScore(docIS, finalQuery, new IBSimilarity(new DistributionLL(), new LambdaDF(), new Normalization.NoNormalization()));

                //Abstract 
                fields = new String[]{PubMedDoc.ID_PMC, PubMedDoc.ABSTRACT};
                queries_lucene = new String[]{pmc, QueryParser.escape(queryTokenized2)};
                finalQuery = MultiFieldQueryParser.parse(queries_lucene, fields, flags, new MyEnglishAnalyzer(PubMedSpecialWords.ENGLISH_STOP_WORDS_SET2, PubMedSpecialWords.KEYWORDS_SET));

                abstractSumTFIDFScore = QueryQualityFeatures.getSimilarityScore(docIS, finalQuery, new MySimilarity());
                abstractLuceneVSMScore = QueryQualityFeatures.getSimilarityScore(docIS, finalQuery, new ClassicSimilarity());
                abstractBM25Score = QueryQualityFeatures.getSimilarityScore(docIS, finalQuery, new BM25Similarity());
                abstractLMJelinekMercerScore = QueryQualityFeatures.getSimilarityScore(docIS, finalQuery, new LMJelinekMercerSimilarity((float) 0.7));
                abstractLMDirichletScore = QueryQualityFeatures.getSimilarityScore(docIS, finalQuery, new LMDirichletSimilarity());
                abstractIBSimilarityScore = QueryQualityFeatures.getSimilarityScore(docIS, finalQuery, new IBSimilarity(new DistributionLL(), new LambdaDF(), new Normalization.NoNormalization()));

                //Body
                fields = new String[]{PubMedDoc.ID_PMC, PubMedDoc.BODY};
                queries_lucene = new String[]{pmc, QueryParser.escape(queryTokenized2)};
                finalQuery = MultiFieldQueryParser.parse(queries_lucene, fields, flags, new MyEnglishAnalyzer(PubMedSpecialWords.ENGLISH_STOP_WORDS_SET2, PubMedSpecialWords.KEYWORDS_SET));

                bodySumTFIDFScore = QueryQualityFeatures.getSimilarityScore(docIS, finalQuery, new MySimilarity());
                bodyLuceneVSMScore = QueryQualityFeatures.getSimilarityScore(docIS, finalQuery, new ClassicSimilarity());
                bodyBM25Score = QueryQualityFeatures.getSimilarityScore(docIS, finalQuery, new BM25Similarity());
                bodyLMJelinekMercerScore = QueryQualityFeatures.getSimilarityScore(docIS, finalQuery, new LMJelinekMercerSimilarity((float) 0.7));
                bodyLMDirichletScore = QueryQualityFeatures.getSimilarityScore(docIS, finalQuery, new LMDirichletSimilarity());
                bodyIBSimilarityScore = QueryQualityFeatures.getSimilarityScore(docIS, finalQuery, new IBSimilarity(new DistributionLL(), new LambdaDF(), new Normalization.NoNormalization()));

                //All
                fields = new String[]{PubMedDoc.ID_PMC, PubMedDoc.TITLE, PubMedDoc.ABSTRACT, PubMedDoc.BODY};
                queries_lucene = new String[]{pmc, QueryParser.escape(queryTokenized2), QueryParser.escape(queryTokenized2), QueryParser.escape(queryTokenized2)};
                BooleanClause.Occur[] allflags = {BooleanClause.Occur.FILTER, BooleanClause.Occur.SHOULD, BooleanClause.Occur.SHOULD, BooleanClause.Occur.SHOULD};
                finalQuery = MultiFieldQueryParser.parse(queries_lucene, fields, allflags, new MyEnglishAnalyzer(PubMedSpecialWords.ENGLISH_STOP_WORDS_SET2, PubMedSpecialWords.KEYWORDS_SET));

                allSumTFIDFScore = QueryQualityFeatures.getSimilarityScore(docIS, finalQuery, new MySimilarity());
                allLuceneVSMScore = QueryQualityFeatures.getSimilarityScore(docIS, finalQuery, new ClassicSimilarity());
                allBM25Score = QueryQualityFeatures.getSimilarityScore(docIS, finalQuery, new BM25Similarity());
                allLMJelinekMercerScore = QueryQualityFeatures.getSimilarityScore(docIS, finalQuery, new LMJelinekMercerSimilarity((float) 0.7));
                allLMDirichletScore = QueryQualityFeatures.getSimilarityScore(docIS, finalQuery, new LMDirichletSimilarity());
                allIBSimilarityScore = QueryQualityFeatures.getSimilarityScore(docIS, finalQuery, new IBSimilarity(new DistributionLL(), new LambdaDF(), new Normalization.NoNormalization()));
                //***********************************************************************************************
                //********************************       Query Scope (QS)  ********************************
                //*********************************************************************************************** 
                QueryParser parser2 = new QueryParser(PubMedDoc.TITLE, new MyEnglishAnalyzer(PubMedSpecialWords.ENGLISH_STOP_WORDS_SET2, PubMedSpecialWords.KEYWORDS_SET));
                titleQS = QueryQualityFeatures.getQS(docIS, parser2.parse(QueryParser.escape(queryTokenized2)));

                parser2 = new QueryParser(PubMedDoc.ABSTRACT, new MyEnglishAnalyzer(PubMedSpecialWords.ENGLISH_STOP_WORDS_SET2, PubMedSpecialWords.KEYWORDS_SET));
                abstractQS = QueryQualityFeatures.getQS(docIS, parser2.parse(QueryParser.escape(queryTokenized2)));

                parser2 = new QueryParser(PubMedDoc.BODY, new MyEnglishAnalyzer(PubMedSpecialWords.ENGLISH_STOP_WORDS_SET2, PubMedSpecialWords.KEYWORDS_SET));
                bodyQS = QueryQualityFeatures.getQS(docIS, parser2.parse(QueryParser.escape(queryTokenized2)));

                fields = new String[]{PubMedDoc.TITLE, PubMedDoc.ABSTRACT, PubMedDoc.BODY};
                queries_lucene = new String[]{QueryParser.escape(queryTokenized2), QueryParser.escape(queryTokenized2), QueryParser.escape(queryTokenized2)};
                BooleanClause.Occur[] allflags2 = {BooleanClause.Occur.SHOULD, BooleanClause.Occur.SHOULD, BooleanClause.Occur.SHOULD};
                allQS = QueryQualityFeatures.getQS(docIS, MultiFieldQueryParser.parse(queries_lucene, fields, allflags2, new MyEnglishAnalyzer(PubMedSpecialWords.ENGLISH_STOP_WORDS_SET2, PubMedSpecialWords.KEYWORDS_SET)));
//
                //*************************************************************************************************
                //**********************************IDF-based features ************************************************
                //*************************************************************************************************
                double[] titleIDFArray = QueryQualityFeatures.getIDF_w(docIS, queryTokenized, PubMedDoc.TITLE);
                titleSumIDF = Statistics.getSumArray(titleIDFArray);
                titleSDIDF = Statistics.getStdDev(titleIDFArray);
                titleMinIDF = Statistics.getMinArray(titleIDFArray);
                titleMaxIDF = Statistics.getMaxArray(titleIDFArray);
                titleArithmeticMeanIDF = Statistics.getArithmeticMean(titleIDFArray);
                titleGeometricMeanIDF = Statistics.getGeometricMean(titleIDFArray);
                titleHarmonicMeanIDF = Statistics.getHarmonicMean(titleIDFArray);
                titleCoefficientofVariationIDF = Statistics.getCoefficientVariation(titleIDFArray);

                double[] abstractIDFArray = QueryQualityFeatures.getIDF_w(docIS, queryTokenized, PubMedDoc.ABSTRACT);
                abstractSumIDF = Statistics.getSumArray(abstractIDFArray);
                abstractSDIDF = Statistics.getStdDev(abstractIDFArray);
                abstractMinIDF = Statistics.getMinArray(abstractIDFArray);
                abstractMaxIDF = Statistics.getMaxArray(abstractIDFArray);
                abstractArithmeticMeanIDF = Statistics.getArithmeticMean(abstractIDFArray);
                abstractGeometricMeanIDF = Statistics.getGeometricMean(abstractIDFArray);
                abstractHarmonicMeanIDF = Statistics.getHarmonicMean(abstractIDFArray);
                abstractCoefficientofVariationIDF = Statistics.getCoefficientVariation(abstractIDFArray);

                double[] bodyIDFArray = QueryQualityFeatures.getIDF_w(docIS, queryTokenized, PubMedDoc.BODY);
                bodySumIDF = Statistics.getSumArray(bodyIDFArray);
                bodySDIDF = Statistics.getStdDev(bodyIDFArray);
                bodyMinIDF = Statistics.getMinArray(bodyIDFArray);
                bodyMaxIDF = Statistics.getMaxArray(bodyIDFArray);
                bodyArithmeticMeanIDF = Statistics.getArithmeticMean(bodyIDFArray);
                bodyGeometricMeanIDF = Statistics.getGeometricMean(bodyIDFArray);
                bodyHarmonicMeanIDF = Statistics.getHarmonicMean(bodyIDFArray);
                bodyCoefficientofVariationIDF = Statistics.getCoefficientVariation(bodyIDFArray);
                //*************************************************************************************************
                //**********************************ICTF-based features ************************************************
                //*************************************************************************************************
                double[] titleICTFArray = QueryQualityFeatures.getICTF(docIS, queryTokenized, PubMedDoc.TITLE);
                titleSumICTF = Statistics.getSumArray(titleICTFArray);
                titleSDICTF = Statistics.getStdDev(titleICTFArray);
                titleMinICTF = Statistics.getMinArray(titleICTFArray);
                titleMaxICTF = Statistics.getMaxArray(titleICTFArray);
                titleArithmeticMeanICTF = Statistics.getArithmeticMean(titleICTFArray);
                titleGeometricMeanICTF = Statistics.getGeometricMean(titleICTFArray);
                titleHarmonicMeanICTF = Statistics.getHarmonicMean(titleICTFArray);
                titleCoefficientofVariationICTF = Statistics.getCoefficientVariation(titleICTFArray);

                double[] abstractICTFArray = QueryQualityFeatures.getICTF(docIS, queryTokenized, PubMedDoc.ABSTRACT);
                abstractSumICTF = Statistics.getSumArray(abstractICTFArray);
                abstractSDICTF = Statistics.getStdDev(abstractICTFArray);
                abstractMinICTF = Statistics.getMinArray(abstractICTFArray);
                abstractMaxICTF = Statistics.getMaxArray(abstractICTFArray);
                abstractArithmeticMeanICTF = Statistics.getArithmeticMean(abstractICTFArray);
                abstractGeometricMeanICTF = Statistics.getGeometricMean(abstractICTFArray);
                abstractHarmonicMeanICTF = Statistics.getHarmonicMean(abstractICTFArray);
                abstractCoefficientofVariationICTF = Statistics.getCoefficientVariation(abstractICTFArray);

                double[] bodyICTFArray = QueryQualityFeatures.getICTF(docIS, queryTokenized, PubMedDoc.BODY);
                bodySumICTF = Statistics.getSumArray(bodyICTFArray);
                bodySDICTF = Statistics.getStdDev(bodyICTFArray);
                bodyMinICTF = Statistics.getMinArray(bodyICTFArray);
                bodyMaxICTF = Statistics.getMaxArray(bodyICTFArray);
                bodyArithmeticMeanICTF = Statistics.getArithmeticMean(bodyICTFArray);
                bodyGeometricMeanICTF = Statistics.getGeometricMean(bodyICTFArray);
                bodyHarmonicMeanICTF = Statistics.getHarmonicMean(bodyICTFArray);
                bodyCoefficientofVariationICTF = Statistics.getCoefficientVariation(bodyICTFArray);
                //*************************************************************************************************
                //**********************************SCQ-based features ************************************************
                //*************************************************************************************************
                double[] titleSCQArray = QueryQualityFeatures.getSCQ(docIS, queryTokenized, PubMedDoc.TITLE);
                titleSumSCQ = Statistics.getSumArray(titleSCQArray);
                titleSDSCQ = Statistics.getStdDev(titleSCQArray);
                titleMinSCQ = Statistics.getMinArray(titleSCQArray);
                titleMaxSCQ = Statistics.getMaxArray(titleSCQArray);
                titleArithmeticMeanSCQ = Statistics.getArithmeticMean(titleSCQArray);
                titleGeometricMeanSCQ = Statistics.getGeometricMean(titleSCQArray);
                titleHarmonicMeanSCQ = Statistics.getHarmonicMean(titleSCQArray);
                titleCoefficientofVariationSCQ = Statistics.getCoefficientVariation(titleSCQArray);

                double[] abstractSCQArray = QueryQualityFeatures.getSCQ(docIS, queryTokenized, PubMedDoc.ABSTRACT);
                abstractSumSCQ = Statistics.getSumArray(abstractSCQArray);
                abstractSDSCQ = Statistics.getStdDev(abstractSCQArray);
                abstractMinSCQ = Statistics.getMinArray(abstractSCQArray);
                abstractMaxSCQ = Statistics.getMaxArray(abstractSCQArray);
                abstractArithmeticMeanSCQ = Statistics.getArithmeticMean(abstractSCQArray);
                abstractGeometricMeanSCQ = Statistics.getGeometricMean(abstractSCQArray);
                abstractHarmonicMeanSCQ = Statistics.getHarmonicMean(abstractSCQArray);
                abstractCoefficientofVariationSCQ = Statistics.getCoefficientVariation(abstractSCQArray);

                double[] bodySCQArray = QueryQualityFeatures.getSCQ(docIS, queryTokenized, PubMedDoc.BODY);
                bodySumSCQ = Statistics.getSumArray(bodySCQArray);
                bodySDSCQ = Statistics.getStdDev(bodySCQArray);
                bodyMinSCQ = Statistics.getMinArray(bodySCQArray);
                bodyMaxSCQ = Statistics.getMaxArray(bodySCQArray);
                bodyArithmeticMeanSCQ = Statistics.getArithmeticMean(bodySCQArray);
                bodyGeometricMeanSCQ = Statistics.getGeometricMean(bodySCQArray);
                bodyHarmonicMeanSCQ = Statistics.getHarmonicMean(bodySCQArray);
                bodyCoefficientofVariationSCQ = Statistics.getCoefficientVariation(bodySCQArray);
                //*************************************************************************************************
                //**********************************SCS-based features ************************************************
                //*************************************************************************************************
                docIS.setSimilarity(new MySimilarity());
                titleSCS = QueryQualityFeatures.getSimplifiedClarityScore(docIS, queryMap, PubMedDoc.TITLE);
                abstractSCS = QueryQualityFeatures.getSimplifiedClarityScore(docIS, queryMap, PubMedDoc.ABSTRACT);
                bodySCS = QueryQualityFeatures.getSimplifiedClarityScore(docIS, queryMap, PubMedDoc.BODY);
//
//                //*************************************************************************************************
//                //**********************************KL-Divergence based features ************************************************
//                //*************************************************************************************************
//                titleKLDiv = QueryQualityFeatures.getKLDivergence(queryMap, tfvTitle);
//                abstractKLDiv = QueryQualityFeatures.getKLDivergence(queryMap, tfvAbstract);
//                bodyKLDiv = QueryQualityFeatures.getKLDivergence(queryMap, tfvBody);
                //*************************************************************************************************
                //**********************************Clarity features ************************************************
                //*************************************************************************************************
                titleClarity1 = QueryQualityFeatures.getQueryClarity2(docIS, RecordQuery.getRecDefinitionQueryWithBigramsNoQE(query_text, new String[]{PubMedDoc.TITLE}), queryMap.keySet(), PubMedDoc.TITLE, 1);
                abstractClarity1 = QueryQualityFeatures.getQueryClarity2(docIS, RecordQuery.getRecDefinitionQueryWithBigramsNoQE(query_text, new String[]{PubMedDoc.ABSTRACT}), queryMap.keySet(), PubMedDoc.ABSTRACT, 1);
                bodyClarity1 = QueryQualityFeatures.getQueryClarity2(docIS, RecordQuery.getRecDefinitionQueryWithBigramsNoQE(query_text, new String[]{PubMedDoc.BODY}), queryMap.keySet(), PubMedDoc.BODY, 1);

                titleClarity5 = QueryQualityFeatures.getQueryClarity2(docIS, RecordQuery.getRecDefinitionQueryWithBigramsNoQE(query_text, new String[]{PubMedDoc.TITLE}), queryMap.keySet(), PubMedDoc.TITLE, 5);
                abstractClarity5 = QueryQualityFeatures.getQueryClarity2(docIS, RecordQuery.getRecDefinitionQueryWithBigramsNoQE(query_text, new String[]{PubMedDoc.ABSTRACT}), queryMap.keySet(), PubMedDoc.ABSTRACT, 5);
                bodyClarity5 = QueryQualityFeatures.getQueryClarity2(docIS, RecordQuery.getRecDefinitionQueryWithBigramsNoQE(query_text, new String[]{PubMedDoc.BODY}), queryMap.keySet(), PubMedDoc.BODY, 5);

                titleClarity10 = QueryQualityFeatures.getQueryClarity2(docIS, RecordQuery.getRecDefinitionQueryWithBigramsNoQE(query_text, new String[]{PubMedDoc.TITLE}), queryMap.keySet(), PubMedDoc.TITLE, 10);
                abstractClarity10 = QueryQualityFeatures.getQueryClarity2(docIS, RecordQuery.getRecDefinitionQueryWithBigramsNoQE(query_text, new String[]{PubMedDoc.ABSTRACT}), queryMap.keySet(), PubMedDoc.ABSTRACT, 10);
                bodyClarity10 = QueryQualityFeatures.getQueryClarity2(docIS, RecordQuery.getRecDefinitionQueryWithBigramsNoQE(query_text, new String[]{PubMedDoc.BODY}), queryMap.keySet(), PubMedDoc.BODY, 10);

                titleClarity20 = QueryQualityFeatures.getQueryClarity2(docIS, RecordQuery.getRecDefinitionQueryWithBigramsNoQE(query_text, new String[]{PubMedDoc.TITLE}), queryMap.keySet(), PubMedDoc.TITLE, 20);
                abstractClarity20 = QueryQualityFeatures.getQueryClarity2(docIS, RecordQuery.getRecDefinitionQueryWithBigramsNoQE(query_text, new String[]{PubMedDoc.ABSTRACT}), queryMap.keySet(), PubMedDoc.ABSTRACT, 20);
                bodyClarity20 = QueryQualityFeatures.getQueryClarity2(docIS, RecordQuery.getRecDefinitionQueryWithBigramsNoQE(query_text, new String[]{PubMedDoc.BODY}), queryMap.keySet(), PubMedDoc.BODY, 20);

                titleClarity50 = QueryQualityFeatures.getQueryClarity2(docIS, RecordQuery.getRecDefinitionQueryWithBigramsNoQE(query_text, new String[]{PubMedDoc.TITLE}), queryMap.keySet(), PubMedDoc.TITLE, 50);
                abstractClarity50 = QueryQualityFeatures.getQueryClarity2(docIS, RecordQuery.getRecDefinitionQueryWithBigramsNoQE(query_text, new String[]{PubMedDoc.ABSTRACT}), queryMap.keySet(), PubMedDoc.ABSTRACT, 50);
                bodyClarity50 = QueryQualityFeatures.getQueryClarity2(docIS, RecordQuery.getRecDefinitionQueryWithBigramsNoQE(query_text, new String[]{PubMedDoc.BODY}), queryMap.keySet(), PubMedDoc.BODY, 50);
//
                titleClarity100 = QueryQualityFeatures.getQueryClarity2(docIS, RecordQuery.getRecDefinitionQueryWithBigramsNoQE(query_text, new String[]{PubMedDoc.TITLE}), queryMap.keySet(), PubMedDoc.TITLE, 100);
                abstractClarity100 = QueryQualityFeatures.getQueryClarity2(docIS, RecordQuery.getRecDefinitionQueryWithBigramsNoQE(query_text, new String[]{PubMedDoc.ABSTRACT}), queryMap.keySet(), PubMedDoc.ABSTRACT, 100);
                bodyClarity100 = QueryQualityFeatures.getQueryClarity2(docIS, RecordQuery.getRecDefinitionQueryWithBigramsNoQE(query_text, new String[]{PubMedDoc.BODY}), queryMap.keySet(), PubMedDoc.BODY, 100);

                //*************************************************************************************************
                //**************************************  Organism Based Features *********************************
                //*************************************************************************************************  
                BooleanQuery.Builder bqBuilder = new BooleanQuery.Builder();
                BooleanQuery.Builder bqBuilder1 = new BooleanQuery.Builder();
                bqBuilder1.add(new TermQuery(new Term("tax_id", organismList.get(0))), BooleanClause.Occur.SHOULD);
                Query q = bqBuilder1.build();
                bqBuilder.add(q, BooleanClause.Occur.MUST);
                PhraseQuery.Builder pqBuilder = new PhraseQuery.Builder();
                pqBuilder.add(new Term("name_class", "scientific name"));
                PhraseQuery pq = pqBuilder.build();
                bqBuilder.add(pq, BooleanClause.Occur.FILTER);
                hits = orgIS.search(bqBuilder.build(), 1);
                String mainOrganismScientificName = orgIS.doc(hits.scoreDocs[0].doc).get("name_txt");
                TermFreqVector tfv = new TermFreqVector(orgIS.getIndexReader().getTermVector(hits.scoreDocs[0].doc, "name_txt"));
                if (StringSimilarity.getOverlapSimilarity(tfv.getTerms(), tfvTitle.getTerms()) >= 0.5) {
                    isMainOrgSciNameInTitle = true;
                }
                if (StringSimilarity.getOverlapSimilarity(tfv.getTerms(), tfvAbstract.getTerms()) >= 0.5) {
                    isMainOrgSciNameInAbstract = true;
                }
                if (StringSimilarity.getOverlapSimilarity(tfv.getTerms(), tfvBody.getTerms()) >= 0.5) {
                    isMainOrgSciNameInBody = true;
                }

                pqBuilder = new PhraseQuery.Builder();
                pqBuilder.add(new Term("name_class", "synonym"));
                pq = pqBuilder.build();
                bqBuilder.add(pq, BooleanClause.Occur.FILTER);
                hits = orgIS.search(bqBuilder.build(), 100);
                for (ScoreDoc scoreDoc : hits.scoreDocs) {
                    tfv = new TermFreqVector(orgIS.getIndexReader().getTermVector(scoreDoc.doc, "name_txt"));
                    if (StringSimilarity.getOverlapSimilarity(tfv.getTerms(), tfvTitle.getTerms()) >= 0.5) {
                        isMainOrgSynonymInTitle = true;
                    }
                    if (StringSimilarity.getOverlapSimilarity(tfv.getTerms(), tfvAbstract.getTerms()) >= 0.5) {
                        isMainOrgSynonymInAbstract = true;
                    }
                    if (StringSimilarity.getOverlapSimilarity(tfv.getTerms(), tfvBody.getTerms()) >= 0.5) {
                        isMainOrgSynonymInBody = true;
                    }
                }
                pqBuilder = new PhraseQuery.Builder();
                pqBuilder.add(new Term("name_class", "misspelling"));
                pq = pqBuilder.build();
                bqBuilder.add(pq, BooleanClause.Occur.FILTER);
                hits = orgIS.search(bqBuilder.build(), 20);
                for (ScoreDoc scoreDoc : hits.scoreDocs) {
                    tfv = new TermFreqVector(orgIS.getIndexReader().getTermVector(scoreDoc.doc, "name_txt"));
                    if (StringSimilarity.getOverlapSimilarity(tfv.getTerms(), tfvTitle.getTerms()) >= 0.5) {
                        isMainOrgMisspellingInTitle = true;
                    }
                    if (StringSimilarity.getOverlapSimilarity(tfv.getTerms(), tfvAbstract.getTerms()) >= 0.5) {
                        isMainOrgMisspellingInAbstract = true;
                    }
                    if (StringSimilarity.getOverlapSimilarity(tfv.getTerms(), tfvBody.getTerms()) >= 0.5) {
                        isMainOrgMisspellingInBody = true;
                    }
                }
                pqBuilder = new PhraseQuery.Builder();
                pqBuilder.add(new Term("name_class", "genbank common name"));
                pq = pqBuilder.build();
                bqBuilder.add(pq, BooleanClause.Occur.FILTER);
                hits = orgIS.search(bqBuilder.build(), 2);
                for (ScoreDoc scoreDoc : hits.scoreDocs) {
                    tfv = new TermFreqVector(orgIS.getIndexReader().getTermVector(scoreDoc.doc, "name_txt"));
                    if (StringSimilarity.getOverlapSimilarity(tfv.getTerms(), tfvTitle.getTerms()) >= 0.5) {
                        isMainOrgGenbankCommonNameInTitle = true;
                    }
                    if (StringSimilarity.getOverlapSimilarity(tfv.getTerms(), tfvAbstract.getTerms()) >= 0.5) {
                        isMainOrgGenbankCommonNameInAbstract = true;
                    }
                    if (StringSimilarity.getOverlapSimilarity(tfv.getTerms(), tfvBody.getTerms()) >= 0.5) {
                        isMainOrgGenbankCommonNameInBody = true;
                    }
                }
                pqBuilder = new PhraseQuery.Builder();
                pqBuilder.add(new Term("name_class", "equivalent name"));
                pq = pqBuilder.build();
                bqBuilder.add(pq, BooleanClause.Occur.FILTER);
                hits = orgIS.search(bqBuilder.build(), 6);
                for (ScoreDoc scoreDoc : hits.scoreDocs) {
                    tfv = new TermFreqVector(orgIS.getIndexReader().getTermVector(scoreDoc.doc, "name_txt"));
                    if (StringSimilarity.getOverlapSimilarity(tfv.getTerms(), tfvTitle.getTerms()) >= 0.5) {
                        isMainOrgEquivalentNameInTitle = true;
                    }
                    if (StringSimilarity.getOverlapSimilarity(tfv.getTerms(), tfvAbstract.getTerms()) >= 0.5) {
                        isMainOrgEquivalentNameInAbstract = true;
                    }
                    if (StringSimilarity.getOverlapSimilarity(tfv.getTerms(), tfvBody.getTerms()) >= 0.5) {
                        isMainOrgEquivalentNameInBody = true;
                    }
                }
                pqBuilder = new PhraseQuery.Builder();
                pqBuilder.add(new Term("name_class", "common name"));
                pq = pqBuilder.build();
                bqBuilder.add(pq, BooleanClause.Occur.FILTER);
                hits = orgIS.search(bqBuilder.build(), 8);
                for (ScoreDoc scoreDoc : hits.scoreDocs) {
                    tfv = new TermFreqVector(orgIS.getIndexReader().getTermVector(scoreDoc.doc, "name_txt"));
                    if (StringSimilarity.getOverlapSimilarity(tfv.getTerms(), tfvTitle.getTerms()) >= 0.5) {
                        isMainOrgCommonNameInTitle = true;
                    }
                    if (StringSimilarity.getOverlapSimilarity(tfv.getTerms(), tfvAbstract.getTerms()) >= 0.5) {
                        isMainOrgCommonNameInAbstract = true;
                    }
                    if (StringSimilarity.getOverlapSimilarity(tfv.getTerms(), tfvBody.getTerms()) >= 0.5) {
                        isMainOrgCommonNameInBody = true;
                    }
                }
                pqBuilder = new PhraseQuery.Builder();
                pqBuilder.add(new Term("name_class", "genbank synonym"));
                pq = pqBuilder.build();
                bqBuilder.add(pq, BooleanClause.Occur.FILTER);
                hits = orgIS.search(bqBuilder.build(), 1);
                for (ScoreDoc scoreDoc : hits.scoreDocs) {
                    tfv = new TermFreqVector(orgIS.getIndexReader().getTermVector(scoreDoc.doc, "name_txt"));
                    if (StringSimilarity.getOverlapSimilarity(tfv.getTerms(), tfvTitle.getTerms()) >= 0.5) {
                        isMainOrgGenbankSynonymInTitle = true;
                    }
                    if (StringSimilarity.getOverlapSimilarity(tfv.getTerms(), tfvAbstract.getTerms()) >= 0.5) {
                        isMainOrgGenbankSynonymInAbstract = true;
                    }
                    if (StringSimilarity.getOverlapSimilarity(tfv.getTerms(), tfvBody.getTerms()) >= 0.5) {
                        isMainOrgGenbankSynonymInBody = true;
                    }
                }
                pqBuilder = new PhraseQuery.Builder();
                pqBuilder.add(new Term("name_class", "misnomer"));
                pq = pqBuilder.build();
                bqBuilder.add(pq, BooleanClause.Occur.FILTER);
                hits = orgIS.search(bqBuilder.build(), 2);
                for (ScoreDoc scoreDoc : hits.scoreDocs) {
                    tfv = new TermFreqVector(orgIS.getIndexReader().getTermVector(scoreDoc.doc, "name_txt"));
                    if (StringSimilarity.getOverlapSimilarity(tfv.getTerms(), tfvTitle.getTerms()) >= 0.5) {
                        isMainOrgMisnomerInTitle = true;
                    }
                    if (StringSimilarity.getOverlapSimilarity(tfv.getTerms(), tfvAbstract.getTerms()) >= 0.5) {
                        isMainOrgMisnomerInAbstract = true;
                    }
                    if (StringSimilarity.getOverlapSimilarity(tfv.getTerms(), tfvBody.getTerms()) >= 0.5) {
                        isMainOrgMisnomerInBody = true;
                    }
                }
                pqBuilder = new PhraseQuery.Builder();
                pqBuilder.add(new Term("name_class", "acronym"));
                pq = pqBuilder.build();
                bqBuilder.add(pq, BooleanClause.Occur.FILTER);
                hits = orgIS.search(bqBuilder.build(), 4);
                for (ScoreDoc scoreDoc : hits.scoreDocs) {
                    tfv = new TermFreqVector(orgIS.getIndexReader().getTermVector(scoreDoc.doc, "name_txt"));
                    if (StringSimilarity.getOverlapSimilarity(tfv.getTerms(), tfvTitle.getTerms()) >= 0.5) {
                        isMainOrgAcronymInTitle = true;
                    }
                    if (StringSimilarity.getOverlapSimilarity(tfv.getTerms(), tfvAbstract.getTerms()) >= 0.5) {
                        isMainOrgAcronymInAbstract = true;
                    }
                    if (StringSimilarity.getOverlapSimilarity(tfv.getTerms(), tfvBody.getTerms()) >= 0.5) {
                        isMainOrgAcronymInBody = true;
                    }
                }

                //*************************************************************************************************
                //********************************Popularity of Main Organism ***************************
                //************************************************************************************************* 
                parser2 = new QueryParser(PubMedDoc.TITLE, new MyEnglishAnalyzer(PubMedSpecialWords.ENGLISH_STOP_WORDS_SET2, PubMedSpecialWords.KEYWORDS_SET));
                titlePopularityMainOrganism = Math.log10(1 + docIS.count(parser2.parse("\"" + QueryParser.escape(mainOrganismScientificName) + "\"~2")));

                parser2 = new QueryParser(PubMedDoc.ABSTRACT, new MyEnglishAnalyzer(PubMedSpecialWords.ENGLISH_STOP_WORDS_SET2, PubMedSpecialWords.KEYWORDS_SET));
                abstractPopularityMainOrganism = Math.log10(1 + docIS.count(parser2.parse("\"" + QueryParser.escape(mainOrganismScientificName) + "\"~2")));
//
                parser2 = new QueryParser(PubMedDoc.BODY, new MyEnglishAnalyzer(PubMedSpecialWords.ENGLISH_STOP_WORDS_SET2, PubMedSpecialWords.KEYWORDS_SET));
                bodyPopularityMainOrganism = Math.log10(1 + docIS.count(parser2.parse("\"" + QueryParser.escape(mainOrganismScientificName) + "\"~2")));

                fields = new String[]{PubMedDoc.TITLE, PubMedDoc.ABSTRACT, PubMedDoc.BODY};
                queries_lucene = new String[]{"\"" + QueryParser.escape(mainOrganismScientificName) + "\"~2", "\"" + QueryParser.escape(mainOrganismScientificName) + "\"~2", "\"" + QueryParser.escape(mainOrganismScientificName) + "\"~2"};
                allflags2 = new BooleanClause.Occur[]{BooleanClause.Occur.SHOULD, BooleanClause.Occur.SHOULD, BooleanClause.Occur.SHOULD};
                allPopularityMainOrganism = Math.log10(1 + docIS.count(MultiFieldQueryParser.parse(queries_lucene, fields, allflags2, new MyEnglishAnalyzer(PubMedSpecialWords.ENGLISH_STOP_WORDS_SET2, PubMedSpecialWords.KEYWORDS_SET))));

                //*************************************************************************************************
                //******************************** Is the main organism in definition ***************************
                //*************************************************************************************************
                overlapMainOrgSciNameDefinition = StringSimilarity.getOverlapSimilarity(mainOrganismScientificName, queryTokenized2, new MyEnglishAnalyzer(PubMedSpecialWords.ENGLISH_STOP_WORDS_SET2, PubMedSpecialWords.KEYWORDS_SET));
                if (overlapMainOrgSciNameDefinition >= 0.5) {
                    isMainOrgSciNameInDefinition = true;
                }

                //*************************************************************************************************
                //********************************Age features + Record Based Features ***************************
                //*************************************************************************************************  
                QLen = queryArray.length;
                if (Status.equalsIgnoreCase("live")) {
                    age = 1460734488 - creationDateseconds;
                    logAge = Statistics.log2(age + 10);
                    cat = "c0";
                } else {
                    age = updateDateseconds - creationDateseconds;
                    logAge = Statistics.log2(age + 10);
                    cat = "c1";
                }
                //*************************************************************************************************
                //**********************************Print Features Values *****************************************
                //************************************************************************************************* 
                //System.out.println(queryid + "\t" + titleKLDiv + "\t" + abstractKLDiv + "\t" + bodyKLDiv);
                System.out.println(queryid //+ "\t" + accession + "\t" + pmc
                        + "\t" + titleOverlapSimilarity + "\t" + abstractOverlapSimilarity + "\t" + bodyOverlapSimilarity + "\t" + allOverlapSimilarity
                        + "\t" + titleJaccardSimilarity + "\t" + abstractJaccardSimilarity + "\t" + bodyJaccardSimilarity + "\t" + allJaccardSimilarity
                        + "\t" + titleDiceSimilarity + "\t" + abstractDiceSimilarity + "\t" + bodyDiceSimilarity + "\t" + allDiceSimilarity
                        + "\t" + titleMatchingSimilarity + "\t" + abstractMatchingSimilarity + "\t" + bodyMatchingSimilarity + "\t" + allMatchingSimilarity
                        + "\t" + titleCosineSimilarity + "\t" + abstractCosineSimilarity + "\t" + bodyCosineSimilarity + "\t" + allCosineSimilarity
                        + "\t" + titleDotProductSimilarity + "\t" + abstractDotProductSimilarity + "\t" + bodyDotProductSimilarity
                        + "\t" + titleSumTF + "\t" + titleSDTF + "\t" + titleMinTF + "\t" + titleMaxTF + "\t" + titleArithmeticMeanTF + "\t" + titleGeometricMeanTF + "\t" + titleHarmonicMeanTF + "\t" + titleCoefficientofVariationTF
                        + "\t" + abstractSumTF + "\t" + abstractSDTF + "\t" + abstractMinTF + "\t" + abstractMaxTF + "\t" + abstractArithmeticMeanTF + "\t" + abstractGeometricMeanTF + "\t" + abstractHarmonicMeanTF + "\t" + abstractCoefficientofVariationTF
                        + "\t" + bodySumTF + "\t" + bodySDTF + "\t" + bodyMinTF + "\t" + bodyMaxTF + "\t" + bodyArithmeticMeanTF + "\t" + bodyGeometricMeanTF + "\t" + bodyHarmonicMeanTF + "\t" + bodyCoefficientofVariationTF
                        + "\t" + titleRDCS + "\t" + abstractRDCS + "\t" + bodyRDCS
                        + "\t" + titleSumTFIDFScore + "\t" + abstractSumTFIDFScore + "\t" + bodySumTFIDFScore + "\t" + allSumTFIDFScore
                        + "\t" + titleLuceneVSMScore + "\t" + abstractLuceneVSMScore + "\t" + bodyLuceneVSMScore + "\t" + allLuceneVSMScore
                        + "\t" + titleBM25Score + "\t" + abstractBM25Score + "\t" + bodyBM25Score + "\t" + allBM25Score
                        + "\t" + titleLMJelinekMercerScore + "\t" + abstractLMJelinekMercerScore + "\t" + bodyLMJelinekMercerScore + "\t" + allLMJelinekMercerScore
                        + "\t" + titleLMDirichletScore + "\t" + abstractLMDirichletScore + "\t" + bodyLMDirichletScore + "\t" + allLMDirichletScore
                        + "\t" + titleIBSimilarityScore + "\t" + abstractIBSimilarityScore + "\t" + bodyIBSimilarityScore + "\t" + allIBSimilarityScore
                        + "\t" + titleQS + "\t" + abstractQS + "\t" + bodyQS + "\t" + allQS
                        + "\t" + titleSumIDF + "\t" + titleSDIDF + "\t" + titleMinIDF + "\t" + titleMaxIDF + "\t" + titleArithmeticMeanIDF + "\t" + titleGeometricMeanIDF + "\t" + titleHarmonicMeanIDF + "\t" + titleCoefficientofVariationIDF
                        + "\t" + abstractSumIDF + "\t" + abstractSDIDF + "\t" + abstractMinIDF + "\t" + abstractMaxIDF + "\t" + abstractArithmeticMeanIDF + "\t" + abstractGeometricMeanIDF + "\t" + abstractHarmonicMeanIDF + "\t" + abstractCoefficientofVariationIDF
                        + "\t" + bodySumIDF + "\t" + bodySDIDF + "\t" + bodyMinIDF + "\t" + bodyMaxIDF + "\t" + bodyArithmeticMeanIDF + "\t" + bodyGeometricMeanIDF + "\t" + bodyHarmonicMeanIDF + "\t" + bodyCoefficientofVariationIDF
                        + "\t" + titleSumICTF + "\t" + titleSDICTF + "\t" + titleMinICTF + "\t" + titleMaxICTF + "\t" + titleArithmeticMeanICTF + "\t" + titleGeometricMeanICTF + "\t" + titleHarmonicMeanICTF + "\t" + titleCoefficientofVariationICTF
                        + "\t" + abstractSumICTF + "\t" + abstractSDICTF + "\t" + abstractMinICTF + "\t" + abstractMaxICTF + "\t" + abstractArithmeticMeanICTF + "\t" + abstractGeometricMeanICTF + "\t" + abstractHarmonicMeanICTF + "\t" + abstractCoefficientofVariationICTF
                        + "\t" + bodySumICTF + "\t" + bodySDICTF + "\t" + bodyMinICTF + "\t" + bodyMaxICTF + "\t" + bodyArithmeticMeanICTF + "\t" + bodyGeometricMeanICTF + "\t" + bodyHarmonicMeanICTF + "\t" + bodyCoefficientofVariationICTF
                        + "\t" + titleSumSCQ + "\t" + titleSDSCQ + "\t" + titleMinSCQ + "\t" + titleMaxSCQ + "\t" + titleArithmeticMeanSCQ + "\t" + titleGeometricMeanSCQ + "\t" + titleHarmonicMeanSCQ + "\t" + titleCoefficientofVariationSCQ
                        + "\t" + abstractSumSCQ + "\t" + abstractSDSCQ + "\t" + abstractMinSCQ + "\t" + abstractMaxSCQ + "\t" + abstractArithmeticMeanSCQ + "\t" + abstractGeometricMeanSCQ + "\t" + abstractHarmonicMeanSCQ + "\t" + abstractCoefficientofVariationSCQ
                        + "\t" + bodySumSCQ + "\t" + bodySDSCQ + "\t" + bodyMinSCQ + "\t" + bodyMaxSCQ + "\t" + bodyArithmeticMeanSCQ + "\t" + bodyGeometricMeanSCQ + "\t" + bodyHarmonicMeanSCQ + "\t" + bodyCoefficientofVariationSCQ
                        + "\t" + titleSCS + "\t" + abstractSCS + "\t" + bodySCS
                        //+ "\t" + titleKLDiv+ "\t" + abstractKLDiv+ "\t" + bodyKLDiv
                        + "\t" + titleClarity1 + "\t" + abstractClarity1 + "\t" + bodyClarity1
                        + "\t" + titleClarity5 + "\t" + abstractClarity5 + "\t" + bodyClarity5
                        + "\t" + titleClarity10 + "\t" + abstractClarity10 + "\t" + bodyClarity10
                        + "\t" + titleClarity20 + "\t" + abstractClarity20 + "\t" + bodyClarity20
                        + "\t" + titleClarity50 + "\t" + abstractClarity50 + "\t" + bodyClarity50
                        + "\t" + titleClarity100 + "\t" + abstractClarity100 + "\t" + bodyClarity100
                        + "\t" + isMainOrgSciNameInTitle + "\t" + isMainOrgSciNameInAbstract + "\t" + isMainOrgSciNameInBody
                        + "\t" + isMainOrgSynonymInTitle + "\t" + isMainOrgSynonymInAbstract + "\t" + isMainOrgSynonymInBody
                        + "\t" + isMainOrgMisspellingInTitle + "\t" + isMainOrgMisspellingInAbstract + "\t" + isMainOrgMisspellingInBody
                        + "\t" + isMainOrgGenbankCommonNameInTitle + "\t" + isMainOrgGenbankCommonNameInAbstract + "\t" + isMainOrgGenbankCommonNameInBody
                        + "\t" + isMainOrgEquivalentNameInTitle + "\t" + isMainOrgEquivalentNameInAbstract + "\t" + isMainOrgEquivalentNameInBody
                        + "\t" + isMainOrgCommonNameInTitle + "\t" + isMainOrgCommonNameInAbstract + "\t" + isMainOrgCommonNameInBody
                        + "\t" + isMainOrgGenbankSynonymInTitle + "\t" + isMainOrgGenbankSynonymInAbstract + "\t" + isMainOrgGenbankSynonymInBody
                        + "\t" + isMainOrgMisnomerInTitle + "\t" + isMainOrgMisnomerInAbstract + "\t" + isMainOrgMisnomerInBody
                        + "\t" + isMainOrgAcronymInTitle + "\t" + isMainOrgAcronymInAbstract + "\t" + isMainOrgAcronymInBody
                        + "\t" + titlePopularityMainOrganism + "\t" + abstractPopularityMainOrganism + "\t" + bodyPopularityMainOrganism + "\t" + allPopularityMainOrganism
                        + "\t" + isMainOrgSciNameInDefinition + "\t" + overlapMainOrgSciNameDefinition
                        + "\t" + QLen + "\t" + citationNbr + "\t" + logCitationNbr + "\t" + nbrOrganisms + "\t" + logAge
                        + "\t" + cat
                );
                long end = System.currentTimeMillis();
//                System.err.println(i + "- it took " + Functions.getTimer(end - start) + " to collect features of " + queryid + ".");
            }
        } catch (IOException ex) {
            Logger.getLogger(MutualReferences.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     * @throws java.io.FileNotFoundException
     * @throws org.apache.lucene.queryparser.classic.ParseException
     */
    public static void main(String[] args) throws IOException, FileNotFoundException, ParseException {
        // TODO code application logic here
        String indexDir;
        String index_organism;
        String queries;
        int a;
        int b;
        if (args.length == 0) {
            indexDir = "/Volumes/Macintosh HD/Users/mbouadjenek/Documents/bioinformatics_data/index_PubMedCentral_v10.0/";
            index_organism = "/Users/mbouadjenek/Documents/bioinformatics_data/index_Organism_Names_v5.0/";
            queries = "/Users/mbouadjenek/Documents/bioinformatics_data/queries_definition_v7.0.txt";
            a = 1;
            b = 2;
        } else {
            indexDir = args[0];
            index_organism = args[1];
            queries = args[2];
            a = Integer.parseInt(args[3]);
            b = Integer.parseInt(args[4]);
        }
        long start = System.currentTimeMillis();
        FeaturesExtraction s = new FeaturesExtraction(indexDir, index_organism, queries);
        s.search(a, b);
        long end = System.currentTimeMillis();
        long millis = (end - start);
        System.err.println("-------------------------------------------------------------------------");
        System.err.println("The process tooks " + Functions.getTimer(millis) + ".");
        System.err.println("-------------------------------------------------------------------------");
    }
}
