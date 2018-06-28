/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unimelb.edu.au.util;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.Feature;
import org.biojavax.Namespace;
import org.biojavax.RichObjectFactory;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.ontology.SimpleComparableTerm;
import unimelb.edu.au.doc.BioinformaticFilter;

/**
 *
 * @author mbouadjenek
 */
public class DownloadESummary {

    private int total = 0;

    public Map<String, Integer> featureTypesCount = new HashMap<>();

    public Map<String, Map<String, Integer>> featureTypesContent = new HashMap<>();

    /**
     * A filter for sequence records.
     */
    private final BioinformaticFilter filter = new BioinformaticFilter(BioinformaticFilter.GENBANK_RECORDS);

    public void printGIs(String dataDir) throws Exception {
        File f = new File(dataDir);
        File[] listFiles = f.listFiles();
        for (File listFile : listFiles) {
            if (listFile.isDirectory()) {
                printGIs(listFile.toString());
            } else {
                if (!listFile.isHidden() && listFile.exists() && listFile.canRead() && filter.accept(listFile)) {
                    printGI(listFile);
                }
            }
        }
    }

    private void printGI(File file) {
        total++;
        if (total > 449732) {
            System.err.println(total + "- " + file.getAbsoluteFile());
            BufferedReader br = null;
            try {
                RichSequence rs;
                // an input GenBank file
                br = new BufferedReader(new FileReader(file));
                // a namespace to override that in the file
                Namespace ns = RichObjectFactory.getDefaultNamespace();
                // we are reading DNA sequences
                rs = RichSequence.IOTools.readGenbankDNA(br, ns).nextRichSequence();
                System.out.println(rs.getAccession() + "\t" + rs.getIdentifier());
                br.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                try {
                    br.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

    }

    /**
     * Retrieve the eSummaries files based on GI numbers.
     */
    public void printESummaries(String src_filename, String db) {
        int Bulk_size = 800;
        try {
            FileInputStream fstream = new FileInputStream(new File(src_filename));
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            Set<String> l = new HashSet<>();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
                String str;
                System.out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                System.out.println("<!DOCTYPE eSummaryResult PUBLIC \"-//NLM//DTD esummary v1 20041029//EN\" \"http://eutils.ncbi.nlm.nih.gov/eutils/dtd/20041029/esummary-v1.dtd\">");
                System.out.println("<eSummaryResult>");
                while ((str = br.readLine()) != null) {
                    if (str.startsWith("#")) {
                        continue;
                    }
                    if (str.trim().length() == 0) {
                        continue;
                    }
                    total++;
//                    StringTokenizer st = new StringTokenizer(str, "\t");
//                    String accession = st.nextToken();
                    String gi = str;
                    if (total % Bulk_size != 0) {
                        l.add(gi);
                    } else {
                        l.add(gi);
                        printESummaries(l, db);
                        l.clear();
                    }
                }
                if (!l.isEmpty()) {
                    printESummaries(l, db);
                }
                System.out.println("</eSummaryResult>");
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    public void printESummaries(Set<String> list, String db) throws MalformedURLException, IOException {
        String url = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?db=" + db + "&id=";
        url = list.stream().map((gi) -> gi + ",").reduce(url, String::concat);
//        System.out.println(url);
        URL queryURL = new URL(url);
        URLConnection urlConnection = queryURL.openConnection();
        urlConnection.setDoOutput(true);
        urlConnection.setConnectTimeout(10000);
        DataInputStream in2 = new DataInputStream(urlConnection.getInputStream());
        BufferedReader reader = new BufferedReader(new InputStreamReader(in2));
        String str;
        while ((str = reader.readLine()) != null) {
            if (!str.trim().equalsIgnoreCase("<eSummaryResult>")
                    && !str.trim().equalsIgnoreCase("</eSummaryResult>")
                    && !str.trim().equalsIgnoreCase("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                    && !str.trim().startsWith("<!DOCTYPE eSummaryResult")) {
                if (!str.trim().startsWith("<ERROR>")) {
                    System.out.println(str);
                } else {
                    System.err.println(str);
                }
            }
        }
//        System.out.println("------------------------------------------------------------------------");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here

        String src;
        if (args.length == 0) {
            src = "/Volumes/Macintosh HD/Users/mbouadjenek/Documents/bioinformatics_data/accession_GI_v4.0.txt";
        } else {
            src = args[0];
        }
        long start = System.currentTimeMillis();
        DownloadESummary r = new DownloadESummary();
        r.printESummaries(src, args[1]);
        long end = System.currentTimeMillis();
//        for (String k1 : r.featureTypesCount.keySet()) {
//            System.out.println(k1 + " -> " + r.featureTypesCount.get(k1));
//            Map<String, Integer> content = r.featureTypesContent.get(k1);
//            for (String k2 : content.keySet()) {
//                System.out.println("\t" + k2 + " -> " + content.get(k2));
//            }
//            System.out.println("***********************");
//        }
        System.err.println("-------------------------------------------------------------------------");
        long millis = (end - start);
        System.err.println("It took " + Functions.getTimer(millis) + " to complete the processing.");
        System.err.println("-------------------------------------------------------------------------");
    }

}
