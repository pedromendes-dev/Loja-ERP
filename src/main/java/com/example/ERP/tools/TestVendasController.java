package com.example.ERP.tools;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.CountDownLatch;

public class TestVendasController {
    public static void main(String[] args) throws Exception {
        // initialize JavaFX platform
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException ex) {
            // already initialized
        }

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                System.out.println("Loading sales.fxml...");
                URL url = TestVendasController.class.getResource("/fxml/sales.fxml");
                if (url == null) { System.out.println("sales.fxml not found"); latch.countDown(); return; }
                FXMLLoader loader = new FXMLLoader(url);
                Parent root = loader.load();
                Object controller = loader.getController();
                System.out.println("Controller loaded: " + controller.getClass().getName());

                // try to call search and add
                try {
                    java.lang.reflect.Method buscar = controller.getClass().getDeclaredMethod("onBuscarProduto");
                    java.lang.reflect.Method adicionar = controller.getClass().getDeclaredMethod("onAdicionarCarrinho");
                    System.out.println("Invoking onBuscarProduto...");
                    buscar.setAccessible(true);
                    adicionar.setAccessible(true);
                    buscar.invoke(controller);
                    // small delay to let search populate (if async)
                    Thread.sleep(500);
                    System.out.println("Invoking onAdicionarCarrinho...");
                    adicionar.invoke(controller);
                    System.out.println("Called onAdicionarCarrinho");
                } catch (NoSuchMethodException ns) {
                    System.out.println("Controller methods not found: " + ns.getMessage());
                }

            } catch (Exception e) {
                e.printStackTrace(System.out);
            } finally {
                latch.countDown();
            }
        });
        latch.await();
        Platform.exit();
    }
}

