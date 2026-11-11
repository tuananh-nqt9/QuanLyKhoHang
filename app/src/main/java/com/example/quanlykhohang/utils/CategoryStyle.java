package com.example.quanlykhohang.utils;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;

import com.example.quanlykhohang.R;

/**
 * Xác định icon và màu sắc hiển thị cho từng loại sản phẩm.
 * Tên loại phải khớp với mảng R.array.product_categories trong strings.xml.
 */
public final class CategoryStyle {

    public static final String ELECTRONICS = "Điện tử";
    public static final String FOOD = "Thực phẩm";
    public static final String OFFICE = "Văn phòng phẩm";
    public static final String HOUSEHOLD = "Gia dụng";
    public static final String OTHER = "Khác";

    private CategoryStyle() {
    }

    /** Icon vector đại diện cho loại sản phẩm. */
    @DrawableRes
    public static int getIcon(String category) {
        if (ELECTRONICS.equals(category)) return R.drawable.ic_devices;
        if (FOOD.equals(category)) return R.drawable.ic_food;
        if (OFFICE.equals(category)) return R.drawable.ic_stationery;
        if (HOUSEHOLD.equals(category)) return R.drawable.ic_home;
        return R.drawable.ic_box;
    }

    /** Màu chính (icon, chữ của badge). */
    @ColorRes
    public static int getColor(String category) {
        if (ELECTRONICS.equals(category)) return R.color.cat_electronics;
        if (FOOD.equals(category)) return R.color.cat_food;
        if (OFFICE.equals(category)) return R.color.cat_office;
        if (HOUSEHOLD.equals(category)) return R.color.cat_household;
        return R.color.cat_other;
    }

    /** Màu nền nhạt (nền ô icon, nền badge). */
    @ColorRes
    public static int getSoftColor(String category) {
        if (ELECTRONICS.equals(category)) return R.color.cat_electronics_soft;
        if (FOOD.equals(category)) return R.color.cat_food_soft;
        if (OFFICE.equals(category)) return R.color.cat_office_soft;
        if (HOUSEHOLD.equals(category)) return R.color.cat_household_soft;
        return R.color.cat_other_soft;
    }
}

