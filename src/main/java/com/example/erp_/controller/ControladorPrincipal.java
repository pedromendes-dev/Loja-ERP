package com.example.erp_.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;
import javafx.scene.control.TextField;
import com.example.erp_.security.Session;
import javafx.stage.Stage;
import com.example.ERP.tools.ReportsPackager;

public class ControladorPrincipal {
    @FXML
    private StackPane painelConteudo;

    @FXML
    private TextField txtHeaderSearch; // added to bind the header search field from main.fxml

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

    @FXML
    private void gerarRelatoriosAction() {
        // run report generation in background
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                ReportsPackager.generateAll();
                ReportsPackager.zipExports();
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                Alert a = new Alert(Alert.AlertType.INFORMATION);
                a.setTitle("Relatórios gerados");
                a.setHeaderText(null);
                a.setContentText("Relatórios gerados e empacotados em data/exports/reports.zip");
                a.showAndWait();
            });
        });
        task.setOnFailed(e -> {
            Platform.runLater(() -> {
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setTitle("Erro ao gerar relatórios");
                a.setHeaderText(null);
                a.setContentText("Falha: " + task.getException().getMessage());
                a.showAndWait();
            });
        });
        new Thread(task, "reports-generator").start();
    }

    private void carregarConteudo(String recurso) {
        try {
            Node n = FXMLLoader.load(getClass().getResource(recurso));
            painelConteudo.getChildren().setAll(n);
        } catch (Exception e) { e.printStackTrace(); }
    }
}
