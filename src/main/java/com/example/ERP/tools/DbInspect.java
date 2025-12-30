package com.example.ERP.tools;

import com.example.erp_.config.AppConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DbInspect {
    public static void main(String[] args) throws Exception {
        String dbFile = AppConfig.DB_FILE;
        String url = "jdbc:sqlite:" + dbFile;
        try (Connection c = DriverManager.getConnection(url)) {
            try (Statement st = c.createStatement()) {
                try (ResultSet rs = st.executeQuery("SELECT count(*) FROM sales")) {
                    if (rs.next()) System.out.println("sales=" + rs.getInt(1));
                }
                try (ResultSet rs = st.executeQuery("SELECT count(*) FROM sale_items")) {
                    if (rs.next()) System.out.println("sale_items=" + rs.getInt(1));
                }
                System.out.println("-- sample sales --");
                try (ResultSet rs = st.executeQuery("SELECT id,sale_number,client_id,total_amount,paid,status FROM sales ORDER BY id LIMIT 5")) {
                    while (rs.next()) {
                        System.out.println(String.format("id=%d num=%s client=%s total=%.2f paid=%d status=%s", rs.getInt(1), rs.getString(2), rs.getString(3), rs.getDouble(4), rs.getInt(5), rs.getString(6)));
                    }
                }
                System.out.println("-- sample sale_items --");
                try (ResultSet rs = st.executeQuery("SELECT id,sale_id,product_id,quantity,unit_price,discount FROM sale_items ORDER BY id LIMIT 10")) {
                    while (rs.next()) {
                        System.out.println(String.format("id=%d sale=%d product=%d qty=%d unit=%.2f disc=%.2f", rs.getInt(1), rs.getInt(2), rs.getInt(3), rs.getInt(4), rs.getDouble(5), rs.getDouble(6)));
                    }
                }
            }
        }
    }
}

