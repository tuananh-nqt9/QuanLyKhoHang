package com.example.quanlykhohang;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.quanlykhohang.model.Product;
import com.example.quanlykhohang.utils.FormatUtils;

import org.junit.Test;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
public class ExampleUnitTest {

    @Test
    public void formatCurrency_usesVietnameseThousandSeparator() {
        assertEquals("5.350.000 đ", FormatUtils.formatCurrency(5_350_000L));
        assertEquals("0 đ", FormatUtils.formatCurrency(0L));
        assertEquals("25.000 đ", FormatUtils.formatCurrency(25_000L));
    }

    @Test
    public void product_lowStockRule() {
        Product keyboard = new Product("Bàn phím cơ", 450_000L, 3, "Điện tử", true, "");
        Product mouse = new Product("Chuột không dây", 150_000L, 20, "Điện tử", true, "");
        Product empty = new Product("Sổ tay A5", 25_000L, 0, "Văn phòng phẩm", true, "");

        assertTrue(keyboard.isLowStock());
        assertFalse(mouse.isLowStock());
        assertTrue(empty.isLowStock());
        assertTrue(empty.isOutOfStock());
        assertEquals(1_350_000L, keyboard.getStockValue());
    }

    @Test
    public void sampleData_totalValueIs5350000() {
        long total = 150_000L * 20 + 450_000L * 3 + 25_000L * 40;
        assertEquals(5_350_000L, total);
    }
}

