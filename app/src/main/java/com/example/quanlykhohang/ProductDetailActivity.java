package com.example.quanlykhohang;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.quanlykhohang.database.DatabaseHelper;
import com.example.quanlykhohang.model.Product;
import com.example.quanlykhohang.utils.CategoryStyle;
import com.example.quanlykhohang.utils.FormatUtils;
import com.example.quanlykhohang.utils.UiUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * Màn hình Chi tiết & Quản lý số lượng sản phẩm (Màn hình 4).
 * <p>
 * Yêu cầu:
 * <ul>
 *     <li>Nhận product_id qua Intent và truy vấn lại SQLite bằng {@link DatabaseHelper#getProductById(int)}.</li>
 *     <li>Không phụ thuộc vào dữ liệu giả hay bản sao cũ trong bộ nhớ.</li>
 *     <li>Nút "+ Nhập thêm 1": tăng quantity lên 1, ghi vào SQLite, truy vấn lại và cập nhật giao diện ngay.</li>
 *     <li>Nút "− Bán 1": giảm quantity đi 1 nếu > 0. Nếu quantity = 0, thông báo chính xác "Hết hàng" và không giảm nữa.</li>
 *     <li>Nút "Quay lại": đóng Activity, trở về {@link MainActivity} (RecyclerView & Thống kê tự cập nhật).</li>
 *     <li>Nút "Xóa sản phẩm": hiển thị AlertDialog xác nhận ("Hủy" / "Xóa sản phẩm"), nếu chọn xóa thì xóa khỏi SQLite và đóng Activity.</li>
 * </ul>
 */
public class ProductDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PRODUCT_ID = "extra_product_id";

    private View headerContent;
    private ImageButton btnHeaderBack;
    private FrameLayout iconContainer;
    private ImageView imgCategoryIcon;
    private TextView tvCategoryBadge;
    private TextView tvStatusBadge;
    private TextView tvProductName;
    private TextView tvProductPrice;
    private TextView tvQuantity;
    private TextView tvStockValue;
    private LinearLayout warningBanner;
    private TextView tvWarningText;
    private TextView tvDescription;
    private MaterialButton btnImportOne;
    private MaterialButton btnSellOne;
    private MaterialButton btnBack;
    private MaterialButton btnDeleteProduct;

    private DatabaseHelper dbHelper;
    private int productId = -1;
    private Product currentProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UiUtils.enableEdgeToEdge(this);
        setContentView(R.layout.activity_product_detail);

        dbHelper = DatabaseHelper.getInstance(this);

        productId = getIntent().getIntExtra(EXTRA_PRODUCT_ID, -1);
        if (productId == -1) {
            Toast.makeText(this, R.string.product_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupInsets();
        setupListeners();
        loadProduct();
    }

    private void initViews() {
        headerContent = findViewById(R.id.headerContent);
        btnHeaderBack = findViewById(R.id.btnHeaderBack);
        iconContainer = findViewById(R.id.iconContainer);
        imgCategoryIcon = findViewById(R.id.imgCategoryIcon);
        tvCategoryBadge = findViewById(R.id.tvCategoryBadge);
        tvStatusBadge = findViewById(R.id.tvStatusBadge);
        tvProductName = findViewById(R.id.tvProductName);
        tvProductPrice = findViewById(R.id.tvProductPrice);
        tvQuantity = findViewById(R.id.tvQuantity);
        tvStockValue = findViewById(R.id.tvStockValue);
        warningBanner = findViewById(R.id.warningBanner);
        tvWarningText = findViewById(R.id.tvWarningText);
        tvDescription = findViewById(R.id.tvDescription);
        btnImportOne = findViewById(R.id.btnImportOne);
        btnSellOne = findViewById(R.id.btnSellOne);
        btnBack = findViewById(R.id.btnBack);
        btnDeleteProduct = findViewById(R.id.btnDeleteProduct);
    }

    private void setupInsets() {
        final int headerTopPadding = headerContent.getPaddingTop();
        UiUtils.applyWindowInsets(findViewById(R.id.coordinatorLayout), (bars, imeBottom) -> {
            headerContent.setPadding(
                    headerContent.getPaddingLeft(),
                    headerTopPadding + bars.top,
                    headerContent.getPaddingRight(),
                    headerContent.getPaddingBottom()
            );
            findViewById(R.id.nestedScrollView).setPadding(
                    bars.left, 0, bars.right, Math.max(bars.bottom, imeBottom)
            );
        });
    }

    private void setupListeners() {
        // Nút Quay lại
        View.OnClickListener backAction = v -> finish();
        btnHeaderBack.setOnClickListener(backAction);
        btnBack.setOnClickListener(backAction);

        // Nút "+ Nhập thêm 1"
        btnImportOne.setOnClickListener(v -> handleImportOne());

        // Nút "− Bán 1"
        btnSellOne.setOnClickListener(v -> handleSellOne());

        // Nút "Xóa sản phẩm"
        btnDeleteProduct.setOnClickListener(v -> confirmDeleteProduct());
    }

    /**
     * BẮT BUỘC: Truy vấn lại SQLite theo id để hiển thị thông tin mới nhất.
     */
    private void loadProduct() {
        currentProduct = dbHelper.getProductById(productId);

        if (currentProduct == null) {
            Toast.makeText(this, R.string.product_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bindProductData(currentProduct);
    }

    /** Hiển thị toàn bộ thông tin sản phẩm lên giao diện. */
    private void bindProductData(Product product) {
        tvProductName.setText(product.getName());
        tvProductPrice.setText(FormatUtils.formatCurrency(product.getPrice()));
        tvQuantity.setText(String.valueOf(product.getQuantity()));
        tvStockValue.setText(FormatUtils.formatCurrency(product.getStockValue()));

        // Phân loại icon và màu sắc
        String category = product.getCategory();
        int iconRes = CategoryStyle.getIcon(category);
        int colorRes = CategoryStyle.getColor(category);
        int softColorRes = CategoryStyle.getSoftColor(category);

        int primaryColor = ContextCompat.getColor(this, colorRes);
        int softColor = ContextCompat.getColor(this, softColorRes);

        imgCategoryIcon.setImageResource(iconRes);
        imgCategoryIcon.setImageTintList(ColorStateList.valueOf(primaryColor));

        GradientDrawable iconBg = new GradientDrawable();
        iconBg.setColor(softColor);
        iconBg.setCornerRadius(getResources().getDimension(R.dimen.radius_icon));
        iconContainer.setBackground(iconBg);

        tvCategoryBadge.setText(category);
        tvCategoryBadge.setTextColor(primaryColor);
        GradientDrawable badgeBg = new GradientDrawable();
        badgeBg.setColor(softColor);
        badgeBg.setCornerRadius(999f);
        tvCategoryBadge.setBackground(badgeBg);

        // Trạng thái kinh doanh
        if (product.isActive()) {
            tvStatusBadge.setText(R.string.status_active);
            tvStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.mint));
            tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_success);
        } else {
            tvStatusBadge.setText(R.string.status_inactive);
            tvStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            tvStatusBadge.setBackgroundResource(R.drawable.bg_badge);
        }

        // Cảnh báo tồn kho
        if (product.isOutOfStock()) {
            warningBanner.setVisibility(View.VISIBLE);
            tvWarningText.setText(R.string.warning_out_of_stock);
            tvQuantity.setTextColor(ContextCompat.getColor(this, R.color.coral));
        } else if (product.isLowStock()) {
            warningBanner.setVisibility(View.VISIBLE);
            tvWarningText.setText(getString(R.string.warning_low_stock_format, product.getQuantity()));
            tvQuantity.setTextColor(ContextCompat.getColor(this, R.color.coral));
        } else {
            warningBanner.setVisibility(View.GONE);
            tvQuantity.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        }

        // Mô tả sản phẩm
        String desc = product.getDescription();
        if (desc == null || desc.trim().isEmpty()) {
            tvDescription.setText(R.string.description_empty);
            tvDescription.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            tvDescription.setTypeface(null, Typeface.ITALIC);
        } else {
            tvDescription.setText(desc.trim());
            tvDescription.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            tvDescription.setTypeface(null, Typeface.NORMAL);
        }
    }

    /**
     * Chức năng: Nhập thêm 1
     * <ol>
     *     <li>Lấy quantity hiện tại.</li>
     *     <li>Tăng lên 1 (kiểm tra tránh tràn số).</li>
     *     <li>Gọi DatabaseHelper.updateQuantity().</li>
     *     <li>Ghi số lượng mới vào SQLite.</li>
     *     <li>Truy vấn lại sản phẩm và cập nhật giao diện ngay.</li>
     * </ol>
     */
    private void handleImportOne() {
        if (currentProduct == null) return;

        int currentQty = currentProduct.getQuantity();
        if (currentQty >= Product.MAX_QUANTITY) {
            UiUtils.showMessage(btnImportOne, getString(R.string.quantity_max_reached),
                    UiUtils.MessageType.WARNING);
            return;
        }

        int newQuantity = currentQty + 1;
        int rows = dbHelper.updateQuantity(productId, newQuantity);

        if (rows > 0) {
            loadProduct();
            UiUtils.pulse(tvQuantity);
            UiUtils.showMessage(btnImportOne, getString(R.string.import_success),
                    UiUtils.MessageType.SUCCESS);
        } else {
            UiUtils.showMessage(btnImportOne, getString(R.string.update_failed),
                    UiUtils.MessageType.WARNING);
        }
    }

    /**
     * Chức năng: Bán 1
     * <ul>
     *     <li>Nếu quantity lớn hơn 0: giảm đi 1, gọi updateQuantity(), cập nhật lại giao diện.</li>
     *     <li>Nếu quantity bằng 0: không được giảm nữa, hiển thị chính xác thông báo: "Hết hàng".</li>
     *     <li>Không cho phép quantity âm.</li>
     * </ul>
     */
    private void handleSellOne() {
        if (currentProduct == null) return;

        int currentQty = currentProduct.getQuantity();

        // BẮT BUỘC: Nếu quantity bằng 0, không được giảm nữa và hiển thị "Hết hàng"
        if (currentQty <= 0) {
            Toast.makeText(this, R.string.out_of_stock, Toast.LENGTH_SHORT).show();
            UiUtils.shake(tvQuantity);
            return;
        }

        int newQuantity = currentQty - 1;
        int rows = dbHelper.updateQuantity(productId, newQuantity);

        if (rows > 0) {
            loadProduct();
            UiUtils.pulse(tvQuantity);
            UiUtils.showMessage(btnSellOne, getString(R.string.sell_success),
                    UiUtils.MessageType.INFO);
        } else {
            UiUtils.showMessage(btnSellOne, getString(R.string.update_failed),
                    UiUtils.MessageType.WARNING);
        }
    }

    /**
     * Chức năng: Xóa sản phẩm
     * <p>
     * Hiển thị AlertDialog xác nhận ("Hủy" / "Xóa sản phẩm").
     * Khi người dùng chọn Xóa:
     * <ol>
     *     <li>Gọi DatabaseHelper.deleteProduct(id).</li>
     *     <li>Xóa bản ghi khỏi SQLite.</li>
     *     <li>Hiển thị thông báo thành công.</li>
     *     <li>Đóng ProductDetailActivity, quay về MainActivity.</li>
     * </ol>
     */
    private void confirmDeleteProduct() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_dialog_title)
                .setMessage(R.string.delete_confirm_message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_delete_product, (dialog, which) -> {
                    int rows = dbHelper.deleteProduct(productId);
                    if (rows > 0) {
                        Toast.makeText(this, R.string.delete_success, Toast.LENGTH_SHORT).show();
                    }
                    finish();
                })
                .show();
    }
}

