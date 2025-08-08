package com.example;

import org.apache.poi.xwpf.usermodel.*;
import com.documents4j.api.DocumentType;
import com.documents4j.api.IConverter;
import com.documents4j.job.LocalConverter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public class App {
    public static void main(String[] args) throws Exception {

        // Load the DOCX template from resources
        InputStream templateStream = App.class.getResourceAsStream("/Template.docx");
        if (templateStream == null) {
            throw new FileNotFoundException("Template.docx not found in resources!");
        }

        XWPFDocument document = new XWPFDocument(templateStream);

        // Assuming the first table is the one we want to edit
        XWPFTable table = document.getTables().get(0);

        // Add two rows with dummy data
        for (int i = 0; i < 2; i++) {
            XWPFTableRow newRow = table.createRow();
            newRow.getCell(0).setText("Row " + (i + 1) + " - Col 1");
            newRow.getCell(1).setText("Row " + (i + 1) + " - Col 2");
            newRow.getCell(2).setText("Row " + (i + 1) + " - Col 3");
        }

        // Save the new DOCX
        File outputDocx = new File("output.docx");
        try (FileOutputStream fos = new FileOutputStream(outputDocx)) {
            document.write(fos);
        }
        document.close();
        templateStream.close();

        System.out.println("DOCX created: " + outputDocx.getAbsolutePath());

        // Convert DOCX to PDF using documents4j
        File outputPdf = new File("output.pdf");
        try (InputStream docxInputStream = new FileInputStream(outputDocx);
             OutputStream pdfOutputStream = new FileOutputStream(outputPdf)) {

            IConverter converter = LocalConverter.builder()
                    .baseFolder(Files.createTempDirectory("temp").toFile())
                    .workerPool(20, 25, 2, TimeUnit.SECONDS)
                    .processTimeout(5, TimeUnit.SECONDS)
                    .build();

            boolean success = converter.convert(docxInputStream)
                    .as(DocumentType.DOCX)
                    .to(pdfOutputStream)
                    .as(DocumentType.PDF)
                    .execute();

            converter.shutDown();

            if (!success) {
                throw new RuntimeException("PDF conversion failed!");
            }
        }

        System.out.println("PDF created: " + outputPdf.getAbsolutePath());
    }
}
