package com.example.erp_.controller;

import com.example.erp_.model.Product;
import com.example.erp_.service.ProductService;
import com.example.erp_.service.sales.ItemVenda;
import com.example.erp_.service.sales.ServicoVendas;
import com.example.erp_.service.sales.CarrinhoItem;
import com.example.erp_.service.sales.ServicoCarrinho;
import com.example.erp_.patterns.observer.EventBus;
import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class VendasController {
    @FXML private TextField txtBuscaProduto;
    @FXML private TableView<Product> tabelaResultados;
    @FXML private TableColumn<Product,Integer> colResId;
    @FXML private TableColumn<Product,String> colResNome;
    @FXML private TableColumn<Product,Double> colResPreco;
    @FXML private TableColumn<Product,Integer> colResEstoque;
    @FXML private TableColumn<Product,Integer> colResQtde;

    @FXML private TableView<CarrinhoItem> tabelaCarrinho;
    @FXML private TableColumn<CarrinhoItem,String> colCarProduto;
    @FXML private TableColumn<CarrinhoItem,Integer> colCarQtde;
    @FXML private TableColumn<CarrinhoItem,Double> colCarPreco;
    @FXML private TableColumn<CarrinhoItem,Double> colCarSubtotal;
    @FXML private Label lblTotal;
    @FXML private Spinner<Integer> spinnerQuantidade;
    @FXML private Button btnAdicionar;

    private ProductService productService = new ProductService();
    private ServicoVendas servicoVendas = new ServicoVendas();
    private final ServicoCarrinho servicoCarrinho = new ServicoCarrinho(productService);

    private final ObservableList<Product> resultados = FXCollections.observableArrayList();
    private final ObservableList<CarrinhoItem> carrinho = FXCollections.observableArrayList();

    private final java.util.Map<Integer,Integer> quantDesejada = new java.util.HashMap<>();

    private volatile Integer ultimoSaleId = null;
    private volatile CompletableFuture<Integer> saleFuture = null;
    private ContextMenu suggestionMenu = new ContextMenu();

    @FXML
    public void initialize() {
        colResId.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(d.getValue().getId()));
        colResNome.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getName()));
        colResPreco.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(d.getValue().getSalePrice()));
        colResPreco.setCellFactory(tc -> new TableCell<>() { @Override protected void updateItem(Double price, boolean empty) { super.updateItem(price, empty); if (empty||price==null) setText(null); else setText(java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("pt","BR")).format(price)); }});
        colResEstoque.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(d.getValue().getStockQty()));
        colResQtde.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(quantDesejada.getOrDefault(d.getValue().getId(), 1)));
        colResQtde.setCellFactory(tc -> new javafx.scene.control.cell.TextFieldTableCell<>(new javafx.util.converter.IntegerStringConverter()));
        colResQtde.setOnEditCommit(e -> {
            Product p = e.getRowValue();
            int novo = e.getNewValue() == null ? e.getOldValue() : e.getNewValue();
            if (novo < 1) novo = 1;
            quantDesejada.put(p.getId(), novo);
            tabelaResultados.refresh();
            // se produto selecionado, atualizar spinner max e value
            Product sel = tabelaResultados.getSelectionModel().getSelectedItem();
            if (sel != null && sel.getId().equals(p.getId())) atualizarSpinnerParaProduto(sel);
        });

        tabelaResultados.setItems(resultados);

        // inicializar spinner com valor default 1..999
        spinnerQuantidade.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999, 1));
        spinnerQuantidade.setDisable(true);
        btnAdicionar.setDisable(true);

        // ao selecionar produto, ajustar máximo do spinner e habilitar botão
        tabelaResultados.getSelectionModel().selectedItemProperty().addListener((obs, oldS, newS) -> {
            atualizarSpinnerParaProduto(newS);
        });

        colCarProduto.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getNome()));
        colCarQtde.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(d.getValue().getQuantidade()));
        colCarPreco.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(d.getValue().getPrecoUnitario()));
        colCarPreco.setCellFactory(tc -> new TableCell<>() { @Override protected void updateItem(Double price, boolean empty) { super.updateItem(price, empty); if (empty||price==null) setText(null); else setText(java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("pt","BR")).format(price)); }});
        colCarSubtotal.setCellValueFactory(d -> new javafx.beans.property.SimpleObjectProperty<>(d.getValue().getSubtotal()));
        colCarSubtotal.setCellFactory(tc -> new TableCell<>() { @Override protected void updateItem(Double price, boolean empty) { super.updateItem(price, empty); if (empty||price==null) setText(null); else setText(java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("pt","BR")).format(price)); }});

        tabelaCarrinho.setItems(carrinho);
        // tornar coluna Qtde editável
        tabelaCarrinho.setEditable(true);
        colCarQtde.setCellFactory(tc -> new javafx.scene.control.cell.TextFieldTableCell<>(new javafx.util.converter.IntegerStringConverter()));
        colCarQtde.setOnEditCommit(e -> {
            CarrinhoItem it = e.getRowValue();
            int novo = e.getNewValue() == null ? e.getOldValue() : e.getNewValue();
            if (novo < 1) novo = 1;
            try {
                servicoCarrinho.atualizarQuantidade(it.getProdutoId(), novo);
                refreshCarrinhoFromService();
                atualizarTotal();
                Product sel = tabelaResultados.getSelectionModel().getSelectedItem();
                if (sel != null && sel.getId().equals(it.getProdutoId())) atualizarSpinnerParaProduto(sel);
            } catch (Exception ex) {
                mostrarAviso("Não foi possível atualizar quantidade: " + ex.getMessage());
                refreshCarrinhoFromService();
            }
        });

        atualizarTotal();

        // inscrever para receber evento de venda concluída
        EventBus.getInstance().subscribe("sale.completed", payload -> {
            if (payload instanceof Integer) {
                ultimoSaleId = (Integer) payload;
                CompletableFuture<Integer> f = saleFuture;
                if (f != null && !f.isDone()) f.complete(ultimoSaleId);
            }
        });

        // debounce live search (autocomplete-like)
        PauseTransition pause = new PauseTransition(Duration.millis(300));
        txtBuscaProduto.textProperty().addListener((obs, oldV, newV) -> {
            pause.stop();
            pause.setOnFinished(e -> {
                // apenas busca quando tiver 2+ chars ou vazio (lista completa)
                if (newV == null || newV.trim().length() < 2) {
                    // opcional: limpar resultados se vazio
                    if (newV == null || newV.trim().isEmpty()) Platform.runLater(() -> resultados.clear());
                    return;
                }
                Platform.runLater(() -> onBuscarProduto());
            });
            pause.playFromStart();
        });

        // Enter em campo busca realiza busca
        txtBuscaProduto.setOnKeyPressed(ev -> {
            if (ev.getCode() == KeyCode.ENTER) {
                onBuscarProduto();
                ev.consume();
            }
        });

        // registrar accelerators quando a scene estiver disponível
        tabelaResultados.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                // Ctrl+Enter -> adicionar
                newScene.getAccelerators().put(new KeyCodeCombination(KeyCode.ENTER, KeyCombination.CONTROL_DOWN), () -> Platform.runLater(() -> onAdicionarCarrinho()));
                // F12 -> finalizar
                newScene.getAccelerators().put(new KeyCodeCombination(KeyCode.F12), () -> Platform.runLater(() -> onFinalizarVenda()));
                // Ctrl+F -> focar busca
                newScene.getAccelerators().put(new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN), () -> Platform.runLater(() -> txtBuscaProduto.requestFocus()));
            }
        });

        // esconder suggestions quando foco perdido
        txtBuscaProduto.focusedProperty().addListener((obs, was, isNow) -> { if (!isNow) suggestionMenu.hide(); });
    }

    // calcula quantas unidades do produto já estão no carrinho
    private int quantidadeNoCarrinho(Integer produtoId) {
        int sum = 0;
        for (CarrinhoItem it : carrinho) if (it.getProdutoId().equals(produtoId)) sum += it.getQuantidade();
        return sum;
    }

    // atualiza o spinner (min/max) de acordo com o produto selecionado e o que já está no carrinho
    private void atualizarSpinnerParaProduto(Product p) {
        if (p == null) {
            spinnerQuantidade.setDisable(true);
            btnAdicionar.setDisable(true);
            return;
        }
        int estoque = p.getStockQty();
        int jaNoCarrinho = quantidadeNoCarrinho(p.getId());
        int disponivel = Math.max(0, estoque - jaNoCarrinho);
        if (disponivel <= 0) {
            spinnerQuantidade.setDisable(true);
            btnAdicionar.setDisable(true);
            spinnerQuantidade.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1, 1));
        } else {
            spinnerQuantidade.setDisable(false);
            btnAdicionar.setDisable(false);
            // prefer quantity desired in results if present
            int desired = quantDesejada.getOrDefault(p.getId(), -1);
            int current = spinnerQuantidade.getValue() == null ? 1 : spinnerQuantidade.getValue();
            int valor = Math.min(current, disponivel);
            if (desired > 0) valor = Math.min(desired, disponivel);
            spinnerQuantidade.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, disponivel, Math.max(1, valor)));
        }
    }

    @FXML
    private void onBuscarProduto() {
        String q = txtBuscaProduto.getText();
        try {
            List<Product> list = productService.search(q);
            resultados.setAll(list);
            buildSuggestions(list);
        } catch (Exception e) { e.printStackTrace(); }
    }

    // constrói sugestões no ContextMenu abaixo do campo de busca
    private void buildSuggestions(List<Product> list) {
        suggestionMenu.getItems().clear();
        if (list == null || list.isEmpty()) { suggestionMenu.hide(); return; }
        int limit = Math.min(8, list.size());
        for (int i = 0; i < limit; i++) {
            Product p = list.get(i);
            MenuItem mi = new MenuItem(p.getName() + " (" + p.getSku() + ")");
            mi.setOnAction(ev -> {
                txtBuscaProduto.setText(p.getName());
                // selecionar produto na tabela de resultados
                tabelaResultados.getSelectionModel().select(p);
                tabelaResultados.scrollTo(p);
                atualizarSpinnerParaProduto(p);
                suggestionMenu.hide();
            });
            suggestionMenu.getItems().add(mi);
        }
        if (!suggestionMenu.isShowing()) {
            suggestionMenu.show(txtBuscaProduto, Side.BOTTOM, 0, 0);
        }
    }

    // sincroniza ObservableList usado pela TableView com o serviço de carrinho
    private void refreshCarrinhoFromService() {
        carrinho.setAll(servicoCarrinho.getItens());
    }

    private void atualizarTotal() { double total = 0.0; for (CarrinhoItem it : carrinho) total += it.getSubtotal(); lblTotal.setText(java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("pt","BR")).format(total)); }

    private void mostrarAviso(String msg) { Alert a = new Alert(Alert.AlertType.WARNING); a.setTitle("Atenção"); a.setHeaderText(msg); a.showAndWait(); }
    private void mostrarInfo(String msg) { Alert a = new Alert(Alert.AlertType.INFORMATION); a.setTitle("Informação"); a.setHeaderText(msg); a.showAndWait(); }
    private void mostrarErro(String msg, Exception e) { e.printStackTrace(); Alert a = new Alert(Alert.AlertType.ERROR); a.setTitle("Erro"); a.setHeaderText(msg); a.setContentText(e.getMessage()); a.showAndWait(); }

    @FXML
    private void onAdicionarCarrinho() {
        Product sel = tabelaResultados.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarAviso("Selecione um produto"); return; }
        int qtd = quantDesejada.getOrDefault(sel.getId(), -1);
        if (qtd <= 0) {
            try { qtd = spinnerQuantidade.getValue(); } catch (Exception ex) { qtd = 1; }
        }
        try {
            servicoCarrinho.adicionar(sel.getId(), qtd);
            refreshCarrinhoFromService();
            atualizarTotal();
            atualizarSpinnerParaProduto(sel);
            quantDesejada.remove(sel.getId()); tabelaResultados.refresh();
        } catch (Exception ex) {
            mostrarAviso("Não foi possível adicionar ao carrinho: " + ex.getMessage());
        }
    }

    @FXML
    private void onRemoverItem() {
        CarrinhoItem sel = tabelaCarrinho.getSelectionModel().getSelectedItem(); if (sel==null) return; Integer produtoId = sel.getProdutoId(); servicoCarrinho.remover(produtoId); refreshCarrinhoFromService(); atualizarTotal();
        Product selProd = tabelaResultados.getSelectionModel().getSelectedItem(); if (selProd != null && selProd.getId().equals(produtoId)) atualizarSpinnerParaProduto(selProd);
    }

    @FXML
    private void onFinalizarVenda() {
        if (carrinho.isEmpty()) { mostrarAviso("Carrinho vazio"); return; }
        List<ItemVenda> itens = new ArrayList<>(); for (CarrinhoItem it : servicoCarrinho.getItens()) itens.add(new ItemVenda(it.getProdutoId(), it.getQuantidade()));

        Task<Void> task = new Task<>() {
            @Override protected Void call() throws Exception {
                servicoVendas.realizarVenda(itens);
                return null;
            }
        };
        task.setOnSucceeded(ev -> {
            saleFuture = new CompletableFuture<>();
            try {
                Integer saleId = saleFuture.get(5, TimeUnit.SECONDS);
                mostrarRecibo(saleId, itens);
            } catch (TimeoutException te) {
                mostrarInfo("Venda realizada com sucesso");
            } catch (Exception ex) {
                mostrarInfo("Venda realizada com sucesso");
            } finally {
                saleFuture = null;
                servicoCarrinho.limpar(); refreshCarrinhoFromService(); resultados.clear(); atualizarTotal();
            }
        });
        task.setOnFailed(ev -> {
            Throwable ex = task.getException(); mostrarErro("Erro ao finalizar venda", (Exception) (ex instanceof Exception ? ex : new Exception(ex)) );
        });
        new Thread(task).start();
    }

    private void mostrarRecibo(Integer saleId, List<ItemVenda> itens) {
        List<String> lines = com.example.erp_.utils.ReceiptBuilder.buildReceiptLines(saleId, itens, productService);
        String content = String.join("\n", lines);

        try {
            java.nio.file.Path outDir = java.nio.file.Paths.get("target", "recibos");
            java.nio.file.Files.createDirectories(outDir);
            java.nio.file.Path pdfPath = outDir.resolve("recibo-" + saleId + ".pdf");

            try (org.apache.pdfbox.pdmodel.PDDocument doc = new org.apache.pdfbox.pdmodel.PDDocument()) {
                org.apache.pdfbox.pdmodel.PDPage page = new org.apache.pdfbox.pdmodel.PDPage();
                doc.addPage(page);
                try (org.apache.pdfbox.pdmodel.PDPageContentStream cs = new org.apache.pdfbox.pdmodel.PDPageContentStream(doc, page)) {
                    org.apache.pdfbox.pdmodel.font.PDType1Font font = org.apache.pdfbox.pdmodel.font.PDType1Font.COURIER;
                    float fontSize = 10;
                    float leading = 12f;
                    float margin = 40f;
                    float startY = page.getMediaBox().getHeight() - margin;

                    cs.beginText();
                    cs.setFont(font, fontSize);
                    cs.newLineAtOffset(margin, startY);

                    for (String line : lines) {
                        cs.showText(line);
                        cs.newLineAtOffset(0, -leading);
                    }

                    cs.endText();
                }
                doc.save(pdfPath.toFile());
            }

            try {
                if (java.awt.Desktop.isDesktopSupported()) {
                    java.awt.Desktop.getDesktop().open(pdfPath.toFile());
                } else {
                    Alert a = new Alert(Alert.AlertType.INFORMATION);
                    a.setTitle("Recibo gerado"); a.setHeaderText("Recibo salvo em:"); a.setContentText(pdfPath.toAbsolutePath().toString()); a.showAndWait();
                }
            } catch (Exception e) {
                Alert a = new Alert(Alert.AlertType.INFORMATION);
                a.setTitle("Recibo gerado"); a.setHeaderText("Recibo salvo em:"); a.setContentText(pdfPath.toAbsolutePath().toString()); a.showAndWait();
            }

        } catch (Exception e) {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Recibo"); a.setHeaderText("Venda registrada: " + saleId); a.setContentText(content); a.showAndWait();
        }
    }
}
