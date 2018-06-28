/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unimelb.edu.au.lucene.analysis.en;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.EnglishPossessiveFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.miscellaneous.SetKeywordMarkerFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;

/**
 *
 * @author mbouadjenek
 */
public class MyEnglishAnalyzer extends StopwordAnalyzerBase {

    private final CharArraySet stemExclusionSet;

    /**
     * Returns an unmodifiable instance of the default stop words set.
     *
     * @return default stop words set.
     */
    public static CharArraySet getDefaultStopSet() {
        return DefaultSetHolder.DEFAULT_STOP_SET;
    }

    /**
     * Atomically loads the DEFAULT_STOP_SET in a lazy fashion once the outer
     * class accesses the static final set the first time.;
     */
    private static class DefaultSetHolder {

        static final CharArraySet DEFAULT_STOP_SET = StandardAnalyzer.STOP_WORDS_SET;
    }

    /**
     * Builds an analyzer with the default stop words:
     * {@link #getDefaultStopSet}.
     */
    public MyEnglishAnalyzer() {
        this(DefaultSetHolder.DEFAULT_STOP_SET);
    }

    /**
     * Builds an analyzer with the given stop words. Stop words are removed
     * after stemming.
     *
     * @param stopwords a stopword set
     */
    public MyEnglishAnalyzer(CharArraySet stopwords) {
        this(stopwords, CharArraySet.EMPTY_SET);
    }

    /**
     * Builds an analyzer with the given stop words. If a non-empty stem
     * exclusion set is provided this analyzer will add a
     * {@link SetKeywordMarkerFilter} before stemming.
     *
     * @param stopwords a stopword set
     * @param stemExclusionSet a set of terms not to be stemmed
     */
    public MyEnglishAnalyzer(CharArraySet stopwords, CharArraySet stemExclusionSet) {
        super(stopwords);
        this.stemExclusionSet = CharArraySet.unmodifiableSet(CharArraySet.copy(stemExclusionSet));
    }

    /**
     * Creates a
     * {@link org.apache.lucene.analysis.Analyzer.TokenStreamComponents} which
     * tokenizes all the text in the provided {@link Reader}. Reda: Here, stop
     * words are removed after stemming.
     *
     * @return A
     * {@link org.apache.lucene.analysis.Analyzer.TokenStreamComponents} built
     * from an {@link StandardTokenizer} filtered with null null     {@link StandardFilter}, {@link EnglishPossessiveFilter}, 
   *         {@link LowerCaseFilter}, {@link StopFilter}
     *         , {@link SetKeywordMarkerFilter} if a stem exclusion set is provided and
     * {@link PorterStemFilter}.
     */
    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        final Tokenizer source = new StandardTokenizer();
        TokenStream result = new StandardFilter(source);
        if (!stemExclusionSet.isEmpty()) {
            result = new SetKeywordMarkerFilter(result, stemExclusionSet);
        }
        result = new EnglishPossessiveFilter(result);
        result = new MyLowerCaseFilter(result, false);
        result = new StopFilter(result, stopwords);
        result = new MyLowerCaseFilter(result, true);
        result = new MyPorterStemFilter(result);
        
        return new TokenStreamComponents(source, result);
    }
}
