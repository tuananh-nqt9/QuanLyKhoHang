package com.example.quanlykhohang.model;

/**
 * Lớp Product biểu diễn một sản phẩm trong kho.
 * <p>
 * Đây là lớp mô hình (model) thuần OOP: chỉ chứa dữ liệu và một vài phép tính đơn giản,
 * KHÔNG chứa bất kỳ câu truy vấn SQLite nào (việc đó thuộc về DatabaseHelper).
 */
public class Product {

    /** Sản phẩm có số lượng nhỏ hơn ngưỡng này được xem là "Sắp hết hàng". */
    public static final int LOW_STOCK_THRESHOLD = 5;

    /** Giới hạn giá bán (10 tỷ đồng) để tránh tràn số khi tính price * quantity. */
    public static final long MAX_PRICE = 10_000_000_000L;

    /** Giới hạn số lượng tồn kho của một sản phẩm, tránh tràn số nguyên. */
    public static final int MAX_QUANTITY = 1_000_000;

    private int id;
    private String name;
    private long price;
    private int quantity;
    private String category;
    private boolean active;
    private String description;

    public Product() {
    }

    /** Dùng khi tạo sản phẩm mới (chưa có id, SQLite sẽ tự sinh id). */
    public Product(String name, long price, int quantity, String category,
                   boolean active, String description) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.category = category;
        this.active = active;
        this.description = description;
    }

    /** Dùng khi đọc sản phẩm từ SQLite (đã có id). */
    public Product(int id, String name, long price, int quantity, String category,
                   boolean active, String description) {
        this(name, price, quantity, category, active, description);
        this.id = id;
    }

    // ===== Getter & Setter =====

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // ===== Các phép tính tiện ích (không truy vấn database) =====

    /** Giá trị tồn kho = giá bán × số lượng. */
    public long getStockValue() {
        return price * quantity;
    }

    /** Sắp hết hàng khi số lượng nhỏ hơn 5 (bao gồm cả số lượng bằng 0). */
    public boolean isLowStock() {
        return quantity < LOW_STOCK_THRESHOLD;
    }

    /** Hết hàng khi số lượng bằng 0. */
    public boolean isOutOfStock() {
        return quantity <= 0;
    }
}

