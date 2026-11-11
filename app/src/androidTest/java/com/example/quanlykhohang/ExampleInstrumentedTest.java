package com.example.quanlykhohang;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.quanlykhohang.database.DatabaseHelper;
import com.example.quanlykhohang.model.Product;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.quanlykhohang", appContext.getPackageName());
    }

    @Test
    public void checkLogin_readsUsersTable() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        DatabaseHelper db = DatabaseHelper.getInstance(context);

        assertTrue(db.checkLogin("admin", "123456"));
        assertFalse(db.checkLogin("admin", "111111"));
    }

    @Test
    public void productCrud_isPersistedInSqlite() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        DatabaseHelper db = DatabaseHelper.getInstance(context);

        int countBefore = db.countProducts();
        long valueBefore = db.getTotalValue();
        int lowBefore = db.countLowStock();

        long newId = db.insertProduct(
                new Product("Sản phẩm kiểm thử", 200_000L, 4, "Điện tử", true, ""));
        assertTrue(newId > 0);
        assertEquals(countBefore + 1, db.countProducts());
        assertEquals(valueBefore + 800_000L, db.getTotalValue());
        assertEquals(lowBefore + 1, db.countLowStock());

        assertEquals(1, db.updateQuantity((int) newId, 5));
        Product updated = db.getProductById((int) newId);
        assertNotNull(updated);
        assertEquals(5, updated.getQuantity());
        assertEquals(lowBefore, db.countLowStock());

        // Không cho phép số lượng âm
        assertEquals(0, db.updateQuantity((int) newId, -1));

        assertEquals(1, db.deleteProduct((int) newId));
        assertNull(db.getProductById((int) newId));
        assertEquals(countBefore, db.countProducts());
    }
}

