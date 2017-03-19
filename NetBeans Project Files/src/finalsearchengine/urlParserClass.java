/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package finalsearchengine;

import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author mbhar
 */
public class urlParserClass {

    static ArrayList<String> getURLlist(String urlS, int fileNo) {
        //return this
        ArrayList<String> urlList = new ArrayList<>();
        urlList.add("OK");
        Document d;

        try {
            //System.out.println("actualURL: "+originalUrl+"\n\n");
            d = Jsoup.connect(urlS).followRedirects(true).get().normalise();
            Elements meta = d.select("meta[HTTP-EQUIV=REFRESH]");
            if (!meta.isEmpty()) {
                Element metaTag = meta.first();
                //redirecting links having REFRESH as attribute
                if (metaTag.attr(("http-equiv")).equals("REFRESH")) {
                    String content = metaTag.attr("content");
                    String attr = "URL=";
                    String reDirect = content.substring(content.lastIndexOf(attr) + attr.length());
                    String newURL;
                    if (urlS.endsWith("/") && reDirect.startsWith("/")) {
                        newURL = urlS + reDirect.substring(1);
                    } else {
                        newURL = urlS + reDirect;
                    }
                    urlList = getURLlist(newURL, fileNo);
                    return urlList;
                }
            }
            Elements link = d.select("a");
            String absHref;
            String fileName = "File" + fileNo + ".txt";
            //String docText = d.select("a").text()+" "+d.select("p").text() + " " + d.select("h1").text()+ " " + d.select("h2").text()+ " " + d.select("h3").text()+ " " + d.select("h4").text()+ " " + d.select("h5").text()+ " " + d.select("h6").text();
            String docText = d.text();
            //Element mainDiv = d.getElementById("main");
            //String t = d.select("p").text()+d.select("h1").text()+d.select("h2").text()+d.select("h3").text()+d.select("h4").text()+d.select("h5").text()+d.select("h6").text();
            PrintWriter writer = new PrintWriter("Resources/DataFiles/" + fileName, "UTF-8");
            writer.println("<DOC>\n");
            writer.println("<TITLE>" + d.select("title").text().replaceAll("[&]+", "") + "</TITLE>" + "\n\n");
            writer.println("<DOCNO>" + fileNo + "</DOCNO>\n\n");
            writer.println("<URL>" + urlS + "</URL>\n\n");
            writer.println("<TEXT>" + docText.replaceAll("[^a-zA-Z0-9 ]+", " ") + "</TEXT>");
            writer.println("</DOC>\n");
            writer.close();
            //wwwwSystem.out.println(d.text());
            for (Element links : link) {
                absHref = links.attr("abs:href");
                if (absHref.endsWith("/")) {
                    absHref = absHref.substring(0, absHref.length() - 1);
                }
                if (checkURL(absHref)) {
                    urlList.add(absHref);
                }
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e);
            urlList.clear();
            urlList.add("Not OK");
            return urlList;
        }
        return urlList;
    }

    public static boolean checkURL(String url) {
        //links within the page
        if (url.contains("#")) {
            return false;
        }
        //Invalid urls
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return false;
        }
        //self loops
        if (url.endsWith("index.htm") || url.endsWith("index") || url.endsWith("index.html")) {
            return false;
        }

        URL Link = null;
        try {
            Link = new URL(url);
        } catch (Exception e) {
        };
        String auth = Link.getAuthority();
        //links outside UNT domain
        if (!auth.contains("unt.edu")) {
            return false;
        }
        //execlude mobile links
        if (auth.equals("m.unt.edu")) {
            return false;
        }

        if (auth.equals("maps.unt.edu")) {
            return false;
        }

        //invalid file types
        if (url.endsWith(".jpg") || url.endsWith(".JPG") || url.endsWith(".png") || url.endsWith(".PNG")
                || url.endsWith(".pdf") || url.endsWith(".PDF") || url.endsWith(".doc") || url.endsWith(".DOC")
                || url.endsWith(".xml") || url.endsWith(".XML") || url.endsWith(".rss") || url.endsWith(".RSS")
                || url.endsWith(".pptx") || url.endsWith(".PPTX")) {
            return false;
        }

        if (url.contains("../")) {
            return false;
        }

        if (url.contains("&")) {
            return false;
        }
        //Links with queries
        if (url.contains("?")) {
            return false;
        }
        return true;
    }
}
