package com.example.quanlykhohang;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlykhohang.adapter.ProductAdapter;
import com.example.quanlykhohang.database.DatabaseHelper;
import com.example.quanlykhohang.model.Product;
import com.example.quanlykhohang.utils.FormatUtils;
import com.example.quanlykhohang.utils.UiUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;

/**
 * Màn hình Danh sách & Thống kê kho (Màn hình 2 - MainActivity).
 * <p>
 * Yêu cầu:
 * <ul>
 *     <li>Hiển thị 3 chỉ số thống kê bắt buộc:
 *         1. Tổng sản phẩm (số bản ghi sản phẩm).
 *         2. Tổng giá trị kho (SUM(price * quantity) định dạng VNĐ).
 *         3. Sắp hết hàng (COUNT với quantity < 5).
 *     </li>
 *     <li>Hiển thị danh sách sản phẩm bằng RecyclerView.</li>
 *     <li>Khi nhấn vào một sản phẩm: lấy id và truyền sang {@link ProductDetailActivity} bằng Intent.</li>
 *     <li>Nút "+ Thêm sản phẩm" mở {@link AddProductActivity}.</li>
 *     <li>Khi danh sách trống: hiển thị khối thông báo "Kho hàng đang trống".</li>
 *     <li>Cập nhật lại toàn bộ dữ liệu trong {@link #onResume()}.</li>
 * </ul>
 */
public class MainActivity extends AppCompatActivity {

    private View headerContent;
    private View bottomBar;
    private TextView tvGreeting;
    private ImageButton btnLogout;
    private TextView tvTotalValue;
    private TextView tvTotalProducts;
    private TextView tvLowStockCount;
    private TextView tvProductCountBadge;
    private RecyclerView rvProducts;
    private View emptyStateLayout;
    private MaterialButton btnEmptyAdd;
    private MaterialButton btnAddProduct;

    private ProductAdapter adapter;
    private DatabaseHelper dbHelper;
    private String loggedInUser = "Admin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UiUtils.enableEdgeToEdge(this);
        setContentView(R.layout.activity_main);

        dbHelper = DatabaseHelper.getInstance(this);

        if (getIntent() != null && getIntent().hasExtra(LoginActivity.EXTRA_LOGGED_IN_USER)) {
            String user = getIntent().getStringExtra(LoginActivity.EXTRA_LOGGED_IN_USER);
            if (user != null && !user.trim().isEmpty()) {
                loggedInUser = UiUtils.capitalize(user);
            }
        }

        initViews();
        setupInsets();
        setupRecyclerView();
        setupListeners();
    }

    private void initViews() {
        headerContent = findViewById(R.id.headerContent);
        bottomBar = findViewById(R.id.bottomBar);
        tvGreeting = findViewById(R.id.tvGreeting);
        btnLogout = findViewById(R.id.btnLogout);
        tvTotalValue = findViewById(R.id.tvTotalValue);
        tvTotalProducts = findViewById(R.id.tvTotalProducts);
        tvLowStockCount = findViewById(R.id.tvLowStockCount);
        tvProductCountBadge = findViewById(R.id.tvProductCountBadge);
        rvProducts = findViewById(R.id.rvProducts);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        btnEmptyAdd = findViewById(R.id.btnEmptyAdd);
        btnAddProduct = findViewById(R.id.btnAddProduct);

        tvGreeting.setText(getString(R.string.greeting_format, loggedInUser));
    }

    private void setupInsets() {
        final int headerTopPadding = headerContent.getPaddingTop();
        final int bottomBarBottomPadding = bottomBar.getPaddingBottom();

        UiUtils.applyWindowInsets(findViewById(R.id.coordinatorLayout), (bars, imeBottom) -> {
            headerContent.setPadding(
                    headerContent.getPaddingLeft(),
                    headerTopPadding + bars.top,
                    headerContent.getPaddingRight(),
                    headerContent.getPaddingBottom()
            );
            bottomBar.setPadding(
                    bottomBar.getPaddingLeft(),
                    bottomBar.getPaddingTop(),
                    bottomBar.getPaddingRight(),
                    bottomBarBottomPadding + bars.bottom
            );
        });
    }

    private void setupRecyclerView() {
        adapter = new ProductAdapter(product -> {
            // Khi người dùng nhấn vào sản phẩm: lấy id và truyền qua Intent
            Intent intent = new Intent(MainActivity.this, ProductDetailActivity.class);
            intent.putExtra(ProductDetailActivity.EXTRA_PRODUCT_ID, product.getId());
            startActivity(intent);
        });

        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        rvProducts.setAdapter(adapter);
    }

    private void setupListeners() {
        // Nút Thêm sản phẩm
        View.OnClickListener openAddAction = v -> {
            Intent intent = new Intent(MainActivity.this, AddProductActivity.class);
            startActivity(intent);
        };
        btnAddProduct.setOnClickListener(openAddAction);
        btnEmptyAdd.setOnClickListener(openAddAction);

        // Nút Đăng xuất
        btnLogout.setOnClickListener(v -> showLogoutConfirmation());
    }

    /**
     * BẮT BUỘC: Khi quay lại từ AddProductActivity hoặc ProductDetailActivity,
     * onResume() sẽ truy vấn lại SQLite và cập nhật RecyclerView + 3 thẻ thống kê.
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadDataAndStats();
    }

    /** Tải lại toàn bộ dữ liệu từ SQLite và cập nhật giao diện. */
    private void loadDataAndStats() {
        // 1. Truy vấn SQLite lấy danh sách sản phẩm
        // (ArrayList chỉ chứa kết quả truy vấn để truyền cho Adapter)
        ArrayList<Product> products = dbHelper.getAllProducts();
        adapter.setProducts(products);

        // 2. Truy vấn 3 chỉ số thống kê từ SQLite
        int totalProducts = dbHelper.countProducts();
        long totalValue = dbHelper.getTotalValue();
        int lowStockCount = dbHelper.countLowStock();

        // 3. Cập nhật giao diện thống kê
        tvTotalProducts.setText(String.valueOf(totalProducts));
        tvTotalValue.setText(FormatUtils.formatCurrency(totalValue));
        tvLowStockCount.setText(String.valueOf(lowStockCount));
        tvProductCountBadge.setText(getString(R.string.product_count_format, totalProducts));

        // 4. Xử lý trạng thái danh sách trống
        if (products.isEmpty()) {
            rvProducts.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
        } else {
            rvProducts.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
        }
    }

    /** Hộp thoại xác nhận đăng xuất. */
    private void showLogoutConfirmation() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.logout_title)
                .setMessage(R.string.logout_message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_logout, (dialog, which) -> {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .show();
    }
}

