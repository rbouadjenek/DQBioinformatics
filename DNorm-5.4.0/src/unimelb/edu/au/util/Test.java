/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unimelb.edu.au.util;

import banner.util.RankedList;
import dnorm.core.DiseaseNameAnalyzer;
import dnorm.core.Lexicon;
import dnorm.core.MEDICLexiconLoader;
import dnorm.core.SynonymTrainer;
import static dnorm.core.DiseaseNameAnalyzer.DEFAULT_STOPWORD_SET;
import dnorm.types.FullRankSynonymMatrix;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharTokenizer;
import org.apache.lucene.analysis.LetterTokenizer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceTokenizer;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.CharsRef;
import unimelb.edu.au.util.Functions;

/**
 *
 * @author mbouadjenek
 */
public class Test {

    public static String processString(String ch, Analyzer analyzer) {
        try {
            String out = "";
            try (TokenStream stream = analyzer.tokenStream(null, new StringReader(ch))) {
                CharTermAttribute cattr = stream.addAttribute(CharTermAttribute.class);
                stream.reset();
                while (stream.incrementToken()) {
                    out += cattr.toString() + " ";
                }
                stream.end();
            }
            return out.trim();
        } catch (IOException ex) {
            Logger.getLogger(Functions.class.getName()).log(Level.SEVERE, null, ex);
            return "";
        }
    }

    public static void main(String[] args) throws ConfigurationException, IOException {
        long start = System.currentTimeMillis();
        DiseaseNameAnalyzer analyzer = DiseaseNameAnalyzer.getDiseaseNameAnalyzer(true, true, false, true);
        Lexicon lex = new Lexicon(analyzer);
        MEDICLexiconLoader loader = new MEDICLexiconLoader();

        loader.loadLexicon(lex, "data/CTD_diseases.tsv");
        lex.prepare();

        System.err.println("Lexicon loaded; elapsed = " + Functions.getTimer(System.currentTimeMillis() - start));
        FullRankSynonymMatrix matrix = FullRankSynonymMatrix.load(new File("output/simmatrix_NCBIDisease_e4.bin"));
        SynonymTrainer syn = new SynonymTrainer(lex, matrix, 1000);
        RankedList<SynonymTrainer.LookupResult> lookup = syn.lookup("Huntington's disease");
        System.out.println(lookup.getObject(0).getConceptId()+"\t"+lookup.getValue(0));
    }
}
