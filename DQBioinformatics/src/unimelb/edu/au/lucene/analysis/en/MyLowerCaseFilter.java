/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unimelb.edu.au.lucene.analysis.en;

import java.io.IOException;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;
import org.apache.lucene.analysis.util.CharacterUtils;

/**
 * Normalizes token text to lower case.
 */
public final class MyLowerCaseFilter extends TokenFilter {

    private final CharacterUtils charUtils = CharacterUtils.getInstance();
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final KeywordAttribute keywordAttr = addAttribute(KeywordAttribute.class);
    private final boolean usekeywords;// reda modification

    /**
     * Create a new LowerCaseFilter, that normalizes token text to lower case.
     *
     * @param in TokenStream to filter
     * @param usekeywords indicates if a keyword should be set to lower case or
     * not.
     */
    public MyLowerCaseFilter(TokenStream in, boolean usekeywords) {
        super(in);
        this.usekeywords = usekeywords;
    }

    @Override
    public final boolean incrementToken() throws IOException {
        if (input.incrementToken()) {
            if (!keywordAttr.isKeyword() || usekeywords) {
                charUtils.toLowerCase(termAtt.buffer(), 0, termAtt.length());
            }
            return true;
        } else {
            return false;
        }
    }
}
