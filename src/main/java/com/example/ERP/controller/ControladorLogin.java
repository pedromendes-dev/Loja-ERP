package com.example.erp.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import com.example.erp_.service.AuthService;
import com.example.erp_.config.AppConfig;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.GridPane;
import javafx.beans.binding.Bindings;
import java.util.ResourceBundle;

// Classe wrapper que estende o controller existente para manter compatibilidade com FXML
public class ControladorLogin {
    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtSenha;
    @FXML private Label lblMensagem;
    @FXML private Button btnEntrar;
    @FXML private StackPane loginCard;

    private AuthService authService = new AuthService();
    private final ResourceBundle bundle = ResourceBundle.getBundle("i18n.messages_pt_BR");

    @FXML
    public void initialize() {
        try { authService.registerDefaultAdminIfNeeded(); } catch (Exception e) { lblMensagem.setText(bundle.getString("init.error") + ": " + e.getMessage()); }
        // Tornar o card de login responsivo à largura da cena
        javafx.application.Platform.runLater(() -> {
            Scene s = btnEntrar.getScene();
            if (s != null) {
                loginCard.maxWidthProperty().bind(Bindings.createDoubleBinding(() -> {
                    double w = s.getWidth() * 0.9; // 90% da largura da cena
                    return Math.max(320, Math.min(420, w));
                }, s.widthProperty()));
            }
            // depois que a cena estiver disponível, garantir configuração do admin
            try {
                if (authService.isAdminMissing()) {
                    showAdminSetupDialog();
                }
            } catch (Exception ex) {
                lblMensagem.setText(bundle.getString("init.admin.error") + ": " + ex.getMessage());
            }
        });
    }

    private void showAdminSetupDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(bundle.getString("setup.title"));
        dialog.setHeaderText(bundle.getString("setup.header"));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        TextField username = new TextField(); username.setPromptText(bundle.getString("prompt.username"));
        PasswordField pwd = new PasswordField(); pwd.setPromptText(bundle.getString("prompt.password"));
        PasswordField pwd2 = new PasswordField(); pwd2.setPromptText(bundle.getString("prompt.password.confirm"));
        grid.add(new Label(bundle.getString("label.user")), 0, 0); grid.add(username, 1, 0);
        grid.add(new Label(bundle.getString("label.password")), 0, 1); grid.add(pwd, 1, 1);
        grid.add(new Label(bundle.getString("label.confirm")), 0, 2); grid.add(pwd2, 1, 2);

        dialog.getDialogPane().setContent(grid);
        ButtonType save = new ButtonType(bundle.getString("button.create"), ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);

        dialog.setResultConverter(bt -> bt);
        dialog.showAndWait().ifPresent(bt -> {
            if (bt == save) {
                String u = username.getText() == null || username.getText().trim().isEmpty() ? "admin" : username.getText().trim();
                String p1 = pwd.getText();
                String p2 = pwd2.getText();
                if (p1 == null || p1.isEmpty() || !p1.equals(p2)) {
                    lblMensagem.setText(bundle.getString("admin.password.mismatch"));
                    showAdminSetupDialog();
                    return;
                }
                try {
                    authService.createAdmin(u, p1);
                    lblMensagem.setText(bundle.getString("admin.created"));
                } catch (Exception e) {
                    lblMensagem.setText(bundle.getString("admin.create.error") + ": " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void onEntrar() {
        String user = txtUsuario.getText();
        String pass = txtSenha.getText();
        try {
            boolean ok = authService.login(user, pass);
            if (ok) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
                Parent root = loader.load();
                Scene scene = btnEntrar.getScene();
                scene.setRoot(root);
                scene.getStylesheets().add(getClass().getResource(AppConfig.THEME_LIGHT).toExternalForm());
            } else {
                lblMensagem.setText(bundle.getString("login.invalid"));
            }
        } catch (Exception e) {
            lblMensagem.setText(bundle.getString("error.generic") + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
