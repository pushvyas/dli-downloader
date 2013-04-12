package org.shunya.dli;

public interface Downloader {
    final Downloader NIO_DOWNLOADER = new NIODownloader();
    final Downloader NORMAL_DOWNLOADER = new BufferedDownloader();

    boolean download(final String rootUrl, final String fileName, final String outputDir);
}
