/*
 * RichSeqIOAdapter.java
 *
 * Created on October 6, 2005, 4:53 PM
 */

/*
 *                    BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 *
 */
package org.biojavax.bio.seq.io;

import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.io.ParseException;
import org.biojava.bio.symbol.Alphabet;
import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojava.bio.symbol.Symbol;
import org.biojavax.Namespace;
import org.biojavax.RankedCrossRef;
import org.biojavax.RankedDocRef;
import org.biojavax.Source;
import org.biojavax.bio.BioEntryRelationship;
import org.biojavax.bio.seq.RichFeature;
import org.biojavax.bio.taxa.NCBITaxon;

/**
 * This class implements all methods of RichSeqIOListener and takes no action.
 * It should be overridden to implement custom listeners that only listen for a
 * small subset of events.
 *
 * @author Mark Schreiber
 * @since 1.5
 */
public class RichSeqIOAdapter implements RichSeqIOListener {

    /**
     * This is a dummy feature. It is returned by the method
     * {@link #getCurrentFeature() getCurrentFeature()}. Access is provided so
     * you can override it.
     */
    protected RichFeature emptyFeature;

    /**
     * Creates a new instance of RichSeqIOAdapter
     */
    public RichSeqIOAdapter() {
        emptyFeature = RichFeature.Tools.makeEmptyFeature();
    }

    @Override
    public void setAccession(String accession) throws ParseException {
    }

    @Override
    public void setIdentifier(String identifier) throws ParseException {
    }

    @Override
    public void setDivision(String division) throws ParseException {
    }

    @Override
    public void setDescription(String description) throws ParseException {
    }

    @Override
    public void setVersion(int version) throws ParseException {
    }

    @Override
    public void setSeqVersion(String version) throws ParseException {
    }

    @Override
    public void setComment(String comment) throws ParseException {
    }

    @Override
    public void setRankedDocRef(RankedDocRef ref) throws ParseException {
    }

    @Override
    public void setTaxon(NCBITaxon taxon) throws ParseException {
    }

    @Override
    public void setNamespace(Namespace namespace) throws ParseException {
    }

    @Override
    public void setRelationship(BioEntryRelationship relationship) throws ParseException {
    }

    @Override
    public void setRankedCrossRef(RankedCrossRef crossRef) throws ParseException {
    }

    @Override
    public void setURI(String uri) throws ParseException {
    }

    @Override
    public RichFeature getCurrentFeature() throws ParseException {
        return this.emptyFeature;
    }

    @Override
    public void setCircular(boolean circular) throws ParseException {
    }

    @Override
    public void addFeatureProperty(Object key, Object value) throws ParseException {
    }

    @Override
    public void endFeature() throws ParseException {
    }

    @Override
    public void startFeature(Feature.Template templ) throws ParseException {
        this.emptyFeature = RichFeature.Tools.makeEmptyFeature();
    }

    @Override
    public void addSequenceProperty(Object key, Object value) throws ParseException {
    }

    @Override
    public void addSymbols(Alphabet alpha, Symbol[] syms, int start, int length)
            throws IllegalAlphabetException {
    }

    @Override
    public void setName(String name) throws ParseException {
    }

    @Override
    public void endSequence() throws ParseException {
    }

    @Override
    public void startSequence() throws ParseException {
    }

    //reda modifs
    @Override
    public void setSource(Source source) throws ParseException {
    }
}
