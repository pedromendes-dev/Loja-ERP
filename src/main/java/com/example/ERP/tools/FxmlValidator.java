package com.example.ERP.tools;

import java.io.File;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.*;

public class FxmlValidator {
    public static void main(String[] args) throws Exception {
        File dir = new File("src/main/resources/fxml");
        if (!dir.exists() || !dir.isDirectory()) {
            System.err.println("FXML folder not found: " + dir.getAbsolutePath());
            System.exit(2);
        }
        Pattern ctrlPat = Pattern.compile("fx:controller\s*=\s*\"([^\"]+)\"");
        Pattern onActionPat = Pattern.compile("onAction\s*=\s*\"#([A-Za-z0-9_]+)\"");
        Pattern fxIdPat = Pattern.compile("fx:id\s*=\s*\"([A-Za-z0-9_]+)\"");

        int files = 0;
        int errors = 0;
        for (File f : Objects.requireNonNull(dir.listFiles((d,n)->n.endsWith(".fxml")))) {
            files++;
            System.out.println("\n=== " + f.getName() + " ===");
            String s = new String(Files.readAllBytes(f.toPath()), java.nio.charset.StandardCharsets.UTF_8);
            Matcher m = ctrlPat.matcher(s);
            if (!m.find()) {
                System.out.println("  WARN: no fx:controller attribute found");
                continue;
            }
            String ctrl = m.group(1);
            System.out.println("  controller: " + ctrl);
            Class<?> cls = null;
            try {
                cls = Class.forName(ctrl);
                System.out.println("    -> class found: " + cls.getName());
            } catch (ClassNotFoundException e) {
                System.out.println("    ERROR: controller class not found: " + ctrl);
                errors++;
                continue;
            }

            // collect onAction handlers
            Set<String> handlers = new HashSet<>();
            m = onActionPat.matcher(s);
            while (m.find()) handlers.add(m.group(1));
            if (!handlers.isEmpty()) System.out.println("  handlers found: " + handlers);
            for (String h : handlers) {
                boolean ok = false;
                // check declared methods
                for (java.lang.reflect.Method mm : cls.getDeclaredMethods()) if (mm.getName().equals(h)) { ok=true; break; }
                for (java.lang.reflect.Method mm : cls.getMethods()) if (mm.getName().equals(h)) { ok=true; break; }
                if (!ok) {
                    System.out.println("    ERROR: handler method not found in controller: " + h);
                    errors++;
                }
            }

            // collect fx:id fields
            Set<String> fxids = new HashSet<>();
            m = fxIdPat.matcher(s);
            while (m.find()) fxids.add(m.group(1));
            if (!fxids.isEmpty()) System.out.println("  fx:ids found: " + fxids.size());
            for (String id : fxids) {
                boolean ok = false;
                try {
                    java.lang.reflect.Field f1 = cls.getDeclaredField(id);
                    ok = true;
                } catch (NoSuchFieldException nsf) {
                    // try public fields
                    try { java.lang.reflect.Field f2 = cls.getField(id); ok = true; } catch (NoSuchFieldException nsf2) { ok = false; }
                }
                if (!ok) {
                    System.out.println("    WARN: fx:id '"+id+"' has no matching field in controller (may be private without same name)");
                }
            }

            System.out.println("  OK for " + f.getName());
        }
        System.out.println(String.format("\nScanned %d files, errors=%d", files, errors));
        if (errors>0) System.exit(1);
    }
}

