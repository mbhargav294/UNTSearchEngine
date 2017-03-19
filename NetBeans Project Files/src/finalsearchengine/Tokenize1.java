/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package finalsearchengine;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

/**
 *
 * @author SAI MADHU BHARGAV PALLEM
 * @studentID 11089870
 */
public class Tokenize1 {

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static Map<String, Integer> tokenizeFun(String args) throws IOException {
        //String docPath = "./src/vectretrieval/cranfieldDocs";
        HashMap<String, Integer> tokens = new HashMap<>();
        List<String> stopWords = new ArrayList<>();
        boolean[] stopWordFlag;
        String con = new Scanner(new File("Resources/stopwords.txt")).useDelimiter("\\Z").next();
        String[] stop = con.split("[\n]");
        for (String sw : stop) {
            stopWords.add(sw);
        }

        //File folder = new File(docPath);
        //File[] listOfFiles = folder.listFiles();
        //for (File listOfFile : listOfFiles) {
        //  if (listOfFile.isFile()) 
        //{
        String content = args;
        //String content = new Scanner(new File(docPath+"/"+listOfFile.getName())).useDelimiter("\\Z").next();
        String[] arr = content.split("[^A-Za-z0-9]");
        for (String ss : arr) {
            if (!ss.equals("") && !ss.equals("\n")) {
                ss = ss.toLowerCase();
                if (tokens.get(ss) == null) {
                    tokens.put(ss, 1);
                } else {
                    tokens.put(ss, tokens.get(ss) + 1);
                }
            }
        }
        //}
        //else if (listOfFile.isDirectory()) 
        //{
        //  System.out.println("Directory " + listOfFile.getName());
        //}
        //}

        int unique = 0;
        int tokens1 = 0;
        Map<String, Integer> tokensMap = new TreeMap<>(tokens);
        for (String key : tokensMap.keySet()) {
            unique++;
            tokens1 = tokens1 + tokensMap.get(key);
        }
        Map<String, Integer> sortedMap = sortByValues(tokens);
        //System.out.println("Name: Sai Madhu Bhargav Pallem\nID: 11089870\n------------\nAssignment 1\n------------\n\n");
        //printMap(sortedMap,tokens1);
        //System.out.printf("%-50s %-10s \n\n","Total Number of words: ",tokens1);
        //System.out.printf("%-50s %-10s \n\n\n","Vocabulary Size(No. of Unique words): ",unique);
        //System.out.printf("%-50s \n","Top 20 Words are: ");
        //printMap(sortedMap,20);
        stopWordFlag = new boolean[unique + 1];
        int i = 1;
        for (String sw : sortedMap.keySet()) {
            if (stopWords.contains(sw)) {
                stopWordFlag[i] = true;
            }
            //System.out.println(i);
            i++;
        }
        //System.out.println("\n\nStop Words in top 20 are: ");
        //printMap1(sortedMap, stopWordFlag, 18);
        //System.out.println("\n\nPercentage:");
        int count = printMap(sortedMap, 15, tokens1);
        //System.out.println("\n\n"+count+" words accounts for 15% of the total "+tokens1+" words.");
        Porter p = new Porter();
        HashMap<String, Integer> portstem = new HashMap<>();
        for (Map.Entry<String, Integer> entry : sortedMap.entrySet()) {
            String str = entry.getKey();
            if (!stopWords.contains(str)) {
                int a = entry.getValue();
                String amv = p.portingFun(str);
                if (portstem.get(amv) == null) {
                    portstem.put(amv, a);
                } else {
                    int ab = portstem.get(amv);
                    portstem.put(amv, a + ab);
                }
            }
        }
        Map<String, Integer> sortportstem = sortByValues(portstem);
        int uni = 0;
        int cou = 0;
        for (Map.Entry<String, Integer> entry : sortportstem.entrySet()) {
            uni++;
            cou += entry.getValue();
        }
        //System.out.printf("%-50s %-10s","Total No. of words: ", cou);
        return sortportstem;
        //System.out.println("\n\n\n----------------------------------------------------------------");
        //System.out.println("After Implementation of Porter Stemmer and stop word eliminatior");
        //System.out.println("----------------------------------------------------------------\n");
        //System.out.printf("%-50s %-10s","Total No. of words: ", cou);
        //System.out.printf("\n\n%-50s %-10s", "Vocabulary Size(No. of Unique words): ", uni);
        //System.out.println("\nTop 20 words are: ");
        //System.out.println(sortportstem);
        //printMap(sortportstem,20);
        //System.out.println("\nStop Words in top 20 are: ");
        //System.out.println("\nThere are no stop words as, \nthe stop word eliminator is implemented\n\n");
        //System.out.println("Percentage:");
        //System.out.println("\n\n"+printMap(sortportstem, 15, cou)+" words accounts for 15% of the total "+cou+" words.");
    }

    public static <K, V extends Comparable<V>> Map<K, V> sortByValues(final Map<K, V> map) {
        Comparator<K> valueComparator = new Comparator<K>() {
            public int compare(K k1, K k2) {
                int compare = map.get(k2).compareTo(map.get(k1));
                if (compare == 0) {
                    return 1;
                } else {
                    return compare;
                }
            }
        };
        Map<K, V> sortedByValues = new TreeMap<K, V>(valueComparator);
        sortedByValues.putAll(map);
        return sortedByValues;
    }

    /*
    public static void printMap(Map<String, Integer> map, int n) {
        int i=1;
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            System.out.printf("%-2s %-1s %-20s \n",(i++),":", entry.getKey());
            if(i>n)
                return;
        }
    }
    
    public static void printMap1(Map<String, Integer> map, boolean[] flag, int n) {
        int i=1;
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            if(flag[i]==true)
                if(i<n)
                    System.out.printf("%-2s %-1s %-20s \n",i,":", entry.getKey());
            else
                break;
            i++;
        }
    }
     */
    public static int printMap(Map<String, Integer> map, float n, int words) {
        double p = 0;
        double s = 0;
        double p1;
        int k = 0;
        DecimalFormat df = new DecimalFormat("#0.00");
        for (Map.Entry<String, Integer> entry : map.entrySet()) {

            s = s + (double) entry.getValue();
            p = s / (double) words * 100.00;
            p1 = entry.getValue() / (double) words * 100.00;
            //System.out.printf("%-10s %-1s %-5s %-1s %-6s\n",entry.getKey(),":", entry.getValue(), ":",df.format(p1));
            //System.out.print("  Percent: "+p);
            k++;
            if (p >= n) {
                return k;
            }
        }
        return 0;
    }
}
