package com.example.ERP;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.example.erp_.config.DbManager;
import com.example.erp_.config.AppConfig;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Initialize DB (creates file and schema if needed)
        DbManager.getInstance().initialize();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());

        primaryStage.setTitle(AppConfig.APP_TITLE);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(600);
        primaryStage.setMinHeight(400);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
