package org.shunya.dli;

import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.io.RandomAccessSource;
import com.itextpdf.text.io.RandomAccessSourceFactory;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.RandomAccessFileOrArray;
import com.itextpdf.text.pdf.codec.TiffImage;

import java.io.File;
import java.io.FileOutputStream;

public class TiffToPDF {

    public static void convert(String rootDir, String barcode) {
        try {
            Document document = new Document(PageSize.A4);
            FileOutputStream os = new FileOutputStream(new File(rootDir, barcode + ".pdf"));
            PdfWriter pdfWriter = PdfWriter.getInstance(document, os);
            pdfWriter.setStrictImageSequence(true);
            document.open();
            document.setPageSize(PageSize.A4);
            document.setMargins(1, 1, 1, 1);
            File file = new File(rootDir, barcode);
            System.out.println(file.getAbsolutePath());
            File[] files = file.listFiles();
//            int count = 0;
            for (File file1 : files) {
//                System.out.println(count++);
                RandomAccessSourceFactory factory = new RandomAccessSourceFactory();
                RandomAccessSource bestSource = factory.createBestSource(file1.getAbsolutePath());
                RandomAccessFileOrArray myTiffFile = new RandomAccessFileOrArray(bestSource);
//                int numberOfPages = TiffImage.getNumberOfPages(myTiffFile);
                Image tiff = TiffImage.getTiffImage(myTiffFile, 1);
                float width = tiff.getWidth() > PageSize.A4.getWidth() ? PageSize.A4.getWidth() : tiff.getWidth();
                float height = tiff.getHeight() > PageSize.A4.getHeight() ? PageSize.A4.getHeight() : tiff.getHeight();
                tiff.scaleToFit(width, height);
                document.add(tiff);
                document.newPage();
            }
            document.close();
            os.close();
            System.out.println("Tiff to PDF Conversion in Completed");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}