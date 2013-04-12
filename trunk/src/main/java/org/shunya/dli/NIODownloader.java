package org.shunya.dli;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;

import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.nio.file.StandardOpenOption.WRITE;

public class NIODownloader implements Downloader {

    @Override
    public boolean download(String rootUrl, String fileName, String outputDir) {
        Path path = Paths.get(outputDir, fileName);
        FileChannel fileChannel = null;
        try {
            if (Files.exists(path)) {
                System.out.println(fileName + " Already Exists!!");
                return true;
            }
            HttpURLConnection con = (HttpURLConnection) new URL(rootUrl + fileName).openConnection();
            con.setReadTimeout(180000);
            con.setConnectTimeout(100000);
            ReadableByteChannel rbc = Channels.newChannel(con.getInputStream());
            fileChannel = FileChannel.open(path, EnumSet.of(CREATE_NEW, WRITE));
            long totalBytesRead = fileChannel.transferFrom(rbc, 0, 1 << 22);   // download file with max size 4MB
            fileChannel.close();
            rbc.close();
            System.out.println(fileName + " [" + (double) totalBytesRead / 1000 + " KB]");
            return true;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                System.out.println("Closing File Channel : " + fileName);
                fileChannel.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } finally {
            if(Thread.currentThread().isInterrupted()){
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }
}
