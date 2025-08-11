package com.example;

import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STVerticalJc;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.nio.file.Files;
import java.nio.file.Paths;

public class App {
    public static void main(String[] args) throws Exception {

        File outputDir = new File("output");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        String empCode = "253286";
        String empName = "Priya Deo";
        String joiningDate = "Jan-2022";
        String newImagePath = "/app/imageBg.png";

        // 1. Load Template.docx
        InputStream templateStream = App.class.getResourceAsStream("/Template.docx");
        if (templateStream == null) {
            throw new FileNotFoundException("Template.docx not found in resources");
        }
        XWPFDocument document = new XWPFDocument(templateStream);

        // Create your variables map
        Map<String, String> variables = new HashMap<>();
        variables.put("emp_code", empCode);
        variables.put("emp_name", empName);
        variables.put("joining_date", joiningDate);

        // Replace placeholders in all paragraphs
        for (XWPFParagraph paragraph : document.getParagraphs()) {
            for (XWPFRun run : paragraph.getRuns()) {
                String text = run.getText(0);
                if (text != null) {
                    for (Map.Entry<String, String> entry : variables.entrySet()) {
                        String placeholder = "${" + entry.getKey() + "}";
                        if (text.contains(placeholder)) {
                            text = text.replace(placeholder, entry.getValue());
                        }
                    }
                    run.setText(text, 0);
                }
            }
        }
            // Get all pictures
        List<XWPFPictureData> pictures = document.getAllPictures();
        if (!pictures.isEmpty()) {
            // Replace only first image
            XWPFPictureData firstPic = pictures.get(0);

            byte[] newImageBytes = Files.readAllBytes(new File(newImagePath).toPath());

            // Overwrite the image data in the package part
            try (OutputStream os = firstPic.getPackagePart().getOutputStream()) {
                os.write(newImageBytes);
            }
            System.out.println("First image replaced successfully!");
        } else {
            System.out.println("No images found in document.");
        }
        

        // 2. Get first table and append rows
        List<XWPFTable> tables = document.getTables();
        if (!tables.isEmpty()) {
            XWPFTable table = tables.get(0);

            // Reference a non-header row for styling (last row in template)
            XWPFTableRow styleRow = table.getRow(table.getNumberOfRows() - 1);

            // Extract font info from first cell
            String fontName = null;
            int fontSize = -1;
            if (!styleRow.getTableCells().isEmpty()) {
                XWPFTableCell refCell = styleRow.getCell(0);
                if (!refCell.getParagraphs().isEmpty() &&
                    !refCell.getParagraphs().get(0).getRuns().isEmpty()) {
                    XWPFRun refRun = refCell.getParagraphs().get(0).getRuns().get(0);
                    fontName = refRun.getFontFamily();
                    fontSize = refRun.getFontSize();
                }
            }

            // Add new rows
            for (int i = 0; i < 2; i++) {
                XWPFTableRow newRow = table.createRow();

                // Copy row height but avoid background fill
                if (styleRow.getCtRow().getTrPr() != null) {
                    newRow.getCtRow().setTrPr(styleRow.getCtRow().getTrPr());
                }

                // Ensure correct number of cells
                int cellCount = styleRow.getTableCells().size();
                while (newRow.getTableCells().size() < cellCount) {
                    newRow.addNewTableCell();
                }

                // Apply formatting to each cell
                for (int c = 0; c < cellCount; c++) {
                    XWPFTableCell targetCell = newRow.getCell(c);

                    // Remove any shading from header style
                    if (targetCell.getCTTc().getTcPr() != null &&
                        targetCell.getCTTc().getTcPr().isSetShd()) {
                        targetCell.getCTTc().getTcPr().unsetShd();
                    }

                    // Set vertical alignment
                    if (targetCell.getCTTc().getTcPr() == null) {
                        targetCell.getCTTc().addNewTcPr();
                    }
                    targetCell.getCTTc().getTcPr()
                              .addNewVAlign()
                              .setVal(STVerticalJc.CENTER);

                    targetCell.setParagraph(
                        targetCell.getParagraphArray(0) == null ? targetCell.addParagraph() : targetCell.getParagraphArray(0)
                    );
                    targetCell.getParagraphArray(0).setAlignment(ParagraphAlignment.CENTER); // horizontal center
                    // Clear old paragraph and insert dummy text
                    targetCell.removeParagraph(0);
                    XWPFParagraph p = targetCell.addParagraph();
                    XWPFRun r = p.createRun();
                    r.setText("Dummy " + (i + 1) + "," + (c + 1));
                    if (fontName != null) r.setFontFamily(fontName);
                    if (fontSize > 0) r.setFontSize(fontSize);
                }
            }
        }

        // 3. Save output.docx
        File outputDocx = new File(outputDir, "output.docx");
        try (FileOutputStream fos = new FileOutputStream(outputDocx)) {
            document.write(fos);
        }
    

        // 4. Convert DOCX to PDF using LibreOffice CLI
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
