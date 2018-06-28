/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unimelb.edu.au.util;

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
public class RedaESummary {

    /**
     * The file representing the article.
     */
    protected File file;

    public RedaESummary(String file) {
        this.file = new File(file);
        parse();
    }

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
//            this.ucid = root.getAttribute("ucid");
            NodeList front = root.getElementsByTagName("DocSum");
            for (int j = 0; j < front.getLength(); j++) {// Iteration over CLAIM NODES
                Element docSumElement = (Element) front.item(j);
                System.out.print(docSumElement.getElementsByTagName("Id").item(0).getTextContent() + "\t");
                System.out.println(docSumElement.getElementsByTagName("Item").item(0).getTextContent());
            }
        } catch (ParserConfigurationException | SAXException | IOException | DOMException | TransformerException e) {
            e.printStackTrace();
        }
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
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        RedaESummary doc = new RedaESummary("/Users/mbouadjenek/Documents/bioinformatics_data/eSummary/eSummary_3.xml");//PMC00015016
    }

}
