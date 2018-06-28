/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unimelb.edu.au.doc;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.biojava.bio.BioException;
import org.biojavax.Namespace;
import org.biojavax.RichObjectFactory;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;
import unimelb.edu.au.lucene.analysis.en.PubMedSpecialWords;
import unimelb.edu.au.search.RecordQuery;
import unimelb.edu.au.util.Functions;

/**
 *
 * @author mbouadjenek
 */
public class RecordLength {

    protected File queries;
    protected File dir_sequences;
    protected File dir_articles;
    Pattern accession_patern = Pattern.compile("([\\d]{4})");
    Pattern article_patern = Pattern.compile("([\\d]{8})");

    public RecordLength(String queries, String dir_sequences, String dir_articles) {
        this.queries = new File(queries);
        this.dir_articles = new File(dir_articles);
        this.dir_sequences = new File(dir_sequences);
    }

    private void get() throws FileNotFoundException {
        int i = 0;
        FileInputStream fstream;
        fstream = new FileInputStream(queries);
        // Get the object of DataInputStream
        DataInputStream in = new DataInputStream(fstream);
//        System.out.println("queryid,acession,pmc,titleOverlapSimilarity,abstractOverlapSimilarity,bodyOverlapSimilarity,allOverlapSimilarity,titleJaccardSimilarity,abstractJaccardSimilarity,bodyJaccardSimilarity,allJaccardSimilarity,titleDiceSimilarity,abstractDiceSimilarity,bodyDiceSimilarity,allDiceSimilarity,titleMatchingSimilarity,abstractMatchingSimilarity,bodyMatchingSimilarity,allMatchingSimilarity,titleCosineSimilarity,abstractCosineSimilarity,bodyCosineSimilarity,allCosineSimilarity,titleDotProductSimilarity,abstractDotProductSimilarity,bodyDotProductSimilarity,titleSumTFIDFScore,abstractSumTFIDFScore,bodySumTFIDFScore,allSumTFIDFScore,titleLuceneVSMScore,abstractLuceneVSMScore,bodyLuceneVSMScore,allLuceneVSMScore,titleBM25Score,abstractBM25Score,bodyBM25Score,allBM25Score,titleLMJelinekMercerScore,abstractLMJelinekMercerScore,bodyLMJelinekMercerScore,allLMJelinekMercerScore,titleLMDirichletScore,abstractLMDirichletScore,bodyLMDirichletScore,allLMDirichletScore,titleIBSimilarityScore,abstractIBSimilarityScore,bodyIBSimilarityScore,allIBSimilarityScore,titleSumTF,titleSDTF,titleMinTF,titleMaxTF,titleArithmeticMeanTF,titleGeometricMeanTF,titleHarmonicMeanTF,titleCoefficientofVariationTF,abstractSumTF,abstractSDTF,abstractMinTF,abstractMaxTF,abstractArithmeticMeanTF,abstractGeometricMeanTF,abstractHarmonicMeanTF,abstractCoefficientofVariationTF,bodySumTF,bodySDTF,bodyMinTF,bodyMaxTF,bodyArithmeticMeanTF,bodyGeometricMeanTF,bodyHarmonicMeanTF,bodyCoefficientofVariationTF,titleRDCS,abstractRDCS,bodyRDCS,titleSumIDF,titleSDIDF,titleMinIDF,titleMaxIDF,titleArithmeticMeanIDF,titleGeometricMeanIDF,titleHarmonicMeanIDF,titleCoefficientofVariationIDF,abstractSumIDF,abstractSDIDF,abstractMinIDF,abstractMaxIDF,abstractArithmeticMeanIDF,abstractGeometricMeanIDF,abstractHarmonicMeanIDF,abstractCoefficientofVariationIDF,bodySumIDF,bodySDIDF,bodyMinIDF,bodyMaxIDF,bodyArithmeticMeanIDF,bodyGeometricMeanIDF,bodyHarmonicMeanIDF,bodyCoefficientofVariationIDF,titleSumICTF,titleSDICTF,titleMinICTF,titleMaxICTF,titleArithmeticMeanICTF,titleGeometricMeanICTF,titleHarmonicMeanICTF,titleCoefficientofVariationICTF,abstractSumICTF,abstractSDICTF,abstractMinICTF,abstractMaxICTF,abstractArithmeticMeanICTF,abstractGeometricMeanICTF,abstractHarmonicMeanICTF,abstractCoefficientofVariationICTF,bodySumICTF,bodySDICTF,bodyMinICTF,bodyMaxICTF,bodyArithmeticMeanICTF,bodyGeometricMeanICTF,bodyHarmonicMeanICTF,bodyCoefficientofVariationICTF,titleSumSCQ,titleSDSCQ,titleMinSCQ,titleMaxSCQ,titleArithmeticMeanSCQ,titleGeometricMeanSCQ,titleHarmonicMeanSCQ,titleCoefficientofVariationSCQ,abstractSumSCQ,abstractSDSCQ,abstractMinSCQ,abstractMaxSCQ,abstractArithmeticMeanSCQ,abstractGeometricMeanSCQ,abstractHarmonicMeanSCQ,abstractCoefficientofVariationSCQ,bodySumSCQ,bodySDSCQ,bodyMinSCQ,bodyMaxSCQ,bodyArithmeticMeanSCQ,bodyGeometricMeanSCQ,bodyHarmonicMeanSCQ,bodyCoefficientofVariationSCQ,titleSCS,abstractSCS,bodySCS,titleClarity1,abstractClarity1,bodyClarity1,titleClarity100,abstractClarity100,bodyClarity100,isMainOrgSciNameInTitle,isMainOrgSciNameInAbstract,isMainOrgSciNameInBody,isMainOrgSynonymInTitle,isMainOrgSynonymInAbstract,isMainOrgSynonymInBody,isMainOrgMisspellingInTitle,isMainOrgMisspellingInAbstract,isMainOrgMisspellingInBody,isMainOrgGenbankCommonNameInTitle,isMainOrgGenbankCommonNameInAbstract,isMainOrgGenbankCommonNameInBody,isMainOrgEquivalentNameInTitle,isMainOrgEquivalentNameInAbstract,isMainOrgEquivalentNameInBody,isMainOrgCommonNameInTitle,isMainOrgCommonNameInAbstract,isMainOrgCommonNameInBody,isMainOrgGenbankSynonymInTitle,isMainOrgGenbankSynonymInAbstract,isMainOrgGenbankSynonymInBody,isMainOrgMisnomerInTitle,isMainOrgMisnomerInAbstract,isMainOrgMisnomerInBody,isMainOrgAcronymInTitle,isMainOrgAcronymInAbstract,isMainOrgAcronymInBody,titleQS,abstractQS,bodyQS,allQS,QLen,citationNbr,logCitationNbr,nbrOrganisms,logAge,cat");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
            String str;
            while ((str = br.readLine()) != null) {
                i++;
                if (str.startsWith("#")) {
                    continue;
                }
                if (str.trim().length() == 0) {
                    continue;
                }
                if (i <= 126125) {
                    continue;
                }
//                System.err.println(i + "- " + str);
                StringTokenizer st = new StringTokenizer(str, "\t");
                String queryid = st.nextToken();
                String article = st.nextToken();
                String pmc = st.nextToken();
                String accession = st.nextToken();

                //****************************************
                //****** Build path to the Article *******
                //****************************************
                Matcher m = article_patern.matcher(article);
                String id = "";
//                while (m.find()) {
//                    id = m.group();
//                }
//                File file_article = new File(dir_articles.getAbsolutePath() + "/"
//                        + id.substring(0, 2) + "/"
//                        + id.substring(2, 4) + "/"
//                        + id.substring(4, 6) + "/" + article);
//                PubMedDoc pubmeddoc = new PubMedDoc(file_article);
//                System.out.print(queryid + "\t");
//                if (pubmeddoc.getTitle() != null) {
//                    st = new StringTokenizer(pubmeddoc.getTitle());
//                    System.out.print(st.countTokens() + "\t");
//                } else {
//                    System.out.print("0" + "\t");
//                }
//                if (pubmeddoc.getAbstract_() != null) {
//                    st = new StringTokenizer(pubmeddoc.getAbstract_());
//                    System.out.print(st.countTokens() + "\t");
//                } else {
//                    System.out.print("0" + "\t");
//                }
//                if (pubmeddoc.getBody() != null) {
//                    st = new StringTokenizer(pubmeddoc.getBody());
//                    System.out.print(st.countTokens());
//                } else {
//                    System.out.print("0");
//                }
//
//                System.out.println("");
                //****************************************
                //***** Build path to the Record *********
                //****************************************
                m = accession_patern.matcher(accession);
                id = "";
                while (m.find()) {
                    id = m.group();
                }
                File file_accession = new File(dir_sequences.getAbsolutePath() + "/" + id.substring(0, 2) + "/" + id.substring(2, 4) + "/" + accession + ".gb");
                Namespace ns = RichObjectFactory.getDefaultNamespace();
                RichSequenceIterator seqs = RichSequence.IOTools.readGenbankDNA(new BufferedReader(new FileReader(file_accession)), ns);
                while (seqs.hasNext()) {
                    RichSequence rs;
                    try {
                        rs = seqs.nextRichSequence();
                        // write it in EMBL format to standard out
                        String descriptionTokenized = Functions.processString(rs.getDescription(), RecordQuery.analyzer);
                        int z = 0;
                        String com_qualifier = "unknown";
                        for (String c : PubMedSpecialWords.KEYWORDS_COMPLETENESS_QUALIFIERS) {
                            if (descriptionTokenized.toLowerCase().contains(c.toLowerCase())) {
                                com_qualifier = c.toLowerCase();
                                z++;
                            }
                        }
                        if (z > 1) {
                            System.out.println(queryid + "\tunknown\t");
                            continue;
                        }
                        System.out.println(queryid + "\t" + com_qualifier.replace(" ", "_"));

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
//                break;

            }
        } catch (IOException ex) {
            Logger.getLogger(MutualReferences.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        try {
            String queries;
            String dir_sequences;
            String dir_articles;
            if (args.length == 0) {
                queries = "/Users/mbouadjenek/Documents/bioinformatics_data/queries_definition_v7.0.txt";
                dir_sequences = "/Users/mbouadjenek/Documents/bioinformatics_data/genbank/";
                dir_articles = "/Users/mbouadjenek/Documents/bioinformatics_data/PubMed_Central/dataset/";
            } else {
                queries = args[0];
                dir_sequences = args[1];
                dir_articles = args[2];
            }
            long start = System.currentTimeMillis();
            RecordLength r = new RecordLength(queries, dir_sequences, dir_articles);
            r.get();
            long end = System.currentTimeMillis();
            System.err.println("-------------------------------------------------------------------------");
            long millis = (end - start);
            System.err.println("The process took " + Functions.getTimer(millis) + ".");
            System.err.println("-------------------------------------------------------------------------");
        } catch (IOException | NoSuchElementException ex) {
            Logger.getLogger(References.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
