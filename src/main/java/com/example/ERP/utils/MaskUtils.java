package com.example.erp.utils;

import javafx.scene.control.TextField;

public final class MaskUtils {

    private MaskUtils() {}

    public static String onlyDigits(String s) {
        if (s == null) return "";
        return s.replaceAll("\\D", "");
    }

    public static String cpfMask(String cpf) {
        String d = onlyDigits(cpf);
        if (d.length() != 11) return cpf == null ? "" : cpf;
        return d.substring(0,3) + "." + d.substring(3,6) + "." + d.substring(6,9) + "-" + d.substring(9);
    }

    public static String cnpjMask(String cnpj) {
        String d = onlyDigits(cnpj);
        if (d.length() != 14) return cnpj == null ? "" : cnpj;
        return d.substring(0,2) + "." + d.substring(2,5) + "." + d.substring(5,8) + "/" + d.substring(8,12) + "-" + d.substring(12);
    }

    public static String cpfOrCnpjMask(String s) {
        String d = onlyDigits(s);
        if (d.length() == 11) return cpfMask(d);
        if (d.length() == 14) return cnpjMask(d);
        return s == null ? "" : s;
    }

    // Lightweight apply* methods: add a listener to keep only digits or format money minimally.
    public static void applyCpfCnpjMask(TextField tf) {
        if (tf == null) return;
        tf.textProperty().addListener((o,ov,nv) -> {
            String only = onlyDigits(nv);
            if (!nv.equals(only)) tf.setText(only);
        });
    }

    public static void applyMoneyMask(TextField tf) {
        if (tf == null) return;
        // minimal implementation: keep numeric characters, comma as decimal
        tf.textProperty().addListener((o,ov,nv) -> {
            if (nv == null) return;
            String cleaned = nv.replaceAll("[^0-9,\\.-]", "");
            if (!nv.equals(cleaned)) tf.setText(cleaned);
        });
    }

    // New: applyPhoneMask used in controllers
    public static void applyPhoneMask(TextField tf) {
        if (tf == null) return;
        tf.textProperty().addListener((o,ov,nv) -> {
            if (nv == null) return;
            String digits = onlyDigits(nv);
            // keep up to 11 digits
            if (digits.length() > 11) digits = digits.substring(0,11);
            tf.setText(digits);
        });
    }
}
