package org.shunya.dli;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class BarCodeInterpreter {
    /**
     * For more information see this blog
     * http://alokshukla.wordpress.com/
     */

    static final String[] rootUrl = {
            "http://202.41.82.144",
            "http://www.new.dli.ernet.in",
            "http://www.new1.dli.ernet.in",
            "http://www.dli.ernet.in",
    };

    public final static BarCodeInterpreter BAR_CODE_INTERPRETER = new BarCodeInterpreter();

    public Map<String, String> collect(String url, String barcode) throws IOException {
        String finalUrl = url + barcode;
        Map<String, String> adminData = new HashMap<>();
        Document doc = Jsoup.connect(finalUrl).userAgent("Mozilla").get();
        Elements links = doc.select("table tbody tr");
        for (Element link : links) {
            String rowKey = link.select("td").get(0).text();
            String rowVal = link.select("td").get(1).text();
            System.out.println(rowKey + " : " + rowVal);
            adminData.put(rowKey, rowVal);
            if (rowKey.equalsIgnoreCase("Read Online")) {
                String hrefAttribute=link.getElementsByAttribute("href").select("a").attr("href");
                System.out.println("hrefAttribute = " + hrefAttribute);
                adminData.put("url", extractUrl(hrefAttribute));
            }
        }
        System.out.println("adminData = " + adminData);
        return adminData;
    }

    public String extractUrl(String input) {
        input = input.replaceAll("[\r\n]", "");

        StringBuilder output2 = new StringBuilder();
        String substring = input.substring(input.indexOf("=") + 1);
        char[] chars = substring.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char aChar = chars[i];
            if (aChar == '&')
                break;
            output2.append(aChar);
        }

        char[] chars1 = input.toCharArray();
        StringBuilder output1 = new StringBuilder();
        if (input.startsWith("http://")) {
            int tmp = 2;
            for (int i = 0; i < chars1.length; i++) {
                char c = chars1[i];
                if (c == '/') {
                    --tmp;
                    if (tmp < 0)
                        break;
                }
                output1.append(c);
            }
        } else {
            String tmp = output2.toString();
            System.out.println(" Resolving URL to the server.");
            for (String url : rootUrl) {
                if (pingUrl(url + tmp + "/PTIFF/" + "00000001.tif")) {
                    output1.append(url);
                    break;
                }
            }
        }
        if (output1.length() == 0) {
            throw new RuntimeException("No Server URL Found for the BarCode");
        }
        return output1.append(output2.toString()).toString();
    }

    public boolean pingUrl(final String address) {
        try {
            System.out.println("pinging url = " + address);
            final URL url = new URL(address);
            final HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setRequestMethod("HEAD");
            urlConn.setConnectTimeout(1000 * 20); // mTimeout is in seconds
            urlConn.setReadTimeout(1000 * 50); // mTimeout is in seconds
            final long startTime = System.currentTimeMillis();
            urlConn.connect();
            int responseCode = urlConn.getResponseCode();
            System.out.println("ResponseCode = " + responseCode);
            final long endTime = System.currentTimeMillis();
            if (200 <= responseCode && responseCode <= 399) {
                System.out.println("Time (ms) : " + (endTime - startTime));
                System.out.println("Ping to " + address + " was success");
                return true;
            }
        } catch (final MalformedURLException e1) {
            e1.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
