package com.example.erp_;

import com.example.erp_.strategy.AdvancedCnpjStrategy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AdvancedCnpjStrategyTest {
    private final AdvancedCnpjStrategy strategy = new AdvancedCnpjStrategy();

    @Test
    public void testValidCnpj() {
        // exemplos válidos conhecidos: 11222333000181 (geralmente usado em exemplos)
        assertTrue(strategy.validate("11.222.333/0001-81"));
        assertTrue(strategy.validate("11222333000181"));
    }

    @Test
    public void testInvalidCnpj() {
        assertFalse(strategy.validate(null));
        assertFalse(strategy.validate(""));
        assertFalse(strategy.validate("11111111111111"));
        assertFalse(strategy.validate("12.345.678/0001-00"));
    }
}

