package com.example.erp_.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;

public class DbManager {
    private static DbManager instance;
    private Connection connection;

    private DbManager() {
    }

    public static synchronized DbManager getInstance() {
        if (instance == null) {
            instance = new DbManager();
        }
        return instance;
    }

    public void initialize() {
        try {
            Path folder = Paths.get(AppConfig.DB_FOLDER);
            if (!Files.exists(folder)) {
                Files.createDirectories(folder);
            }
            File dbFile = new File(AppConfig.DB_FILE);
            boolean needInit = !dbFile.exists();
            String url = "jdbc:sqlite:" + AppConfig.DB_FILE;
            connection = DriverManager.getConnection(url);

            // Define pragmas para melhorar o comportamento de concorrência no SQLite
            try (Statement pragma = connection.createStatement()) {
                // Habilita WAL para melhor concorrência entre leituras/escritas
                pragma.executeUpdate("PRAGMA journal_mode=WAL;");
                // Define tempo de espera (busy timeout) para aguardar locks (em milissegundos)
                pragma.executeUpdate("PRAGMA busy_timeout = 10000;");
                // Usa synchronous = NORMAL para balancear segurança e desempenho com WAL
                pragma.executeUpdate("PRAGMA synchronous = NORMAL;");
            } catch (Exception e) {
                // Não-fatal: continua mesmo se os pragmas não puderem ser aplicados
                e.printStackTrace();
            }

            if (needInit) {
                loadSchema();
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao inicializar banco de dados", e);
        }
    }

    private void loadSchema() {
        try (InputStream is = getClass().getResourceAsStream(AppConfig.SCHEMA_RESOURCE)) {
            if (is == null) {
                throw new RuntimeException("Arquivo de schema não encontrado: " + AppConfig.SCHEMA_RESOURCE);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                String sql = sb.toString();
                try (Statement stmt = connection.createStatement()) {
                    stmt.executeUpdate("PRAGMA foreign_keys = ON;");
                    // Separa instruções por ponto-e-vírgula e executa cada uma
                    String[] parts = sql.split(";\s*\n");
                    for (String part : parts) {
                        String s = part.trim();
                        if (!s.isEmpty()) {
                            stmt.executeUpdate(s);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao carregar schema", e);
        }
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            String url = "jdbc:sqlite:" + AppConfig.DB_FILE;
            connection = DriverManager.getConnection(url);
            // Aplica pragmas também na conexão recém-criada
            try (Statement pragma = connection.createStatement()) {
                pragma.executeUpdate("PRAGMA journal_mode=WAL;");
                pragma.executeUpdate("PRAGMA busy_timeout = 10000;");
                pragma.executeUpdate("PRAGMA synchronous = NORMAL;");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return connection;
    }

    // Auxiliares de teste: permitem injetar uma Connection (por exemplo in-memory) e resetar o singleton
    /**
     * Injeta uma instância de Connection para testes. Use em testes para fornecer uma conexão em memória.
     * Deve ser usado apenas em testes; mantido público para permitir que o código de teste em outros pacotes o chame.
     */
    public synchronized void setTestConnection(Connection conn) {
        this.connection = conn;
    }

    /**
     * Reseta o singleton DbManager (para testes). Use em teardown de testes para evitar interferência entre testes.
     * Mantido público para uso em testes.
     */
    public static synchronized void resetInstanceForTests() {
        instance = null;
    }
}
