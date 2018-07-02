/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unimelb.edu.au.search;

import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.SmallFloat;

/**
 *
 * @author rbouadjenek
 */
public class MySim extends TFIDFSimilarity {

    @Override
    public float coord(int overlap, int maxOverlap) {
        return (float) Math.pow(overlap, 4) / (float) Math.pow(maxOverlap, 4); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public float queryNorm(float sumOfSquaredWeights) {
        return 1; //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public float tf(float freq) {
//        System.out.println(Math.log(1));

        return (float) Math.log(1 + freq); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public float idf(long docFreq, long numDocs) {
        return 1; //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public float lengthNorm(FieldInvertState state) {
        return 1; //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public float decodeNormValue(long norm) {
        return 1;  // & 0xFF maps negative bytes to positive above 127
    }

    @Override
    public long encodeNormValue(float f) {
        return SmallFloat.floatToByte315(f);
    }

    @Override
    public float sloppyFreq(int distance) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public float scorePayload(int doc, int start, int end, BytesRef payload) {
        return 1;
    }

}
