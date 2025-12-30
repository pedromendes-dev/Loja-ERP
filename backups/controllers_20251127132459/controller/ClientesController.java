package com.example.erp_.controller;

import com.example.erp_.model.Client;
import com.example.erp_.service.ClientService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Controlador de clientes: gerencia listagem, busca, CRUD e interações.
 */
public class ClientesController {
    @FXML private TableView<Client> tabelaClientes;
    @FXML private TableColumn<Client, Integer> colId;
    @FXML private TableColumn<Client, String> colName;
    @FXML private TableColumn<Client, String> colCpf;
    @FXML private TableColumn<Client, String> colEmail;
    @FXML private TableColumn<Client, String> colPhone;
    @FXML private TextField txtBusca;

    private ClientService servicoCliente = new ClientService();
    private ObservableList<Client> clientes = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getId()));
        colName.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getName()));
        colCpf.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getCpfCnpj()));
        colEmail.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getEmail()));
        colPhone.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getPhone()));

        tabelaClientes.setItems(clientes);
        carregarTodos();
    }

    private void carregarTodos() {
        try {
            List<Client> list = servicoCliente.findAll();
            clientes.setAll(list);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void onNovo() {
        Client c = new Client();
        boolean salvo = mostrarFormularioCliente(c, "Novo Cliente"); if (salvo) carregarTodos();
    }

    @FXML
    private void onEditar() {
        Client sel = tabelaClientes.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarAviso("Selecione um cliente para editar."); return; }
        Client copy = new Client(); copy.setId(sel.getId()); copy.setName(sel.getName()); copy.setCpfCnpj(sel.getCpfCnpj()); copy.setEmail(sel.getEmail()); copy.setPhone(sel.getPhone()); copy.setAddress(sel.getAddress());
        boolean salvo = mostrarFormularioCliente(copy, "Editar Cliente"); if (salvo) carregarTodos();
    }

    @FXML
    private void onExcluir() {
        Client sel = tabelaClientes.getSelectionModel().getSelectedItem(); if (sel == null) { mostrarAviso("Selecione um cliente para excluir."); return; }
        Alert a = new Alert(Alert.AlertType.CONFIRMATION); a.setTitle("Confirmar exclusão"); a.setHeaderText("Excluir cliente"); a.setContentText("Deseja realmente excluir o cliente '" + sel.getName() + "'? Esta ação não pode ser desfeita."); Optional<ButtonType> opt = a.showAndWait(); if (opt.isPresent() && opt.get()==ButtonType.OK) { try { servicoCliente.delete(sel.getId()); carregarTodos(); } catch (Exception e) { mostrarErro("Erro ao excluir cliente", e); } }
    }

    @FXML
    private void onPesquisar() {
        String q = txtBusca.getText(); if (q==null||q.trim().isEmpty()) { carregarTodos(); return; }
        try { List<Client> list = servicoCliente.searchByName(q); clientes.setAll(list); } catch (Exception e) { mostrarErro("Erro na pesquisa", e); }
    }

    private boolean mostrarFormularioCliente(Client client, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/client_form.fxml"));
            GridPane pane = loader.load();
            FormularioClienteController form = loader.getController(); form.setClient(client);
            Dialog<ButtonType> dialog = new Dialog<>(); dialog.setTitle(title); dialog.getDialogPane().setContent(pane); ButtonType save = new ButtonType("Salvar", ButtonBar.ButtonData.OK_DONE); dialog.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);
            Node saveBtn = dialog.getDialogPane().lookupButton(save); saveBtn.disableProperty().bind(form.propriedadeValida().not()); Optional<ButtonType> result = dialog.showAndWait(); if (result.isPresent() && result.get()==save) { if (!form.validate()) { mostrarAviso("Corrija os erros"); return false; } Client out = form.getClient(); out.setId(client.getId()); try { if (out.getId()==null) servicoCliente.create(out); else servicoCliente.update(out); return true; } catch (Exception e) { mostrarErro("Erro ao salvar cliente", e); return false; } }
        } catch (IOException e) { mostrarErro("Erro abrir formulário", e); return false; }
        return false;
    }

    private void mostrarErro(String msg, Exception e) { e.printStackTrace(); Alert a=new Alert(Alert.AlertType.ERROR); a.setTitle("Erro"); a.setHeaderText(msg); a.setContentText(e.getMessage()); a.showAndWait(); }
    private void mostrarAviso(String msg) { Alert a=new Alert(Alert.AlertType.WARNING); a.setTitle("Atenção"); a.setHeaderText(msg); a.showAndWait(); }
}
