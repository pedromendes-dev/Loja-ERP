package com.example.erp_.controller;

import com.example.erp_.model.Client;
import com.example.erp_.strategy.AdvancedCpfStrategy;
import com.example.erp_.strategy.BasicCnpjStrategy;
import com.example.erp_.utils.MaskUtils;
import com.example.erp_.utils.ValidationUtils;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Formulário de cliente: parsing/validação e mensagens em português.
 */
public class FormularioClienteController implements Initializable {
    @FXML private TextField txtName;
    @FXML private TextField txtCpfCnpj;
    @FXML private TextField txtEmail;
    @FXML private TextField txtPhone;
    @FXML private TextArea txtAddress;
    @FXML private Label lblError;
    @FXML private Label lblCpfStatus;

    private Client cliente;
    private final BooleanProperty valido = new SimpleBooleanProperty(false);

    public void setClient(Client c) {
        this.cliente = c;
        if (c != null) {
            txtName.setText(c.getName());
            txtCpfCnpj.setText(c.getCpfCnpj());
            txtEmail.setText(c.getEmail());
            txtPhone.setText(c.getPhone());
            txtAddress.setText(c.getAddress());
        }
        valido.set(validate());
    }

    public Client getClient() {
        if (cliente == null) cliente = new Client();
        cliente.setName(txtName.getText());
        cliente.setCpfCnpj(txtCpfCnpj.getText());
        cliente.setEmail(txtEmail.getText());
        cliente.setPhone(txtPhone.getText());
        cliente.setAddress(txtAddress.getText());
        return cliente;
    }

    public BooleanProperty propriedadeValida() { return valido; }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        MaskUtils.applyCpfCnpjMask(txtCpfCnpj);
        MaskUtils.applyPhoneMask(txtPhone);
        txtName.textProperty().addListener((o,ov,nv) -> valido.set(validate()));
        txtCpfCnpj.textProperty().addListener((o,ov,nv) -> { valido.set(validate()); updateCpfStatus(nv); });
        txtEmail.textProperty().addListener((o,ov,nv) -> valido.set(validate()));
        txtPhone.textProperty().addListener((o,ov,nv) -> valido.set(validate()));
        txtAddress.textProperty().addListener((o,ov,nv) -> valido.set(validate()));
        lblError.setText("");
        lblCpfStatus.setText("");
        valido.set(false);
    }

    private void updateCpfStatus(String value) {
        if (value == null || value.trim().isEmpty()) {
            lblCpfStatus.setText("");
            lblCpfStatus.getStyleClass().removeAll("valid","invalid");
            return;
        }
        boolean ok;
        if (ValidationUtils.isCpf(value)) ok = ValidationUtils.validateCpf(value, new AdvancedCpfStrategy());
        else ok = ValidationUtils.validateCnpj(value, new BasicCnpjStrategy());
        if (ok) {
            lblCpfStatus.setText("✓");
            lblCpfStatus.getStyleClass().removeAll("invalid");
            if (!lblCpfStatus.getStyleClass().contains("valid")) lblCpfStatus.getStyleClass().add("valid");
        } else {
            lblCpfStatus.setText("✕");
            lblCpfStatus.getStyleClass().removeAll("valid");
            if (!lblCpfStatus.getStyleClass().contains("invalid")) lblCpfStatus.getStyleClass().add("invalid");
        }
    }

    public boolean validate() {
        String name = txtName.getText();
        if (name == null || name.trim().isEmpty()) { lblError.setText("Nome é obrigatório."); return false; }
        String cpfCnpj = txtCpfCnpj.getText();
        if (cpfCnpj != null && !cpfCnpj.trim().isEmpty()) {
            if (ValidationUtils.isCpf(cpfCnpj) && !ValidationUtils.validateCpf(cpfCnpj, new AdvancedCpfStrategy())) { lblError.setText("CPF inválido."); return false; }
            else if (!ValidationUtils.isCpf(cpfCnpj) && !ValidationUtils.validateCnpj(cpfCnpj, new BasicCnpjStrategy())) { lblError.setText("CNPJ inválido."); return false; }
        }
        String email = txtEmail.getText();
        if (email != null && !email.trim().isEmpty() && !ValidationUtils.isValidEmail(email)) { lblError.setText("Email inválido."); return false; }
        lblError.setText("");
        return true;
    }
}
