package uk.ac.man.documentparser.input;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

import uk.ac.man.documentparser.dataholders.Document;
import uk.ac.man.documentparser.dataholders.ExternalID;
import uk.ac.man.documentparser.dataholders.Document.Text_raw_type;
import uk.ac.man.documentparser.dataholders.ExternalID.Source;

public class TextFile implements DocumentIterator {

    private int nextArticle = 0;

    private File[] files;

    private String text;
    private String id;

    public TextFile(File[] files) {
        this.files = files;
    }

    public TextFile(String text) {
        this.text = text;
        files = new File[]{null};
    }

    @Override
    public boolean hasNext() {
        return nextArticle < files.length;
    }

    @Override
    public Iterator<Document> iterator() {
        return this;
    }

    public uk.ac.man.documentparser.dataholders.Document next() {
        if (files != null && text == null) {
            StringBuilder sb = new StringBuilder();
            try {
                //BufferedReader inStream = new BufferedReader(new FileReader(files[nextArticle]));
                BufferedReader inStream = new BufferedReader(new InputStreamReader(new FileInputStream(files[nextArticle]), "UTF-8"));

                String line = inStream.readLine();

                while (line != null) {
                    sb.append(line).append("\n");
                    line = inStream.readLine();
                }

                inStream.close();
            } catch (Exception e) {
                System.err.println(e);
                e.printStackTrace();
                System.exit(-1);
            }
            id = files[nextArticle].getName();
            if (id.endsWith(".txt")) {
                id = id.substring(0, id.length() - 4);
            }

            text = sb.toString();
        }

        ExternalID externalID = new ExternalID(id, Source.TEXT);
        Document d = new Document(id, null, null, null, text, Text_raw_type.TEXT, null, null, null, null, null, null, null, null, externalID);

        nextArticle++;
        text = null;

        return d;
    }

    @Override
    public void remove() {
        throw new IllegalStateException("remove() is not supported");
    }

    @Override
    public void skip() {
        nextArticle++;
    }
}
