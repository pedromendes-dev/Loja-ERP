package com.example.erp_.utils;

public final class MaskUtils {
    private MaskUtils() {}

    public static String onlyDigits(String s) { return com.example.erp.utils.MaskUtils.onlyDigits(s); }
    public static String formatCpf(String s) { return com.example.erp.utils.MaskUtils.cpfMask(s); }
    public static String formatCnpj(String s) { return com.example.erp.utils.MaskUtils.cnpjMask(s); }
    public static String formatCpfOrCnpj(String s) { return com.example.erp.utils.MaskUtils.cpfOrCnpjMask(s); }

    public static String formatPhone(String s) {
        String d = onlyDigits(s);
        if (d == null || d.isEmpty()) return "";
        if (d.length() <= 2) return d;
        if (d.length() == 10) {
            return String.format("(%s) %s-%s", d.substring(0,2), d.substring(2,6), d.substring(6));
        }
        if (d.length() == 11) {
            return String.format("(%s) %s-%s", d.substring(0,2), d.substring(2,7), d.substring(7));
        }
        // fallback: return digits
        return d;
    }

    public static void applyPhoneMask(javafx.scene.control.TextField tf) { com.example.erp.utils.MaskUtils.applyPhoneMask(tf); }
}
