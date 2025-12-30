package com.example.erp_.strategy;

public class BasicCnpjStrategy implements CnpjStrategy {
    public BasicCnpjStrategy() {}

    @Override
    public boolean validate(String cnpj) {
        return com.example.erp.strategy.BasicCnpjStrategy.isValid(cnpj);
    }

    public static boolean isValid(String cnpj) {
        return com.example.erp.strategy.BasicCnpjStrategy.isValid(cnpj);
    }
}
