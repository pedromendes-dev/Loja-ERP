package com.example.ERP.tools;

import com.example.erp_.config.AppConfig;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class AddClientsDirect {
    public static void main(String[] args) {
        try {
            String dbFile;
            if (args.length > 0 && args[0] != null && !args[0].isBlank()) {
                dbFile = args[0];
            } else {
                dbFile = AppConfig.DB_FILE; // uses the configured DB path
            }
            System.out.println("Using DB file: " + dbFile);
            Path csv = Path.of("data", "new_clients.csv");
            File f = csv.toFile();
            if (!f.exists()) {
                System.err.println("Arquivo data/new_clients.csv não encontrado.");
                System.exit(1);
            }

            String url = "jdbc:sqlite:" + dbFile;
            try (Connection conn = DriverManager.getConnection(url)) {
                String sql = "INSERT OR IGNORE INTO clients (name, cpf_cnpj, email, phone, address, active) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                        String line;
                        boolean first = true;
                        int count = 0;
                        while ((line = br.readLine()) != null) {
                            if (first) { first = false; continue; }
                            if (line.trim().isEmpty()) continue;
                            // simple CSV split and trim, remove surrounding quotes if any
                            String[] raw = line.split(",");
                            String[] parts = new String[raw.length];
                            for (int i = 0; i < raw.length; i++) {
                                String s = raw[i].trim();
                                if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
                                    s = s.substring(1, s.length() - 1).replace("\"\"", "\"");
                                }
                                parts[i] = s;
                            }

                            String name = parts.length>0?parts[0].trim():"";
                            String cpf = parts.length>1?parts[1].trim():null;
                            String email = parts.length>2?parts[2].trim():null;
                            String phone = parts.length>3?parts[3].trim():null;
                            String address = parts.length>4?parts[4].trim():null;
                            String active = parts.length>5?parts[5].trim():"1";

                            ps.setString(1, name);
                            ps.setString(2, cpf);
                            ps.setString(3, email);
                            ps.setString(4, phone);
                            ps.setString(5, address);
                            ps.setInt(6, "1".equals(active)?1:0);
                            int r = ps.executeUpdate();
                            if (r>0) count++;
                        }
                        System.out.println("Inserted clients: " + count);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(2);
        }
    }
}
