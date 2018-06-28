/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unimelb.edu.au.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.Similarity;
import unimelb.edu.au.indexing.TermFreqVector;
import unimelb.edu.au.util.Statistics;

/**
 * This class implements Post-retrieval and Post-retrieval query quality
 * predictors.
 * <p>
 * Pre-retrieval predictors analyze the query expression and utilize
 * corpus-based statistics. By definition, then, their prediction is not based
 * on the ranking at hand, the effectiveness of which is the goal of prediction.
 * </p>
 * <p>
 *
 * Post-retrieval predictors, on the other hand, analyze also the result list of
 * the most highly ranked documents. Hence, they often yield prediction quality
 * that (substantially) transcends that of pre-retrieval predictors.
 * </p>
 *
 * @author mbouadjenek
 */
public class QueryQualityFeatures {

    /**
     * This method implements a method for predicting query performance by
     * computing the relative entropy between a query language model and the
     * corresponding collection language model. The resulting clarity score
     * measures the coherence of the language usage in documents whose models
     * are likely to generate the query. The clarity scores measure the
     * ambiguity of a query with respect to a collection of documents and are
     * expected to correlate positively with average precision in a variety of
     * TREC test sets. Thus, the clarity score may be used to identify
     * ineffective queries, on average, without relevance information. For more
     * information see:
     *
     * <p>
     * 1- Steve Cronen-Townsend, Yun Zhou, and W. Bruce Croft. 2002. Predicting
     * query performance. In Proceedings of the 25th annual international ACM
     * SIGIR conference on Research and development in information retrieval
     * (SIGIR '02). ACM, New York, NY, USA, 299-306.
     * </p>
     * <p>
     * 2- Giridhar Kumaran and Vitor R. Carvalho. 2009. Reducing long queries
     * using query quality predictors. In Proceedings of the 32nd annual
     * international ACM SIGIR conference on Research and development in
     * information retrieval (SIGIR '09). ACM, New York, NY, USA, 564-571.
     * </p>
     * <p>
     * 3- Hao Lang , Bin Wang, Gareth Jones, Jin-Tao Li, Fan Ding, Yi-Xuan Liu.
     * Query Performance Prediction for Information Retrieval Based on Covering
     * Topic Score. Journal of Computer Science and Technology July 2008, Volume
     * 23, Issue 4, pp 590-601.
     * </p>
     *
     * @param is
     * @param query
     * @param querySet The query tokenized in a set.
     * @param field The field which is used to compute the clarity score.
     * @param prfSize The size of the pseudo-relevance feedback set to compute
     * the clarity score. This set is computed based on top ranked documents.
     * @return The clarity score of the query.
     * @throws IOException
     */
    public static double getQueryClarity(IndexSearcher is, Query query, Set<String> querySet, String field, int prfSize) throws IOException {
        TopDocs hits = is.search(query, prfSize);
        double lambda = 0.6, clarity = 0.0;
        long start2 = System.currentTimeMillis();
        int k = 0;
        for (String w : querySet) { //** Iteration over the query terms **
            double pcw = (double) is.getIndexReader().totalTermFreq(new Term(field, w)) / (double) is.getIndexReader().getSumTotalTermFreq(field);
            double pwq = 0;
            for (ScoreDoc scoreDoc : hits.scoreDocs) {//** Iteration over the top documents **
                TermFreqVector tfv = new TermFreqVector(is.getIndexReader().getTermVector(scoreDoc.doc, field));
                //We estimate P(w|D) by relative frequencies of terms linearly smoothed with collection frequencies.
                double pwd = lambda * ((double) tfv.getFreq(w) / (double) tfv.numberOfTerms()) + (1 - lambda) * pcw;
                //*****************************************************************************************************************************
                double pqd = 1;
                double pd = (double) tfv.numberOfTerms() / ((double) (tfv.numberOfTerms()));
                for (String q : querySet) {//** Iteration over the query terms **
                    //We estimate P(q|D)  by relative frequencies of terms linearly smoothed with collection frequencies.
                    k++;
                    double pqd_tmp = lambda * ((double) tfv.getFreq(q) / (double) tfv.numberOfTerms());
                    if (pqd_tmp != 0) {
                        pqd *= pqd_tmp + ((1 - lambda) * ((double) is.getIndexReader().totalTermFreq(new Term(field, q)) / (double) is.getIndexReader().getSumTotalTermFreq(field)));
                    }
                    //*****************************************************************************************************************************
                }
                double pdq = pqd * pd;
                pwq += (pwd * pdq);
            }
            if (pcw != 0) {
                System.out.println(pwq + " * Statistics.log2(" + pwq + " / " + pcw + ") = " + pwq * Statistics.log2(pwq / pcw));
                clarity += pwq * Statistics.log2(pwq / pcw);
            }
        }
        long end2 = System.currentTimeMillis();
        System.out.println("Time2: " + (end2 - start2) + "ms - Iterations: " + k);
        return clarity;
    }

    public static double getQueryClarity2(IndexSearcher is, Query query, Set<String> querySet, String field, int prfSize) throws IOException {
        double clarity = 0.0;
        Map<String, Integer> QMVec = new HashMap<>();
        int total = 0;
        TopDocs hits = is.search(query, prfSize);
        for (ScoreDoc scoreDoc : hits.scoreDocs) {//** Iteration over the top documents **
            TermFreqVector tfv = new TermFreqVector(is.getIndexReader().getTermVector(scoreDoc.doc, field));
            tfv.getTerms().stream().forEach((term) -> {
                if (QMVec.containsKey(term)) {
                    int val = QMVec.get(term) + tfv.getFreq(term);
                    QMVec.put(term, val);
                } else {
                    QMVec.put(term, tfv.getFreq(term));
                }
            });
        }
        total = QMVec.values().stream().map((v) -> v).reduce(total, Integer::sum);
        for (String w : querySet) { //** Iteration over the query terms **
            double pcw = (double) is.getIndexReader().totalTermFreq(new Term(field, w)) / (double) is.getIndexReader().getSumTotalTermFreq(field);
            double pwq = 0;
            if (QMVec.containsKey(w)) {
                pwq = (double) QMVec.get(w) / (double) total;
            }
            if (pcw != 0 && pwq != 0) {
//                System.out.println(pwq + " * Statistics.log2(" + pwq + " / " + pcw + ") = " + pwq * Statistics.log2(pwq / pcw));
                clarity += pwq * Statistics.log2(pwq / pcw);
            }
        }
        return clarity;
    }

    /**
     * This method computes the Simplified query Clarity Score (SCS) as
     * described in:
     * <p>
     * B. He and I. Ounis. String Processing and Information Retrieval: 11th
     * International Conference, SPIRE 2004, Padova, Italy, October 5-8, 2004.
     * Proceedings, chapter Inferring Query Performance Using Pre-retrieval
     * Predictors, pages 43–54. Springer Berlin Heidelberg, Berlin, Heidelberg,
     * 2004.
     * </p>
     *
     * @param is
     * @param queryMap The query in a Map representation. Keys are terms in the
     * query, and values are their frequencies in the query.
     * @param field The field which is used to compute the SCS.
     * @return The SCS score.
     * @throws IOException
     */
    public static double getSimplifiedClarityScore(IndexSearcher is, Map<String, Integer> queryMap, String field) throws IOException {
        double SCS = 0.0;
        int querySize = 0;
        querySize = queryMap.values().stream().map((i) -> i).reduce(querySize, Integer::sum);
        for (String w : queryMap.keySet()) {
            double pml = (double) queryMap.get(w) / (double) querySize;
            double pcw = (double) is.getIndexReader().totalTermFreq(new Term(field, w)) / (double) is.getIndexReader().getSumTotalTermFreq(field);
            if (pcw != 0) {
                SCS += pml * Statistics.log2(pml / pcw);
            }
        }
        return SCS;
    }

    /**
     * Proposed by Zhao et al. [1], this query quality predictor is based on the
     * hypothesis that queries that have higher similarity to the collection as
     * a whole will be of higher quality. For each term w in the query, and SCQ
     * is computed as defined in:
     *
     * <p>
     * Y. Zhao, F. Scholer, and Y. Tsegay. Effective pre-retrieval query
     * performance prediction using similarity and variability evidence. In
     * Advances in Information Retrieval , 30th European Conference on IR
     * Research, ECIR 2008, Glasgow, UK, March 30-April 3, 2008., volume 4956 of
     * Lecture Notes in Computer Science, pages 52–64. Springer, 2008.
     * </p>
     *
     * @param is
     * @param queryTokenized The query tokenized in a string.
     * @param field The field which is used to compute the SCQ.
     * @return An array containing the SCQ value for each query term.
     * @throws IOException
     */
    public static double[] getSCQ(IndexSearcher is, String queryTokenized, String field) throws IOException {
        List<Double> SCQList = new ArrayList<>();
        String[] queryArray = queryTokenized.split(" ");
        for (String w : queryArray) {
            double SCQ = (double) is.getIndexReader().docFreq(new Term(field, w));
            if (SCQ != 0) {
                SCQ = Math.log(1 + ((double) is.getIndexReader().numDocs() / SCQ));
                SCQ = (1 + Math.log(is.getIndexReader().totalTermFreq(new Term(field, w)))) * SCQ;
                SCQList.add(SCQ);
            }
        }
        return SCQList.stream().mapToDouble(i -> i).toArray();
    }

    /**
     * Inverse Collection Term Frequency-based features (ICTF) is computed for
     * each query term as defined in:
     *
     * <p>
     * G. Kumaran and V. R. Carvalho. Reducing long queries using query quality
     * predictors. In Proceedings of the 32nd international ACM SIGIR conference
     * on Research and development in information retrieval, SIGIR ’09, pages
     * 564–571, New York, NY, USA, 2009. ACM.
     * </p>
     *
     * @param is
     * @param queryTokenized The query tokenized in a string.
     * @param field The field which is used to compute the SCQ.
     * @return An array containing the ICTF value for each query term.
     * @throws IOException
     */
    public static double[] getICTF(IndexSearcher is, String queryTokenized, String field) throws IOException {
        List<Double> ICTFList = new ArrayList<>();
        String[] queryArray = queryTokenized.split(" ");
        for (String w : queryArray) {
            double ICTF = (double) is.getIndexReader().totalTermFreq(new Term(field, w));
            if (ICTF != 0) {
                ICTF = Math.log10(1 + ((double) is.getIndexReader().getSumTotalTermFreq(field) / ICTF));
                ICTFList.add(ICTF);
            }
        }
        return ICTFList.stream().mapToDouble(z -> z).toArray();
    }

    /**
     * IDF-based features (IDF_w) is computed for each query term as defined in:
     *
     * <p>
     * G. Kumaran and V. R. Carvalho. Reducing long queries using query quality
     * predictors. In Proceedings of the 32nd international ACM SIGIR conference
     * on Research and development in information retrieval, SIGIR ’09, pages
     * 564–571, New York, NY, USA, 2009. ACM.
     * </p>
     *
     * @param is
     * @param queryTokenized The query tokenized in a string.
     * @param field The field which is used to compute the SCQ.
     * @return An array containing the ICTF value for each query term.
     * @throws IOException
     */
    public static double[] getIDF_w(IndexSearcher is, String queryTokenized, String field) throws IOException {
        List<Double> IDFList = new ArrayList<>();
        String[] queryArray = queryTokenized.split(" ");
        for (String w : queryArray) {
            double IDF = (double) is.getIndexReader().docFreq(new Term(field, w));
            if (IDF != 0) {
                IDF = Math.log10(1 + ((double) is.getIndexReader().numDocs() / IDF));
                IDFList.add(IDF);
            }
        }
        return IDFList.stream().mapToDouble(z -> z).toArray();
    }

    /**
     * Query scope is a measure of the size of the retrieved document set
     * relative to the size of the collection. We can expect that high values of
     * query scope are predictive of poor-quality queries as they retrieve far
     * too many documents.
     *
     * <p>
     * 1- G. Kumaran and V. R. Carvalho. Reducing long queries using query
     * quality predictors. In Proceedings of the 32nd international ACM SIGIR
     * conference on Research and development in information retrieval, SIGIR
     * ’09, pages 564–571, New York, NY, USA, 2009. ACM.
     * </p>
     *
     * @param is
     * @param query The query
     * @return The Query Scope score.
     * @throws IOException
     */
    public static double getQS(IndexSearcher is, Query query) throws IOException {
        int nbrMatchedDoc = is.count(query);
        if (nbrMatchedDoc != 0) {
            return Math.log10(1 + ((double) is.getIndexReader().numDocs() / (double) nbrMatchedDoc));
        } else {
            return 0.0;
        }
    }

    /**
     * This method computes the similarity between the query and the first
     * retrieved document using a given similarity measure.
     *
     * @param is
     * @param query The query.
     * @param similarity the similarity used.
     * @return
     * @throws IOException
     */
    public static double getSimilarityScore(IndexSearcher is, Query query, Similarity similarity) throws IOException {
        is.setSimilarity(similarity);
        TopDocs hits = is.search(query, 1);
        return hits.scoreDocs[0].score;
    }

    /**
     * TF-based features (TF_w) is computed for each query term as the frequency
     * the term in a document given by its TermFreqVector. It if computed for
     * each query term was: TF_w = log10(1 + freq(w))
     *
     * @param queryTokenized The query tokenized in a string.
     * @param tfv The TermFreqVector representing a document.
     * @return
     */
    public static double[] getTF_w(String queryTokenized, TermFreqVector tfv) {
        List<Double> TFList = new ArrayList<>();
        String[] queryArray = queryTokenized.split(" ");
        for (String w : queryArray) {
            double TF_w = (double) Math.log10(1 + tfv.getFreq(w));
            if (TF_w != 0) {
                TFList.add(TF_w);
            }
        }
        return TFList.stream().mapToDouble(z -> z).toArray();
    }

    /**
     * This method computes a query Clarity Score based on the relevant
     * document. We call it a Relevant documents based clarity score (RDCS).
     *
     * @param is
     * @param querySet The query tokenized in a set.
     * @param tfv
     * @param field The field which is used to compute the clarity score.
     * @return The RDCS value.
     * @throws IOException
     */
    public static double getRelDocClarityScore(IndexSearcher is, Set<String> querySet, TermFreqVector tfv, String field) throws IOException {
        double RDCS = 0.0;
        if (tfv.numberOfTerms() != 0) {
            for (String w : querySet) {
                double pwq = (double) tfv.getFreq(w) / (double) tfv.numberOfTerms();
                double pcw = (double) is.getIndexReader().totalTermFreq(new Term(field, w)) / (double) is.getIndexReader().getSumTotalTermFreq(field);
                if (pwq != 0 && pcw != 0) {
                    RDCS += pwq * Statistics.log2(pwq / pcw);
                }
            }
        }
        return RDCS;
    }
    
    
     /**
     * This method computes the KL divergence between the query and the relevant
     * document.
     *
     * @param queryMap The query tokenized in a set.
     * @param tfv
     * @return The RDCS value.
     * @throws IOException
     */
    public static double getKLDivergence( Map<String, Integer> queryMap, TermFreqVector tfv) throws IOException {
       double KL = 0.0;
        int querySize = 0;
        querySize = queryMap.values().stream().map((i) -> i).reduce(querySize, Integer::sum);
        for (String w : queryMap.keySet()) {
            double pml = (double) queryMap.get(w) / (double) querySize;
            double pcw = (double) tfv.getFreq(w) / (double) tfv.numberOfTerms();
            if (pcw != 0) {
                KL += pml * Statistics.log2(pml / pcw);
            }
        }
        return KL;
    }

}
