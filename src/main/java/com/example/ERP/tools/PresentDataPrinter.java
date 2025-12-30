package com.example.ERP.tools;

import com.example.erp_.config.DbManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class PresentDataPrinter {
    public static void main(String[] args) {
        try {
            System.out.println("Inicializando DB...");
            DbManager.getInstance().initialize();
            try (Connection conn = DbManager.getInstance().getConnection();
                 Statement st = conn.createStatement()) {

                printTable(st, "clients", "id, name, cpf_cnpj, email, phone, address, active");
                printTable(st, "suppliers", "id, name, contact, cnpj, email, address, active");
                printTable(st, "categories", "id, name, description");
                printTable(st, "products", "id, sku, name, category_id, supplier_id, sale_price, stock_qty");
                printTable(st, "sales", "id, sale_number, client_id, total_amount, paid, status");
                printTable(st, "sale_items", "id, sale_id, product_id, quantity, unit_price, discount");

                System.out.println("\nVisualização concluída.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void printTable(Statement st, String table, String columns) throws Exception {
        System.out.println("\n=== " + table.toUpperCase() + " ===");
        String sql = "SELECT " + columns + " FROM " + table + " ORDER BY id";
        try (ResultSet rs = st.executeQuery(sql)) {
            int colCount = columns.split(",").length;
            while (rs.next()) {
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i <= colCount; i++) {
                    if (i > 1) sb.append(" | ");
                    sb.append(rs.getString(i));
                }
                System.out.println(sb.toString());
            }
        }
    }
}

