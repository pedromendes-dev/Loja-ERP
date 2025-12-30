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

/**
 * Gerenciador singleton para a conexão com o banco de dados (SQLite).
 * Contém helpers para testes (injeção de Connection e reinicialização do singleton).
 */
public class GerenciadorBancoDados {
    private static GerenciadorBancoDados instancia;
    private Connection conexao;

    private GerenciadorBancoDados() {
    }

    /** Retorna a instância singleton de GerenciadorBancoDados. */
    public static synchronized GerenciadorBancoDados obterInstancia() {
        if (instancia == null) {
            instancia = new GerenciadorBancoDados();
        }
        return instancia;
    }

    /** Inicializa o banco de dados: cria pasta/arquivo se necessário e carrega o schema. */
    public void inicializar() {
        try {
            Path pasta = Paths.get(AppConfig.DB_FOLDER);
            if (!Files.exists(pasta)) {
                Files.createDirectories(pasta);
            }
            File arquivoDb = new File(AppConfig.DB_FILE);
            boolean precisaInicializar = !arquivoDb.exists();
            String url = "jdbc:sqlite:" + AppConfig.DB_FILE;
            conexao = DriverManager.getConnection(url);
            if (precisaInicializar) {
                carregarSchema();
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao inicializar banco de dados", e);
        }
    }

    /** Carrega e executa o schema SQL contido em AppConfig.SCHEMA_RESOURCE. */
    private void carregarSchema() {
        try (InputStream is = getClass().getResourceAsStream(AppConfig.SCHEMA_RESOURCE)) {
            if (is == null) {
                throw new RuntimeException("Arquivo de schema não encontrado: " + AppConfig.SCHEMA_RESOURCE);
            }
            try (BufferedReader leitor = new BufferedReader(new InputStreamReader(is))) {
                StringBuilder sb = new StringBuilder();
                String linha;
                while ((linha = leitor.readLine()) != null) {
                    sb.append(linha).append("\n");
                }
                String sql = sb.toString();
                try (Statement stmt = conexao.createStatement()) {
                    stmt.executeUpdate("PRAGMA foreign_keys = ON;");
                    // divide por ponto-e-vírgula seguido de espaços opcionais
                    String[] partes = sql.split(";\\s*");
                    for (String parte : partes) {
                        String s = parte.trim();
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

    /** Retorna a conexão ativa, abrindo uma nova se necessário. */
    public Connection obterConexao() throws SQLException {
        if (conexao == null || conexao.isClosed()) {
            String url = "jdbc:sqlite:" + AppConfig.DB_FILE;
            conexao = DriverManager.getConnection(url);
        }
        return conexao;
    }

    /** Injeta uma Connection para uso em testes (ex.: in-memory). */
    public synchronized void definirConexaoDeTeste(Connection conn) {
        this.conexao = conn;
    }

    /** Reinicia o singleton para uso em teardown de testes. */
    public static synchronized void reiniciarInstanciaParaTestes() {
        instancia = null;
    }
}

