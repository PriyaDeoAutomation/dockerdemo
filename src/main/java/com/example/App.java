package com.example;

import org.apache.poi.xwpf.usermodel.*;
import java.io.*;
import java.util.List;

public class App {
    public static void main(String[] args) throws Exception {
        // 1. Load Template.docx
        InputStream templateStream = App.class.getResourceAsStream("/Template.docx");
        XWPFDocument document = new XWPFDocument(templateStream);

        // 2. Get first table and append rows
        List<XWPFTable> tables = document.getTables();
        if (!tables.isEmpty()) {
            XWPFTable table = tables.get(0);

            for (int i = 0; i < 2; i++) { // 2 dummy rows
                XWPFTableRow row = table.createRow();
                row.getCell(0).setText("Dummy Name " + (i + 1));
                row.getCell(1).setText("Dummy Value " + (i + 1));
            }
        }

        // 3. Save updated docx
        File outputDocx = new File("output.docx");
        try (FileOutputStream fos = new FileOutputStream(outputDocx)) {
            document.write(fos);
        }

        // 4. Convert DOCX to PDF using LibreOffice CLI
        Process process = new ProcessBuilder(
                "libreoffice",
                "--headless",
                "--convert-to", "pdf",
                "--outdir", ".",
                outputDocx.getAbsolutePath()
        ).start();
        process.waitFor();

        System.out.println("PDF created: output.pdf");
    }
}
