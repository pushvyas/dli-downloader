package org.shunya.dli;

import javax.swing.*;

public class DLIDownloadManager {
    private DLIWindow window;

    public DLIDownloadManager() throws ClassNotFoundException {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    window = new DLIWindow();
                    UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
                    window.createAndShowGUI();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public static void main(String[] args) throws ClassNotFoundException {
        DLIDownloadManager manager = new DLIDownloadManager();

    }
}
