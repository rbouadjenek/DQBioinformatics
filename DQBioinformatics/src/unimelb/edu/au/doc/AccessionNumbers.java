/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unimelb.edu.au.doc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.biojava.bio.seq.Feature;
import org.biojavax.Namespace;
import org.biojavax.RichObjectFactory;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;
import org.biojavax.ontology.SimpleComparableTerm;
import unimelb.edu.au.util.Functions;

/**
 * This class extracts the accession numbers cited in a set of PubMed articles.
 *
 * @author mbouadjenek
 */
public final class AccessionNumbers {

    /**
     * The total number of articles processed.
     */
    private int total = 0;
    /**
     * The total number of accession numbers extracted.
     */
    private int nbr_accession = 0;
    /**
     * A filter for PubMed articles.
     */
    private final BioinformaticFilter filter = new BioinformaticFilter(BioinformaticFilter.PUBMED_ARTICLES);
    /**
     * The pattern that contains the regular expression representing an
     * accession numbers. See: http://www.bioperl.org/wiki/Accession_number See:
     * http://www.ncbi.nlm.nih.gov/Sequin/acc.html
     */
    Pattern r = Pattern.compile("([A-Z]{2}[\\d]{6})|([ ][A-Z]{1}[\\d]{5}[ ])");

    /**
     * This method extracts the accession numbers from PubMed articles.
     *
     * @param dataDir The path of the directory that contains the PubMed
     * articles.
     * @throws java.lang.Exception
     */
    public void extract(String dataDir) throws Exception {
        File f = new File(dataDir);
        File[] listFiles = f.listFiles();
        for (File listFile : listFiles) {
            if (listFile.isDirectory()) {
                extract(listFile.toString());
            } else {
                if (!listFile.isHidden() && listFile.exists() && listFile.canRead() && filter.accept(listFile)) {
                    extractFile(listFile);
                }
            }
        }
    }

    private void extractFile(File f) throws Exception {
        total++;
        System.err.println(total + "- Treating " + f.getCanonicalPath());// print on stderr the file name currently processed.
        PubMedDoc article = new PubMedDoc(f);
        if (article.getBody() != null) {
            // Now create matcher object.
            Matcher m = r.matcher(article.getBody());
//        System.out.println(r.);
            while (m.find()) {
                nbr_accession++;
                System.out.println(article.getFile_Name() + "\tPMC" + article.getId_pmc() + "\t" + m.group().trim()); // print the file and an accession number detected.
            }
        }
    }

    /**
     * This method extracts the accession numbers of aa from GenBank records of
     * nucleotide sequences.
     *
     * @param dataDir The path of the directory that contains the PubMed
     * articles.
     * @throws java.lang.Exception
     */
    public void extractAA(String dataDir) throws Exception {
        File f = new File(dataDir);
        File[] listFiles = f.listFiles();
        for (File listFile : listFiles) {
            if (listFile.isDirectory()) {
                extract(listFile.toString());
            } else {
                if (!listFile.isHidden() && listFile.exists() && listFile.canRead() && filter.accept(listFile)) {
                    extractFileAA(listFile);
                }
            }
        }
    }

    private void extractFileAA(File file) throws Exception {
        total++;
        System.err.println(total + "- Treating " + file.getCanonicalPath());// print on stderr the file name currently processed.

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            Namespace ns = RichObjectFactory.getDefaultNamespace();
            RichSequenceIterator seqs = RichSequence.IOTools.readGenbankDNA(br, ns);
            while (seqs.hasNext()) {
                try {
                    RichSequence rs = seqs.nextRichSequence();
                    // write it in EMBL format to standard out
                    for (Feature feature : rs.getFeatureSet()) {
                        System.out.println("Type: " + feature.getType());

                        Map<SimpleComparableTerm, String> m = feature.getAnnotation().asMap();
//                System.out.println("m = " + m);
                        for (SimpleComparableTerm key : m.keySet()) {
//                    System.out.println(key.getName());
                            System.out.println("Key = " + key.toString().replace("biojavax:", "") + " -> value = " + m.get(key));
                            
                            nbr_accession++;
//                            System.out.println(article.getFile_Name() + "\tPMC" + article.getId_pmc() + "\t" + m.group().trim()); // print the file and an accession number detected.
                        }

                        System.out.println("------------");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            br.close();
        }
    }

    /**
     *
     * @return The total number of PubMed articles processed.
     */
    public int getTotal() {
        return total;
    }

    /**
     * @return The total number of accession numbers extracted.
     */
    public int getNbr_accession() {
        return nbr_accession;
    }

    /**
     * @param args the command line arguments
     * @throws java.lang.Exception
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        String src;
        if (args.length == 0) {
            src = "/Volumes/Macintosh HD/Users/mbouadjenek/Documents/bioinformatics_data/PubMed_Central/dataset/";
        } else {
            src = args[0];
        }
        long start = System.currentTimeMillis();
        AccessionNumbers detect = new AccessionNumbers();
        detect.extractAA(src);
        long end = System.currentTimeMillis();
        System.err.println("-------------------------------------------------------------------------");
        long millis = (end - start);
        System.err.println("Processing " + detect.getTotal() + " files took " + Functions.getTimer(millis) + ".");
        System.err.println("Detected " + detect.getNbr_accession() + " accession numbers.");
        System.err.println("-------------------------------------------------------------------------");
    }

}
