/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unimelb.edu.au.doc;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author mbouadjenek
 */
public class MedlineDoc {

    /**
     * The file representing the article.
     */
    protected File file;
    /**
     * The PM id of the article.
     */
    protected String pmid;
    /**
     * The title of the article.
     */
    protected String title;
    /**
     * The abstract of the article.
     */
    protected String abstract_;

    public static String TITLE = "title";
    public static String ABSTRACT = "abstract";
    public static String ID_PMID = "id_pmid";
    public static String FILE_NAME = "file_name";

    /**
     * Initializes a newly created Medline article object.
     *
     * @param file the path of the article to read.
     */
    public MedlineDoc(String file) {
        this.file = new File(file);
        this.parse();
    }

    /**
     * Initializes a newly created Medline article object.
     *
     * @param file the path of the article to read.
     */
    public MedlineDoc(File file) {
        this.file = file;
        this.parse();
    }

    private InputStream formatter() throws TransformerConfigurationException, SAXException, IOException, ParserConfigurationException, TransformerException {
        // creation d'une fabrique de documents
        DocumentBuilderFactory fabrique = DocumentBuilderFactory.newInstance();
        fabrique.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        // creation d'un constructeur de documents
        DocumentBuilder constructeur = fabrique.newDocumentBuilder();
        // lecture du contenu d'un fichier XML avec DOM
        Document document = constructeur.parse(this.file);

        Element root = document.getDocumentElement();
        Transformer transformer;
        transformer = TransformerFactory.newInstance().newTransformer();
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
            DocumentBuilderFactory fabrique = DocumentBuilderFactory.newInstance();
            fabrique.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            // creation d'un constructeur de documents
            DocumentBuilder constructeur = fabrique.newDocumentBuilder();
            // lecture du contenu d'un fichier XML avec DOM
            Document document = constructeur.parse(formatter());
            Element root = document.getDocumentElement();

            Element pmidElement = (Element) root.getElementsByTagName("PMID").item(0);
            if (pmidElement != null) {
                pmid = pmidElement.getTextContent();
            }

            Element titleElement = (Element) root.getElementsByTagName("ArticleTitle").item(0);
            if (titleElement != null) {
                title = titleElement.getTextContent();
            }

            Element abstractElement = (Element) root.getElementsByTagName("AbstractText").item(0);
            if (abstractElement != null) {
                abstract_ = abstractElement.getTextContent();
            }

        } catch (ParserConfigurationException | SAXException | IOException | DOMException | TransformerException e) {
            e.printStackTrace();
        }
    }

    public String getTitle() {
        return title;
    }

    public String getAbstract() {
        return abstract_;
    }

    public String getPmid() {
        return pmid;
    }

    public File getFile() {
        return file;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        MedlineDoc doc = new MedlineDoc("/Volumes/TOSHIBA/medlinedata/22/96/13/22961396.xml");
        System.out.println(doc.pmid);
        System.out.println(doc.getTitle());
        System.out.println(doc.getAbstract());
    }

}
