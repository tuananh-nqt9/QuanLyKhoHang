package com.example.quanlykhohang.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.quanlykhohang.model.Product;

import java.util.ArrayList;

/**
 * DatabaseHelper quản lý cơ sở dữ liệu SQLite "warehouse.db" (phiên bản 1).
 * <p>
 * Nhiệm vụ:
 * <ul>
 *     <li>Tạo 2 bảng users và products trong onCreate().</li>
 *     <li>Chèn dữ liệu mẫu (1 tài khoản + 3 sản phẩm) – chỉ chạy MỘT lần khi database được tạo.</li>
 *     <li>Cung cấp các phương thức truy vấn / thêm / cập nhật / xóa dữ liệu.</li>
 * </ul>
 * Mọi câu truy vấn có tham số đều dùng dấu ? và selectionArgs để tránh SQL Injection.
 * <p>
 * Lớp được dùng theo mẫu Singleton (getInstance) để toàn ứng dụng chỉ có MỘT đối tượng
 * DatabaseHelper, tránh mở nhiều kết nối không cần thiết khi chuyển Activity.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    // ===== Thông tin database =====
    public static final String DATABASE_NAME = "warehouse.db";
    public static final int DATABASE_VERSION = 1;

    // ===== Bảng users =====
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";

    // ===== Bảng products =====
    public static final String TABLE_PRODUCTS = "products";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_PRICE = "price";
    public static final String COLUMN_QUANTITY = "quantity";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_ACTIVE = "active";
    public static final String COLUMN_DESCRIPTION = "description";

    private static final String SQL_CREATE_USERS =
            "CREATE TABLE " + TABLE_USERS + " ("
                    + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_USERNAME + " TEXT NOT NULL UNIQUE, "
                    + COLUMN_PASSWORD + " TEXT NOT NULL)";

    private static final String SQL_CREATE_PRODUCTS =
            "CREATE TABLE " + TABLE_PRODUCTS + " ("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_NAME + " TEXT NOT NULL, "
                    + COLUMN_PRICE + " INTEGER NOT NULL CHECK (" + COLUMN_PRICE + " > 0), "
                    + COLUMN_QUANTITY + " INTEGER NOT NULL CHECK (" + COLUMN_QUANTITY + " >= 0), "
                    + COLUMN_CATEGORY + " TEXT NOT NULL, "
                    + COLUMN_ACTIVE + " INTEGER NOT NULL, "
                    + COLUMN_DESCRIPTION + " TEXT)";

    private static DatabaseHelper instance;

    /**
     * Lấy đối tượng DatabaseHelper duy nhất của ứng dụng.
     * Dùng ApplicationContext để không giữ tham chiếu tới Activity (tránh rò rỉ bộ nhớ).
     */
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Chỉ được gọi MỘT lần khi file warehouse.db chưa tồn tại.
     * Vì vậy dữ liệu mẫu không bị chèn lại mỗi lần mở ứng dụng.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_USERS);
        db.execSQL(SQL_CREATE_PRODUCTS);
        insertSampleData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Phiên bản hiện tại là 1 nên phương thức này chưa được gọi.
        // Khi nâng version trong tương lai: xóa bảng cũ và tạo lại.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    /** Chèn tài khoản mẫu admin/123456 và 3 sản phẩm mẫu theo đề bài. */
    private void insertSampleData(SQLiteDatabase db) {
        // Lưu ý: bài thực hành không yêu cầu mã hóa mật khẩu (chỉ là tài khoản demo cục bộ).
        insertUser(db, "admin", "123456");

        insertProduct(db, new Product("Chuột không dây", 150000, 20, "Điện tử", true,
                "Chuột không dây kết nối 2.4GHz, thiết kế công thái học, pin sử dụng đến 12 tháng."));
        insertProduct(db, new Product("Bàn phím cơ", 450000, 3, "Điện tử", true,
                "Bàn phím cơ 87 phím, switch Blue cho cảm giác gõ rõ ràng, có đèn nền LED."));
        insertProduct(db, new Product("Sổ tay A5", 25000, 40, "Văn phòng phẩm", true,
                "Sổ tay khổ A5 bìa cứng, 200 trang giấy kẻ dòng, phù hợp ghi chép hằng ngày."));
    }

    // =====================================================================
    //  BẢNG USERS
    // =====================================================================

    /**
     * Kiểm tra đăng nhập: trả về true nếu có dòng trong bảng users khớp username và password.
     */
    public boolean checkLogin(String username, String password) {
        if (username == null || password == null) {
            return false;
        }
        SQLiteDatabase db = getReadableDatabase();
        String selection = COLUMN_USERNAME + " = ? AND " + COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = {username, password};

        try (Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_USER_ID},
                selection, selectionArgs, null, null, null, "1")) {
            return cursor.moveToFirst();
        }
    }

    /** Thêm tài khoản mới vào bảng users. Trả về id vừa thêm hoặc -1 nếu lỗi (ví dụ trùng username). */
    public long insertUser(String username, String password) {
        return insertUser(getWritableDatabase(), username, password);
    }

    private long insertUser(SQLiteDatabase db, String username, String password) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);
        return db.insert(TABLE_USERS, null, values);
    }

    // =====================================================================
    //  BẢNG PRODUCTS
    // =====================================================================

    /** Thêm sản phẩm. Trả về id vừa thêm, hoặc -1 nếu thêm thất bại. */
    public long insertProduct(Product product) {
        return insertProduct(getWritableDatabase(), product);
    }

    private long insertProduct(SQLiteDatabase db, Product product) {
        if (product == null) {
            return -1;
        }
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, product.getName());
        values.put(COLUMN_PRICE, product.getPrice());
        values.put(COLUMN_QUANTITY, product.getQuantity());
        values.put(COLUMN_CATEGORY, product.getCategory());
        values.put(COLUMN_ACTIVE, product.isActive() ? 1 : 0);
        values.put(COLUMN_DESCRIPTION, product.getDescription());
        // db.insert() tự bắt lỗi ràng buộc và trả về -1 nếu thất bại
        return db.insert(TABLE_PRODUCTS, null, values);
    }

    /** Lấy toàn bộ sản phẩm (sản phẩm mới thêm hiển thị trên cùng). Trả về danh sách rỗng nếu không có. */
    public ArrayList<Product> getAllProducts() {
        // ArrayList chỉ dùng để CHỨA KẾT QUẢ truy vấn và truyền cho Adapter hiển thị.
        ArrayList<Product> products = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        try (Cursor cursor = db.query(TABLE_PRODUCTS, null, null, null,
                null, null, COLUMN_ID + " DESC")) {
            while (cursor.moveToNext()) {
                products.add(cursorToProduct(cursor));
            }
        }
        return products;
    }

    /** Lấy một sản phẩm theo id. Trả về null nếu sản phẩm không tồn tại. */
    public Product getProductById(int id) {
        SQLiteDatabase db = getReadableDatabase();
        String selection = COLUMN_ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};

        try (Cursor cursor = db.query(TABLE_PRODUCTS, null, selection, selectionArgs,
                null, null, null, "1")) {
            if (cursor.moveToFirst()) {
                return cursorToProduct(cursor);
            }
        }
        return null;
    }

    /**
     * Cập nhật số lượng tồn kho (dùng cho Nhập thêm / Bán).
     * Trả về số dòng bị ảnh hưởng: 1 nếu thành công, 0 nếu thất bại / không tìm thấy.
     */
    public int updateQuantity(int id, int quantity) {
        if (quantity < 0) {
            return 0; // Không cho phép số lượng âm
        }
        ContentValues values = new ContentValues();
        values.put(COLUMN_QUANTITY, quantity);
        try {
            return getWritableDatabase().update(TABLE_PRODUCTS, values,
                    COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        } catch (SQLException e) {
            Log.e(TAG, "updateQuantity failed", e);
            return 0;
        }
    }

    /** Xóa sản phẩm theo id. Trả về số dòng đã xóa (0 nếu sản phẩm không còn tồn tại). */
    public int deleteProduct(int id) {
        return getWritableDatabase().delete(TABLE_PRODUCTS,
                COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }

    // =====================================================================
    //  THỐNG KÊ
    // =====================================================================

    /** Tổng số sản phẩm (số bản ghi trong bảng products, không phải tổng quantity). */
    public int countProducts() {
        return (int) queryForLong("SELECT COUNT(*) FROM " + TABLE_PRODUCTS, null);
    }

    /** Tổng giá trị kho = SUM(price * quantity). COALESCE giúp trả về 0 khi kho trống. */
    public long getTotalValue() {
        return queryForLong("SELECT COALESCE(SUM(" + COLUMN_PRICE + " * " + COLUMN_QUANTITY
                + "), 0) FROM " + TABLE_PRODUCTS, null);
    }

    /** Số sản phẩm sắp hết hàng: quantity < 5 (bao gồm cả sản phẩm có quantity = 0). */
    public int countLowStock() {
        return (int) queryForLong("SELECT COUNT(*) FROM " + TABLE_PRODUCTS
                        + " WHERE " + COLUMN_QUANTITY + " < ?",
                new String[]{String.valueOf(Product.LOW_STOCK_THRESHOLD)});
    }

    // =====================================================================
    //  HÀM HỖ TRỢ
    // =====================================================================

    /** Thực thi câu truy vấn trả về một giá trị số duy nhất. Cursor luôn được đóng. */
    private long queryForLong(String sql, String[] selectionArgs) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.rawQuery(sql, selectionArgs)) {
            if (cursor.moveToFirst()) {
                return cursor.getLong(0);
            }
        } catch (SQLException e) {
            Log.e(TAG, "queryForLong failed: " + sql, e);
        }
        return 0;
    }

    /** Chuyển một dòng dữ liệu của Cursor thành đối tượng Product. */
    private Product cursorToProduct(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
        String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
        long price = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_PRICE));
        int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_QUANTITY));
        String category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY));
        boolean active = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ACTIVE)) == 1;

        int descriptionIndex = cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION);
        String description = cursor.isNull(descriptionIndex) ? "" : cursor.getString(descriptionIndex);

        return new Product(id, name, price, quantity, category, active, description);
    }
}

