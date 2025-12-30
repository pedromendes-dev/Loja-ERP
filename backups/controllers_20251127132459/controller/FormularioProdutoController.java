package com.example.erp_.controller;

import com.example.erp_.model.Product;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Formulário de produto: parsing tolerante, validação e mensagens em português.
 */
public class FormularioProdutoController implements Initializable {
    @FXML private TextField txtSku;
    @FXML private TextField txtName;
    @FXML private TextArea txtDescription;
    @FXML private TextField txtPrecoVenda;
    @FXML private TextField txtQuantidade;
    @FXML private TextField txtEstoqueMinimo;
    @FXML private Label lblErro;

    private Product produto;
    private final BooleanProperty valido = new SimpleBooleanProperty(false);

    public void setProduct(Product p) {
        this.produto = p;
        if (p != null) {
            txtSku.setText(p.getSku());
            txtName.setText(p.getName());
            txtDescription.setText(p.getDescription());
            txtPrecoVenda.setText(p.getSalePrice() != 0 ? NumberFormat.getNumberInstance(new Locale("pt","BR")).format(p.getSalePrice()) : "");
            txtQuantidade.setText(String.valueOf(p.getStockQty()));
            txtEstoqueMinimo.setText(String.valueOf(p.getMinStock()));
        }
        valido.set(validate());
    }

    public Product getProduct() {
        if (produto == null) produto = new Product();
        produto.setSku(txtSku.getText());
        produto.setName(txtName.getText());
        produto.setDescription(txtDescription.getText());

        double preco = 0.0;
        String precoText = txtPrecoVenda.getText();
        if (precoText != null && !precoText.trim().isEmpty()) {
            try { preco = parseDoubleFromPtBR(precoText.trim()); } catch (NumberFormatException e) { /* mantem 0 */ }
        }

        int qtd = 0;
        String qtdText = txtQuantidade.getText();
        if (qtdText != null && !qtdText.trim().isEmpty()) {
            try { NumberFormat nf = NumberFormat.getIntegerInstance(new Locale("pt", "BR")); Number n = nf.parse(qtdText.trim()); qtd = n.intValue(); } catch (Exception e) { try { qtd = Integer.parseInt(qtdText.trim()); } catch (NumberFormatException ex) { /* leave 0 */ } }
        }

        int min = 0;
        String minText = txtEstoqueMinimo.getText();
        if (minText != null && !minText.trim().isEmpty()) {
            try { NumberFormat nf = NumberFormat.getIntegerInstance(new Locale("pt", "BR")); Number n = nf.parse(minText.trim()); min = n.intValue(); } catch (Exception e) { try { min = Integer.parseInt(minText.trim()); } catch (NumberFormatException ex) { /* leave 0 */ } }
        }

        produto.setSalePrice(preco);
        produto.setStockQty(qtd);
        produto.setMinStock(min);
        return produto;
    }

    public BooleanProperty propriedadeValida(){ return valido; }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        txtSku.textProperty().addListener((o,ov,nv) -> valido.set(validate()));
        txtName.textProperty().addListener((o,ov,nv) -> valido.set(validate()));
        txtPrecoVenda.textProperty().addListener((o,ov,nv) -> valido.set(validate()));
        txtQuantidade.textProperty().addListener((o,ov,nv) -> valido.set(validate()));
        txtEstoqueMinimo.textProperty().addListener((o,ov,nv) -> valido.set(validate()));
        // aplicar máscara de moeda
        com.example.erp_.utils.MaskUtils.applyMoneyMask(txtPrecoVenda);
        valido.set(false);
    }

    public boolean validate() {
        List<String> errors = new ArrayList<>();
        if (txtName.getText() == null || txtName.getText().trim().isEmpty()) errors.add("Nome obrigatório");

        String precoText = txtPrecoVenda.getText();
        if (precoText == null || precoText.trim().isEmpty()) {
            errors.add("Preço obrigatório");
        } else {
            try { com.example.erp_.utils.NumberUtils.parseDoubleFromPtBR(precoText.trim()); } catch (NumberFormatException e) { errors.add("Preço inválido: '" + precoText + "'"); }
        }

        String qtdText = txtQuantidade.getText();
        if (qtdText != null && !qtdText.trim().isEmpty()) {
            try { NumberFormat.getIntegerInstance(new Locale("pt", "BR")).parse(qtdText.trim()); } catch (Exception e) { try { Integer.parseInt(qtdText.trim()); } catch (Exception ex) { errors.add("Quantidade inválida: '" + qtdText + "'"); } }
        }

        String minText = txtEstoqueMinimo.getText();
        if (minText != null && !minText.trim().isEmpty()) {
            try { NumberFormat.getIntegerInstance(new Locale("pt", "BR")).parse(minText.trim()); } catch (Exception e) { try { Integer.parseInt(minText.trim()); } catch (Exception ex) { errors.add("Estoque mínimo inválido: '" + minText + "'"); } }
        }

        if (errors.isEmpty()) { lblErro.setText(""); return true; } else { lblErro.setText(String.join("; ", errors)); return false; }
    }

    private double parseDoubleFromPtBR(String text) throws NumberFormatException {
        // delega para utilitário
        return com.example.erp_.utils.NumberUtils.parseDoubleFromPtBR(text);
    }
}
