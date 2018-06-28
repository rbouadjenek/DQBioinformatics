/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.biojavax;

import java.util.ArrayList;
import java.util.List;
import org.biojavax.utils.StringTools;

/**
 * Free-format information including an abbreviated form of the organism name,
 * sometimes followed by a molecule type.
 *
 * @author reda bouadjenek
 */
public class Source {

    private String source_name;
    private String organism_name;
    private final List<String> lineage;

    public Source() {
        this.lineage = new ArrayList<>();
    }

    /**
     * Return the source name.
     *
     * @param name
     */
    public void setSourceName(String name) {
        this.source_name = name.replaceAll("\n", " ");
    }

    public void setOrganism(String organism) {
        String[] org = organism.split("\n");
        this.organism_name = org[0];
        boolean end = false;
        for (int i = 1; i < org.length; i++) {
            if (!org[i].contains(";") && !org[i].endsWith(".") && end == false) {
                this.organism_name += " " + org[i];
                continue;
            } else {
                end = true;
            }
            String[] l = org[i].split(";");
            for (String s : l) {
                s = s.trim();
                lineage.add(s.endsWith(".") ? s.substring(0, s.length() - 1) : s);
            }
        }
    }

    /**
     *
     * @return Return the source name.
     */
    public String getSourceName() {
        return source_name;
    }

    /**
     *
     * @return Return the organism name.
     */
    public String getOrganismName() {
        return organism_name;
    }

    /**
     *
     * @return Return the lineage size.
     */
    public int sizeLineage() {
        return lineage.size();
    }

    public String getLineage(int index) {
        return lineage.get(index);
    }

    public String getStringLineage() {
        String out = "";
        out = lineage.stream().map((l) -> l + "; ").reduce(out, String::concat);
        out = out.trim();
        return out.substring(0, out.length() - 1) + ".";
    }

}
