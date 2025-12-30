package com.example.erp_;

import com.example.erp_.strategy.AdvancedCpfStrategy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AdvancedCpfStrategyTest {
    private final AdvancedCpfStrategy strategy = new AdvancedCpfStrategy();

    @Test
    public void testValidCpf() {
        assertTrue(strategy.validate("111.444.777-35")); // example valid
        assertTrue(strategy.validate("11144477735"));
    }

    @Test
    public void testInvalidCpf() {
        assertFalse(strategy.validate(null));
        assertFalse(strategy.validate(""));
        assertFalse(strategy.validate("11111111111"));
        assertFalse(strategy.validate("123.456.789-00"));
    }
}

