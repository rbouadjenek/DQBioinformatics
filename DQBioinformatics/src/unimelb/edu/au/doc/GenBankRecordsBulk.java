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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.biojavax.Namespace;
import org.biojavax.RichObjectFactory;
import org.biojavax.bio.db.RichSequenceDB;
import org.biojavax.bio.db.ncbi.GenbankRichSequenceDB;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;
import unimelb.edu.au.util.Functions;

/**
 * This class implements methods that retrieve and download a set of GenBank
 * records. The records are stored in the GenBank format (file.gb) and in a tree
 * format based on the accession numbers. For example: The GenBank record with
 * accession number AB231298 will be located in ./23/12/AB231298.gb The GenBank
 * record with accession number A00004 will be located in ./00/00/A00004.gb
 *
 * @author mbouadjenek
 */
public class GenBankRecordsBulk {

    /**
     * The list of accession numbers to retrieve.
     */
    private final File src_filename;

    /**
     * The folder where to store the GenBank files.
     */
    private final File dst_folder;

    /**
     * The total number of GenBank records downloaded.
     */
    private int nbr_retrieveddoc = 0;

    /**
     * The total number of accession numbers processed.
     */
    private int total = 0;

    /**
     * The pattern that contains the regular expression to build a folder from
     * an accession number.
     */
    Pattern r = Pattern.compile("([\\d]{4})");

    /**
     * Size of the bulk to download.
     */
    private final int Bulk_size = 850;

    /**
     * A constructor that creates an instance of the class that retrieves the
     * GenBenk records.
     *
     * @param src_filename String The full path of the file that contains the
     * accession numbers.
     * @param dst_folder
     */
    public GenBankRecordsBulk(String src_filename, String dst_folder) {
        this.src_filename = new File(src_filename);
        this.dst_folder = new File(dst_folder);
    }

    /**
     * Retrieve the GenBank files based on accession numbers.
     */
    public void retrieve() {
        try {
            FileInputStream fstream = new FileInputStream(this.src_filename);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            Set<String> l = new HashSet<>();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
                String str;
                while ((str = br.readLine()) != null) {
                    if (str.startsWith("#")) {
                        continue;
                    }
                    if (str.trim().length() == 0) {
                        continue;
                    }
                    total++;
                    if (total % Bulk_size != 0) {
                        l.add(str);
                    } else {
                        l.add(str);
                        download(l);
                        l.clear();
                    }
                }
                if (!l.isEmpty()) {
                    download(l);
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param accession_number The accession number of the record to download.
     */
    private void download(Set<String> list) {
        long start = System.currentTimeMillis();
        try {
            RichSequenceDB rsDB;
            RichSequence rs;
            GenbankRichSequenceDB grsdb = new GenbankRichSequenceDB();
            rsDB = grsdb.getRichSequences2(list);
            Namespace ns = RichObjectFactory.getDefaultNamespace();
            RichSequenceIterator it = rsDB.getRichSequenceIterator();
            while (it.hasNext()) {
                rs = it.nextRichSequence();
                String accession_number = rs.getAccession();
                //****************************************
                //*****This will create tree folder*******
                //****************************************
                Matcher m = r.matcher(accession_number);
                String id = "";
                while (m.find()) {
                    id = m.group();
                }
                File dir = new File(dst_folder.getAbsolutePath() + "/" + id.substring(0, 2) + "/");
                dir.mkdir();
                dir = new File(dir.getAbsolutePath() + "/" + id.substring(2, 4) + "/");
                dir.mkdir();
                File f = new File(dir + "/" + accession_number + ".gb");
                //****************************************
                try {
//                    RichSequence.IOTools.writeGenbank(System.out, rs, ns);
                    grsdb.writeFastRichSequence(accession_number, new PrintWriter(f));
                    nbr_retrieveddoc++;
//                    System.out.println(nbr_retrieveddoc + "- " + accession_number);// print in stdout an acession number of a record successfully downloaded.
                    list.remove(accession_number);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        long end = System.currentTimeMillis();
        long millis = (end - start);
        System.out.println(total + "- It tooks " + Functions.getTimer(millis) + " to download " + Bulk_size + " records from GenBank.");
        list.stream().forEach((accession_number) -> {
            System.err.println("Error: " + accession_number);// print in stderr an acession number of a record not downloaded.
        });

    }

    /**
     * @return The total number of record successfully downloaded.
     */
    public int getNbr_retrieveddoc() {
        return nbr_retrieveddoc;
    }

    /**
     * @return The total number of accession numbers processed.
     */
    public int getTotal() {
        return total;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        String src;
        String dst;
        if (args.length == 0) {
            src = "/Users/mbouadjenek/Documents/bioinformatics_data/errors.txt";
            dst = "/Users/mbouadjenek/Documents/bioinformatics_data/genbank/";
        } else {
            src = args[0];
            dst = args[1];
        }
        long start = System.currentTimeMillis();
        GenBankRecordsBulk r = new GenBankRecordsBulk(src, dst);
        r.retrieve();
        long end = System.currentTimeMillis();
        System.err.println("-------------------------------------------------------------------------");
        long millis = (end - start);
        System.err.println("Downloading " + r.getNbr_retrieveddoc() + " GenBank files took " + Functions.getTimer(millis) + ".");
        System.err.println("-------------------------------------------------------------------------");
    }
}
