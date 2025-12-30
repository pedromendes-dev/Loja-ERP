package com.example.erp.utils;

import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class NumberUtils {
    private NumberUtils() {}

    /**
     * Parse a string in pt-BR formatting (e.g. "1.234,56" or "1234,56" or "R$ 1.234,56")
     * into a double using a resilient approach. Throws NumberFormatException on invalid input.
     */
    public static double parseDoubleFromPtBR(String text) throws NumberFormatException {
        if (text == null) throw new NumberFormatException("null");
        String s = text.trim();
        if (s.isEmpty()) throw new NumberFormatException("empty");
        // remove currency symbol and spaces
        s = s.replaceAll("R\\$", "").trim();
        // remove non-digit except comma and dot
        s = s.replaceAll("[^0-9,.-]", "");
        // If there are both dot and comma, assume dot is thousand separator and comma is decimal
        if (s.contains(".") && s.contains(",")) {
            s = s.replaceAll("\\.", ""); // remove thousands
            s = s.replace(',', '.');
        } else if (s.indexOf(',') >= 0 && s.indexOf('.') == -1) {
            s = s.replace(',', '.');
        }
        return Double.parseDouble(s);
    }
}

