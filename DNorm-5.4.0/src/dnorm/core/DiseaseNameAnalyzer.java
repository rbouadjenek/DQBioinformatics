package dnorm.core;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//import org.apache.lucene.analysis.ASCIIFoldingFilter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharTokenizer;
import org.apache.lucene.analysis.PorterStemFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.synonym.SynonymFilter;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.CharsRef;
import org.apache.lucene.util.Version;

import dnorm.util.AcronymPreservingLowerCaseFilter;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.util.CharArraySet;

public final class DiseaseNameAnalyzer extends Analyzer {

    public static final int DEFAULT_CASE_FOLDING_LENGTH = 4;

    public static final Set<String> DEFAULT_STOPWORD_SET = new HashSet<String>(Arrays.asList("a", "an", "and", "are", "as", "at", "be", "but", "by", "for", "if", "in", "into", "is", "it", "no",
            "not", "of", "on", "or", "such", "that", "the", "their", "then", "there", "these", "they", "this", "to", "was", "will", "with"));

    private int caseFoldingLength;
    private boolean useStemming;
    private Set<String> stopWords;
    private SynonymMap synonyms;
    private boolean retainDigits;
    private int minLength;

    // TODO Better integrate this into the class
    public static DiseaseNameAnalyzer getDiseaseNameAnalyzer(boolean useStopWords, boolean numericSynonyms, boolean nearSynonyms, boolean stem) {
        try {
            Set<String> stopWords = null;
            if (useStopWords) {
                stopWords = DEFAULT_STOPWORD_SET;
            }
            SynonymMap.Builder synonyms = new SynonymMap.Builder(true);
            if (numericSynonyms) {
                synonyms.add(new CharsRef("first"), new CharsRef("1"), false);
                synonyms.add(new CharsRef("second"), new CharsRef("2"), false);
                synonyms.add(new CharsRef("third"), new CharsRef("3"), false);
                synonyms.add(new CharsRef("fourth"), new CharsRef("4"), false);
                synonyms.add(new CharsRef("fifth"), new CharsRef("5"), false);
                synonyms.add(new CharsRef("sixth"), new CharsRef("6"), false);
                synonyms.add(new CharsRef("seventh"), new CharsRef("7"), false);
                synonyms.add(new CharsRef("eighth"), new CharsRef("8"), false);
                synonyms.add(new CharsRef("ninth"), new CharsRef("9"), false);
                synonyms.add(new CharsRef("I"), new CharsRef("1"), false);
                synonyms.add(new CharsRef("II"), new CharsRef("2"), false);
                synonyms.add(new CharsRef("III"), new CharsRef("3"), false);
                synonyms.add(new CharsRef("IV"), new CharsRef("4"), false);
                synonyms.add(new CharsRef("V"), new CharsRef("5"), false);
                synonyms.add(new CharsRef("VI"), new CharsRef("6"), false);
                synonyms.add(new CharsRef("VII"), new CharsRef("7"), false);
                synonyms.add(new CharsRef("VIII"), new CharsRef("8"), false);
                synonyms.add(new CharsRef("IX"), new CharsRef("9"), false);
            }
            if (nearSynonyms) {
                synonyms.add(new CharsRef("dominant"), new CharsRef("genetic"), false);
                synonyms.add(new CharsRef("recessive"), new CharsRef("genetic"), false);
                synonyms.add(new CharsRef("inherited"), new CharsRef("genetic"), false);
                synonyms.add(new CharsRef("hereditary"), new CharsRef("genetic"), false);
                synonyms.add(new CharsRef("disorder"), new CharsRef("disease"), false);
                synonyms.add(new CharsRef("abnormality"), new CharsRef("disease"), false);
                synonyms.add(new CharsRef("absence"), new CharsRef("deficiency"), false);
                synonyms.add(new CharsRef("handicap"), new CharsRef("deficiency"), false);
            }
            SynonymMap map = null;
            if (numericSynonyms || nearSynonyms) {
                map = synonyms.build();
            }
            return new DiseaseNameAnalyzer(stem, stopWords, map);
        } catch (IOException e) {
            // TODO Improve exception handling
            throw new RuntimeException(e);
        }
    }

    public DiseaseNameAnalyzer(boolean useStemming) {
        this(useStemming, null, null, true, 1, DEFAULT_CASE_FOLDING_LENGTH);
    }

    public DiseaseNameAnalyzer(boolean useStemming, Set<String> stopWords, SynonymMap synonyms) {
        this(useStemming, stopWords, synonyms, true, 1, DEFAULT_CASE_FOLDING_LENGTH);
    }

    public DiseaseNameAnalyzer(boolean useStemming, Set<String> stopWords, SynonymMap synonyms, boolean retainDigits, int minLength) {
        this(useStemming, stopWords, synonyms, retainDigits, minLength, DEFAULT_CASE_FOLDING_LENGTH);
    }

    public DiseaseNameAnalyzer(boolean useStemming, Set<String> stopWords, SynonymMap synonyms, boolean retainDigits, int minLength, int caseFoldingLength) {
        this.useStemming = useStemming;
        this.stopWords = stopWords;
        this.synonyms = synonyms;
        this.retainDigits = retainDigits;
        this.minLength = minLength;
        this.caseFoldingLength = caseFoldingLength;
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        final Tokenizer source = new StandardTokenizer();
        TokenStream result = new ASCIIFoldingFilter(source);

        if (minLength > 1) {
            result = new LengthFilter(result, minLength);
        }
        if (caseFoldingLength >= 0) {
            result = new AcronymPreservingLowerCaseFilter(Version.LATEST, result, caseFoldingLength);
        }
        if (stopWords != null) {
            result = new org.apache.lucene.analysis.core.StopFilter(result, CharArraySet.unmodifiableSet(new CharArraySet(stopWords, false)));
        }
        
        if (synonyms != null) {
            result = new SynonymFilter(result, synonyms, false);
        }
        // stream = new AmericanBritishSpellingNormalizer(stream);
        if (useStemming) {
            result = new org.apache.lucene.analysis.en.PorterStemFilter(result);
        }
        return new TokenStreamComponents(source, result);
    }

//    @Override
//    public TokenStream tokenStream_(String fieldName, Reader reader) {
//        final Tokenizer source = new StandardTokenizer();
//        TokenStream stream = new ASCIIFoldingFilter(source);
//        if (minLength > 1) {
//            stream = new LengthFilter(stream, minLength);
//        }
//        if (caseFoldingLength >= 0) {
//            stream = new AcronymPreservingLowerCaseFilter(Version.LATEST, stream, caseFoldingLength);
//        }
//        if (stopWords != null) {
//            stream = new org.apache.lucene.analysis.core.StopFilter(stream, CharArraySet.unmodifiableSet(new CharArraySet(stopWords, false)));
//        }
//        if (synonyms != null) {
//            stream = new SynonymFilter(stream, synonyms, false);
//        }
////         stream = new AmericanBritishSpellingNormalizer(stream);
////        if (useStemming) {
////            stream = new PorterStemFilter(stream);
////        }
//
//        return stream;
//    }
//    public TokenStream reusableTokenStream(String fieldName, Reader reader) throws IOException {
//        SavedStreams streams = (SavedStreams) getPreviousTokenStream();
//        if (streams == null) {
//            streams = new SavedStreams();
//            streams.source = getTokenizer(reader);
//            streams.result = new ASCIIFoldingFilter(streams.source);
//            if (minLength > 1) {
//                streams.result = new LengthFilter(streams.result, minLength);
//            }
//            if (caseFoldingLength >= 0) {
//                streams.result = new AcronymPreservingLowerCaseFilter(Version.LATEST, streams.result, caseFoldingLength);
//            }
//            if (stopWords != null) {
//                streams.result = new StopFilter(streams.result, stopWords);
//            }
//            if (synonyms != null) {
//                streams.result = new SynonymFilter(streams.result, synonyms, false);
//            }
//            // streams.result = new AmericanBritishSpellingNormalizer(streams.result);
//            if (useStemming) {
//                streams.result = new PorterStemFilter(streams.result);
//            }
//            setPreviousTokenStream(streams);
//        } else {
//            streams.source.reset(reader);
//        }
//        return streams.result;
//    }

//    private static class SavedStreams {
//
//        Tokenizer source;
//        TokenStream result;
//    }
//
//    private Tokenizer getTokenizer(Reader reader) {
//        // reader = modifyReader(reader); // FIXME
//        if (retainDigits) {
//            return new CharTokenizer(reader) {
//                @Override
//                protected boolean isTokenChar(char c) {
//                    return Character.isLetter(c) || Character.isDigit(c);
//                }
//
//                @Override
//                public boolean incrementToken() throws IOException {
//                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//                }
//            };
//        } else {
//            return new CharTokenizer(reader) {
//                @Override
//                protected boolean isTokenChar(char c) {
//                    return Character.isLetter(c);
//                }
//
//                @Override
//                public boolean incrementToken() throws IOException {
//                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//                }
//            };
//        }
//    }

    private static class LengthFilter extends TokenFilter {

        int minLength;

        protected LengthFilter(TokenStream input, int minLength) {
            super(input);
            this.minLength = minLength;
        }

        @Override
        public final boolean incrementToken() throws IOException {
            // return the first non-stop word found
            while (input.incrementToken()) {
                CharTermAttribute term = input.getAttribute(CharTermAttribute.class);
                if (term.length() >= minLength) {
                    return true;
                }
            }
            // reached EOS -- return false
            return false;
        }
    }

    // TODO Refactor code to eliminate this call
    public List<String> getTokens(String name) {
        try {
            List<String> docTerms = new ArrayList<>();
//            TokenStream tokenStream = tokenStream_("name", new StringReader(name));

//            String out = "";
//            try (TokenStream stream = this.tokenStream(null, new StringReader(name))) {
//                CharTermAttribute cattr = stream.addAttribute(CharTermAttribute.class);
//                stream.reset();
//                while (stream.incrementToken()) {
//                    out += cattr.toString() + " ";
//                }
//                stream.end();
//            }
//            System.out.println(out);
            TokenStream tokenStream = this.tokenStream("name", new StringReader(name));
            CharTermAttribute cattr = tokenStream.addAttribute(CharTermAttribute.class);
            tokenStream.reset();
            while (tokenStream.incrementToken()) {
                docTerms.add(cattr.toString());
            }
            tokenStream.close();
            // TODO Is this needed? tokenStream.close();
            return docTerms;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

//    public String getTransformed(String name) {
//        try {
//            TokenStream tokenStream = tokenStream_("name", new StringReader(name));
//            StringBuilder transformed = new StringBuilder();
//            while (tokenStream.incrementToken()) {
//                CharTermAttribute term = tokenStream.getAttribute(CharTermAttribute.class);
//                transformed.append(" ");
//                transformed.append(term.toString());
//            }
//            return transformed.toString();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
}
