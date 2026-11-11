package com.example.quanlykhohang.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Tiện ích định dạng số và tiền tệ theo kiểu Việt Nam.
 * Ví dụ: 5350000 → "5.350.000 đ".
 */
public final class FormatUtils {

    private FormatUtils() {
        // Lớp tiện ích, không cho phép tạo đối tượng
    }

    /** Định dạng số có dấu chấm phân cách hàng nghìn: 5350000 → "5.350.000". */
    public static String formatNumber(long value) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setGroupingSeparator('.');
        DecimalFormat formatter = new DecimalFormat("#,##0", symbols);
        return formatter.format(value);
    }

    /** Định dạng tiền Việt Nam: 5350000 → "5.350.000 đ". */
    public static String formatCurrency(long value) {
        return formatNumber(value) + " đ";
    }
}

