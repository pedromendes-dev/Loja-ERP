package com.example.erp.controller;

import com.example.erp.model.Product;
import com.example.erp.patterns.observer.EventBus;
import com.example.erp.service.ProductService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Controlador de produtos: gerencia listagem, busca, CRUD e interações com o EventBus.
 */
public class ProdutosController {
    @FXML private TableView<Product> tabelaProdutos;
    @FXML private TableColumn<Product,Integer> colId;
    @FXML private TableColumn<Product,String> colSku;
    @FXML private TableColumn<Product,String> colName;
    @FXML private TableColumn<Product,Double> colPrice;
    @FXML private TableColumn<Product,Integer> colQty;
    @FXML private TableColumn<Product,Integer> colMin;
    @FXML private TextField txtBusca;

    private ProductService servicoProdutos = new ProductService();
    private final ObservableList<Product> mestre = FXCollections.observableArrayList();
    private FilteredList<Product> filtrado;

    private final Consumer<Object> onCriado = payload -> atualizarAsync();
    private final Consumer<Object> onAtualizado = payload -> atualizarAsync();
    private final Consumer<Object> onDeletado = payload -> atualizarAsync();
    private final Consumer<Object> onEstoqueAlterado = payload -> atualizarAsync();
    private final Consumer<Object> onEstoqueBaixo = payload -> atualizarAsync();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(d.getValue().getId()));
        colSku.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getSku()));
        colName.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getName()));
        NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        colPrice.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(d.getValue().getSalePrice()));
        colPrice.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) setText(null); else setText(currency.format(price));
            }
        });

        colQty.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(d.getValue().getStockQty()));
        colMin.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(d.getValue().getMinStock()));

        filtrado = new FilteredList<>(mestre, p -> true);
        SortedList<Product> sorted = new SortedList<>(filtrado);
        sorted.comparatorProperty().bind(tabelaProdutos.comparatorProperty());
        tabelaProdutos.setItems(sorted);

        if (txtBusca != null) {
            txtBusca.textProperty().addListener((obs, oldV, newV) -> {
                String term = newV == null ? "" : newV.trim().toLowerCase();
                if (term.isEmpty()) {
                    filtrado.setPredicate(p -> true);
                } else {
                    filtrado.setPredicate(p -> {
                        if (p == null) return false;
                        String name = p.getName() == null ? "" : p.getName().toLowerCase();
                        String sku = p.getSku() == null ? "" : p.getSku().toLowerCase();
                        return name.contains(term) || sku.contains(term);
                    });
                }
            });
        }

        // destaca linhas com estoque baixo
        tabelaProdutos.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().remove("low-stock");
                if (!empty && item != null) {
                    int min = item.getMinStock();
                    if (item.getStockQty() <= min) {
                        if (!getStyleClass().contains("low-stock")) getStyleClass().add("low-stock");
                    }
                }
            }
        });

        EventBus.getInstance().subscribe("product.created", onCriado);
        EventBus.getInstance().subscribe("product.updated", onAtualizado);
        EventBus.getInstance().subscribe("product.deleted", onDeletado);
        EventBus.getInstance().subscribe("product.stockchanged", onEstoqueAlterado);
        EventBus.getInstance().subscribe("product.lowstock", onEstoqueBaixo);

        tabelaProdutos.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (oldScene != null && oldScene.getWindow() != null) oldScene.getWindow().setOnHidden(null);
            if (newScene != null) {
                newScene.getWindow().setOnHidden(e -> {
                    EventBus.getInstance().unsubscribe("product.created", onCriado);
                    EventBus.getInstance().unsubscribe("product.updated", onAtualizado);
                    EventBus.getInstance().unsubscribe("product.deleted", onDeletado);
                    EventBus.getInstance().unsubscribe("product.stockchanged", onEstoqueAlterado);
                    EventBus.getInstance().unsubscribe("product.lowstock", onEstoqueBaixo);
                });
            }
        });

        carregarTodos();
    }

    private void atualizarAsync() { javafx.application.Platform.runLater(this::carregarTodos); }

    private void carregarTodos() {
        try {
            List<Product> list = servicoProdutos.findAll();
            mestre.setAll(list);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void onNovo() {
        Product p = new Product();
        p.setStockQty(0);
        p.setMinStock(1);
        boolean salvo = mostrarFormularioProduto(p, "Novo Produto");
        if (salvo) carregarTodos();
    }

    @FXML
    private void onEditar() {
        Product sel = tabelaProdutos.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarAviso("Selecione um produto para editar."); return; }
        Product copy = new Product();
        copy.setId(sel.getId()); copy.setSku(sel.getSku()); copy.setName(sel.getName()); copy.setSalePrice(sel.getSalePrice()); copy.setStockQty(sel.getStockQty()); copy.setMinStock(sel.getMinStock()); copy.setDescription(sel.getDescription());
        boolean salvo = mostrarFormularioProduto(copy, "Editar Produto"); if (salvo) carregarTodos();
    }

    @FXML
    private void onExcluir() {
        Product sel = tabelaProdutos.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarAviso("Selecione um produto para excluir."); return; }
        Alert c = new Alert(Alert.AlertType.CONFIRMATION);
        c.setTitle("Confirmar"); c.setHeaderText("Excluir produto"); c.setContentText("Excluir? " + sel.getName());
        Optional<ButtonType> opt = c.showAndWait();
        if (opt.isPresent() && opt.get()==ButtonType.OK) {
            try { servicoProdutos.delete(sel.getId()); carregarTodos(); } catch (Exception e) { mostrarErro("Erro ao excluir", e); }
        }
    }

    @FXML
    private void onAjustarEstoque() {
        Product sel = tabelaProdutos.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarAviso("Selecione um produto para ajustar estoque."); return; }
        TextInputDialog d = new TextInputDialog("1"); d.setTitle("Ajustar Estoque"); d.setHeaderText("Informe quantidade (+ para entrada, - para saída)"); Optional<String> res = d.showAndWait(); if (res.isPresent()) {
            try { int delta = Integer.parseInt(res.get()); servicoProdutos.adjustStock(sel.getId(), delta); carregarTodos(); } catch (Exception e) { mostrarErro("Erro ajuste", e); }
        }
    }

    @FXML
    private void onPesquisar() {
        String q = txtBusca.getText();
        if (q==null||q.trim().isEmpty()) { carregarTodos(); return; }
        try {
            List<Product> list = servicoProdutos.search(q);
            mestre.setAll(list);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private boolean mostrarFormularioProduto(Product p, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/product_form.fxml"));
            GridPane pane = loader.load();
            FormularioProdutoController form = loader.getController();
            form.setProduct(p);
            Dialog<ButtonType> dialog = new Dialog<>(); dialog.setTitle(title); dialog.getDialogPane().setContent(pane); ButtonType save = new ButtonType("Salvar", ButtonBar.ButtonData.OK_DONE); dialog.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);
            Node saveBtn = dialog.getDialogPane().lookupButton(save); saveBtn.disableProperty().bind(form.propriedadeValida().not()); Optional<ButtonType> result = dialog.showAndWait(); if (result.isPresent() && result.get()==save) { if (!form.validate()) { mostrarAviso("Corrija os erros"); return false; } Product out = form.getProduct(); out.setId(p.getId()); try { if (out.getId()==null) servicoProdutos.create(out); else servicoProdutos.update(out); return true; } catch (Exception e) { mostrarErro("Erro ao salvar produto", e); return false; } }
        } catch (IOException e) { mostrarErro("Erro abrir formulário", e); return false; }
        return false;
    }

    private void mostrarErro(String msg, Exception e) { e.printStackTrace(); Alert a=new Alert(Alert.AlertType.ERROR); a.setTitle("Erro"); a.setHeaderText(msg); a.setContentText(e.getMessage()); a.showAndWait(); }
    private void mostrarAviso(String msg) { Alert a=new Alert(Alert.AlertType.WARNING); a.setTitle("Atenção"); a.setHeaderText(msg); a.showAndWait(); }
}
