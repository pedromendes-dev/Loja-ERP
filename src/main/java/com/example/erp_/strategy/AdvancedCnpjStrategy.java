package com.example.erp_.strategy;

public class AdvancedCnpjStrategy implements CnpjStrategy {
    public AdvancedCnpjStrategy() {}
    @Override
    public boolean validate(String cnpj) { return com.example.erp.strategy.AdvancedCnpjStrategy.validateStatic(cnpj); }
}
