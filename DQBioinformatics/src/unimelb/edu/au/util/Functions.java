/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unimelb.edu.au.util;

import java.io.IOException;
import java.io.StringReader;
import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.queryparser.classic.QueryParser;

/**
 *
 * @author mbouadjenek
 */
public class Functions {

    
    /**
     * Simple analyzer without any stemming or stop-word removal.
     */
    public static Analyzer analyzer = new Analyzer() {
        @Override
        protected Analyzer.TokenStreamComponents createComponents(String fieldName) {
            final Tokenizer source = new StandardTokenizer();
            TokenStream result = new StandardFilter(source);
            result = new LowerCaseFilter(result);
            return new Analyzer.TokenStreamComponents(source, result);
        }
    };

    public static String getTimer(long millis) {
        return String.format("%d hour, %d min, %d sec",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis)
                - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis)
                - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
        );
    }

    public static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?") || str.matches("-?\\d+(\\,\\d+)?");  //match a number with optional '-' and decimal.
    }
    public static final DecimalFormat df = new DecimalFormat();

    static {
        df.setMaximumFractionDigits(2); //arrondi à 2 chiffres apres la virgules
        df.setMinimumFractionDigits(2);
        df.setDecimalSeparatorAlwaysShown(true);
    }

    /**
     * Process a string using an analyzer and return a new string.
     *
     * @param ch
     * @param analyzer
     * @return
     */
    public static String processString(String ch, Analyzer analyzer) {
        try {
            String out = "";
            try (TokenStream stream = analyzer.tokenStream(null, new StringReader(QueryParser.escape(ch)))) {
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

    public static void main(String[] args) {
        System.out.println(processString("Reda Is going To School", analyzer));
    }

}
