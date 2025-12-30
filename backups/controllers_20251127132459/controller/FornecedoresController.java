package com.example.erp_.controller;

import com.example.erp_.model.Supplier;
import com.example.erp_.service.SupplierService;
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

public class FornecedoresController {
    @FXML private TableView<Supplier> tabelaFornecedores;
    @FXML private TableColumn<Supplier, Integer> colId;
    @FXML private TableColumn<Supplier, String> colName;
    @FXML private TableColumn<Supplier, String> colCnpj;
    @FXML private TableColumn<Supplier, String> colEmail;
    @FXML private TableColumn<Supplier, String> colContact;
    @FXML private TextField txtBusca;

    private SupplierService servicoFornecedor = new SupplierService();
    private ObservableList<Supplier> fornecedores = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getId()));
        colName.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getName()));
        colCnpj.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getCnpj()));
        colEmail.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getEmail()));
        colContact.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getContact()));

        tabelaFornecedores.setItems(fornecedores);
        carregarTodos();
    }

    private void carregarTodos() {
        try { List<Supplier> list = servicoFornecedor.findAll(); fornecedores.setAll(list); } catch (Exception e) { mostrarErro("Erro ao carregar fornecedores", e); }
    }

    @FXML
    private void onNovo() {
        Supplier s = new Supplier(); boolean salvo = mostrarFormularioFornecedor(s, "Novo Fornecedor"); if (salvo) carregarTodos();
    }

    @FXML
    private void onEditar() {
        Supplier sel = tabelaFornecedores.getSelectionModel().getSelectedItem(); if (sel == null) { mostrarAviso("Selecione um fornecedor para editar."); return; }
        Supplier copy = new Supplier(); copy.setId(sel.getId()); copy.setName(sel.getName()); copy.setCnpj(sel.getCnpj()); copy.setEmail(sel.getEmail()); copy.setContact(sel.getContact()); copy.setAddress(sel.getAddress());
        boolean salvo = mostrarFormularioFornecedor(copy, "Editar Fornecedor"); if (salvo) carregarTodos();
    }

    @FXML
    private void onExcluir() {
        Supplier sel = tabelaFornecedores.getSelectionModel().getSelectedItem(); if (sel == null) { mostrarAviso("Selecione um fornecedor para excluir."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION); confirm.setTitle("Confirmar exclusão"); confirm.setHeaderText("Excluir fornecedor"); confirm.setContentText("Deseja realmente excluir o fornecedor '" + sel.getName() + "'? Esta ação não pode ser desfeita."); Optional<ButtonType> opt = confirm.showAndWait(); if (opt.isPresent() && opt.get()==ButtonType.OK) { try { servicoFornecedor.delete(sel.getId()); carregarTodos(); } catch (Exception e) { mostrarErro("Erro ao excluir fornecedor", e); } }
    }

    @FXML
    private void onPesquisar() {
        String q = txtBusca.getText(); if (q==null||q.trim().isEmpty()) { carregarTodos(); return; } try { List<Supplier> list = servicoFornecedor.searchByName(q); fornecedores.setAll(list); } catch (Exception e) { mostrarErro("Erro na pesquisa", e); }
    }

    private boolean mostrarFormularioFornecedor(Supplier supplier, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/supplier_form.fxml"));
            GridPane pane = loader.load();
            FormularioFornecedorController form = loader.getController(); form.setSupplier(supplier);
            Dialog<ButtonType> dialog = new Dialog<>(); dialog.setTitle(title); dialog.getDialogPane().setContent(pane); ButtonType save = new ButtonType("Salvar", ButtonBar.ButtonData.OK_DONE); dialog.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);
            Node saveBtn = dialog.getDialogPane().lookupButton(save); saveBtn.disableProperty().bind(form.propriedadeValida().not()); Optional<ButtonType> result = dialog.showAndWait(); if (result.isPresent() && result.get()==save) { if (!form.validate()) { mostrarAviso("Corrija os erros"); return false; } Supplier out = form.getSupplier(); out.setId(supplier.getId()); try { if (out.getId()==null) servicoFornecedor.create(out); else servicoFornecedor.update(out); return true; } catch (Exception e) { mostrarErro("Erro ao salvar fornecedor", e); return false; } }
        } catch (IOException e) { mostrarErro("Erro abrir formulário", e); return false; }
        return false;
    }

    private void mostrarErro(String msg, Exception e) { e.printStackTrace(); Alert a=new Alert(Alert.AlertType.ERROR); a.setTitle("Erro"); a.setHeaderText(msg); a.setContentText(e.getMessage()); a.showAndWait(); }
    private void mostrarAviso(String msg) { Alert a=new Alert(Alert.AlertType.WARNING); a.setTitle("Atenção"); a.setHeaderText(msg); a.showAndWait(); }
}

