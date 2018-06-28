/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unimelb.edu.au.tools;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 *
 * @author mbouadjenek
 */
public class NewMain4 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws MalformedURLException, IOException {
        // TODO code application logic here
        if (args.length == 0) {
            args = new String[2];
            args[0] = "/Volumes/Macintosh HD/Users/mbouadjenek/Documents/bioinformatics_data/test3.txt";
            args[1] = "0";
        }
        try {
            FileInputStream fstream = new FileInputStream(args[0]);
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            int i = 0;
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
                    if (i <= Integer.parseInt(args[1])) {
                        continue;
                    }
                    String accession = str;
                    URL queryURL = new URL("http://www.ncbi.nlm.nih.gov/nuccore/" + accession); //get URL based on ID
                    URLConnection urlConnection = queryURL.openConnection();
                    urlConnection.setDoOutput(true);
                    urlConnection.setConnectTimeout(10000);
                    DataInputStream in2 = new DataInputStream(urlConnection.getInputStream());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in2));
                    String str2;
                    boolean ok = false;
                    while ((str2 = reader.readLine()) != null) {
                        if (str2.contains("content=\"genbank\"") || str2.contains("meta name=\"ncbi_report\" content=\"est\"")) {
                            System.out.println(accession + "\t1");
                            ok = true;
                            break;
                        }
                    }
                    if (!ok) {
                        System.out.println(accession + "\t0");
                    }
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
        
    }
    
}
