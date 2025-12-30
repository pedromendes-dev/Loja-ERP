package com.example.ERP.tools;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SalesPdfReport {
    public static void main(String[] args) {
        try {
            Path exports = Path.of("data", "exports");
            if (!Files.exists(exports)) Files.createDirectories(exports);
            File csv = exports.resolve("sales.csv").toFile();
            if (!csv.exists()) {
                System.err.println("Arquivo sales.csv não encontrado em data/exports. Gera os CSVs primeiro.");
                System.exit(1);
            }

            List<String[]> rows = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(csv))) {
                String line;
                boolean first = true;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    if (first) { first = false; continue; } // skip header
                    // simple CSV split (no quoted fields expected)
                    String[] parts = line.split(",");
                    rows.add(parts);
                }
            }

            File out = exports.resolve("sales-report.pdf").toFile();
            try (PDDocument doc = new PDDocument()) {
                PDPage page = new PDPage(PDRectangle.LETTER);
                doc.addPage(page);

                PDPageContentStream cs = new PDPageContentStream(doc, page);
                try {
                    cs.setFont(PDType1Font.HELVETICA_BOLD, 16);
                    cs.beginText();
                    cs.newLineAtOffset(50, 750);
                    cs.showText("Relatório de Vendas");
                    cs.endText();

                    cs.setFont(PDType1Font.HELVETICA, 10);
                    float y = 720;
                    float left = 50;
                    float rowHeight = 14;

                    // header
                    cs.beginText();
                    cs.newLineAtOffset(left, y);
                    cs.showText(String.format("%-6s %-12s %-8s %-10s %-6s %-10s", "ID", "Sale#", "Client", "Total", "Paid", "Status"));
                    cs.endText();
                    y -= rowHeight;

                    for (String[] r : rows) {
                        if (y < 80) {
                            // fechar stream atual e criar nova página + novo stream
                            cs.close();
                            page = new PDPage(PDRectangle.LETTER);
                            doc.addPage(page);
                            cs = new PDPageContentStream(doc, page);

                            // re-desenhar header no início da nova página
                            cs.setFont(PDType1Font.HELVETICA_BOLD, 16);
                            cs.beginText();
                            cs.newLineAtOffset(50, 750);
                            cs.showText("Relatório de Vendas (cont.)");
                            cs.endText();
                            cs.setFont(PDType1Font.HELVETICA, 10);
                            y = 720 - rowHeight; // start below title
                            cs.beginText();
                            cs.newLineAtOffset(left, y + rowHeight); // header above first row
                            cs.showText(String.format("%-6s %-12s %-8s %-10s %-6s %-10s", "ID", "Sale#", "Client", "Total", "Paid", "Status"));
                            cs.endText();
                            y -= rowHeight;
                        }

                        String id = safeGet(r,0);
                        String sale_number = safeGet(r,1);
                        String client_id = safeGet(r,2);
                        String total = safeGet(r,3);
                        String paid = safeGet(r,4);
                        String status = safeGet(r,5);

                        cs.beginText();
                        cs.newLineAtOffset(left, y);
                        cs.showText(String.format("%-6s %-12s %-8s %-10s %-6s %-10s", id, sale_number, client_id, total, paid, status));
                        cs.endText();
                        y -= rowHeight;
                    }
                } finally {
                    // garante que o stream esteja fechado
                    if (cs != null) {
                        try { cs.close(); } catch (Exception ex) { /* ignore */ }
                    }
                }

                doc.save(out);
            }

            System.out.println("PDF gerado: " + out.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(2);
        }
    }

    private static String safeGet(String[] arr, int i) {
        if (arr == null || i >= arr.length) return "";
        return arr[i] == null ? "" : arr[i];
    }
}
