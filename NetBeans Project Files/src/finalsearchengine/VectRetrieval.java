/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package finalsearchengine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;

/**
 *
 * @author mbhar
 */
class wordProp {

    double tf;
    double tf_idf;
}

class tfidf {

    public int termFreq;
    public int maxTf;
    public String Title;
    public String Link;
    public String Desc;
    public double mag;
    public Map<String, Integer> sortportstem = new TreeMap<>();
    public Map<String, wordProp> tf_idf = new TreeMap<>();
}

class query {

    public int maxTf;
    public double mag;
    public Map<String, Integer> queFreq = new TreeMap<>();
    public Map<String, wordProp> queTf_Idf = new TreeMap<>();
    public Map<Integer, Double> cosSim = new TreeMap<>();
    //public List<Integer> relevancy = new ArrayList<>();
}

/*class recallPre{
    double recall;
    double precession;
}*/
public class VectRetrieval {

    public static int pBar = 0;
    public static HashMap<Integer, String> docName = new HashMap<>();
    public static HashMap<Integer, tfidf> lis = new HashMap<>();
    public static HashMap<String, Integer> df = new HashMap<>();
    public static HashMap<Integer, query> queryMap = new HashMap<>();
    public static DecimalFormat decF = new DecimalFormat("#0.0000");
    public static Tokenize1 obj1 = new Tokenize1();

    public static int currDocNumber = 0;
    public static int N = 0;
    public static int k = 0;

    /**
     * @param queryString
     * @param state
     * @throws java.io.IOException
     */
    public static void queryAnalyze(String queryString, int state) {
        pBar = 0;

        if (state == 0) {
            //df.put("hello", 1);
            String docPath = "Resources/DataFiles";
            File folder = new File(docPath);
            File[] listOfFiles = folder.listFiles();

            String content;
            for (File listOfFile : listOfFiles) {
                if (listOfFile.isFile()) {
                    try {
                        currDocNumber++;
                        N++;
                        tfidf a = new tfidf();
                        Map<String, Integer> portstem = new TreeMap<>();
                        //content = new Scanner(new File(docPath + "/" + listOfFile.getName())).useDelimiter("\\Z").next();
                        content = new String(Files.readAllBytes(Paths.get(docPath + "/" + listOfFile.getName())));
                        content = content.replace("\n", " ");
                        String docTitle = "";
                        String docLink = "";
                        String docNumber = "";
                        String docText = "";
                        Document doc = null;
                        try {
                            File fXmlFile = new File(docPath + "/" + listOfFile.getName());
                            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                            doc = dBuilder.parse(fXmlFile);
                            doc.getDocumentElement().normalize();
                            docTitle = doc.getElementsByTagName("TITLE").item(0).getTextContent();
                            docNumber = doc.getElementsByTagName("DOCNO").item(0).getTextContent();
                            docLink = doc.getElementsByTagName("URL").item(0).getTextContent();
                            docText = doc.getElementsByTagName("TEXT").item(0).getTextContent();
                        } catch (Exception e) {
                            System.out.println("Exception: " + e);
                            continue;
                        }
                        if (docText.equals("")) {
                            continue;
                        }
                        int docNo = Integer.parseInt(docNumber);
                        docName.put(docNo, docTitle);
                        //String docTitle1 = "";
                        /*for (int i = 0; i < 100; i++) {
                        docTitle1 += docTitle + " " + docLink + " ";
                        }*/
                        portstem = obj1.tokenizeFun(docTitle.toLowerCase() + " " + docLink.toLowerCase() + " " + docText.toLowerCase());

                        a.Title = docTitle;
                        a.Link = docLink;
                        if (docText.length() < 700) {
                            a.Desc = docText.substring(0, docText.length());
                        } else {
                            a.Desc = docText.substring(0, 700);
                        }
                        a.Desc = a.Desc + "...";
                        a.sortportstem = portstem;
                        a.termFreq = calTF(portstem);

                        boolean first = true;
                        int maxTf = 1;
                        for (Map.Entry<String, Integer> entry : portstem.entrySet()) {
                            String s = entry.getKey();
                            //if(s.equals("unt"))
                            //System.out.println(s+" "+entry.getValue());
                            if (first == true) {
                                maxTf = entry.getValue();
                                first = false;
                            }
                            if (df.get(s) == null) {
                                df.put(s, 1);
                                k++;
                            } else {
                                df.replace(s, df.get(s), df.get(s) + 1);
                            }
                        }

                        a.maxTf = maxTf;
                        lis.put(docNo, a);

                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(VectRetrieval.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        Logger.getLogger(VectRetrieval.class.getName()).log(Level.SEVERE, null, ex);
                    }

                } else if (listOfFile.isDirectory()) {
                    System.out.println("Directory " + listOfFile.getName());
                }
                pBar++;
            }
            for (Map.Entry<Integer, tfidf> entry : lis.entrySet()) {
                tfidf a = entry.getValue();
                a = calTfIdf(a, df, N);
                lis.replace(entry.getKey(), entry.getValue(), a);
                pBar++;
            }
            magnitude(lis);
        } else {
            try {
                int q = 1;
                //queryString = queryString;
                Map<String, Integer> qmap = obj1.tokenizeFun(queryString);
                query qu = new query();
                qu.queFreq = qmap;
                qu.maxTf = Collections.max(qmap.values());
                queryMap.put(q, qu);
                QueryWeight(queryMap, df, N);
                magnitudeQuery(queryMap);
                for (Map.Entry<Integer, query> entry : queryMap.entrySet()) {
                    double cSim = 0;
                    int i = 1;
                    for (Map.Entry<Integer, tfidf> entry1 : lis.entrySet()) {
                        cSim = QueryProcessor(entry.getValue().queTf_Idf, entry1.getValue().tf_idf, entry.getValue().mag, entry1.getValue().mag);
                        entry.getValue().cosSim.put(entry1.getKey(), cSim);
                    }
                    pBar++;
                }
                int i = 1;
                for (Map.Entry<Integer, query> entry : queryMap.entrySet()) {
                    Map<Integer, Double> relMap = sortByValues(entry.getValue().cosSim);
                    printMap(relMap, lis, 50);
                    pBar++;
                }
                pBar = 0;
            } catch (IOException ex) {
                Logger.getLogger(VectRetrieval.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     *
     * @param q
     * @param d
     * @param a
     * @param b
     * @return
     */
    public static double QueryProcessor(Map<String, wordProp> q, Map<String, wordProp> d, double a, double b) {
        double cSim = 0;
        double sum = 0;
        for (Map.Entry<String, wordProp> e : q.entrySet()) {
            double docWeight = 0;
            double queWeight = e.getValue().tf_idf;
            if (d.containsKey(e.getKey())) {
                docWeight = d.get(e.getKey()).tf_idf;
                sum += docWeight * queWeight;
                //System.out.println(e.getKey()+" "+docWeight+" "+sum);
            }
        }
        cSim = sum / (a * b);
        //System.out.println(cSim);
        return cSim;
    }

    /**
     *
     * @param map
     * @return
     */
    public static int calTF(final Map<String, Integer> map) {
        int count = 0;
        for (int a : map.values()) {
            count += a;
        }
        return count;
    }

    /**
     *
     * @param a
     * @param df
     * @param N
     * @return
     */
    public static tfidf calTfIdf(tfidf a, HashMap<String, Integer> df, int N) {
        tfidf a1 = a;
        Map<String, Integer> map = a.sortportstem;
        a1.Title = a.Title;
        a1.sortportstem = map;
        a1.termFreq = a.termFreq;
        a1.maxTf = a.maxTf;
        Map<String, wordProp> k = new TreeMap<>();
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            double docFreq = df.get(entry.getKey());
            wordProp ab = new wordProp();
            ab.tf = (double) entry.getValue() / (double) a.maxTf;
            ab.tf_idf = ab.tf * Math.log(N / docFreq) / Math.log(2);
            k.put(entry.getKey(), ab);
        }
        a1.tf_idf = k;

        return a1;
    }

    /**
     *
     * @param <K>
     * @param <V>
     * @param map
     * @return
     */
    public static <K, V extends Comparable<V>> Map<K, V> sortByValues(final Map<K, V> map) {
        Comparator<K> valueComparator;
        valueComparator = (K k1, K k2) -> {
            int compare = map.get(k2).compareTo(map.get(k1));
            if (compare == 0) {
                return 1;
            } else {
                return compare;
            }
        };
        Map<K, V> sortedByValues = new TreeMap<>(valueComparator);
        sortedByValues.putAll(map);
        return sortedByValues;
    }

    /**
     *
     * @param a
     */
    public static void magnitude(Map<Integer, tfidf> a) {
        for (Map.Entry<Integer, tfidf> k : a.entrySet()) {
            k.getValue().mag = 0.0;
            double val;
            double magnitude = 0.0;
            for (Map.Entry<String, wordProp> s : k.getValue().tf_idf.entrySet()) {
                val = k.getValue().tf_idf.get(s.getKey()).tf_idf;
                magnitude += val * val;
            }
            k.getValue().mag = Math.sqrt(magnitude);
        }
    }

    /**
     *
     * @param a
     */
    public static void magnitudeQuery(Map<Integer, query> a) {
        for (Map.Entry<Integer, query> k : a.entrySet()) {
            k.getValue().mag = 0.0;
            double val;
            double magnitude = 0.0;
            for (Map.Entry<String, wordProp> s : k.getValue().queTf_Idf.entrySet()) {
                val = k.getValue().queTf_Idf.get(s.getKey()).tf_idf;
                magnitude += val * val;
            }
            k.getValue().mag = Math.sqrt(magnitude);
            //System.out.println("mag"+magnitude);
        }
    }

    /**
     *
     * @param a
     * @param df
     * @param N
     */
    public static void QueryWeight(Map<Integer, query> a, Map<String, Integer> df, int N) {
        for (Map.Entry<Integer, query> k : a.entrySet()) {
            double mTf = k.getValue().maxTf;
            for (Map.Entry<String, Integer> q : k.getValue().queFreq.entrySet()) {
                double tf = q.getValue() / mTf;
                double d = 1;
                double tf_idf = 0;
                if (df.get(q.getKey()) != null) {
                    d = df.get(q.getKey());
                    tf_idf = tf * Math.log(N / d) / Math.log(2);
                }
                wordProp p = new wordProp();
                p.tf = tf;
                p.tf_idf = tf_idf;
                System.out.println(tf + " " + tf_idf);
                k.getValue().queTf_Idf.put(q.getKey(), p);
            }
        }
    }

    /**
     *
     * @param map
     * @param n
     * @param k
     */
    public static void printMap(Map<Integer, Double> map, HashMap<Integer, tfidf> n, int k) {
        PrintWriter writer = null;
        try {
            int i = 1;
            int j = 0;
            //System.out.println(map.size());
            writer = new PrintWriter("Resources/tableData.xml", "UTF-8");
            writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            writer.println("<SEARCHRESULTS>");

            for (Map.Entry<Integer, Double> entry : map.entrySet()) {
                //System.out.println(entry.getValue());
                if (entry.getValue() > 0) {
                    //System.out.println(entry.getValue());
                    writer.println("<RESULT>");
                    writer.println("<TITLE>" + n.get(entry.getKey()).Title + "</TITLE>");
                    writer.println("<DESC>" + n.get(entry.getKey()).Desc + "</DESC>");
                    writer.println("<LINK>" + n.get(entry.getKey()).Link + "</LINK>");
                    writer.println("</RESULT>");
                    j++;
                }
            }
            writer.println("</SEARCHRESULTS>");
            writer.close();
        } catch (Exception ex) {
            System.out.println("Exception in printMap: \n" + ex.getMessage());
            writer.println("</RESULT>");
            writer.println("</SEARCHRESULTS>");
            writer.close();
        }
    }
}
