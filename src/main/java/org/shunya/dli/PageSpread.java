package org.shunya.dli;

import java.util.LinkedList;
import java.util.Queue;

public class PageSpread {
    public static class Page {
        final String filename;
        volatile int downloadCount =0;
        static final int maxCount = 1;

        Page(String filename) {this.filename = filename;}

        public String getAndIncrement() {
            ++downloadCount;
            return filename;
        }

        public String getFilename(){
            return filename;
        }

        public boolean canDownload() {
            return downloadCount < maxCount;
        }

        @Override
        public String toString() {
            return filename;
        }
    }

    private final char[] filename = {'0', '0', '0', '0', '0', '0', '0', '0', '.', 't', 'i', 'f'};
    private final Queue<Page> pageQueue = new LinkedList<>();
    private int totalPages;
    private int count;

    /**
     * Constructor calculates the spread of pages for the given input start page and end page.
     * @param start
     * @param end
     */
    public PageSpread(int start, int end) {
        int currentPage = start;
        while (currentPage >= start && currentPage <= end){
            char[] seq = String.valueOf(currentPage++).toCharArray();
            int length = seq.length;
            int i = 0;
            for (int j = 8 - length; j < 8; j++) {
                filename[j] = seq[i++];
            }
            Page page = new Page(String.valueOf(filename));
            pageQueue.offer(page);
            totalPages++;
        }
    }

    /**
     * Multiple threads can use this method to poll their next job.
     * This method is non-blocking and returns null if no element is left in the queue.
     * @return
     */
    public synchronized Page poll() {
        count++;
       return pageQueue.poll();
    }

    /**
     * This method should be used to put back a page if the download has failed by some reason.
     * @param page
     */
    public synchronized void offer(Page page){
        pageQueue.offer(page);
    }

    public int getTotalPages(){
        return totalPages;
    }

    public synchronized int getCount(){
        return count;
    }
}
