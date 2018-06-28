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
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.biojava.bio.BioException;
import org.biojavax.Namespace;
import org.biojavax.RichObjectFactory;
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
public class GenBankRecords {

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
     * A filter for GenBank records.
     */
    private final BioinformaticFilter filter = new BioinformaticFilter(BioinformaticFilter.GENBANK_RECORDS);

    /**
     * A constructor that creates an instance of the class that retrieves the
     * GenBank records.
     *
     * @param src_filename String The full path of the file that contains the
     * accession numbers.
     * @param dst_folder
     */
    public GenBankRecords(String src_filename, String dst_folder) {
        this.src_filename = new File(src_filename);
        this.dst_folder = new File(dst_folder);
    }

    /**
     * Retrieve the GenBank files based on accession numbers.
     */
    public void retrieve() {
        try {
            int i = 0;
            FileInputStream fstream = new FileInputStream(this.src_filename);
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
                    download(str);
//                    if (i == 200) {
//                        break;
//                    }

                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param accession_number The accession number of the record to download.
     */
    private void download(String accession_number) {
        GenbankRichSequenceDB grsdb = new GenbankRichSequenceDB();
        try {
            total++;
            long start = System.currentTimeMillis();
            //****************************************
            //*****This will create tree folder*******
            //****************************************
//            Matcher m = r.matcher(accession_number);
//            String id = "";
//            while (m.find()) {
//                id = m.group();
//            }
//            File dir = new File(dst_folder.getAbsolutePath() + "/" + id.substring(0, 2) + "/");
//            dir.mkdir();
//            dir = new File(dir.getAbsolutePath() + "/" + id.substring(2, 4) + "/");
//            dir.mkdir();
//            File f = new File(dir + "/" + accession_number + ".gb");
            //****************************************            
            grsdb.writeFastRichSequence(accession_number, new PrintWriter(new File("/dev/null")));
            long end = System.currentTimeMillis();
            long millis = (end - start);
            System.out.println(total + "- Downloading the record " + accession_number + " from GenBank took " + Functions.getTimer(millis) + ".");
//            System.out.println(accession_number);// print in stdout an acession number of a record successfully downloaded.
            nbr_retrieveddoc++;
        } catch (Exception e) {
//            e.printStackTrace();
            e.printStackTrace();
            System.err.println("Error: " + accession_number);// print in stderr an acession number of a record not downloaded.
        }
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
     * .
     * @param src_records
     */
    public void copy(String src_records) {
        try {
            Set<String> accession_numbers = new HashSet<>();
            FileInputStream fstream = new FileInputStream(this.src_filename);
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
                    accession_numbers.add(str);
                }
                br.close();
            }
            int i = 0, j = 0;
            File f = new File(src_records);
            File[] listFiles = f.listFiles();
            for (File file : listFiles) {
                if (!file.isHidden() && file.exists() && file.canRead()) {
                    Namespace ns = RichObjectFactory.getDefaultNamespace();
                    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                        RichSequenceIterator seqs = RichSequence.IOTools.readGenbankDNA(br, ns);
                        while (seqs.hasNext()) {
                            try {
                                RichSequence rs = seqs.nextRichSequence();
                                // write it in EMBL format to standard out
                                if (accession_numbers.contains(rs.getAccession())) {// copy to the folder
                                    //****************************************
                                    //*****This will create tree folder*******
                                    //****************************************
                                    Matcher m = r.matcher(rs.getAccession());
                                    String id = "";
                                    while (m.find()) {
                                        id = m.group();
                                    }
                                    File dir = new File(dst_folder.getAbsolutePath() + "/" + id.substring(0, 2) + "/");
                                    dir.mkdir();
                                    dir = new File(dir.getAbsolutePath() + "/" + id.substring(2, 4) + "/");
                                    dir.mkdir();
                                    File fcopy = new File(dir + "/" + rs.getAccession() + ".gb");
                                    try (FileOutputStream fos = new FileOutputStream(fcopy)) {
                                        RichSequence.IOTools.writeGenbank(fos, rs, ns);
                                        i++;
                                        System.out.println(i + "- " + rs.getAccession() + "\t(file: " + file.getName() + ")");
                                        fos.close();
                                    }
                                } else {
                                    j++;
                                    System.err.println(j + "- " + rs.getAccession() + "\t(file: " + file.getName() + ")");
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        br.close();
                    }
                }
            }
        } catch (IOException | NumberFormatException | NoSuchElementException e) {
            e.printStackTrace();
        }
    }

    /**
     * .
     */
    public void check() {
        try {
            Set<String> accession_numbers = new HashSet<>();
            FileInputStream fstream = new FileInputStream(this.src_filename);
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
                    accession_numbers.add(str);
                }
                br.close();
            }
            check(this.dst_folder, accession_numbers);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void check(File dataDir, Set<String> accession_numbers) throws Exception {
        File[] listFiles = dataDir.listFiles();
        for (File file : listFiles) {
            if (file.isDirectory()) {
                check(file, accession_numbers);
            } else {
                if (!file.isHidden() && file.exists() && file.canRead() && filter.accept(file)) {
                    if (!accession_numbers.contains(file.getName().replace(".gb", "")) || file.length() == 0) {
                        System.out.println("rm " + file.getAbsoluteFile());
                    } else {
                        System.err.println(file.getName().replace(".gb", ""));
                    }
                }
            }
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        String src;
        String dst;
        if (args.length == 0) {
            src = "/Users/mbouadjenek/Documents/bioinformatics_data/test.txt";
            dst = "/Users/mbouadjenek/Documents/bioinformatics_data/genbank_test/";
        } else {
            src = args[0];
            dst = args[1];
        }
        long start = System.currentTimeMillis();
        GenBankRecords r = new GenBankRecords(src, dst);
        r.retrieve();
//        r.copy("/Users/mbouadjenek/Documents/bioinformatics_data/PubMed_Central/reda/");
//        r.check();
        long end = System.currentTimeMillis();
        System.err.println("-------------------------------------------------------------------------");
        long millis = (end - start);
        System.err.println("Downloading " + r.getNbr_retrieveddoc() + " GenBank files took " + Functions.getTimer(millis) + ".");
        System.err.println("-------------------------------------------------------------------------");
    }
}
