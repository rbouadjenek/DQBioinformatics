/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unimelb.edu.au.doc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import unimelb.edu.au.util.Functions;

/**
 * This class restructures the folder containing the PubMed articles. The new
 * structure is organized based on the PMC id. For example, the article with PMC
 * id 3501941 will be located in the folder ./03/50/19/PMC03501941.nxml. The
 * file is also renamed based on the PMC id.
 *
 * @author mbouadjenek
 */
public final class PubMedFolderRestructuration {

    /**
     * The folder where the new structure is created.
     */
    private final File destDir;

    /**
     * The total number of articles processed.
     */
    private int total = 0;

    /**
     * A filter for PubMed articles.
     */
    private final BioinformaticFilter filter = new BioinformaticFilter(BioinformaticFilter.PUBMED_ARTICLES);

    public PubMedFolderRestructuration(String dist) throws IOException, Exception {
        this.destDir = new File(dist);
    }

    /**
     * This method restructures the folder containing the PubMed articles.
     *
     * @param dataDir The path of the directory that contains the PubMed
     * articles.
     * @throws java.lang.Exception
     */
    public void restructure(String dataDir) throws Exception {
        File f = new File(dataDir);
        File[] listFiles = f.listFiles();
        for (File listFile : listFiles) {
            if (listFile.isDirectory()) {
                restructure(listFile.toString());
            } else {
                if (!listFile.isHidden() && listFile.exists() && listFile.canRead() && filter.accept(listFile)) {
//                    restructureFile(listFile);
                    printIDs(listFile);
                }
            }
        }
    }

    private void printIDs(File f) {
        System.err.println(f.getAbsoluteFile());
        PubMedDoc pmc = new PubMedDoc(f);
        System.out.println("PMC"+pmc.getId_pmc() + "\t" + pmc.getId_pmid());
    }

    private void restructureFile(File f) throws Exception {
        total++;
        System.out.println(total + "- Treating " + f.getCanonicalPath());
        PubMedDoc article = new PubMedDoc(f);
        String fname = article.getId_pmc();
        while (fname.length() < 8) {
            fname = "0" + fname;
        }
        FileChannel inputChannel = null;
        FileChannel outputChannel = null;
        try {
            //****************************************
            //*****This will create tree folder*******
            //****************************************
            File dir = new File(destDir.getAbsolutePath() + "/" + fname.substring(0, 2) + "/");
            dir.mkdir();
            dir = new File(dir.getAbsolutePath() + "/" + fname.substring(2, 4) + "/");
            dir.mkdir();
            dir = new File(dir.getAbsolutePath() + "/" + fname.substring(4, 6) + "/");
            dir.mkdir();
            File dest = new File(dir + "/PMC" + fname + ".nxml");
            //****************************************            
            inputChannel = new FileInputStream(f).getChannel();
            outputChannel = new FileOutputStream(dest).getChannel();
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
        } finally {
            inputChannel.close();
            outputChannel.close();
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
     * @param args the command line arguments
     * @throws java.lang.Exception
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        String src;
        String dist;
        if (args.length == 0) {
            src = "/Volumes/Macintosh HD/Users/mbouadjenek/Documents/bioinformatics_data/PubMed_Central/xml/articles.A-B/BMC_Bioinformatics/";
            dist = "/Volumes/Macintosh HD/Users/mbouadjenek/Documents/bioinformatics_data/PubMed_Central/dataset/";
        } else {
            src = args[0];
            dist = args[1];
        }
        long start = System.currentTimeMillis();
        PubMedFolderRestructuration r = new PubMedFolderRestructuration(dist);
        r.restructure(dist);
        long end = System.currentTimeMillis();
        System.out.println("-------------------------------------------------------------------------");
        long millis = (end - start);
        System.out.println("Treating " + r.getTotal() + " files took " + Functions.getTimer(millis) + ".");
        System.out.println("-------------------------------------------------------------------------");
    }

}
