/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unimelb.edu.au.run;

import banner.eval.BANNER;
import banner.postprocessing.PostProcessor;
import banner.tagging.CRFTagger;
import banner.tokenization.Tokenizer;
import banner.types.Mention;
import banner.types.Sentence;
import banner.types.SentenceWithOffset;
import banner.util.RankedList;
import dnorm.core.DiseaseNameAnalyzer;
import dnorm.core.Lexicon;
import dnorm.core.MEDICLexiconLoader;
import dnorm.core.SynonymTrainer;
import dnorm.types.FullRankSynonymMatrix;
import dnorm.types.SynonymMatrix;
import dragon.nlp.tool.Tagger;
import dragon.nlp.tool.lemmatiser.EngLemmatiser;
import dragon.util.EnvVariable;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;

/**
 *
 * @author mbouadjenek
 */
public class DNorm {

    private HierarchicalConfiguration config;
    private CRFTagger tagger;
    private final DiseaseNameAnalyzer analyzer;
    private final Lexicon lex;
    private final MEDICLexiconLoader loader;
    private static SynonymTrainer syn;

    public DNorm(String configurationFilename, String lexiconFilename, String matrixFilename) {
        try {
            prepareBANNER(configurationFilename);
        } catch (ConfigurationException | IOException e1) {
            throw new RuntimeException(e1);
        }
        analyzer = DiseaseNameAnalyzer.getDiseaseNameAnalyzer(true, true, false, true);
        loader = new MEDICLexiconLoader();
        lex = new Lexicon(analyzer);
        loader.loadLexicon(lex, lexiconFilename);
        lex.prepare();
        SynonymMatrix matrix = FullRankSynonymMatrix.load(new File(matrixFilename));
        syn = new SynonymTrainer(lex, matrix, 1000);
    }

    private void prepareBANNER(String configurationFile) throws ConfigurationException, IOException {
        long start = System.currentTimeMillis();
        config = new XMLConfiguration(configurationFile);
        EnvVariable.setDragonHome(".");
        EnvVariable.setCharSet("US-ASCII");
        EngLemmatiser lemmatiser = BANNER.getLemmatiser(config);
        Tagger posTagger = BANNER.getPosTagger(config);
        HierarchicalConfiguration localConfig = config.configurationAt(BANNER.class.getPackage().getName());
        String modelFilename = localConfig.getString("modelFilename");
        System.err.println("Model: " + modelFilename);
        tagger = CRFTagger.load(new File(modelFilename), lemmatiser, posTagger);
        System.err.println("Completed input: " + (System.currentTimeMillis() - start));
    }

    public List<String> processSentences_BANNER(String text) {
        // TODO Refactor this into separate NER and normalization methods
        List<String> output = new ArrayList<>();
        Sentence bannerSentence = new SentenceWithOffset("", "", text, 0);
        Tokenizer tokenizer = BANNER.getTokenizer(config);
        PostProcessor postProcessor = BANNER.getPostProcessor(config);
        Sentence outputSentence = BANNER.process(tagger, tokenizer, postProcessor, bannerSentence);
        for (Mention mention : outputSentence.getMentions(Mention.MentionType.Found)) {
            String lookupText = mention.getText();
            output.add(lookupText);

            RankedList<SynonymTrainer.LookupResult> results = syn.lookup(lookupText);
            if (results.size() > 0) {
                String conceptId = results.getObject(0).getConceptId();
                mention.setConceptId(conceptId);
                System.out.println(conceptId + "\t" + mention.getText());
            }
        }
        return output;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws ConfigurationException, IOException {
        // TODO code application logic here
        ApplyDNorm dnorm = new ApplyDNorm("config/banner_NCBIDisease_UMLS2013AA_TEST.xml", "data/CTD_diseases_old.tsv", "output/simmatrix_NCBIDisease_e4.bin", "AB3P_DIR/", "tmp/");

//        DNorm dnorm = new DNorm("config/banner_NCBIDisease_TEST.xml", "data/CTD_diseases_old.tsv", "output/simmatrix_NCBIDisease_e4.bin");
        try {
            FileInputStream fstream = new FileInputStream(args[0]);
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
                    String diseaseid = st.nextToken();
                    String diseasename = st.nextToken();
                    System.err.println(i + "- " + str);
                    List<ApplyDNorm.DNormResult> out = dnorm.process(diseasename);
                    for (ApplyDNorm.DNormResult r : out) {
                        if (!r.getMentionText().equals(diseaseid)) {
                            System.out.println(diseaseid + "\t" + r.getConceptID());
                        }
                    }

                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }

    }

}
