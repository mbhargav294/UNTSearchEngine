/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package finalsearchengine;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.jsoup.Jsoup;
import org.w3c.dom.Document;

/**
 *
 * @author mbhar
 */
public class MadhuSearchEngine {

    public ArrayList<String> linkDup = new ArrayList<>();
    public static int ab = 1;

    /**
     *
     * @param noOfPages
     * @return
     */
    public static boolean reBuildDB(int noOfPages) {
        int i;
        int count = 0;
        int count1 = 0;
        int MAX_PAGES = noOfPages;
        String root = "http://www.unt.edu";

        ArrayList<String> linkDup = new ArrayList<String>();
        Queue<String> linkQ = new LinkedList<String>();;
        linkQ.add(root);
        String acLink = root.replaceAll("https://", "").replaceAll("http://", "");
        linkDup.add(acLink);
        linkQ.add("http://www.cse.unt.edu");
        linkDup.add("http://www.cse.unt.edu");

        linkQ.add("http://www.cse.unt.edu/site/node/18");
        linkDup.add("http://www.cse.unt.edu/site/node/18");

        urlParserClass a = new urlParserClass();
        while (ab <= MAX_PAGES && !linkQ.isEmpty()) {
            ArrayList<String> urlList = a.getURLlist(root, ab);
            if (urlList.get(0).equals("Not OK")) {
                linkQ.remove();
                root = linkQ.peek();
                count1 = 0;
                continue;
            } else {
                urlList.remove(0);
            }
            for (i = 0; i < urlList.size(); i++) {
                String link = urlList.get(i);
                acLink = link.replaceAll("https://", "").replaceAll("http://", "");
                //System.out.println(acLink);
                if (!linkDup.contains(acLink)) {
                    count++;
                    count1++;
                    linkQ.add(link);
                    linkDup.add(acLink);
                } else {
                    urlList.remove(i);
                }
            }
            System.out.println(ab++ + ": " + root + " -> " + count1 + " Total: " + count);
            linkQ.remove();
            root = linkQ.peek();
            count1 = 0;
        }
        //System.out.println("Total No. of Links: " + count);
        ab = 1;
        return true;
    }

    /**
     *
     * @param link
     * @return
     */
    public static boolean linkCheck(String link) {
        URL Link = null;
        try {
            Link = new URL(link);
        } catch (Exception e) {
        };
        String auth = Link.getAuthority();
        return auth.contains("unt.edu");
    }
}
