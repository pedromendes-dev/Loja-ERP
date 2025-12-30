package com.example.erp_.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import com.example.erp_.security.Session;
import javafx.stage.Stage;

public class ControladorPrincipal {
    @FXML
    private StackPane painelConteudo;

    @FXML
    public void initialize() {
        abrirPainel();
    }

    @FXML
    private void abrirPainel() { carregarConteudo("/fxml/dashboard_panel.fxml"); }

    @FXML
    private void abrirClientes() { carregarConteudo("/fxml/clients.fxml"); }

    @FXML
    private void abrirProdutos() { carregarConteudo("/fxml/products.fxml"); }

    @FXML
    private void abrirVendas() { carregarConteudo("/fxml/sales.fxml"); }

    @FXML
    private void abrirRelatorios() { carregarConteudo("/fxml/reports.fxml"); }

    @FXML
    private void sair() {
        Session.getInstance().clear();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Stage stage = (Stage) painelConteudo.getScene().getWindow();
            stage.getScene().setRoot(loader.load());
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void carregarConteudo(String recurso) {
        try {
            Node n = FXMLLoader.load(getClass().getResource(recurso));
            painelConteudo.getChildren().setAll(n);
        } catch (Exception e) { e.printStackTrace(); }
    }
}
