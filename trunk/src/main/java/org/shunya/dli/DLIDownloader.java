package org.shunya.dli;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.shunya.dli.BarCodeInterpreter.BAR_CODE_INTERPRETER;


public class DLIDownloader implements InteractiveTask {
    private String rootUrl;
    private String outputDir;
    private DownloadRequest request;
    private int threads = 2;
    private PageSpread pageSpreadQueue;
    private Downloader downloader;
    private List<PageSpread.Page> failedDownloadBasket;
    private ExecutorService executorService;
    private DownloadObserver observer;
    private RunState runState = RunState.Queued;

    public static DLIDownloader instance(DownloadRequest request) throws IOException {
        return new DLIDownloader(request);
    }

    public DLIDownloader(DownloadRequest request) {
        this.request = request;
    }

    public void captureDetails() throws IOException {
//        Map<String, String> adminData = BAR_CODE_INTERPRETER.collect("http://www.dli.ernet.in/cgi-bin/DBscripts/allmetainfo.cgi?barcode=", request.barcode);
        Map<String, String> adminData = BAR_CODE_INTERPRETER.collect("http://www.dli.ernet.in/cgi-bin/DBscripts/allmetainfo.cgi?barcode=", request.barcode);
        URI url = URI.create(adminData.get("url") + "/PTIFF/");
        int startPage = 1;
        int endPage = Integer.parseInt(adminData.get("TotalPages"));
        outputDir = FileSystems.getDefault().getPath(request.rootDirectory, request.barcode).toString();
        Files.createDirectories(Paths.get(outputDir));
        System.out.println(url.toURL().toString() + " End Page = " + endPage);
        System.out.println("OutputDir = " + outputDir);

        this.rootUrl = url.toURL().toString();
        this.downloader = Downloader.NIO_DOWNLOADER;
        endPage = 20;
        this.pageSpreadQueue = new PageSpread(startPage, endPage);
        this.failedDownloadBasket = Collections.synchronizedList(new ArrayList<PageSpread.Page>());
    }

    public void download() throws InterruptedException, IOException {
        try {
            runState = RunState.Running;
            executorService = Executors.newFixedThreadPool(threads);
            captureDetails();
            for (int i = 0; i < threads; i++) {
                executorService.submit(new HttpDownloadWorker(pageSpreadQueue, rootUrl, outputDir, downloader, failedDownloadBasket, observer, this));
            }
            executorService.shutdown();
            executorService.awaitTermination(100, TimeUnit.HOURS);
            runState = RunState.Converting;
            observer.update(this);
            TiffToPDF.convert(request.rootDirectory, request.barcode);
            runState = RunState.Completed;
            observer.update(this);
            System.out.println("Failed Downloads = " + failedDownloadBasket);
        } catch (Exception e) {e.printStackTrace();
        } finally {
            if (!executorService.isShutdown()) {
                executorService.shutdown();
                executorService.awaitTermination(1, TimeUnit.HOURS);
            }
        }
    }

    @Override
    public String getName() {
        return request.barcode;
    }

    @Override
    public int getProgress() {
        if (pageSpreadQueue != null)
            return 100 * (pageSpreadQueue.getCount()) / pageSpreadQueue.getTotalPages();
        return 0;
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void remove() {

    }

    @Override
    public void stop() {
        try {
            executorService.shutdownNow();
            executorService.awaitTermination(1, TimeUnit.HOURS);
            runState = RunState.Cancelled;
            observer.update(this);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stopAsync() {
        executorService.shutdownNow();
    }

    @Override
    public void awaitShutdown() {
        try {
            executorService.awaitTermination(1, TimeUnit.HOURS);
            runState = RunState.Cancelled;
            observer.update(this);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public RunState getState() {
        return runState;
    }

    @Override
    public String getTimeLeft() {
        return null;
    }

    public void addObserver(DownloadObserver observer) {
        this.observer = observer;
    }

    static class DownloadRequest {
        final String barcode;
        final String rootDirectory;

        public static DownloadRequest request(String barcode, String rootDirectory) {
            return new DownloadRequest(barcode, rootDirectory);
        }

        DownloadRequest(String barcode, String rootDirectory) {
            this.barcode = barcode;
            this.rootDirectory = rootDirectory;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DownloadRequest that = (DownloadRequest) o;
            if (barcode != null ? !barcode.equals(that.barcode) : that.barcode != null) return false;
            return true;
        }

        @Override
        public String toString() {
            return "DownloadRequest{" +
                    "barcode='" + barcode + '\'' +
                    ", rootDirectory='" + rootDirectory + '\'' +
                    '}';
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DLIDownloader that = (DLIDownloader) o;
        if (!request.equals(that.request)) return false;
        return true;
    }

    @Override
    public String toString() {
        return request.toString();
    }
}
