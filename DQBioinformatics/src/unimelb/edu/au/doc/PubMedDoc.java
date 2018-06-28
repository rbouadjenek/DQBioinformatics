/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unimelb.edu.au.doc;

import unimelb.edu.au.indexing.EntityMention;
import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;
import com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl;
import java.io.File;
import javax.xml.parsers.*;
import org.xml.sax.SAXException;
import org.w3c.dom.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.biojavax.SimpleRankedDocRef;
import org.biojavax.bio.seq.RichSequence;
import unimelb.edu.au.util.StringSimilarity;
//import unimelb.edu.au.search.similarities.TextSimilarities;

/**
 * This class represents a pubmed central article.
 *
 * @author mbouadjenek
 */
public class PubMedDoc {

    /**
     * The file representing the article.
     */
    protected File file;
    /**
     * The PMC id of the article.
     */
    protected String id_pmc;
    /**
     * The PM id of the article.
     */
    protected String id_pmid;
    /**
     * The publisher id of the article.
     */
    protected String id_publisher;
    /**
     * The doi of the article.
     */
    protected String id_doi;
    /**
     * The title of the article.
     */
    protected String title;
    /**
     * The abstract of the article.
     */
    protected String abstract_;
    /**
     * The text of the body of the article.
     */
    protected String body;
    /**
     * The subject of the article.
     */
    protected String subject;
    /**
     * The type of the article.
     */
    protected String type;

    public static String TITLE = "title";
    public static String ABSTRACT = "abstract";
    public static String BODY = "body";
    public static String ID_PMC = "id_pmc";
    public static String ID_PMID = "id_pmid";
    public static String ID_PUBLISHER = "id_publisher";
    public static String ID_DOI = "id_doi";
    public static String SUBJECT = "subject";
    public static String TYPE = "type";
    public static String FILE_NAME = "file_name";
    public static Pattern article_patern = Pattern.compile("([\\d]{8})");

    public static final String SUFFIX_GENE = "GENE_";
    public static final String SUFFIX_DISEASE = "DISEASE_";
    public static final String SUFFIX_GENE_A = "GENE_A_";
    public static final String SUFFIX_GENE_B = "GENE_B_";

    /**
     * Initializes a newly created PubMed article object.
     *
     * @param file the path of the article to read.
     */
    public PubMedDoc(String file) {
        this.file = new File(file);
        this.parse();
    }

    /**
     * Initializes a newly created PubMed article object.
     *
     * @param file the path of the article to read.
     */
    public PubMedDoc(File file) {
        this.file = file;
        this.parse();
    }

    private InputStream formatter() throws TransformerConfigurationException, SAXException, IOException, ParserConfigurationException, TransformerException {
        // creation d'une fabrique de documents
        DocumentBuilderFactory fabrique = new DocumentBuilderFactoryImpl();
        fabrique.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        // creation d'un constructeur de documents
        DocumentBuilder constructeur = fabrique.newDocumentBuilder();
        // lecture du contenu d'un fichier XML avec DOM
        Document document = constructeur.parse(this.file);
        Element root = document.getDocumentElement();
        Transformer transformer;

        transformer = (new TransformerFactoryImpl()).newTransformer();

//        transformer = TransformerFactory.newInstance().newTransformer();
//        transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        //initialize StreamResult with File object to save to file
        StreamResult result = new StreamResult(new StringWriter());
        DOMSource source = new DOMSource(root);
        transformer.transform(source, result);
        String xmlString = result.getWriter().toString();
//        System.out.println(xmlString);
        return new ByteArrayInputStream(xmlString.getBytes());
    }

    /**
     * Parse the article to load the data into appropriate fields.
     *
     */
    private void parse() {
        try {
            // creation d'une fabrique de documents
//            DocumentBuilderFactory fabrique = DocumentBuilderFactory.newInstance();
            DocumentBuilderFactory fabrique = new DocumentBuilderFactoryImpl();
            fabrique.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            // creation d'un constructeur de documents

            DocumentBuilder constructeur = fabrique.newDocumentBuilder();
            // lecture du contenu d'un fichier XML avec DOM
            Document document = constructeur.parse(formatter());
            Element root = document.getDocumentElement();
//            this.ucid = root.getAttribute("ucid");

            this.type = root.getAttribute("article-type");
            Element front = (Element) root.getElementsByTagName("front").item(0);
            if (front != null) {
                Element article_meta = (Element) front.getElementsByTagName("article-meta").item(0);
                if (article_meta != null) {
                    NodeList elementArticle_id = article_meta.getElementsByTagName("article-id");
                    for (int j = 0; j < elementArticle_id.getLength(); j++) {// Iteration over CLAIM NODES
                        Element element_id = (Element) elementArticle_id.item(j);
                        if (element_id != null) {
                            switch (element_id.getAttribute("pub-id-type")) {
                                case "pmc":
                                    this.id_pmc = element_id.getTextContent();
                                    break;
                                case "pmid":
                                    this.id_pmid = element_id.getTextContent();
                                    break;
                                case "publisher-id":
                                    this.id_publisher = element_id.getTextContent();
                                    break;
                                case "doi":
                                    this.id_doi = element_id.getTextContent();
                                    break;
                            }
                        }
                    }
                    Element article_categories = (Element) article_meta.getElementsByTagName("article-categories").item(0);
                    if (article_categories != null) {
                        NodeList subj_group = article_categories.getElementsByTagName("subj-group");
                        for (int j = 0; j < subj_group.getLength(); j++) {// Iteration over CLAIM NODES
                            Element element_subj_group = (Element) subj_group.item(j);

                            if (element_subj_group.getAttribute("subj-group-type").equals("heading")) {
                                Element element_subject = (Element) element_subj_group.getElementsByTagName("subject").item(0);
                                if (element_subject != null) {
                                    this.subject = element_subject.getTextContent().toLowerCase();
                                }
                            }
                        }
                    }
                    Element title_group = (Element) article_meta.getElementsByTagName("title-group").item(0);
                    if (title_group != null) {
                        Element element_title = (Element) title_group.getElementsByTagName("article-title").item(0);
                        if (element_title != null) {
                            this.title = element_title.getTextContent().trim();
                        }
                    }
                    Element element_abstract_ = (Element) article_meta.getElementsByTagName("abstract").item(0);
                    if (element_abstract_ != null) {
                        this.abstract_ = element_abstract_.getTextContent().trim();
                    }
                }
                Element element_body = (Element) root.getElementsByTagName("body").item(0);
                if (element_body != null) {
                    this.body = element_body.getTextContent().trim();
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException | DOMException | TransformerException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method check if the current article is referenced in a sequence
     * record.
     *
     * @param rs RichSequence object that represents the sequence record.
     * @return return if the article is referenced by the sequence.
     */
    public boolean isCitedInSequence(RichSequence rs) {
        Iterator it = rs.getRankedDocRefs().iterator();
        while (it.hasNext()) {
            SimpleRankedDocRef ref = (SimpleRankedDocRef) it.next();
            if (ref.getDocumentReference().getCrossref() != null) {
                if (ref.getDocumentReference().getCrossref().getAccession().equals(getId_pmid())) {
                    return true;
                }
            }
            if (ref.getDocumentReference().getTitle() != null) {
                if (ref.getDocumentReference().getTitle().toLowerCase().trim().equals(getTitle())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * This method check if the current article is referenced in a sequence
     * record.
     *
     * @param rs RichSequence object that represents the sequence record.
     * @return return if the article is referenced by the sequence.
     */
    public double getSimSequenceReferences(RichSequence rs) {
        Iterator it = rs.getRankedDocRefs().iterator();
        double sim = 0;
        while (it.hasNext()) {
            SimpleRankedDocRef ref = (SimpleRankedDocRef) it.next();
            if (ref.getDocumentReference().getCrossref() != null) {
                if (ref.getDocumentReference().getCrossref().getAccession().equals(getId_pmid())) {
                    return 1.0;
                }
            }
            if (ref.getDocumentReference().getTitle() != null) {
                double jaccard = StringSimilarity.getJaccardSimilarity(ref.getDocumentReference().getTitle(), getTitle(), new EnglishAnalyzer(null));
                sim = Double.max(sim, jaccard);
                if (sim == 1) {
                    return 1.0;
                }
            }
        }
        return sim;
    }

    /**
     * This method check if the current article is referenced in a sequence
     * record and return the position of the reference in the sequence.
     *
     * @param rs RichSequence object that represents the sequence record.
     * @return return if the article is referenced by the sequence.
     */
    public double getPosSequenceReferences(RichSequence rs) {
        Iterator it = rs.getRankedDocRefs().iterator();
        double sim = 0;
        int pos = 0, currentpos = -1;
        while (it.hasNext()) {
            SimpleRankedDocRef ref = (SimpleRankedDocRef) it.next();
            if (ref.getDocumentReference().getTitle() != null) {
                double jaccard = StringSimilarity.getJaccardSimilarity(ref.getDocumentReference().getTitle(), getTitle(), new EnglishAnalyzer(null));
                if (sim < jaccard) {
                    sim = jaccard;
                    currentpos = pos;
                }
            }
            pos++;
        }
        return currentpos;
    }

    /**
     *
     * @return The article title.
     */
    public String getTitle() {
        return title;
    }

    /**
     *
     * @return The article abstract.
     */
    public String getAbstract() {
        return abstract_;
    }

    /**
     *
     * @return The article body content.
     */
    public String getBody() {
        return body;
    }

    /**
     *
     */
    public String getSubject() {
        return subject;
    }

    /**
     *
     * @return The article PMC id.
     */
    public String getId_pmc() {
        return id_pmc;
    }

    /**
     *
     * @return The article PM id.
     */
    public String getId_pmid() {
        return id_pmid;
    }

    /**
     *
     * @return The article publisher id.
     */
    public String getId_publisher() {
        return id_publisher;
    }

    /**
     *
     * @return The article doi.
     */
    public String getId_doi() {
        return id_doi;
    }

    /**
     *
     * @return The article type.
     */
    public String getType() {
        return type;
    }

    /**
     *
     * @return The article file name.
     */
    public String getFile_Name() {
        return file.getName();
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAbstract(String abstract_) {
        this.abstract_ = abstract_;
    }

    public void setBody(String body) {
        this.body = body;
    }

    /**
     * This method replaces all occurrences of the two concepts in the document
     * with their ids.
     *
     * @param object1
     * @param suffix1
     * @param object1_mentions
     * @param object2
     * @param suffix2
     * @param object2_mentions
     */
    public void replaceConcepts(String object1, String suffix1, EntityMention object1_mentions, String object2, String suffix2, EntityMention object2_mentions) {
        if (object1_mentions == null) {
            object1_mentions = new EntityMention();
        }
        if (object2_mentions == null) {
            object2_mentions = new EntityMention();
        }
        setTitle(replace(title, suffix1 + object1, object1_mentions.title_map.get(object1), suffix2 + object2, object2_mentions.title_map.get(object2)));
        setAbstract(replace(abstract_, suffix1 + object1, object1_mentions.abstract_map.get(object1), suffix2 + object2, object2_mentions.abstract_map.get(object2)));
        setBody(replace(body, suffix1 + object1, object1_mentions.body_map.get(object1), suffix2 + object2, object2_mentions.body_map.get(object2)));
//        System.out.println(doc.getTitle());
//        System.out.println("****************************************");
//        System.out.println(doc.getAbstract());
//        System.out.println("****************************************");
//        System.out.println(doc.getBody());
    }

    /**
     * This method replace the mention of the two concepts in the text by their
     * ids. On the other hand, it also removes overlaps between concepts. If
     * there is an overlap, the longest is kept. The purpose is to do
     * normalization.
     *
     * @param text The text to process
     * @param id1 The id1 to replace the first concept mention with.
     * @param occ1 The occurrences of the first concept in the text.
     * @param id2 The id2 to replace the second concept mention with.
     * @param occ2 The occurrences of the second concept in the text.
     * @return The new text with replacements.
     */
    public String replace(String text, String id1, Set<String> occ1, String id2, Set<String> occ2) {
        String out = text;
        if (occ1 == null) {
            occ1 = new HashSet<>();
        }
        if (occ2 == null) {
            occ2 = new HashSet<>();
        }
        /**
         * 1- This part tackles the overlap problem.
         */
        Set<String> occ1_2remove = new HashSet<>();
        Set<String> occ2_2remove = new HashSet<>();
        for (String s1 : occ1) {
            StringTokenizer st1 = new StringTokenizer(s1, "_");
            int char_start1 = Integer.parseInt(st1.nextToken());
            int char_end1 = Integer.parseInt(st1.nextToken()) - 1;
            for (String s2 : occ2) {
                StringTokenizer st2 = new StringTokenizer(s2, "_");
                int char_start2 = Integer.parseInt(st2.nextToken());
                int char_end2 = Integer.parseInt(st2.nextToken()) - 1;
                if ((char_start1 <= char_start2 && char_start2 <= char_end1)
                        || (char_start2 <= char_start1 && char_start1 <= char_end2)) {
                    int size1 = char_end1 - char_start1;
                    int size2 = char_end2 - char_start2;
                    if (size1 < size2) {
                        occ1_2remove.add(s1);
                    } else {
                        occ2_2remove.add(s2);
                    }

                }
            }
        }
        occ1.removeAll(occ1_2remove);
        occ2.removeAll(occ2_2remove);
        /**
         * 2- This part tackles the replacement part.
         */
        List<Occurrence> l = new ArrayList<>();
        for (String s : occ1) {
            StringTokenizer st = new StringTokenizer(s, "_");
            int char_start = Integer.parseInt(st.nextToken());
            int char_end = Integer.parseInt(st.nextToken()) - 1;
            Occurrence o = new Occurrence(id1, char_start, char_end);
            l.add(o);
        }
        for (String s : occ2) {
            StringTokenizer st = new StringTokenizer(s, "_");
            int char_start = Integer.parseInt(st.nextToken());
            if (char_start == -1) {// Tackle the problem of DNorm
                char_start++;
            }
            int char_end = Integer.parseInt(st.nextToken()) - 1;
            Occurrence o = new Occurrence(id2, char_start, char_end);
            l.add(o);
        }
        int offset = 0;
        Collections.sort(l);
        for (Occurrence o : l) {
            out = out.substring(0, o.getChar_start() - offset) + o.getId().replace(":", "_") + out.substring(o.getChar_end() - offset + 1);
            offset = offset + (o.getChar_end() - o.getChar_start() + 1) - o.getId().length();
        }
        return out;
    }

    /**
     * @param pmc PMC of the article
     * @param dir_articles directory of the articles
     * @return The absolute article file name.
     */
    public static String getAbsoluteFile(String pmc, String dir_articles) {
        File dir_articles_ = new File(dir_articles);
        String fname = pmc.toLowerCase().replace("pmc", "");
        while (fname.length() < 8) {
            fname = "0" + fname;
        }
        return dir_articles_.getAbsolutePath() + "/" + fname.substring(0, 2) + "/" + fname.substring(2, 4) + "/" + fname.substring(4, 6) + "/PMC" + fname + ".nxml";
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws TransformerException {
        // TODO code application logic here
        String pmc = getAbsoluteFile("PMC3509075", "/Users/mbouadjenek/Documents/bioinformatics_data/PubMed_Central/dataset/");
        PubMedDoc doc = new PubMedDoc(pmc);
//        System.out.println(doc.getBody().length());
//        PubMedDoc doc = new PubMedDoc("/Volumes/Macintosh HD/Users/mbouadjenek/Documents/bioinformatics_data/PubMed_Central/xml/articles.A-B/BMC_Bioinformatics/BMC_Bioinformatics_2014_Feb_26_15_59.nxml");
//        PubMedDoc doc = new PubMedDoc("/Volumes/Macintosh HD/Users/mbouadjenek/Documents/bioinformatics_data/PubMed_Central/xml/articles.A-B/3_Biotech/3_Biotech_2014_Aug_20_4(4)_357-365.nxml");
//        PubMedDoc doc = new PubMedDoc("/Users/mbouadjenek/Documents/bioinformatics_data/PubMed_Central/xml/articles.A-B/Ann_Neurosci/Ann_Neurosci_2015_Jul_22(3)_194.nxml");
//        PubMedDoc doc = new PubMedDoc("/Users/mbouadjenek/Documents/bioinformatics_data/PubMed_Central/dataset/02/71/10/PMC02711072.nxml");//PMC00015016
//        PubMedDoc doc = new PubMedDoc("/Users/mbouadjenek/Documents/bioinformatics_data/PubMed_Central/test_xml/test1.nxml");
//        String pmc = getAbsoluteFile("PMC59574", "/Users/mbouadjenek/Documents/bioinformatics_data/PubMed_Central/dataset/");
//        System.out.println(pmc);
//        PubMedDoc doc = new PubMedDoc(pmc);

//        System.out.println(doc.getId_pmc());
//        System.out.println(doc.getId_doi());
//        System.out.println(doc.getId_pmid());
//        System.out.println(doc.getId_publisher());
//        System.out.println(doc.getSubject());
//        System.out.println(doc.getTitle());
        System.out.println(doc.getTitle().substring(0, 39));        
        System.out.println(doc.getTitle().substring(12, 32));        
////        System.out.println(doc.getTitle().substring(78, 107).replace(" ", "%"));        
//        System.out.println("********");
//        System.out.println(doc.getAbstract().substring(12, 34).replace(" ", "%"));
//        System.out.println(doc.getAbstract().substring(41, 68).replace(" ", "%"));
//        System.out.println("********");
//        System.out.println(doc.getBody().substring(36649, 36814).replace(" ", " "));
//        System.out.println(doc.getBody().substring(100000, 181583).replace(" ", "%"));
//        System.out.println(doc.getBody().substring(100000, 186076).replace(" ", "%"));
//        System.out.println(doc.getAbstract_().replace("\n", " ").replace("\r", " ").substring(14, 18));
//        System.out.println(doc.getAbstract_().replace("\n", " ").replace("\r", " ").substring(127, 131));
//        System.out.println(doc.getAbstract_().replace("\n", " ").replace("\r", " ").substring(384, 388));
//        
//        
//        System.out.println(doc.getBody().substring(12231, 12239));
//        System.out.println(doc.getBody().substring(19, 23));
//        System.out.println(doc.getBody().substring(1711, 1715));
//        System.out.println(doc.getBody().substring(852, 864));
//        System.out.println(doc.getBody().substring(22, 26));
//        System.out.println(doc.getTitle().replace("\n", " ").replace("\r", " ").length());
//        System.out.println(doc.getAbstract_().replace("\n", " ").replace("\r", " ").length());
//        System.out.println(doc.getBody().replace("\n", " ").replace("\r", " ").indexOf("ET-1"));
//        System.out.println(doc.getBody().replace("\n", " ").replace("\r", " ").substring(22, 26));
//        if (doc.getBody().contains("Ilyocoris cimicoides")) {
//            System.out.println("ok");
//        }
//        StringTokenizer st = new StringTokenizer(doc.getBody());
//        while (st.hasMoreTokens()) {
//            System.out.println(st.nextToken());
//        }
//        String pattern = "([a-z]{2}[\\d]{6})|([^a-z][a-z]{1}[\\d]{5}[^\\d])";
        // Create a Pattern object
//        Pattern r = Pattern.compile(pattern);
        // Now create matcher object.
//        Matcher m = r.matcher(doc.getBody());
//        System.out.println(r.);
//        while (m.find()) {
//            System.out.println(m.group());
//        }
    }

}
