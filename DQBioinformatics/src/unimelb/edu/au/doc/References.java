/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unimelb.edu.au.doc;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.biojava.bio.BioException;
import org.biojavax.Namespace;
import org.biojavax.RichObjectFactory;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;
import unimelb.edu.au.util.Functions;

/**
 *
 * @author mbouadjenek
 */
public final class References {

    protected File references;
    protected File dir_sequences;
    protected File dir_articles;
    Pattern accession_patern = Pattern.compile("([\\d]{4})");
    Pattern article_patern = Pattern.compile("([\\d]{8})");

    public References(String references, String dir_sequences, String dir_articles) throws FileNotFoundException, IOException, NoSuchElementException, BioException {
        this.references = new File(references);
        this.dir_sequences = new File(dir_sequences);
        this.dir_articles = new File(dir_articles);

    }

    void check() throws FileNotFoundException {
//        int i = 0;
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
//                i++;
//                if (i <= 134961) {
//                    continue;
//                }
                
                StringTokenizer st = new StringTokenizer(str);
                String article = st.nextToken();
                String pmc = st.nextToken();
                String accession = st.nextToken();
                //****************************************
                //****** Build path to the Article *******
                //****************************************
                Matcher m = article_patern.matcher(article);
                String id = "";
                while (m.find()) {
                    id = m.group();
                }
                File file_article = new File(dir_articles.getAbsolutePath() + "/"
                        + id.substring(0, 2) + "/"
                        + id.substring(2, 4) + "/"
                        + id.substring(4, 6) + "/" + article);
                PubMedDoc pubmeddoc = new PubMedDoc(file_article);
                //****************************************
                //***** Build path to the Record *********
                //****************************************
                m = accession_patern.matcher(accession);
                id = "";
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
                        // write it in EMBL format to standard out
                        System.out.println(article + "\t" + pmc + "\t" + accession + "\t" + rs.getRankedDocRefs().size());
//                        double sim = pubmeddoc.getPosSequenceReferences(rs);
//                        if (sim >= 0.75) {
//                            
//                        }
                    } catch (Exception ex) {
                        System.err.println("file: " + file_accession.getAbsoluteFile());
                        ex.printStackTrace();
                    }
                }
//                System.out.println("*********************");

            }
        } catch (IOException ex) {
            Logger.getLogger(References.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            String references;
            String dir_sequences;
            String dir_articles;
            if (args.length == 0) {
                references = "/Users/mbouadjenek/Documents/bioinformatics_data/articles_accession_numbers_v4.0.txt";
                dir_sequences = "/Users/mbouadjenek/Documents/bioinformatics_data/genbank/";
                dir_articles = "/Users/mbouadjenek/Documents/bioinformatics_data/PubMed_Central/dataset/";
            } else {
                references = args[0];
                dir_sequences = args[1];
                dir_articles = args[2];
            }
            long start = System.currentTimeMillis();
            References r = new References(references, dir_sequences, dir_articles);
            r.check();
            long end = System.currentTimeMillis();
            System.err.println("-------------------------------------------------------------------------");
            long millis = (end - start);
            System.err.println("The process took " + Functions.getTimer(millis) + ".");
            System.err.println("-------------------------------------------------------------------------");
        } catch (IOException | NoSuchElementException | BioException ex) {
            Logger.getLogger(References.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
