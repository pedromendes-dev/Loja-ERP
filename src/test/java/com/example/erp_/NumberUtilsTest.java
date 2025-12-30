package com.example.erp_;

import com.example.erp_.utils.NumberUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class NumberUtilsTest {

    @Test
    void parseVariousFormats() {
        assertEquals(1234.56, NumberUtils.parseDoubleFromPtBR("1.234,56"), 0.0001);
        assertEquals(1234.56, NumberUtils.parseDoubleFromPtBR("1234,56"), 0.0001);
        assertEquals(1234.56, NumberUtils.parseDoubleFromPtBR("1234.56"), 0.0001);
        assertEquals(1234.56, NumberUtils.parseDoubleFromPtBR("R$ 1.234,56"), 0.0001);
        assertEquals(1000.0, NumberUtils.parseDoubleFromPtBR("1.000"), 0.0001);
    }

    @Test
    void parseInvalidThrows() {
        assertThrows(NumberFormatException.class, () -> NumberUtils.parseDoubleFromPtBR("abc"));
        assertThrows(NumberFormatException.class, () -> NumberUtils.parseDoubleFromPtBR(""));
    }
}

