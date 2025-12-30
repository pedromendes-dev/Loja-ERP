package com.example.erp_.strategy;

public class AdvancedCpfStrategy implements CpfStrategy {
    public AdvancedCpfStrategy() {}
    @Override
    public boolean validate(String cpf) { return com.example.erp.strategy.AdvancedCpfStrategy.validateStatic(cpf); }
}
