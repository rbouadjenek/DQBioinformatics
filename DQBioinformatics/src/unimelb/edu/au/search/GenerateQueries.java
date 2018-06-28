/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unimelb.edu.au.search;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.Feature;
import org.biojavax.Namespace;
import org.biojavax.RichObjectFactory;
import org.biojavax.bio.db.ncbi.GenbankRichSequenceDB;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;
import org.biojavax.bio.seq.io.GenbankFormat;
import org.biojavax.bio.taxa.NCBITaxon;
import org.biojavax.ontology.SimpleComparableTerm;
import unimelb.edu.au.doc.PubMedDoc;
import unimelb.edu.au.doc.MutualReferences;
import unimelb.edu.au.indexing.TermFreqVector;
import unimelb.edu.au.search.similarities.MySimilarity;
import unimelb.edu.au.util.Functions;

/**
 *
 * @author mbouadjenek
 */
public class GenerateQueries {

    private final String feature_type = "source";
    private final String annotation = "product";
    protected Map<String, Set<String>> queries = new HashMap<>();
    protected File references;
    protected File dir_sequences;
    Pattern accession_patern = Pattern.compile("([\\d]{4})");
    EnglishAnalyzer analyzer = new EnglishAnalyzer(null);

    public GenerateQueries(String references, String dir_sequences) throws IOException {
        this.references = new File(references);
        this.dir_sequences = new File(dir_sequences);
    }

    void generate(int k) throws FileNotFoundException, ParseException, IOException {
        int i = 0;
        FileInputStream fstream;
        fstream = new FileInputStream(references);
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

                i++;
//                if (i < 277) {
//                    continue;
//                }
                System.err.println(i + "- " + str);

                StringTokenizer st = new StringTokenizer(str);
                String article = st.nextToken();
                String pmc = st.nextToken();
                String accession = st.nextToken();
                //****************************************
                //***** Build path to the Record *********
                //****************************************
                Matcher m = accession_patern.matcher(accession);
                String id = "";
                while (m.find()) {
                    id = m.group();
                }
                File file_accession = new File(dir_sequences.getAbsolutePath() + "/" + id.substring(0, 2) + "/" + id.substring(2, 4) + "/" + accession + ".gb");
                //*****************************************
                Namespace ns = RichObjectFactory.getDefaultNamespace();
                RichSequenceIterator seqs = RichSequence.IOTools.readGenbankDNA(new BufferedReader(new FileReader(file_accession)), ns);

                while (seqs.hasNext()) {
                    RichSequence rs;
                    try {
                        rs = seqs.nextRichSequence();

                        //****************************************
                        //***** Extract annotation queries *********
                        //****************************************
                        String listOrg = "";
                        for (Feature f : rs.getFeatureSet()) {
                            if (f.getType().equals(GenbankFormat.SOURCE_TAG.toLowerCase())) {
                                String organism = "";
                                String tax_id = "";
                                Map<SimpleComparableTerm, String> annotations = f.getAnnotation().asMap();
                                for (SimpleComparableTerm key : annotations.keySet()) {
                                    if (key.toString().equals("biojavax:db_xref")) {
                                        Pattern dbxp = Pattern.compile("^([^:]+):(\\S+)$");
                                        Matcher match = dbxp.matcher(annotations.get(key));
                                        if (match.matches()) {
                                            String dbname = match.group(1);
                                            String raccession = match.group(2);
                                            if (dbname.equalsIgnoreCase("taxon")) {
                                                tax_id = raccession;
                                                if (!listOrg.contains(raccession)) {
                                                    listOrg += "\t" + raccession;                                                    
                                                }
                                            }
                                        }
                                    } else if (key.toString().equals("biojavax:organism")) {
                                        organism = annotations.get(key);
                                    }
                                }
//                                Directory dir = FSDirectory.open(new File("/Volumes/Macintosh HD/Users/mbouadjenek/Documents/bioinformatics_data/index_Organism_Names_v3.0/").toPath());
//                                IndexSearcher is = new IndexSearcher(DirectoryReader.open(dir));
//                                is.setSimilarity(new MySimilarity());
//                                PhraseQuery.Builder builder = new PhraseQuery.Builder();
//                                builder.add(new Term("name_class", NCBITaxon.SCIENTIFIC_NAME));
//                                PhraseQuery pq = builder.build();
//                                BooleanQuery.Builder bqBuilder = new BooleanQuery.Builder();
//                                bqBuilder.add(new TermQuery(new Term("tax_id", tax_id)), BooleanClause.Occur.MUST);
//                                bqBuilder.add(pq, BooleanClause.Occur.MUST);
//                                TopDocs hits = is.search(bqBuilder.build(), 1000);
//                                if (hits.totalHits == 0) {
//                                    System.out.println("Error: " + accession + "\t" + tax_id);
//                                } else {
//                                    Document doc = is.doc(hits.scoreDocs[0].doc);
//                                    if (!doc.get("name_txt").equalsIgnoreCase(organism)) {
//                                        System.out.println("Error: " + accession + "\t" + tax_id);
//                                    }
//                                }

                            }
                        }
                        if (listOrg.equals("")) {
                            System.err.println("Error organism:\t" + accession);
                        }
                        System.out.println("Query-" + i + "\t" + article + "\t" + pmc + "\t" + accession + "\tdefinition\t" + generateDefinitionQuery(rs) + listOrg);
//                        
//                        if (rs.getSource() != null) {
//                           
//                            Directory dir = FSDirectory.open(new File("/Volumes/Macintosh HD/Users/mbouadjenek/Documents/bioinformatics_data/index_Organism_Names_v3.0/").toPath());
//                            IndexSearcher is = new IndexSearcher(DirectoryReader.open(dir));
//                            is.setSimilarity(new MySimilarity());
//                            PhraseQuery.Builder builder = new PhraseQuery.Builder();
//                            builder.add(new Term("name_txt", rs.getSource().getOrganismName().toLowerCase()));
//                            PhraseQuery pq = builder.build();
//                            TopDocs hits = is.search(pq, 1000);
//                            if (hits.totalHits == 0) {
//                                System.err.println("Error: " + accession + "\t" + rs.getSource().getOrganismName());
//                            } else {
//
//                                Document doc = is.doc(hits.scoreDocs[0].doc);
//                                System.out.println(article + "\t" + pmc + "\t" + accession + "\tdefinition\t" + generateDefinitionQuery(rs) + "\t" + doc.get("tax_id"));
//                            }
//                        } else {
//                            System.out.println(accession + "\tUnknown Organism");
//                        }
                    } catch (NoSuchElementException | BioException ex) {
                        System.err.println("file: " + file_accession.getAbsoluteFile());
                        ex.printStackTrace();
                    }
                }
                if (i >= k) {
                    break;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(MutualReferences.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public String generateDefinitionQuery(RichSequence rs) {
        String query = rs.getDescription().replace("\n", " ");
        return query;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, FileNotFoundException, ParseException {
        // TODO code application logic here
        String references;
        String dir_sequences;
        int k;
        if (args.length == 0) {
            references = "/Users/mbouadjenek/Documents/bioinformatics_data/articles_accession_numbers_v4.0.txt";
            dir_sequences = "/Users/mbouadjenek/Documents/bioinformatics_data/genbank/";
            k = 10;
        } else {
            references = args[0];
            dir_sequences = args[1];
            k = Integer.parseInt(args[2]);
        }
        long start = System.currentTimeMillis();
        GenerateQueries g = new GenerateQueries(references, dir_sequences);
        g.generate(k);
        long end = System.currentTimeMillis();
        long millis = (end - start);
        System.err.println("-------------------------------------------------------------------------");
        System.err.println("The process tooks " + Functions.getTimer(millis) + ".");
        System.err.println("-------------------------------------------------------------------------");
    }

}
