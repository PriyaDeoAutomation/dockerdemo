package com.example;

import org.apache.poi.xwpf.usermodel.*;
import java.io.*;
import java.util.List;

public class App {
    public static void main(String[] args) throws Exception {
        // Output directory inside container (mounted to host)
        File outputDir = new File("output");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        // 1. Load Template.docx
        InputStream templateStream = App.class.getResourceAsStream("/Template.docx");
        if (templateStream == null) {
            throw new FileNotFoundException("Template.docx not found in resources");
        }
        XWPFDocument document = new XWPFDocument(templateStream);

        // 2. Get first table and append rows
        List<XWPFTable> tables = document.getTables();
        if (!tables.isEmpty()) {
            XWPFTableRow styleRow = table.getRow(table.getNumberOfRows() - 1);
            XWPFTable table = tables.get(0);
            for (int i = 0; i < 2; i++) {
                XWPFTableRow row = table.createRow();
                 // Copy row properties (borders, height, shading, etc.)
                if (styleRow.getCtRow().getTrPr() != null) {
                    newRow.getCtRow().setTrPr(styleRow.getCtRow().getTrPr());
                }
                // Ensure same number of cells
                int cellCount = styleRow.getTableCells().size();
                for (int c = 0; c < cellCount; c++) {
                    XWPFTableCell targetCell;
                    if (c < newRow.getTableCells().size()) {
                        targetCell = newRow.getCell(c);
                    } else {
                        targetCell = newRow.createCell();
                    }
            
                    // Copy cell style
                    if (styleRow.getCell(c).getCTTc().getTcPr() != null) {
                        targetCell.getCTTc().setTcPr(styleRow.getCell(c).getCTTc().getTcPr());
                    }
            
                    // Clear old paragraph and insert dummy text
                    targetCell.removeParagraph(0);
                    XWPFParagraph p = targetCell.addParagraph();
                    XWPFRun r = p.createRun();
                    r.setText("Dummy " + (i + 1) + "," + (c + 1));
                }
            }
        }

      
       
        File outputDocx = new File(outputDir, "output.docx");
        try (FileOutputStream fos = new FileOutputStream(outputDocx)) {
            document.write(fos);
        }
        // 4. Convert DOCX to PDF using LibreOffice CLI into mounted folder
        Process process = new ProcessBuilder(
                "libreoffice",
                "--headless",
                "--convert-to", "pdf",
                "--outdir", outputDir.getAbsolutePath(),
                outputDocx.getAbsolutePath()
        ).start();
        process.waitFor();

        System.out.println("PDF created: " + new File(outputDir, "output.pdf").getAbsolutePath());
    }
}
