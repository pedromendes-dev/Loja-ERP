package com.example.ERP.tools;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;

public class FxmlRuntimeLoader {
    public static void main(String[] args) throws Exception {
        // Initialize JavaFX toolkit so controls can be constructed
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ex) {
            // already initialized
        }

        File dir = new File("target/classes/fxml");
        if (!dir.exists() || !dir.isDirectory()) {
            System.err.println("No compiled fxml folder found at target/classes/fxml");
            System.exit(2);
        }
        int files = 0;
        int errors = 0;
        for (File f : Objects.requireNonNull(dir.listFiles((d,n)->n.endsWith(".fxml")))) {
            files++;
            String path = "/fxml/" + f.getName();
            System.out.println("Loading: " + path);
            try (InputStream is = FxmlRuntimeLoader.class.getResourceAsStream(path)) {
                if (is == null) { System.out.println("  NOT FOUND as resource: " + path); errors++; continue; }
                URL url = FxmlRuntimeLoader.class.getResource(path);
                Parent root = FXMLLoader.load(url);
                System.out.println("  OK: " + f.getName() + " -> root=" + (root == null ? "null" : root.getClass().getSimpleName()));
            } catch (Throwable t) {
                System.out.println("  ERROR loading " + f.getName() + ": " + t.getMessage());
                t.printStackTrace(System.out);
                errors++;
            }
        }
        System.out.println(String.format("Finished: scanned=%d errors=%d", files, errors));
        if (errors>0) System.exit(1);
        // Shutdown JavaFX platform cleanly
        try { Platform.exit(); } catch (Exception ignore) {}
    }
}
