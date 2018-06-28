/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unimelb.edu.au.doc;

import java.io.File;
import java.io.FileFilter;

/**
 * This class represents a filter for PubMed articles.
 *
 * @author mbouadjenek
 */
public class BioinformaticFilter implements FileFilter {

    private final String extention;

    static public String PUBMED_ARTICLES = ".nxml";
    static public String GENBANK_RECORDS = ".gb";

    public BioinformaticFilter(String extention) {
        this.extention = extention;
    }

    @Override
    public boolean accept(File path) {
        return path.getName().toLowerCase().endsWith(extention);
    }
}
