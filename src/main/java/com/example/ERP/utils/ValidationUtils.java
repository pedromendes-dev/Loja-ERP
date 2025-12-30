package com.example.erp.utils;

import java.util.regex.Pattern;
import com.example.erp.strategy.CpfStrategy;
import com.example.erp.strategy.CnpjStrategy;

public final class ValidationUtils {
    private ValidationUtils() {}

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public static boolean isNotBlank(String s) { return s != null && !s.trim().isEmpty(); }

    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        return EMAIL.matcher(email).matches();
    }

    public static boolean isCpf(String s) {
        if (s == null) return false;
        String d = s.replaceAll("\\D", "");
        return d.length() == 11;
    }

    public static boolean isCnpj(String s) {
        if (s == null) return false;
        String d = s.replaceAll("\\D", "");
        return d.length() == 14;
    }

    // Validate using a provided strategy instance
    public static boolean validateCpf(String cpf, CpfStrategy strategy) {
        if (cpf == null || strategy == null) return false;
        return strategy.validate(cpf);
    }

    public static boolean validateCnpj(String cnpj, CnpjStrategy strategy) {
        if (cnpj == null || strategy == null) return false;
        return strategy.validate(cnpj);
    }
}
