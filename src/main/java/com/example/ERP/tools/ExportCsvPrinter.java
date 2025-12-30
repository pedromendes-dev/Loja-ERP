package com.example.ERP.tools;

import com.example.erp_.config.DbManager;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class ExportCsvPrinter {
    public static void main(String[] args) {
        try {
            System.out.println("Inicializando DB e exportando dados para CSV...");
            DbManager.getInstance().initialize();
            Path outDir = Path.of("data", "exports");
            if (!Files.exists(outDir)) Files.createDirectories(outDir);

            try (Connection conn = DbManager.getInstance().getConnection(); Statement st = conn.createStatement()) {
                export(st, "clients", "id,name,cpf_cnpj,email,phone,address,active", outDir.resolve("clients.csv").toFile());
                export(st, "products", "id,sku,name,description,category_id,supplier_id,cost_price,sale_price,stock_qty,min_stock,active", outDir.resolve("products.csv").toFile());
                export(st, "sales", "id,sale_number,client_id,total_amount,paid,status", outDir.resolve("sales.csv").toFile());
            }
            System.out.println("Export concluído em data/exports/");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void export(Statement st, String table, String columnsCsv, File outFile) throws Exception {
        String[] cols = columnsCsv.split(",");
        String sql = "SELECT " + String.join(",", cols) + " FROM " + table + " ORDER BY id";
        try (ResultSet rs = st.executeQuery(sql); PrintWriter pw = new PrintWriter(new FileWriter(outFile))) {
            // header
            pw.println(String.join(",", cols));
            while (rs.next()) {
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i <= cols.length; i++) {
                    if (i > 1) sb.append(',');
                    String v = rs.getString(i);
                    if (v == null) v = "";
                    // escape quotes
                    if (v.contains(",") || v.contains("\n") || v.contains("\"")) {
                        v = v.replace("\"", "\"\"");
                        v = '"' + v + '"';
                    }
                    sb.append(v);
                }
                pw.println(sb.toString());
            }
        }
    }
}

