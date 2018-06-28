/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package linnaeus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import martin.common.ArgParser;
import martin.common.Loggers;
import martin.common.Misc;
import martin.common.compthreads.IteratorBasedMaster;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import uk.ac.man.documentparser.input.DocumentIterator;
import uk.ac.man.documentparser.input.TextFile;
import static uk.ac.man.entitytagger.EntityTagger.getMatcher;
import uk.ac.man.entitytagger.Mention;
import uk.ac.man.entitytagger.doc.TaggedDocument;
import uk.ac.man.entitytagger.matching.Matcher;
import uk.ac.man.entitytagger.matching.matchers.ConcurrentMatcher;

/**
 *
 * @author mbouadjenek
 */
public class Linnaeus {

    Matcher matcher;
    private final IndexSearcher orgIS;

    public Linnaeus() throws IOException {
        ArgParser ap = new ArgParser(new String[0]);
        ap.addProperties("internal:/resources-linnaeus/properties.conf");
        Logger logger = Loggers.getDefaultLogger(ap);
        matcher = getMatcher(ap, logger);
        Directory dir = FSDirectory.open(new File("/Users/mbouadjenek/Documents/bioinformatics_data/index_Organism_Names_v5.0/").toPath());
        orgIS = new IndexSearcher(DirectoryReader.open(dir));
    }

    /**
     * Return the list of organisms cited in the text given.
     *
     * @param text
     * @return
     */
    public Map<String, String> getOrganisms(String text) {
        Map<String, String> out = new HashMap<>();
//        File[] retres = new File[1];
//        retres[0] = new File("queries_test.txt");
        DocumentIterator documents = new TextFile(text);
        ConcurrentMatcher tm = new ConcurrentMatcher(matcher, documents);
        IteratorBasedMaster<TaggedDocument> master = new IteratorBasedMaster<>(tm, 1);
        new Thread(master).start();
        while (master.hasNext()) {
            TaggedDocument td = master.next();
            ArrayList<Mention> matches = td.getAllMatches();
            if (matches != null) {
                matches = Misc.sort(matches);
                for (Mention m : matches) {
                    out.put(m.getMostProbableID().replace("species:ncbi:", ""), m.getText());
                }
            }
        }
        return out;
    }

    /**
     * Return the list of organisms cited in the text by giving the scientific
     * name.
     *
     * @param text
     * @return
     * @throws java.io.IOException
     */
    public Set<Organism> getOrganismsBySciName(String text) throws IOException {
        Set<String> contains = new HashSet<>();
        Set<Organism> out = new HashSet<>();
//        File[] retres = new File[1];
//        retres[0] = new File("queries_test.txt");
        DocumentIterator documents = new TextFile(text);
        ConcurrentMatcher tm = new ConcurrentMatcher(matcher, documents);
        IteratorBasedMaster<TaggedDocument> master = new IteratorBasedMaster<>(tm, 1);
        new Thread(master).start();
        while (master.hasNext()) {
            TaggedDocument td = master.next();
            ArrayList<Mention> matches = td.getAllMatches();
            if (matches != null) {
                matches = Misc.sort(matches);
                for (Mention m : matches) {
                    String tax_id = m.getMostProbableID().replace("species:ncbi:", "");
                    if (!contains.contains(tax_id)) {
                        String organismInText = m.getText();
                        String organismScientificName = null;
                        Set<String> common_names = new HashSet<>();
                        Set<String> includes = new HashSet<>();
                        BooleanQuery.Builder bqBuilder = new BooleanQuery.Builder();
                        bqBuilder.add(new TermQuery(new Term("tax_id", m.getMostProbableID().replace("species:ncbi:", ""))), BooleanClause.Occur.MUST);
//                        bqBuilder.add(new TermQuery(new Term("name_class", "scientific name")), BooleanClause.Occur.MUST);
                        TopDocs hits = orgIS.search(bqBuilder.build(), 100);
                        for (ScoreDoc scoreDoc : hits.scoreDocs) {
                            Document doc = orgIS.doc(scoreDoc.doc);
                            String name_txt = doc.get("name_txt");
                            String name_class = doc.get("name_class");
                            switch (name_class) {
                                case "scientific name":
                                    organismScientificName = name_txt;
                                    break;
                                case "includes":
                                    includes.add(name_txt);
                                    break;
                                case "common name":
                                    common_names.add(name_txt);
                                    break;
                            }
                        }
                        Organism o = new Organism(tax_id, organismInText, organismScientificName, common_names, includes);
                        out.add(o);
                        contains.add(tax_id);
                    }
                }
            }
        }
        return out;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        Linnaeus l = new Linnaeus();
        Set<Organism> r = l.getOrganismsBySciName("mice energy M. musculus metabolism in obese M. musculus M. musculus M. musculus");
        for (Organism o : r) {
            System.out.println(o.getTax_id() + "\t" + o.getOrganismScientificName());
            for(String common_name:o.getCommon_names()){
                System.out.println(common_name);
            }
            System.out.println("------");
            for(String include:o.getIncludes()){
                System.out.println(include);
            }
        }
    }

}
