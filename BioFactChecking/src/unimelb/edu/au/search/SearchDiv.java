/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unimelb.edu.au.search;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import jdbm.PrimaryHashMap;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import unimelb.edu.au.doc.MedlineDoc;
import unimelb.edu.au.doc.MutualReferences;
import unimelb.edu.au.doc.PubMedDoc;
import unimelb.edu.au.indexing.EntityMention;
import unimelb.edu.au.lucene.analysis.en.PubMedSpecialWords;
import unimelb.edu.au.util.Functions;
import unimelb.edu.au.util.StringSimilarity;
import static unimelb.edu.au.util.StringSimilarity.tokenizeToArrayList;

/**
 *
 * @author mbouadjenek
 */
public class SearchDiv {

    private final IndexSearcher is;
    private final int topK;
    protected File queries;

    RecordManager recman;
    String dir_articles;
    PrimaryHashMap<String, EntityMention> pmcid2diseases;
    PrimaryHashMap<String, EntityMention> pmcid2genes;
    SentenceDetectorME detector;

    public SearchDiv(String indexDir, String queries, String dir_articles, String mapFile, String nlpmodel, int topK) throws IOException {
        Directory dir = FSDirectory.open(new File(indexDir).toPath());
        is = new IndexSearcher(DirectoryReader.open(dir));
        this.queries = new File(queries);
        is.setSimilarity(new MySim());
        this.topK = topK;
        recman = RecordManagerFactory.createRecordManager(mapFile);
        pmcid2diseases = recman.hashMap("pmcid2diseases");
        pmcid2genes = recman.hashMap("pmcid2genes");
        pmcid2genes.get("PMC3509075").title_map.get("326").remove("12_32");
        this.dir_articles = dir_articles;
        InputStream inputStream = new FileInputStream(nlpmodel);
        SentenceModel model = new SentenceModel(inputStream);
        detector = new SentenceDetectorME(model);

    }

    /**
     * This method converts the Lucene TopDocs structure to a List.
     *
     * @param hits
     * @return
     * @throws IOException
     */
    public Map<String, Double> TopDocs2Map(TopDocs hits) throws IOException {
        Map<String, Double> m = new LinkedHashMap<>();
        for (ScoreDoc scoreDoc : hits.scoreDocs) {
            Document doc = is.doc(scoreDoc.doc);
            m.put(doc.get(MedlineDoc.ID_PMID).toUpperCase(), (double) scoreDoc.score);
        }
        return m;
    }

    public String getSentences(PubMedDoc doc, String suffix1, String suffix2) {
        String out = "";
        String text = doc.getTitle() + ".\n";
        text += doc.getAbstract() + ".\n";
        text += doc.getBody() + ".";
        String[] sentences = detector.sentDetect(text.replace("\n", " "));
        for (String sentence : sentences) {
            if (sentence.contains(suffix1) || sentence.contains(suffix2)) {
                out += sentence + "\n";
            }
        }
        return out;
    }

    Map<String, Double> reRank(String suffix1, String object1, String suffix2, String object2, TopDocs hits, double lambda) throws IOException {
        Map<String, Double> map1 = TopDocs2Map(hits);
        Map<String, Double> out = new LinkedHashMap<>();
        Map<String, Set<String>> docs = new HashMap<>();
        for (String pmcid : map1.keySet()) {
            String path = PubMedDoc.getAbsoluteFile(pmcid, dir_articles);
            PubMedDoc doc = new PubMedDoc(path);
            doc.replaceConcepts(object1, suffix1, pmcid2genes.get(pmcid), object2, suffix2, pmcid2diseases.get(pmcid));
            String docText = getSentences(doc, suffix1, suffix2);
            List<String> strTokens = tokenizeToArrayList(docText, new EnglishAnalyzer());
            Set<String> stringTokens = new HashSet<>(strTokens);
            docs.put(pmcid, stringTokens);
        }
        while (out.size() < topK) {
            Map<String, Double> map2 = new LinkedHashMap<>();
            for (String pmcid1 : map1.keySet()) {
                double maxSim = 0;
                for (String pmcid2 : out.keySet()) {
                    Double sim = (double) StringSimilarity.getCosineSimilarity(docs.get(pmcid1), docs.get(pmcid2));
                    if (maxSim < sim) {
                        maxSim = sim;
                    }
                }
                double sim = lambda * Math.log10(1 + map1.get(pmcid1)) - (1 - lambda) * maxSim;
                map2.put(pmcid1, sim);
            }
            List<String> list = new ArrayList<>(map2.keySet());
            list.sort((String e1, String e2) -> {
                try {
                    double v1 = map2.get(e1);
                    double v2 = map2.get(e2);
                    return Double.compare(v2, v1);
                } catch (Exception ex) {
                    return -1;
                }
            });
            out.put(list.get(0), map2.get(list.get(0)));
//            System.out.println(list.get(0) + ": " + map2.get(list.get(0)));
            map1.remove(list.get(0));
            if (map1.size() == 0) {
                break;
            }
        }
        return out;

    }

    public void search(double lambda) throws Exception {
        FileInputStream fstream;
        int i = 0;
        fstream = new FileInputStream(queries);
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
                i++;
                StringTokenizer st = new StringTokenizer(str, "\t");
                String queryid = st.nextToken();
                String object1 = st.nextToken();
                String object2 = st.nextToken();
                String y = st.nextToken().toLowerCase();
                BooleanQuery.Builder builder = new BooleanQuery.Builder();
                String suffix1 = PubMedDoc.SUFFIX_GENE;
                String suffix2 = PubMedDoc.SUFFIX_DISEASE;

                TermQuery tq1 = new TermQuery(new Term(MedlineDoc.ABSTRACT, (suffix1 + object1).toLowerCase()));
                TermQuery tq2 = new TermQuery(new Term(MedlineDoc.ABSTRACT, (suffix2 + object2.replace(":", "_")).toLowerCase()));
                builder.add(tq1, BooleanClause.Occur.SHOULD);
                builder.add(tq2, BooleanClause.Occur.SHOULD);
                Query query = builder.build();
                System.err.println(query);
                long start = System.currentTimeMillis();
                TopDocs hits = is.search(query, 30);
                long end = System.currentTimeMillis();
                System.err.println(i + "- Found " + hits.totalHits
                        + " document(s) has matched query. Processed in " + Functions.getTimer(end - start) + ".");

                Map<String, Double> map = reRank(suffix1, object1, suffix2, object2, hits, lambda);
//                 Map<String, Double> map = TopDocs2Map(hits);
                int j = 0;
                for (String pmcid : map.keySet()) {
                    j++;
//                    Explanation explanation = is.explain(query, scoreDoc.doc);
                    System.out.println(queryid + "\t" + object1 + "\t" + object2 + "\t" + y + "\t" + pmcid.toUpperCase() + "\t" + j + "\t" + map.get(pmcid));
//                    System.out.println(explanation.toString());
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(MutualReferences.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        String indexDir;
        String queries;
        String dir_articles;
        String mapFile;
        String nlpmodel;
        int topk = 30;
        double lambda;
        if (args.length == 0) {
//            indexDir = "/Volumes/Macintosh HD/Users/mbouadjenek/Documents/bioFactChecking/medline_index_v1.0/";
            indexDir = "/Volumes/TOSHIBA EXT/Documents/bioFactChecking/index_mentions_v1.0/";
            queries = "/Volumes/TOSHIBA EXT/Documents/bioFactChecking/queries/gd_dataset.txt";
            dir_articles = "/Users/rbouadjenek/Desktop/test/";
            mapFile = "/Volumes/TOSHIBA EXT/Documents/bioFactChecking/index_JDBM_v3.0/db_index";
            nlpmodel = "en-sent.bin";
            lambda = 0.5;
        } else {
            indexDir = args[0];
            queries = args[1];
            dir_articles = args[2];
            mapFile = args[3];
            nlpmodel = args[4];
            lambda = Double.parseDouble(args[5]);
        }
        try {
            SearchDiv searcher = new SearchDiv(indexDir, queries, dir_articles, mapFile, nlpmodel, topk);
            searcher.search(lambda);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
