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
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author mbouadjenek
 */
public final class MedlineFolderRestructuration {

    private int total = 0;

    /**
     * The folder where the new structure is created.
     */
    private final File destDir;

    /**
     * A filter for dataset files.
     */
    private final BioinformaticFilter filter = new BioinformaticFilter(BioinformaticFilter.DATASET_FILES);

    /**
     * Initializes a newly created .....
     *
     * @param dist Destination folder.
     * @throws java.lang.Exception
     */
    public MedlineFolderRestructuration(String dist) throws Exception {
        this.destDir = new File(dist);

    }

    public void restructure(String dataDir) throws Exception {
        File f = new File(dataDir);
        File[] listFiles = f.listFiles();
        
        for (File listFile : listFiles) {
            if (listFile.isDirectory()) {
                restructure(listFile.toString());
            } else {
                if (!listFile.isHidden() && listFile.exists() && listFile.canRead() && filter.accept(listFile)) {
                    System.out.println("Processing "+listFile.getAbsoluteFile()+". Please wait few minutes....");
                    parse(listFile);
                }
            }
        }
    }

    private InputStream formatter(File file) throws TransformerConfigurationException, SAXException, IOException, ParserConfigurationException, TransformerException {
        // creation d'une fabrique de documents
        DocumentBuilderFactory fabrique = DocumentBuilderFactory.newInstance();
        fabrique.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        // creation d'un constructeur de documents
        DocumentBuilder constructeur = fabrique.newDocumentBuilder();
        // lecture du contenu d'un fichier XML avec DOM
        Document document = constructeur.parse(file);
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
    private void parse(File file) {
        try {
            // creation d'une fabrique de documents
            DocumentBuilderFactory fabrique = DocumentBuilderFactory.newInstance();
            fabrique.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            // creation d'un constructeur de documents
            DocumentBuilder constructeur = fabrique.newDocumentBuilder();
            // lecture du contenu d'un fichier XML avec DOM
            Document document = constructeur.parse(formatter(file));
            Element root = document.getDocumentElement();
//            this.ucid = root.getAttribute("ucid");
            NodeList medlineCitation = root.getElementsByTagName("MedlineCitation");
            for (int i = 0; i < medlineCitation.getLength(); i++) {
                // make sure not to pick up grandchildren.
                Element pmid_ = (Element) ((Element) medlineCitation.item(i)).getElementsByTagName("PMID").item(0);
                String fname = pmid_.getTextContent();
                while (fname.length() < 8) {
                    fname = "0" + fname;
                }
                //****************************************
                //*****This will create tree folder*******
                //****************************************
                File dir = new File(destDir.getAbsolutePath() + "/" + fname.substring(0, 2) + "/");
                dir.mkdir();
                dir = new File(dir.getAbsolutePath() + "/" + fname.substring(2, 4) + "/");
                dir.mkdir();
                dir = new File(dir.getAbsolutePath() + "/" + fname.substring(4, 6) + "/");
                dir.mkdir();
                File dest = new File(dir + "/" + fname + ".xml");
                //****************************************  
                StringBuilder sb = new StringBuilder();
                sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                sb.append(toString(medlineCitation.item(i)));
                sb.append("\n");
                java.nio.file.Files.write(Paths.get(dest.toURI()), sb.toString().getBytes("utf-8"), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                total++;
                System.out.println(total + "- Create " + dest.getName() + " from " + file.getName());
            }

//            System.out.println(medlineCitation.get);
//            
        } catch (ParserConfigurationException | SAXException | IOException | DOMException | TransformerException e) {
            e.printStackTrace();
        }
    }

    public static String toString(Node node) {
        String xmlString = "";
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            //transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            Source source = new DOMSource(node);

            StringWriter sw = new StringWriter();
            StreamResult result = new StreamResult(sw);

            transformer.transform(source, result);
            xmlString = sw.toString();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return xmlString;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here

        String src;
        String dest;
        if (args.length == 0) {
            src = "/Volumes/TOSHIBA/medlinedata/missing/";
            dest = "/Volumes/TOSHIBA/medlinedata/";
        } else {
            src = args[0];
            dest = args[1];
        }

        MedlineFolderRestructuration doc = new MedlineFolderRestructuration(dest);
        doc.restructure(src);
    }

}
