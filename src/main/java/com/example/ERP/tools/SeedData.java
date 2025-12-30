package com.example.ERP.tools;

import com.example.erp_.config.AppConfig;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.stream.Collectors;
import java.util.Random;
import java.util.Arrays;

/**
 * Insere dados de exemplo no banco (INSERT OR IGNORE) para clients, suppliers, categories, products e uma venda.
 * Uso: java -Dminierp.db="./.minierp/minierp.db" -cp target/classes;target/dependency/* com.example.ERP.tools.SeedData [numClients] [numProducts]
 * Suporta flags: --clients=N --products=M --batch=N --force
 * Variáveis de ambiente: MINIERP_SEED_CLIENTS, MINIERP_SEED_PRODUCTS, MINIERP_SEED_BATCH, MINIERP_SEED_FORCE
 */
public class SeedData {
    public static void main(String[] args) throws Exception {
        // Defaults (larger so user sees more data). Can be overridden by:
        // - positional args: [numClients] [numProducts]
        // - flags: --clients=NNN --products=NNN --batch=NN --force
        // - env vars: MINIERP_SEED_CLIENTS, MINIERP_SEED_PRODUCTS, MINIERP_SEED_BATCH, MINIERP_SEED_FORCE
        int numClients = 1000;
        int numProducts = 1000;
        int batchSize = 500; // how many statements per batch execution
        boolean force = false; // if true, clears some tables before seeding

        // small data for name generation
        String[] firstNames = new String[]{"Ana","Lucas","Mariana","Pedro","João","Carla","Paulo","Bruna","Rafael","Clara","Lucas","Mateus","Sofia","Gustavo"};
        String[] lastNames = new String[]{"Silva","Souza","Pereira","Oliveira","Costa","Almeida","Nunes","Ribeiro","Ferreira","Lima","Gomes"};
        Random rnd = new Random(42);

        // parse positional args first (backwards-compatible)
        if (args.length > 0) {
            try {
                numClients = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
                // fall through to parse flags / env
            }
        }
        if (args.length > 1) {
            try {
                numProducts = Integer.parseInt(args[1]);
            } catch (NumberFormatException ignored) {
                // fall through to parse flags / env
            }
        }

        // parse flag style args --clients=NN --products=NN
        for (String a : args) {
            if (a.startsWith("--clients=")) {
                try { numClients = Integer.parseInt(a.substring("--clients=".length())); } catch (NumberFormatException ignored) {}
            } else if (a.startsWith("--batch=")) {
                try { batchSize = Integer.parseInt(a.substring("--batch=".length())); } catch (NumberFormatException ignored) {}
            } else if (a.equals("--force")) {
                force = true;
            } else if (a.startsWith("--products=")) {
                try { numProducts = Integer.parseInt(a.substring("--products=".length())); } catch (NumberFormatException ignored) {}
            } else if (a.equals("-h") || a.equals("--help")) {
                System.out.println("Usage: SeedData [numClients] [numProducts] or --clients=N --products=M --batch=N --force");
                return;
            }
        }

        // environment variables override
        String envClients = System.getenv("MINIERP_SEED_CLIENTS");
        String envProducts = System.getenv("MINIERP_SEED_PRODUCTS");
        String envBatch = System.getenv("MINIERP_SEED_BATCH");
        String envForce = System.getenv("MINIERP_SEED_FORCE");
        if (envClients != null) {
            try { numClients = Integer.parseInt(envClients); } catch (NumberFormatException ignored) {}
        }
        if (envProducts != null) {
            try { numProducts = Integer.parseInt(envProducts); } catch (NumberFormatException ignored) {}
        }
        if (envBatch != null) {
            try { batchSize = Integer.parseInt(envBatch); } catch (NumberFormatException ignored) {}
        }
        if (envForce != null) {
            force = "1".equals(envForce) || "true".equalsIgnoreCase(envForce) || "yes".equalsIgnoreCase(envForce);
        }

        // sanitize bounds
        if (numClients < 0) numClients = 0;
        if (numProducts < 0) numProducts = 0;
        if (numClients > 100000) numClients = 100000; // safety cap
        if (numProducts > 100000) numProducts = 100000;
        if (batchSize <= 0) batchSize = 500;
        if (batchSize > 10000) batchSize = 10000;

        System.out.println("Seeding with clients=" + numClients + ", products=" + numProducts + ", batchSize=" + batchSize + ", force=" + force);

        String dbFile = AppConfig.DB_FILE;
        Path dbPath = Path.of(dbFile);
        Path folder = dbPath.getParent();
        if (folder != null && !Files.exists(folder)) Files.createDirectories(folder);

        String url = "jdbc:sqlite:" + dbFile;
        try (Connection c = DriverManager.getConnection(url)) {
            try (Statement st = c.createStatement()) {
                st.executeUpdate("PRAGMA foreign_keys = ON;");

                // If force flag set, clear certain tables (in safe order)
                if (force) {
                    System.out.println("--force specified: clearing existing sale_items, sales, products, clients (keeps categories/suppliers)");
                    try {
                        st.executeUpdate("PRAGMA foreign_keys = OFF;");
                        st.executeUpdate("DELETE FROM sale_items;");
                        st.executeUpdate("DELETE FROM sales;");
                        st.executeUpdate("DELETE FROM products;");
                        st.executeUpdate("DELETE FROM clients;");
                        st.executeUpdate("PRAGMA foreign_keys = ON;");
                    } catch (Exception ex) {
                        System.err.println("Warning: could not clear all tables: " + ex.getMessage());
                        // attempt to re-enable keys
                        try { st.executeUpdate("PRAGMA foreign_keys = ON;"); } catch (Exception ignore) {}
                    }
                }

                // If DB has no tables, apply schema
                boolean needsSchema = false;
                try (ResultSet rs = st.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='clients';")) {
                    if (!rs.next()) needsSchema = true;
                } catch (Exception ex) {
                    needsSchema = true;
                }
                if (needsSchema) {
                    System.out.println("Applying DB schema from resource: " + AppConfig.SCHEMA_RESOURCE);
                    try (InputStream is = SeedData.class.getResourceAsStream(AppConfig.SCHEMA_RESOURCE)) {
                        if (is == null) throw new RuntimeException("Schema resource not found: " + AppConfig.SCHEMA_RESOURCE);
                        String sql = new BufferedReader(new InputStreamReader(is)).lines().collect(Collectors.joining("\n"));
                        // split by ; and execute
                        for (String part : sql.split(";")) {
                            String t = part.trim();
                            if (!t.isEmpty()) st.executeUpdate(t + ";");
                        }
                    }
                }

                // seed base categories and suppliers
                st.executeUpdate("INSERT OR IGNORE INTO categories (id, name, description) VALUES (10, 'Bebidas', 'Refrigerantes, sucos e bebidas');");
                st.executeUpdate("INSERT OR IGNORE INTO categories (id, name, description) VALUES (11, 'Higiene', 'Produtos de higiene pessoal');");
                st.executeUpdate("INSERT OR IGNORE INTO suppliers (id, name, contact, cnpj, email, address) VALUES (10, 'Distri Alimentar', '(11) 90000-1111', '11.111.111/0001-11', 'contato@distri.com', 'Av. das Américas, 100');");
                st.executeUpdate("INSERT OR IGNORE INTO suppliers (id, name, contact, cnpj, email, address) VALUES (11, 'Higiene Ltda', '(21) 90000-2222', '22.222.222/0001-22', 'vendas@higiene.com', 'Rua do Comercio, 200');");

                // seed fixed example clients/products to keep stable ids
                st.executeUpdate("INSERT OR IGNORE INTO clients (id, name, cpf_cnpj, email, phone, address, active) VALUES (20, 'Lucas Pereira', '55566677788', 'lucas.p@example.com', '(11) 99999-0001', 'Rua A, 123', 1);");
                st.executeUpdate("INSERT OR IGNORE INTO clients (id, name, cpf_cnpj, email, phone, address, active) VALUES (21, 'Clara Mendes', '66677788899', 'clara.m@example.com', '(21) 98888-1111', 'Av. B, 456', 1);");
                st.executeUpdate("INSERT OR IGNORE INTO products (id, sku, name, description, category_id, supplier_id, cost_price, sale_price, stock_qty, min_stock, active) VALUES (20, 'BEB-001', 'Refrigerante Lata 350ml', 'Refri sabor cola', 10, 10, 1.2, 3.5, 200, 10, 1);");
                st.executeUpdate("INSERT OR IGNORE INTO products (id, sku, name, description, category_id, supplier_id, cost_price, sale_price, stock_qty, min_stock, active) VALUES (21, 'HIG-001', 'Sabonete 90g', 'Sabonete neutro', 11, 11, 0.6, 1.8, 500, 30, 1);");

                int applied = 0;
                // generate additional clients (batched)
                int baseClientId = 1000;
                int batchCount = 0;
                for (int i = 1; i <= numClients; i++) {
                    int id = baseClientId + i;
                    // more realistic name
                    String name = firstNames[rnd.nextInt(firstNames.length)] + " " + lastNames[rnd.nextInt(lastNames.length)];
                    String cpf = String.format("%011d", 70000000000L + i); // simple numeric string to keep unique
                    String email = (name.replaceAll("[^A-Za-z]", "").toLowerCase() + i + "@ex.com");
                    String phone = String.format("(11) 9%04d-%04d", i % 10000, i % 10000);
                    String address = "Rua Exemplo " + i;
                    String sql = String.format("INSERT OR IGNORE INTO clients (id, name, cpf_cnpj, email, phone, address, active) VALUES (%d, '%s', '%s', '%s', '%s', '%s', 1);", id, escape(name), cpf, escape(email), phone, escape(address));
                    try {
                        st.addBatch(sql);
                        batchCount++;
                        if (batchCount >= batchSize) {
                            int[] res = st.executeBatch();
                            st.clearBatch();
                            for (int r : res) if (r != Statement.EXECUTE_FAILED) applied++;
                            batchCount = 0;
                        }
                    } catch (Exception ex) {
                        System.err.println("Erro ao preparar client " + id + ": " + ex.getMessage());
                    }
                }
                if (batchCount > 0) {
                    try {
                        int[] res = st.executeBatch();
                        st.clearBatch();
                        for (int r : res) if (r != Statement.EXECUTE_FAILED) applied++;
                    } catch (Exception ex) {
                        System.err.println("Erro ao executar batch clients: " + ex.getMessage());
                    }
                }

                // generate additional products (batched)
                int baseProductId = 2000;
                batchCount = 0;
                for (int i = 1; i <= numProducts; i++) {
                    int id = baseProductId + i;
                    String sku = String.format("GEN-%06d", i);
                    String name = "Produto " + (i);
                    String desc = "Produto gerado " + i;
                    int category = (i % 2 == 0) ? 10 : 11;
                    int supplier = (i % 2 == 0) ? 10 : 11;
                    double cost = Math.round((1.0 + (i % 50) * 0.3) * 100.0) / 100.0;
                    double sale = Math.round((cost * (1.5 + (i % 3)*0.25)) * 100.0) / 100.0;
                    int stock = 50 + (i % 200);
                    String sql = String.format("INSERT OR IGNORE INTO products (id, sku, name, description, category_id, supplier_id, cost_price, sale_price, stock_qty, min_stock, active) VALUES (%d, '%s', '%s', '%s', %d, %d, %.2f, %.2f, %d, 5, 1);", id, sku, escape(name), escape(desc), category, supplier, cost, sale, stock);
                    try {
                        st.addBatch(sql);
                        batchCount++;
                        if (batchCount >= batchSize) {
                            int[] res = st.executeBatch();
                            st.clearBatch();
                            for (int r : res) if (r != Statement.EXECUTE_FAILED) applied++;
                            batchCount = 0;
                        }
                    } catch (Exception ex) {
                        System.err.println("Erro ao preparar product " + id + ": " + ex.getMessage());
                    }
                }
                if (batchCount > 0) {
                    try {
                        int[] res = st.executeBatch();
                        st.clearBatch();
                        for (int r : res) if (r != Statement.EXECUTE_FAILED) applied++;
                    } catch (Exception ex) {
                        System.err.println("Erro ao executar batch products: " + ex.getMessage());
                    }
                }

                // optionally create a sample sale linking one generated client and product
                try {
                    int saleId = 9000;
                    int clientId = baseClientId + 1;
                    int productId = baseProductId + 1;
                    st.executeUpdate(String.format("INSERT OR IGNORE INTO sales (id, sale_number, client_id, total_amount, paid, status) VALUES (%d, 'SS%04d', %d, 10.0, 1, 'COMPLETED');", saleId, saleId, clientId));
                    st.executeUpdate(String.format("INSERT OR IGNORE INTO sale_items (id, sale_id, product_id, quantity, unit_price, discount) VALUES (%d, %d, %d, 2, 5.0, 0.0);", saleId, saleId, productId));
                    applied += 2;
                } catch (Exception ex) {
                    System.err.println("Erro ao inserir sale: " + ex.getMessage());
                }

                // Apply any sales/sale_items INSERTs present in the schema resource even if schema already exists
                try (InputStream is2 = SeedData.class.getResourceAsStream(AppConfig.SCHEMA_RESOURCE)) {
                    if (is2 != null) {
                        String sqlAll = new BufferedReader(new InputStreamReader(is2)).lines().collect(Collectors.joining("\n"));
                        for (String part : sqlAll.split(";")) {
                            String t = part.trim();
                            if (t.isEmpty()) continue;
                            String tUp = t.toUpperCase();
                            if (tUp.startsWith("INSERT") && (tUp.contains("INTO SALES") || tUp.contains("INTO SALE_ITEMS"))) {
                                try {
                                    st.executeUpdate(t + ";");
                                } catch (Exception ex) {
                                    // ignore individual failures (may already exist)
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    System.err.println("Aviso: falha ao aplicar INSERTs de vendas do schema: " + ex.getMessage());
                }

                System.out.println("Seed statements executed: " + applied + " (some may be ignored if already present)");
            }
        }
    }

    private static String escape(String s) {
        return s.replace("'", "''");
    }
}
