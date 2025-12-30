package com.example.erp_;

import com.example.erp_.config.GerenciadorBancoDados;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DbInitTest {

    @Test
    public void testDbInitializationCreatesFile() throws Exception {
        GerenciadorBancoDados.obterInstancia().inicializar();
        Path dbPath = Paths.get(System.getProperty("user.home"), ".minierp", "minierp.db");
        assertTrue(Files.exists(dbPath), "Banco de dados não foi criado em: " + dbPath);
    }
}
