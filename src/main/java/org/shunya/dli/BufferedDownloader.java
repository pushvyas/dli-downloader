package org.shunya.dli;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.MalformedInputException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class BufferedDownloader implements Downloader {

    @Override
    public boolean download(String rootUrl, String fileName, String outputDir) {
        try {
            URL url = new URL(rootUrl + fileName);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setReadTimeout(180000);
            con.setConnectTimeout(100000);
            Path path = Paths.get(outputDir, fileName);
            BufferedInputStream bis = new BufferedInputStream(con.getInputStream());
            BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(path, StandardOpenOption.CREATE_NEW), 1024);
            byte data[] = new byte[10 * 1024];  // reading 10KB block at a time
            int bytesRead = 0;
            int totalBytesRead = 0;
            while ((bytesRead = bis.read(data, 0, 10 * 1024)) >= 0) {
                bos.write(data, 0, bytesRead);
                totalBytesRead += bytesRead;
            }
            bos.close();
            bis.close();
            System.out.println(fileName + " [" + totalBytesRead / 1000 + " KB]");
            return true;
        } catch (MalformedInputException malformedInputException) {
            malformedInputException.printStackTrace();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        System.out.println("Failed : " + fileName);
        return false;
    }
}