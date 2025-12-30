package com.example.erp_.controller;

import com.example.erp_.model.Client;
import com.example.erp_.model.Supplier;
import com.example.erp_.patterns.observer.EventBus;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;

public class PainelDashboardController implements Initializable {
    @FXML private Label lblQuantidadeClientes;
    @FXML private Label lblQuantidadeFornecedores;

    private final AtomicInteger clientes = new AtomicInteger(0);
    private final AtomicInteger fornecedores = new AtomicInteger(0);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        EventBus.getInstance().subscribe("client.created", payload -> onEventoCliente(payload));
        EventBus.getInstance().subscribe("client.deleted", payload -> onEventoCliente(payload));
        EventBus.getInstance().subscribe("supplier.created", payload -> onEventoFornecedor(payload));
        EventBus.getInstance().subscribe("supplier.deleted", payload -> onEventoFornecedor(payload));
    }

    private void onEventoCliente(Object payload) {
        if (payload instanceof Client) clientes.incrementAndGet();
        else if (payload instanceof Integer) clientes.decrementAndGet();
        atualizarUi();
    }

    private void onEventoFornecedor(Object payload) {
        if (payload instanceof Supplier) fornecedores.incrementAndGet();
        else if (payload instanceof Integer) fornecedores.decrementAndGet();
        atualizarUi();
    }

    private void atualizarUi() {
        Platform.runLater(() -> {
            lblQuantidadeClientes.setText(String.valueOf(clientes.get()));
            lblQuantidadeFornecedores.setText(String.valueOf(fornecedores.get()));
        });
    }
}

