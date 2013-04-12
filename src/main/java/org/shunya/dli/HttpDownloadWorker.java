package org.shunya.dli;

import java.util.List;

import static org.shunya.dli.PageSpread.Page;

public class HttpDownloadWorker implements Runnable {
    private final PageSpread queue;
    private final String rootUrl;
    private final String outputDir;
    private final Downloader downloader;
    private final List<Page> failedDownloads;
    private final DownloadObserver observer;
    private final DLIDownloader task;

    public HttpDownloadWorker(PageSpread queue, String rootUrl, String outputDir, Downloader downloader, List<PageSpread.Page> failedDownloads, DownloadObserver observer, DLIDownloader dliDownloader) {
        this.queue = queue;
        this.rootUrl = rootUrl;
        this.outputDir = outputDir;
        this.downloader = downloader;
        this.failedDownloads = failedDownloads;
        this.observer = observer;
        task = dliDownloader;
    }

    @Override
    public void run() {
        Page page = queue.poll();
        while (page != null && !Thread.interrupted()) {
            if (page.canDownload()) {
                boolean status = downloader.download(rootUrl, page.getAndIncrement(), outputDir);
                if(!status){
                    queue.offer(page);
                }
            }else {
                failedDownloads.add(page);
            }
            page = queue.poll();
            observer.update(task);
        }
    }
}
