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
import banner.util.RankedList;
import dnorm.core.DiseaseNameAnalyzer;
import dnorm.core.Lexicon;
import dnorm.core.MEDICLexiconLoader;
import dnorm.core.SynonymTrainer;
import dnorm.types.FullRankSynonymMatrix;
import dnorm.util.AbbreviationIdentifier;
import dnorm.util.AbbreviationResolver;
import dnorm.util.PubtatorReader;
import dragon.nlp.tool.Tagger;
import dragon.nlp.tool.lemmatiser.EngLemmatiser;
import dragon.util.EnvVariable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import unimelb.edu.au.util.Functions;

/**
 *
 * @author mbouadjenek
 */
public class ApplyDNorm {

    // TODO Re-integrate abbreviation resolution
    private AbbreviationIdentifier abbrev;
    private CRFTagger tagger;
    private Tokenizer tokenizer;
    private PostProcessor postProcessor;
    private SynonymTrainer syn;

    public ApplyDNorm(String configurationFilename, String lexiconFilename, String matrixFilename, String abbreviationDirectory, String tempDirectory) throws ConfigurationException, IOException {
        long start = System.currentTimeMillis();
        DiseaseNameAnalyzer analyzer = DiseaseNameAnalyzer.getDiseaseNameAnalyzer(true, true, false, true);
        Lexicon lex = new Lexicon(analyzer);
        MEDICLexiconLoader loader = new MEDICLexiconLoader();

        loader.loadLexicon(lex, lexiconFilename);
        lex.prepare();

        System.err.println("Lexicon loaded; elapsed = " + Functions.getTimer(System.currentTimeMillis() - start));
        start = System.currentTimeMillis();
        FullRankSynonymMatrix matrix = FullRankSynonymMatrix.load(new File(matrixFilename));
        syn = new SynonymTrainer(lex, matrix, 1000);
        System.err.println("Matrix loaded; elapsed = " + Functions.getTimer(System.currentTimeMillis() - start));

        start = System.currentTimeMillis();
        HierarchicalConfiguration config = new XMLConfiguration(configurationFilename);
        EnvVariable.setDragonHome(".");
        EnvVariable.setCharSet("US-ASCII");
        EngLemmatiser lemmatiser = BANNER.getLemmatiser(config);

        Tagger posTagger = BANNER.getPosTagger(config);

        HierarchicalConfiguration localConfig = config.configurationAt(BANNER.class.getPackage().getName());
        String modelFilename = localConfig.getString("modelFilename");
        tokenizer = BANNER.getTokenizer(config);
        postProcessor = BANNER.getPostProcessor(config);
        tagger = CRFTagger.load(new File(modelFilename), lemmatiser, posTagger);
        System.err.println("BANNER loaded; elapsed = " + Functions.getTimer(System.currentTimeMillis() - start));
        abbrev = new AbbreviationIdentifier("./identify_abbr", abbreviationDirectory, tempDirectory, 1000);
    }

    public List<DNormResult> process(String content) throws IOException {
        PubtatorReader.Abstract a = new PubtatorReader.Abstract();
        a.setId("ID");
        a.setAbstractText(content);
        a.setTitleText("");
        List<DNormResult> results = process(a);
        Collections.sort(results, new Comparator<DNormResult>() {
            @Override
            public int compare(DNormResult r1, DNormResult r2) {
                return r1.getStartChar() - r2.getStartChar();
            }
        });
        return results;
    }

    private List<DNormResult> process(PubtatorReader.Abstract a) throws IOException {

        String text = a.getText();
//        System.err.println("Text received: " + text);
        if (text == null) {
            return new ArrayList<>();
        }
        Map<String, String> abbreviationMap = abbrev.getAbbreviations(a.getId(), text);
        List<DNormResult> found = processText(a, abbreviationMap);
//        System.out.println(found.size());
//        System.out.println("Mentions found:");
//        for (DNormResult result : found) {
//            System.out.println("\t" + result.toString());
//        }
        if (abbreviationMap == null) {
            return found;
        }
        List<DNormResult> returned = extendResults(text, found, abbreviationMap);
//        System.out.println("Mentions added:");
        List<DNormResult> added = new ArrayList<>(returned);
        added.removeAll(found);
//        for (DNormResult result : added) {
//            System.out.println("\t" + result.toString());
//        }
//        System.out.println("Mentions removed:");
        List<DNormResult> removed = new ArrayList<>(found);
        removed.removeAll(returned);
//        for (DNormResult result : removed) {
//            System.out.println("\t" + result.toString());
//        }
        return returned;
    }

    private List<DNormResult> processText(PubtatorReader.Abstract a, Map<String, String> abbreviationMap) {
        List<DNormResult> results = new ArrayList<>();
        int index = 0;
        List<String> sentences = a.getSentenceTexts();
        for (int i = 0; i < sentences.size(); i++) {
            String sentence = sentences.get(i);
            int length = sentence.length();
            sentence = sentence.trim();
            if (sentence.length() == 0) {
                continue;
            }
            Sentence sentence1 = new Sentence(a.getId() + "-" + i, a.getId(), sentence);
            Sentence sentence2 = BANNER.process(tagger, tokenizer, postProcessor, sentence1);
            for (Mention mention : sentence2.getMentions(Mention.MentionType.Found)) {
                int start = index + mention.getStartChar();
                int end = start + mention.getText().length();

                DNormResult result = new DNormResult(start, end, mention.getText());
                String lookupText = result.getMentionText();
                lookupText = AbbreviationResolver.expandAbbreviations(lookupText, abbreviationMap);
                RankedList<SynonymTrainer.LookupResult> lookup = syn.lookup(lookupText);
                if (lookup.size() > 0) {
                    result.setConceptID(lookup.getObject(0).getConceptId(), lookup.getValue(0));
                }
                results.add(result);
            }
            index += length;
        }
        return results;
    }

    private List<DNormResult> extendResults(String text, List<DNormResult> results, Map<String, String> shortLongAbbrevMap) {
        // Get long->short map
        Map<String, String> longShortAbbrevMap = new HashMap<>();
        for (String shortText : shortLongAbbrevMap.keySet()) {
            String longText = shortLongAbbrevMap.get(shortText);
            longShortAbbrevMap.put(longText, shortText);
        }

        // Create a set of strings to be set as results
        Set<DNormResult> unlocalizedResults = new HashSet<>();
        for (DNormResult result : results) {
            if (result.getConceptID() != null) {
                unlocalizedResults.add(new DNormResult(-1, -1, result.getMentionText(), result.getConceptID(), result.getScore()));
                if (shortLongAbbrevMap.containsKey(result.getMentionText())) {
                    String mentionText = shortLongAbbrevMap.get(result.getMentionText());
                    // TODO Verify mentionText realistically normalizes to the concept intended, or we will drop the original result
                    unlocalizedResults.add(new DNormResult(-1, -1, mentionText, result.getConceptID(), result.getScore()));
                }
                if (longShortAbbrevMap.containsKey(result.getMentionText())) {
                    String mentionText = longShortAbbrevMap.get(result.getMentionText());
                    unlocalizedResults.add(new DNormResult(-1, -1, mentionText, result.getConceptID(), result.getScore()));
                }
            }
        }

        return localizeResults(text, unlocalizedResults);
    }

    private List<DNormResult> localizeResults(String text, Set<DNormResult> unlocalizedResults) {
        // Add a result for each instance of a mention found
        List<DNormResult> newResults = new ArrayList<>();
        for (DNormResult result : unlocalizedResults) {
            String pattern = "\\b" + Pattern.quote(result.getMentionText()) + "\\b";
            Pattern mentionPattern = Pattern.compile(pattern);
            Matcher textMatcher = mentionPattern.matcher(text);
            while (textMatcher.find()) {
                newResults.add(new DNormResult(textMatcher.start(), textMatcher.end(), result.getMentionText(), result.getConceptID(), result.getScore()));
            }
        }

        // If two results overlap, remove the shorter of the two
        List<DNormResult> newResults2 = new ArrayList<>();
        for (int i = 0; i < newResults.size(); i++) {
            DNormResult result1 = newResults.get(i);
            boolean add = true;
            for (int j = 0; j < newResults.size() && add; j++) {
                DNormResult result2 = newResults.get(j);
                if (i != j && result1.overlaps(result2) && result2.getMentionText().length() > result1.getMentionText().length()) {
                    add = false;
                }
            }
            if (add) {
                newResults2.add(result1);
            }
        }
        return newResults2;
    }

    public static class DNormResult {

        private int startChar;
        private int endChar;
        private String mentionText;
        private String conceptID;
        private double score;

        public DNormResult(int startChar, int endChar, String mentionText) {
            this.startChar = startChar;
            this.endChar = endChar;
            this.mentionText = mentionText;
        }

        public DNormResult(int startChar, int endChar, String mentionText, String conceptID, double score) {
            this.startChar = startChar;
            this.endChar = endChar;
            this.mentionText = mentionText;
            this.conceptID = conceptID;
            this.score = score;
        }

        public String getConceptID() {
            return conceptID;
        }

        public void setConceptID(String conceptID, double score) {
            this.conceptID = conceptID;
            this.score = score;
        }

        public int getStartChar() {
            return startChar;
        }

        public int getEndChar() {
            return endChar;
        }

        public String getMentionText() {
            return mentionText;
        }

        public double getScore() {
            return score;
        }

        public boolean overlaps(DNormResult result) {
            return endChar > result.startChar && startChar < result.endChar;
        }

        @Override
        public String toString() {
            return mentionText + " (" + startChar + ", " + endChar + ") -> " + conceptID + " @ " + score;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((conceptID == null) ? 0 : conceptID.hashCode());
            result = prime * result + endChar;
            result = prime * result + ((mentionText == null) ? 0 : mentionText.hashCode());
            long temp;
            temp = Double.doubleToLongBits(score);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            result = prime * result + startChar;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            DNormResult other = (DNormResult) obj;
            if (conceptID == null) {
                if (other.conceptID != null) {
                    return false;
                }
            } else if (!conceptID.equals(other.conceptID)) {
                return false;
            }
            if (endChar != other.endChar) {
                return false;
            }
            if (mentionText == null) {
                if (other.mentionText != null) {
                    return false;
                }
            } else if (!mentionText.equals(other.mentionText)) {
                return false;
            }
            if (Double.doubleToLongBits(score) != Double.doubleToLongBits(other.score)) {
                return false;
            }
            if (startChar != other.startChar) {
                return false;
            }
            return true;
        }
    }

    /**
     * @param args the command line arguments
     * @throws org.apache.commons.configuration.ConfigurationException
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws ConfigurationException, IOException {
        // TODO code application logic here
        ApplyDNorm dnorm = new ApplyDNorm("config/banner_NCBIDisease_UMLS2013AA_TEST.xml", "data/CTD_diseases_old.tsv", "output/simmatrix_NCBIDisease_e4.bin", "AB3P_DIR/", "tmp/");
        long start = System.currentTimeMillis();
        List<DNormResult> results = dnorm.process("The small GTPase Rab7 is a key regulator of endosomal maturation in eukaryotic cells. Mutations in rab7 are thought to cause the dominant neuropathy Charcot-Marie-Tooth 2B (CMT2B) by a gain-of-function mechanism. Here we show that loss of rab7, but not overexpression of rab7 CMT2B mutants, causes adult-onset neurodegeneration in a Drosophila model. All CMT2B mutant proteins retain 10-50% function based on quantitative imaging, electrophysiology, and rescue experiments in sensory and motor neurons in vivo. Consequently, expression of CMT2B mutants at levels between 0.5 and 10-fold their endogenous levels fully rescues the neuropathy-like phenotypes of the rab7 mutant. Live imaging reveals that CMT2B proteins are inefficiently recruited to endosomes, but do not impair endosomal maturation. These findings are not consistent with a gain-of-function mechanism. Instead, they indicate a dosage-dependent sensitivity of neurons to rab7-dependent degradation. Our results suggest a therapeutic approach opposite to the currently proposed reduction of mutant protein function. DOI: http://dx.doi.org/10.7554/eLife.01064.001.");

        for (DNormResult r : results) {
            System.out.print(r.getStartChar() + "\t" + r.getEndChar() + "\t" + r.getMentionText() + "\tDisease");
            if (r.getConceptID() != null) {
                System.out.print("\t" + r.getConceptID());
            }
            System.out.println("");
        }
        long end = System.currentTimeMillis();
        System.out.println("-------It took " + Functions.getTimer(end - start) + " ---------------");
//        start = System.currentTimeMillis();
//        results = dnorm.process("Search for data on neural brain tissue in transgenic mice related to Huntington's disease. ");
//
//        for (DNormResult r : results) {
//            System.out.print(r.getStartChar() + "\t" + r.getEndChar() + "\t" + r.getMentionText() + "\tDisease");
//            if (r.getConceptID() != null) {
//                System.out.print("\t" + r.getConceptID());
//            }
//            System.out.println("");
//        }
//        end = System.currentTimeMillis();
//        System.out.println("-------It took " + Functions.getTimer(end - start) + " ---------------");
//        start = System.currentTimeMillis();
//        results = dnorm.process(" Search for all data on the SNCA gene related to Parkinson's disease across all databases.");
//
//        for (DNormResult r : results) {
//            System.out.print(r.getStartChar() + "\t" + r.getEndChar() + "\t" + r.getMentionText() + "\tDisease");
//            if (r.getConceptID() != null) {
//                System.out.print("\t" + r.getConceptID());
//            }
//            System.out.println("");
//        }
//        end = System.currentTimeMillis();
//        System.out.println("-------It took " + Functions.getTimer(end - start) + " ---------------");
//        start = System.currentTimeMillis();
//        results = dnorm.process("Inflammation during oxidative stress in human hepatic cells.");
//
//        for (DNormResult r : results) {
//            System.out.print(r.getStartChar() + "\t" + r.getEndChar() + "\t" + r.getMentionText() + "\tDisease");
//            if (r.getConceptID() != null) {
//                System.out.print("\t" + r.getConceptID());
//            }
//            System.out.println("");
//        }
//        end = System.currentTimeMillis();
//        System.out.println("-------It took " + Functions.getTimer(end - start) + " ---------------");

    }
}
