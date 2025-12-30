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

public class ProductsPdfReport {
    public static void main(String[] args) {
        try {
            Path exports = Path.of("data", "exports");
            if (!Files.exists(exports)) Files.createDirectories(exports);
            File csv = exports.resolve("products.csv").toFile();
            if (!csv.exists()) {
                System.err.println("Arquivo products.csv não encontrado em data/exports. Gera os CSVs primeiro.");
                System.exit(1);
            }

            List<String[]> rows = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(csv))) {
                String line;
                boolean first = true;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    if (first) { first = false; continue; }
                    String[] parts = line.split(",");
                    rows.add(parts);
                }
            }

            File out = exports.resolve("products-report.pdf").toFile();
            try (PDDocument doc = new PDDocument()) {
                PDPage page = new PDPage(PDRectangle.LETTER);
                doc.addPage(page);

                PDPageContentStream cs = new PDPageContentStream(doc, page);
                try {
                    cs.setFont(PDType1Font.HELVETICA_BOLD, 16);
                    cs.beginText();
                    cs.newLineAtOffset(50, 750);
                    cs.showText("Relatório de Produtos");
                    cs.endText();

                    cs.setFont(PDType1Font.HELVETICA, 10);
                    float y = 720;
                    float left = 50;
                    float rowHeight = 14;

                    cs.beginText();
                    cs.newLineAtOffset(left, y);
                    cs.showText(String.format("%-4s %-10s %-24s %-8s %-8s %-6s", "ID", "SKU", "Nome", "Preço", "Estoque", "Ativo"));
                    cs.endText();
                    y -= rowHeight;

                    for (String[] r : rows) {
                        if (y < 80) {
                            cs.close();
                            page = new PDPage(PDRectangle.LETTER);
                            doc.addPage(page);
                            cs = new PDPageContentStream(doc, page);

                            cs.setFont(PDType1Font.HELVETICA_BOLD, 14);
                            cs.beginText();
                            cs.newLineAtOffset(50, 750);
                            cs.showText("Relatório de Produtos (cont.)");
                            cs.endText();

                            cs.setFont(PDType1Font.HELVETICA, 10);
                            y = 720 - rowHeight;
                            cs.beginText();
                            cs.newLineAtOffset(left, y + rowHeight);
                            cs.showText(String.format("%-4s %-10s %-24s %-8s %-8s %-6s", "ID", "SKU", "Nome", "Preço", "Estoque", "Ativo"));
                            cs.endText();
                            y -= rowHeight;
                        }

                        String id = safeGet(r,0);
                        String sku = safeGet(r,1);
                        String name = safeGet(r,2);
                        String price = safeGet(r,7);
                        String qty = safeGet(r,8);
                        String active = safeGet(r,10);

                        cs.beginText();
                        cs.newLineAtOffset(left, y);
                        String line = String.format("%-4s %-10s %-24s %-8s %-8s %-6s", id, sku, truncate(name,24), price, qty, active);
                        cs.showText(line);
                        cs.endText();
                        y -= rowHeight;
                    }
                } finally {
                    if (cs != null) try { cs.close(); } catch (Exception ex) { }
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
        if (arr==null || i>=arr.length) return "";
        return arr[i]==null?"":arr[i];
    }

    private static String truncate(String s, int len) {
        if (s==null) return "";
        return s.length()<=len? s : s.substring(0,len-3)+"...";
    }
}

