/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package linnaeus;

import java.util.Set;

/**
 *
 * @author mbouadjenek
 */
public class Organism {

    private final String tax_id;
    private final String organismInText;
    private final String organismScientificName;
    private final Set<String> common_names;
    private final Set<String> includes;

    public Organism(String tax_id, String organismInText, String organismScientificName, Set<String> common_names, Set<String> includes) {
        this.tax_id = tax_id;
        this.organismInText = organismInText;
        this.organismScientificName = organismScientificName;
        this.common_names = common_names;
        this.includes = includes;
    }

    public String getTax_id() {
        return tax_id;
    }

    public String getOrganismScientificName() {
        return organismScientificName;
    }

    public String getOrganismInText() {
        return organismInText;
    }

    public Set<String> getCommon_names() {
        return common_names;
    }

    public Set<String> getIncludes() {
        return includes;
    }

}
