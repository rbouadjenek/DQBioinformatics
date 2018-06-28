/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unimelb.edu.au.simseq;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.search.spell.EditDistance;
import org.apache.lucene.search.spell.LevensteinDistance;
import org.biojavax.Namespace;
import org.biojavax.RichObjectFactory;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;
import unimelb.edu.au.doc.PubMedDoc;
import unimelb.edu.au.util.StringSimilarity;

/**
 *
 * @author mbouadjenek
 */
public class SimSeq {

    final File pairsFile;
    Set<String> pairs = new HashSet<>();
    Pattern accession_patern = Pattern.compile("([\\d]{4})");
    String dir_sequences = "/Users/mbouadjenek/Documents/bioinformatics_data/genbank/";
    int max_length = 20000;

    public SimSeq(String filePairs) {
        this.pairsFile = new File(filePairs);
    }

    public void run(int t) throws FileNotFoundException {
        LevensteinDistance ld = new LevensteinDistance();
        try {
            FileInputStream fstream = new FileInputStream(pairsFile);
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
                    StringTokenizer st = new StringTokenizer(str);
                    String accession = st.nextToken();
                    String pmc = st.nextToken();
                    pairs.add(accession + "\t" + pmc);
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
        int i = 0;
        for (String pair1 : pairs) {

            StringTokenizer st1 = new StringTokenizer(pair1);
            String accession1 = st1.nextToken();
            String pmc1 = st1.nextToken();
            PubMedDoc doc1 = new PubMedDoc(PubMedDoc.getAbsoluteFile(pmc1, "/Users/mbouadjenek/Documents/bioinformatics_data/PubMed_Central/dataset/"));
            if (doc1.getTitle() == null || doc1.getAbstract() == null || doc1.getBody() == null) {
                continue;
            }
            String seq1 = getSeq(accession1);
            if (seq1.length() > max_length) {
                continue;
            }
            for (String pair2 : pairs) {
                StringTokenizer st2 = new StringTokenizer(pair2);
                String accession2 = st2.nextToken();
                String pmc2 = st2.nextToken();
                if (accession1.equals(accession2)) {
                    continue;
                }
                PubMedDoc doc2 = new PubMedDoc(PubMedDoc.getAbsoluteFile(pmc2, "/Users/mbouadjenek/Documents/bioinformatics_data/PubMed_Central/dataset/"));
                String seq2 = getSeq(accession2);
                if (seq2.length() > max_length) {
                    continue;
                }
                if (doc2.getTitle() == null || doc2.getAbstract() == null || doc2.getBody() == null) {
                    continue;
                }
                i++;
                if (i < t + 1) {
                    continue;
                }
                System.out.println(EditDistance.minDistance(seq1, seq2)
                        + "\t" + StringSimilarity.getCosineSimilarity(doc1.getTitle(), doc2.getTitle(), new EnglishAnalyzer())
                        + "\t" + StringSimilarity.getCosineSimilarity(doc1.getAbstract(), doc2.getAbstract(), new EnglishAnalyzer())
                        + "\t" + StringSimilarity.getCosineSimilarity(doc1.getBody(), doc2.getBody(), new EnglishAnalyzer()));

            }
        }
    }

    String getSeq(String accession) throws FileNotFoundException {
        Matcher m = accession_patern.matcher(accession);
        String id = "";
        while (m.find()) {
            id = m.group();
        }
        File file_accession = new File(dir_sequences + id.substring(0, 2) + "/" + id.substring(2, 4) + "/" + accession + ".gb");
        Namespace ns = RichObjectFactory.getDefaultNamespace();
        RichSequenceIterator seqs = RichSequence.IOTools.readGenbankDNA(new BufferedReader(new FileReader(file_accession)), ns);
        String seq = "";
        while (seqs.hasNext()) {
            RichSequence rs;
            try {
                rs = seqs.nextRichSequence();
                seq = rs.seqString();

            } catch (Exception ex) {
                System.err.println("file: " + file_accession.getAbsoluteFile());
                ex.printStackTrace();
            }
        }
        return seq;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException {
        // TODO code application logic here
        SimSeq ss = new SimSeq("/Users/mbouadjenek/Documents/bioinformatics_data/simseq/pairs.txt");
        ss.run(Integer.parseInt(args[0]));
    }

}
