package com.example.erp.controller;

import com.example.erp_.model.Client;
import com.example.erp_.model.Supplier;
import com.example.erp_.patterns.observer.EventBus;
import com.example.erp.service.ProductService;
import com.example.erp_.service.ClientService;
import com.example.erp.service.sales.SalesService;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.Node;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;
import com.example.erp_.config.DbManager;

public class PainelDashboardController implements Initializable {
    @FXML private Label lblQuantidadeClientes;
    @FXML private Label lblQuantidadeFornecedores;
    @FXML private Label lblQuantidadeProdutos;
    @FXML private Label lblQuantidadeVendas;
    @FXML private ListView<String> lstAtividades;

    private final AtomicInteger clientes = new AtomicInteger(0);
    private final AtomicInteger fornecedores = new AtomicInteger(0);
    private final AtomicInteger produtos = new AtomicInteger(0);
    private final AtomicInteger vendas = new AtomicInteger(0);

    private final ClientService clientService = new ClientService();
    private final ProductService productService = new ProductService();
    private final SalesService salesService = new SalesService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // initialize counts from DB
        refreshCounts();
        // subscribe to events
        EventBus.getInstance().subscribe("client.created", payload -> { clientes.incrementAndGet(); pushActivity("Cliente criado: " + ((Client)payload).getName()); atualizarUi(); });
        EventBus.getInstance().subscribe("client.deleted", payload -> { clientes.decrementAndGet(); atualizarUi(); });
        EventBus.getInstance().subscribe("supplier.created", payload -> { fornecedores.incrementAndGet(); atualizarUi(); });
        EventBus.getInstance().subscribe("supplier.deleted", payload -> { fornecedores.decrementAndGet(); atualizarUi(); });
        EventBus.getInstance().subscribe("product.created", payload -> { produtos.incrementAndGet(); atualizarUi(); });
        EventBus.getInstance().subscribe("sale.completed", payload -> { vendas.incrementAndGet(); pushActivity("Venda concluída: #" + payload); atualizarUi(); });
    }

    private void pushActivity(String text) {
        Platform.runLater(() -> {
            lstAtividades.getItems().add(0, text);
            if (lstAtividades.getItems().size() > 50) lstAtividades.getItems().remove(50);
        });
    }

    private void refreshCounts() {
        Platform.runLater(() -> {
            try {
                clientes.set(clientService.findAll().size());
            } catch (Exception e) { clientes.set(0); }
            try {
                produtos.set(productService.findAll().size());
            } catch (Exception e) { produtos.set(0); }
            // vendas: count rows in sales table
            try {
                Connection conn = DbManager.getInstance().getConnection();
                try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM sales")) {
                    try (ResultSet rs = ps.executeQuery()) { if (rs.next()) vendas.set(rs.getInt(1)); else vendas.set(0); }
                }
            } catch (Exception e) { vendas.set(0); }
            // fornecedores
            try {
                fornecedores.set(new com.example.erp_.service.SupplierService().findAll().size());
            } catch (Exception e) { fornecedores.set(0); }
            atualizarUi();
        });
    }

    private void atualizarUi() {
        Platform.runLater(() -> {
            lblQuantidadeClientes.setText(String.valueOf(clientes.get()));
            lblQuantidadeFornecedores.setText(String.valueOf(fornecedores.get()));
            lblQuantidadeProdutos.setText(String.valueOf(produtos.get()));
            lblQuantidadeVendas.setText(String.valueOf(vendas.get()));
        });
    }

    @FXML
    private void onRefreshDashboard() { refreshCounts(); pushActivity("Painel atualizado"); }

    @FXML
    private void onOpenClients() { navegarPara("/fxml/clients.fxml"); }
    @FXML
    private void onOpenProducts() { navegarPara("/fxml/products.fxml"); }
    @FXML
    private void onOpenSales() { navegarPara("/fxml/sales.fxml"); }

    @FXML
    private void onNovoClienteQuick() { navegarPara("/fxml/clients.fxml"); Platform.runLater(() -> {/* UI will offer New */}); }
    @FXML
    private void onNovoProdutoQuick() { navegarPara("/fxml/products.fxml"); }
    @FXML
    private void onNovaVendaQuick() { navegarPara("/fxml/sales.fxml"); }

    private void navegarPara(String recurso) {
        try {
            // find top-level stack pane from main controller (hack: use MainApp to get primary stage root)
            javafx.scene.Parent root = FXMLLoader.load(getClass().getResource(recurso));
            // attempt to set into main content area by locating painelConteudo if available
            // fallback: show in dialog
            // Note: ControladorPrincipal handles setRoot; here we'll attempt simple dialog
            Dialog<ButtonType> d = new Dialog<>(); d.setTitle("Carregar " + recurso); d.getDialogPane().setContent(root); d.getDialogPane().getButtonTypes().add(ButtonType.CLOSE); d.showAndWait();
        } catch (Exception e) { e.printStackTrace(); }
    }
}
