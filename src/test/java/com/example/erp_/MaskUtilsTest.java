package com.example.erp_;

import com.example.erp_.utils.MaskUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MaskUtilsTest {

    @Test
    void testOnlyDigits() {
        assertEquals("123456", MaskUtils.onlyDigits("a1b2c3 4-5.6"));
        assertEquals("", MaskUtils.onlyDigits(null));
    }

    @Test
    void testFormatCpf() {
        assertEquals("123.456.789-01", MaskUtils.formatCpf("12345678901"));
        assertEquals("123.456.789-01", MaskUtils.formatCpf("123.456.789-01"));
        assertEquals("", MaskUtils.formatCpf(null));
    }

    @Test
    void testFormatCnpj() {
        assertEquals("12.345.678/9012-34", MaskUtils.formatCnpj("12345678901234"));
        assertEquals("12.345.678/9012-34", MaskUtils.formatCnpj("12.345.678/9012-34"));
        assertEquals("", MaskUtils.formatCnpj(null));
    }

    @Test
    void testFormatCpfOrCnpj() {
        assertEquals("123.456.789-01", MaskUtils.formatCpfOrCnpj("12345678901"));
        assertEquals("12.345.678/9012-34", MaskUtils.formatCpfOrCnpj("12345678901234"));
    }

    @Test
    void testFormatPhoneShort() {
        assertEquals("(12) 3456-7890", MaskUtils.formatPhone("1234567890"));
        assertEquals("(12) 34567-8901", MaskUtils.formatPhone("12345678901"));
    }

    @Test
    void testFormatPhoneEdge() {
        assertEquals("", MaskUtils.formatPhone("") );
        assertEquals("1", MaskUtils.formatPhone("1"));
    }
}
