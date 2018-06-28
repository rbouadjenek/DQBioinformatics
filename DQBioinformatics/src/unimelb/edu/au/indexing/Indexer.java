/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unimelb.edu.au.indexing;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.codecs.simpletext.SimpleTextCodec;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.store.FSDirectory;
import org.biojava.bio.BioException;
import org.biojavax.Namespace;
import org.biojavax.RichObjectFactory;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;
import org.biojavax.bio.seq.io.GenbankFormat;
import unimelb.edu.au.doc.PubMedDoc;
import unimelb.edu.au.doc.BioinformaticFilter;
import unimelb.edu.au.lucene.analysis.en.MyEnglishAnalyzer;
import unimelb.edu.au.lucene.analysis.en.PubMedSpecialWords;
import unimelb.edu.au.util.Functions;

/**
 * This class is designed to index a collection of PubMed articles.
 *
 * @author mbouadjenek
 */
public class Indexer {

    /**
     * A filter for PubMed articles.
     */
    private final IndexWriter writer;
    /**
     * An analyzer for text.
     */
    private final Analyzer analyzer;
    /**
     * A filter for PubMed articles.
     */
    private final BioinformaticFilter filter_pubmed = new BioinformaticFilter(BioinformaticFilter.PUBMED_ARTICLES);

    /**
     * A filter for PubMed articles.
     */
    private final BioinformaticFilter filter_genbank = new BioinformaticFilter(BioinformaticFilter.GENBANK_RECORDS);

    /**
     * Build an indexer object to index the PubMed collection.
     *
     * @param indexDir The path to the folder where the PubMed articles are
     * located.
     * @throws java.io.IOException
     */
    public Indexer(String indexDir) throws IOException {
        File indexDirFile = new File(indexDir);
        analyzer = new MyEnglishAnalyzer();
//                analyzer = new EnglishAnalyzer(PubMedSpecialWords.ENGLISH_STOP_WORDS_SET);
        IndexWriterConfig conf = new IndexWriterConfig(analyzer);
        conf.setUseCompoundFile(false);
//        conf.setCodec(new SimpleTextCodec()); // set a simple text codec.
        writer = new IndexWriter(FSDirectory.open(indexDirFile.toPath()), conf);
    }

    public int indexString(String file) throws Exception {
        FileInputStream fstream;
        fstream = new FileInputStream(new File(file));
        // Get the object of DataInputStream
        DataInputStream in = new DataInputStream(fstream);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            String str;
            while ((str = br.readLine()) != null) {
                if (str.startsWith("#")) {
                    continue;
                }
                if (str.trim().length() == 0) {
                    continue;
                }
                StringTokenizer st = new StringTokenizer(str, "\t");
                String queryid = st.nextToken();
                String article = st.nextToken();
                String pmc = st.nextToken();
                String feature_type = st.nextToken();
                String annotation = st.nextToken();
                String query_text = st.nextToken();
                System.out.println(writer.numDocs() + 1 + "- Indexing " + query_text);
                Document doc = new Document();
                doc.add(new TextField("body", query_text, Field.Store.NO));// Index Filename
                writer.addDocument(doc);
            }
        }
        return writer.numDocs();
    }

    public int indexOrganismNames(String file) throws Exception {
        FileInputStream fstream;
        fstream = new FileInputStream(new File(file));
        // Get the object of DataInputStream
        DataInputStream in = new DataInputStream(fstream);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            String str;
            while ((str = br.readLine()) != null) {
                if (str.startsWith("#")) {
                    continue;
                }
                if (str.trim().length() == 0) {
                    continue;
                }
                StringTokenizer st = new StringTokenizer(str, "\t");
                String tax_id = st.nextToken();
                String name_txt = st.nextToken().toLowerCase();
                String name_class = st.nextToken().toLowerCase();
                System.out.println(writer.numDocs() + 1 + "- Indexing \"" + str + "\"");
                Document doc = new Document();
                doc.add(new StringField("tax_id", tax_id, Field.Store.YES));// tax_id 
                doc.add(new StringField("name_txt", name_txt, Field.Store.YES));// Index name_txt
                doc.add(new StringField("name_class", name_class, Field.Store.YES));// Index name_class
                writer.addDocument(doc);
            }
        }
        return writer.numDocs();
    }

    public int indexPubMedCentral(String dataDir) throws Exception {
        File f = new File(dataDir);
        File[] listFiles = f.listFiles();
        for (File listFile : listFiles) {
            if (listFile.isDirectory()) {
                indexPubMedCentral(listFile.toString());
            } else {
                if (!listFile.isHidden() && listFile.exists() && listFile.canRead() && filter_pubmed.accept(listFile)) {
                    indexPubMedArticle(listFile);
                }
            }
        }
        return writer.numDocs();
    }

    private void indexPubMedArticle(File f) throws Exception {
        System.out.println(writer.numDocs() + 1 + "- Indexing " + f.getCanonicalPath());
        PubMedDoc article = new PubMedDoc(f);
        writer.addDocument(this.getDocument(article));
    }

    private Document getDocument(PubMedDoc article) throws IOException {
        Document doc = new Document();
        doc.add(new StringField(PubMedDoc.FILE_NAME, article.getFile_Name(), Field.Store.YES));// Index Filename
        if (article.getId_doi() != null) {
            doc.add(new StringField(PubMedDoc.ID_DOI, article.getId_doi(), Field.Store.YES));// Index id doi
        }
        if (article.getId_pmc() != null) {
            doc.add(new StringField(PubMedDoc.ID_PMC, "pmc".concat(article.getId_pmc()), Field.Store.YES));// Index id pmc
        }
        if (article.getId_pmid() != null) {
            doc.add(new StringField(PubMedDoc.ID_PMID, article.getId_pmid(), Field.Store.YES));// Index id pm
        }
        if (article.getId_publisher() != null) {
            doc.add(new StringField(PubMedDoc.ID_PUBLISHER, article.getId_publisher(), Field.Store.YES));// Index id publisher
        }
        if (article.getType() != null) {
            doc.add(new StringField(PubMedDoc.TYPE, article.getType(), Field.Store.YES));// Index id publisher
        }

        if (article.getTitle() != null) {
            doc.add(new VecTextField(PubMedDoc.TITLE, article.getTitle(), Store.YES));// Index Title
        }
        if (article.getAbstract() != null) {
            doc.add(new VecTextField(PubMedDoc.ABSTRACT, article.getAbstract(), Store.NO));// Index Abstract
        }
        if (article.getBody() != null) {
            doc.add(new VecTextField(PubMedDoc.BODY, article.getBody(), Store.NO));// Index Body
        }
        if (article.getSubject() != null) {
            doc.add(new StringField(PubMedDoc.SUBJECT, article.getSubject(), Field.Store.YES));// Index Body
        }
        return doc;
    }

    public int indexGenBankRecords(String dataDir) throws Exception {
        File f = new File(dataDir);
        File[] listFiles = f.listFiles();
        for (File listFile : listFiles) {
            if (listFile.isDirectory()) {
                indexGenBankRecords(listFile.toString());
            } else {
                if (!listFile.isHidden() && listFile.exists() && listFile.canRead() && filter_genbank.accept(listFile)) {
                    indexGenBankFile(listFile);
                }
            }
        }
        return writer.numDocs();
    }

    private void indexGenBankFile(File f) throws Exception {
        System.out.println(writer.numDocs() + 1 + "- Indexing " + f.getCanonicalPath());
        Analyzer a = new Analyzer() {
            @Override
            protected Analyzer.TokenStreamComponents createComponents(String fieldName) {
                final Tokenizer source = new StandardTokenizer();
                TokenStream result = new StandardFilter(source);
                return new Analyzer.TokenStreamComponents(source, result);
            }
        };
        Namespace ns = RichObjectFactory.getDefaultNamespace();
        RichSequenceIterator seqs = RichSequence.IOTools.readGenbankDNA(new BufferedReader(new FileReader(f)), ns);
        while (seqs.hasNext()) {
            RichSequence rs;
            rs = seqs.nextRichSequence();
            String query_text_tokenized = "";
            TokenStream stream = a.tokenStream(null, new StringReader(QueryParser.escape(rs.getDescription().replace("\n", " "))));
            CharTermAttribute cattr = stream.addAttribute(CharTermAttribute.class);
            stream.reset();
            while (stream.incrementToken()) {
                query_text_tokenized += cattr.toString() + " ";
            }
            stream.end();
            stream.close();
            query_text_tokenized = query_text_tokenized.trim();
            for (String com_qualifier : PubMedSpecialWords.KEYWORDS_COMPLETENESS_QUALIFIERS) {
                query_text_tokenized = query_text_tokenized.replace(com_qualifier, ""); // This remove the completeness qualifiers
            }
            Document doc = new Document();
            doc.add(new StringField(GenbankFormat.ACCESSION_TAG, rs.getAccession(), Field.Store.YES));// Index the accession number
            doc.add(new TextField(GenbankFormat.DEFINITION_TAG, query_text_tokenized, Field.Store.NO));// Index the description            
            writer.addDocument(doc);
        }
    }

    private void close() throws IOException {
        writer.close();
    }

    /**
     * @return The number of articles indexed.
     */
    public int getNumberofDocs() {
        return writer.numDocs();
    }

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException, Exception {
        // TODO code application logic here
        String dataDir;
        String indexDir;
        if (args.length == 0) {
//            dataDir = "/Volumes/Macintosh HD/Users/mbouadjenek/Documents/bioinformatics_data/PubMed_Central/dataset/02/71/10/";
//            indexDir = "/Volumes/Macintosh HD/Users/mbouadjenek/Documents/bioinformatics_data/index_test/";
//            dataDir = "/Volumes/Macintosh HD/Users/mbouadjenek/Documents/bioinformatics_data/queries_definition.txt";
//            indexDir = "/Volumes/Macintosh HD/Users/mbouadjenek/Documents/bioinformatics_data/index_Definitions_v2.0";
//            dataDir = "/Volumes/Macintosh HD/Users/mbouadjenek/Documents/bioinformatics_data/genbank/00/00/";
//            indexDir = "/Volumes/Macintosh HD/Users/mbouadjenek/Documents/bioinformatics_data/index_GenBankRecords_v1.0/";
            dataDir = "/Volumes/Macintosh HD/Users/mbouadjenek/Documents/bioinformatics_data/organism_names.txt";
            indexDir = "/Volumes/Macintosh HD/Users/mbouadjenek/Documents/bioinformatics_data/index_Organism_Names_v6.0/";
        } else {
            dataDir = args[0];
            indexDir = args[1];
        }
        long start = System.currentTimeMillis();
        Indexer indexer = new Indexer(indexDir);
        int numIndexed;
        try {
//            numIndexed = indexer.indexString(dataDir);
//            numIndexed = indexer.indexPubMedCentral(dataDir);
//            numIndexed = indexer.indexGenBankRecords(dataDir);
            numIndexed = indexer.indexOrganismNames(dataDir);
            long end = System.currentTimeMillis();
            System.out.println("-------------------------------------------------------------------------");
            long millis = (end - start);
            System.out.println("Indexing " + numIndexed + " files took " + Functions.getTimer(millis) + ".");
            System.out.println("Indexed " + indexer.getNumberofDocs() + " files.");
            System.out.println("-------------------------------------------------------------------------");
        } finally {
            indexer.close();
        }
    }

}
