/*
 *                  BioJava development code
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
 * Created on Jun 24, 2008
 * 
 */

package org.biojavax.bio.seq.io;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.NoSuchElementException;

import org.biojava.bio.Annotation;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.SequenceIterator;
import org.biojava.bio.seq.Feature.Template;
import org.biojava.bio.seq.db.SequenceDB;
import org.biojava.bio.seq.io.ParseException;
import org.biojava.bio.seq.io.SeqIOTools;
import org.biojava.bio.symbol.Alphabet;
import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojava.bio.symbol.Symbol;
import org.biojava.bio.symbol.SymbolList;
import org.biojavax.Namespace;
import org.biojavax.RankedCrossRef;
import org.biojavax.RankedDocRef;
import org.biojavax.Source;
import org.biojavax.bio.BioEntry;
import org.biojavax.bio.BioEntryRelationship;
import org.biojavax.bio.seq.RichFeature;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;
import org.biojavax.bio.seq.SimpleRichSequence;
import org.biojavax.bio.taxa.NCBITaxon;


/** Iterates over a Fasta file that is kept in memory for optimized access.
 * @since 1.7
 * @author Andreas Prlic
 *
 */
public class HashedFastaIterator implements RichSequenceIterator{

	Alphabet alpha;
	Namespace ns;
	SequenceDB db ;
	FastaFormat format;
	SequenceIterator iterator;
	MyRichSeqIOListener listener;
	
	public HashedFastaIterator(BufferedInputStream is, Alphabet alpha,Namespace ns) throws BioException{

//		get a SequenceDB of all sequences in the file
		db = SeqIOTools.readFasta(is, alpha);
		iterator = db.sequenceIterator();
		this.ns = ns;
		format = new FastaFormat();
		listener = new MyRichSeqIOListener();
		this.alpha = alpha;
		
	}

	
        @Override
	public RichSequence nextRichSequence() throws NoSuchElementException, BioException {
		listener.startSequence();
		
		Sequence s = iterator.nextSequence();

		Annotation a = s.getAnnotation();

		if ( a.containsProperty("description_line")){
			//process the description line...
			try {
				format.processHeader(">"+a.getProperty("description_line"), listener, ns);
			} catch (NoSuchElementException | IOException | ParseException e){
				throw new BioException(e);
			}
		}
		listener.setSymbolList(s);

		listener.endSequence();
		
		return listener.getCurrentSequence();
	}

        @Override
	public boolean hasNext() {
		return iterator.hasNext();		
	}

        @Override
	public BioEntry nextBioEntry() throws NoSuchElementException, BioException {
		return this.nextRichSequence();
	}

        @Override
	public Sequence nextSequence() throws NoSuchElementException, BioException {

		return  iterator.nextSequence();
	}
}

/** a RichSeqIOListener plus more...
 * 
 * @author Andreas Prlic
 *
 */
class MyRichSeqIOListener implements RichSeqIOListener{

	SimpleRichSequence currentSequence;
	Namespace ns;
	String ac;
	String name;
	int version;
	Double sversion;
	SymbolList symbolList;
	
	public MyRichSeqIOListener(){
		currentSequence = null;
	}

	public SimpleRichSequence getCurrentSequence(){
		return currentSequence;
	}
	
        @Override
	public RichFeature getCurrentFeature() throws ParseException {
		// TODO Auto-generated method stub
		return null;
	}

	
	public SymbolList getSymbolList() {
		return symbolList;
	}

	public void setSymbolList(SymbolList symbolList) {
		this.symbolList = symbolList;
	}

        @Override
	public void setAccession(String accession) throws ParseException {
		ac = accession;

	}

        @Override
	public void setCircular(boolean circular) throws ParseException {
		// TODO Auto-generated method stub

	}

        @Override
	public void setComment(String comment) throws ParseException {
		// TODO Auto-generated method stub

	}

        @Override
	public void setDescription(String description) throws ParseException {
		// TODO Auto-generated method stub

	}

        @Override
	public void setDivision(String division) throws ParseException {
		// TODO Auto-generated method stub

	}

        @Override
	public void setIdentifier(String identifier) throws ParseException {
		// TODO Auto-generated method stub

	}

        @Override
	public void setNamespace(Namespace namespace) throws ParseException {
		ns = namespace;
	}

        @Override
	public void setRankedCrossRef(RankedCrossRef crossRef) throws ParseException {
		// TODO Auto-generated method stub

	}

        @Override
	public void setRankedDocRef(RankedDocRef ref) throws ParseException {
		// TODO Auto-generated method stub

	}

        @Override
	public void setRelationship(BioEntryRelationship relationship) throws ParseException {
		// TODO Auto-generated method stub

	}

        @Override
	public void setSeqVersion(String version) throws ParseException {
		try {
			sversion = Double.parseDouble(version);
		} catch (Exception e){
			throw new ParseException(e.getMessage());
		}

	}

        @Override
	public void setTaxon(NCBITaxon taxon) throws ParseException {
		// TODO Auto-generated method stub

	}

        @Override
	public void setURI(String uri) throws ParseException {
		// TODO Auto-generated method stub

	}

        @Override
	public void setVersion(int version) throws ParseException {
		this.version = version;

	}

        @Override
	public void addFeatureProperty(Object key, Object value) throws ParseException {
		// TODO Auto-generated method stub

	}

        @Override
	public void addSequenceProperty(Object key, Object value) throws ParseException {
		// TODO Auto-generated method stub

	}

        @Override
	public void addSymbols(Alphabet alpha, Symbol[] syms, int start, int length) throws IllegalAlphabetException {
		// TODO Auto-generated method stub

	}

        @Override
	public void endFeature() throws ParseException {
		// TODO Auto-generated method stub

	}

        @Override
	public void endSequence() throws ParseException {
		
		currentSequence = new SimpleRichSequence(
				ns, 
				name, 
				ac, 
				version, 
				symbolList, 
				sversion);
		

	}

        @Override
	public void setName(String name) throws ParseException {
		this.name = name;
	}

        @Override
	public void startFeature(Template templ) throws ParseException {
		// TODO Auto-generated method stub

	}

        @Override
	public void startSequence() throws ParseException {
		currentSequence = null;

	}

        //reda modif
    @Override
    public void setSource(Source source) throws ParseException {
        // TODO Auto-generated method stub
    }
}
