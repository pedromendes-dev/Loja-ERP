package com.example.erp_.controller;

import com.example.erp_.model.Supplier;
import com.example.erp_.strategy.AdvancedCnpjStrategy;
import com.example.erp_.utils.ValidationUtils;
import com.example.erp_.utils.MaskUtils;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class FormularioFornecedorController implements Initializable {
    @FXML private TextField txtName;
    @FXML private TextField txtCnpj;
    @FXML private TextField txtEmail;
    @FXML private TextField txtContact;
    @FXML private TextArea txtAddress;
    @FXML private Label lblError;
    @FXML private Label lblCpfStatus;

    private Supplier supplier;
    private final BooleanProperty valido = new SimpleBooleanProperty(false);

    public void setSupplier(Supplier s) {
        this.supplier = s; if (s != null) { txtName.setText(s.getName()); txtCnpj.setText(s.getCnpj()); txtEmail.setText(s.getEmail()); txtContact.setText(s.getContact()); txtAddress.setText(s.getAddress()); }
        valido.set(validate());
    }

    public Supplier getSupplier() { if (supplier == null) supplier = new Supplier(); supplier.setName(txtName.getText()); supplier.setCnpj(txtCnpj.getText()); supplier.setEmail(txtEmail.getText()); supplier.setContact(txtContact.getText()); supplier.setAddress(txtAddress.getText()); return supplier; }

    public BooleanProperty propriedadeValida() { return valido; }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        MaskUtils.applyCpfCnpjMask(txtCnpj);
        MaskUtils.applyPhoneMask(txtContact);
        txtName.textProperty().addListener((o,ov,nv) -> valido.set(validate()));
        txtCnpj.textProperty().addListener((o,ov,nv) -> { valido.set(validate()); updateCnpjStatus(nv); });
        txtEmail.textProperty().addListener((o,ov,nv) -> valido.set(validate()));
        txtContact.textProperty().addListener((o,ov,nv) -> valido.set(validate()));
        txtAddress.textProperty().addListener((o,ov,nv) -> valido.set(validate()));
        lblError.setText(""); lblCpfStatus.setText(""); valido.set(false);
    }

    private void updateCnpjStatus(String value) { if (value == null || value.trim().isEmpty()) { lblCpfStatus.setText(""); lblCpfStatus.getStyleClass().removeAll("valid","invalid"); return; } boolean ok = ValidationUtils.validateCnpj(value, new AdvancedCnpjStrategy()); if (ok) { lblCpfStatus.setText("✓"); lblCpfStatus.getStyleClass().removeAll("invalid"); if (!lblCpfStatus.getStyleClass().contains("valid")) lblCpfStatus.getStyleClass().add("valid"); } else { lblCpfStatus.setText("✕"); lblCpfStatus.getStyleClass().removeAll("valid"); if (!lblCpfStatus.getStyleClass().contains("invalid")) lblCpfStatus.getStyleClass().add("invalid"); } }

    public boolean validate() { String name = txtName.getText(); if (name == null || name.trim().isEmpty()) { lblError.setText("Nome é obrigatório."); return false; } String email = txtEmail.getText(); if (email != null && !email.trim().isEmpty() && !ValidationUtils.isValidEmail(email)) { lblError.setText("Email inválido."); return false; } String cnpj = txtCnpj.getText(); if (cnpj != null && !cnpj.trim().isEmpty()) { if (!ValidationUtils.validateCnpj(cnpj, new AdvancedCnpjStrategy())) { lblError.setText("CNPJ inválido."); return false; } } lblError.setText(""); return true; }
}
