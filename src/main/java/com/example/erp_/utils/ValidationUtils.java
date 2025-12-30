package com.example.erp_.utils;

public final class ValidationUtils {
    private ValidationUtils() {}

    public static boolean isNotBlank(String s) { return com.example.erp.utils.ValidationUtils.isNotBlank(s); }
    public static boolean isValidEmail(String s) { return com.example.erp.utils.ValidationUtils.isValidEmail(s); }
    public static boolean isCpf(String s) { return com.example.erp.utils.ValidationUtils.isCpf(s); }
    public static boolean isCnpj(String s) { return com.example.erp.utils.ValidationUtils.isCnpj(s); }

    // Delegadores para métodos de validação que aceitam instâncias de estratégia do pacote underscore
    public static boolean validateCpf(String cpf, com.example.erp_.strategy.CpfStrategy strategy) {
        if (strategy == null) return false;
        // delega para a implementação canônica adaptando a interface
        return com.example.erp.utils.ValidationUtils.validateCpf(cpf, new com.example.erp.strategy.CpfStrategy() {
            @Override public boolean validate(String c) { return strategy.validate(c); }
        });
    }

    public static boolean validateCnpj(String cnpj, com.example.erp_.strategy.CnpjStrategy strategy) {
        if (strategy == null) return false;
        return com.example.erp.utils.ValidationUtils.validateCnpj(cnpj, new com.example.erp.strategy.CnpjStrategy() {
            @Override public boolean validate(String c) { return strategy.validate(c); }
        });
    }
}
