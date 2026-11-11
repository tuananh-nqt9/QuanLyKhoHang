package com.example.quanlykhohang;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quanlykhohang.database.DatabaseHelper;
import com.example.quanlykhohang.model.Product;
import com.example.quanlykhohang.utils.CategoryStyle;
import com.example.quanlykhohang.utils.FormatUtils;
import com.example.quanlykhohang.utils.UiUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Màn hình Thêm sản phẩm mới (Màn hình 3).
 * <p>
 * Yêu cầu:
 * <ul>
 *     <li>Tên sản phẩm: không được để trống (kể cả chỉ toàn khoảng trắng).</li>
 *     <li>Giá bán: số nguyên lớn hơn 0, đơn vị VNĐ. Tránh tràn số với {@code long}.</li>
 *     <li>Số lượng: số nguyên không âm (cho phép nhập 0).</li>
 *     <li>Loại sản phẩm: Spinner gồm đúng 5 giá trị (Điện tử, Thực phẩm, Văn phòng phẩm, Gia dụng, Khác).</li>
 *     <li>Đang kinh doanh: Switch mặc định bật (active = 1; tắt = 0).</li>
 *     <li>Mô tả: cho phép để trống.</li>
 *     <li>Lưu thành công: ghi vào SQLite qua {@link DatabaseHelper#insertProduct(Product)},
 *         thông báo thành công và {@link #finish()} để quay về {@link MainActivity}.</li>
 * </ul>
 */
public class AddProductActivity extends AppCompatActivity {

    private View headerContent;
    private ImageButton btnBack;
    private TextInputLayout tilProductName;
    private TextInputEditText edtProductName;
    private Spinner spinnerCategory;
    private TextInputLayout tilPrice;
    private TextInputEditText edtPrice;
    private TextView tvPricePreview;
    private TextInputLayout tilQuantity;
    private TextInputEditText edtQuantity;
    private MaterialSwitch switchActive;
    private TextView tvActiveDescription;
    private TextInputLayout tilDescription;
    private TextInputEditText edtDescription;
    private MaterialButton btnSaveProduct;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UiUtils.enableEdgeToEdge(this);
        setContentView(R.layout.activity_add_product);

        dbHelper = DatabaseHelper.getInstance(this);

        initViews();
        setupInsets();
        setupCategorySpinner();
        setupListeners();
    }

    private void initViews() {
        headerContent = findViewById(R.id.headerContent);
        btnBack = findViewById(R.id.btnBack);
        tilProductName = findViewById(R.id.tilProductName);
        edtProductName = findViewById(R.id.edtProductName);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        tilPrice = findViewById(R.id.tilPrice);
        edtPrice = findViewById(R.id.edtPrice);
        tvPricePreview = findViewById(R.id.tvPricePreview);
        tilQuantity = findViewById(R.id.tilQuantity);
        edtQuantity = findViewById(R.id.edtQuantity);
        switchActive = findViewById(R.id.switchActive);
        tvActiveDescription = findViewById(R.id.tvActiveDescription);
        tilDescription = findViewById(R.id.tilDescription);
        edtDescription = findViewById(R.id.edtDescription);
        btnSaveProduct = findViewById(R.id.btnSaveProduct);
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

    private void setupCategorySpinner() {
        // Đúng 5 giá trị: Điện tử, Thực phẩm, Văn phòng phẩm, Gia dụng, Khác
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.product_categories,
                R.layout.item_spinner
        );
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerCategory.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        // Thay đổi mô tả khi bật / tắt switch Đang kinh doanh
        switchActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
            tvActiveDescription.setText(isChecked
                    ? R.string.active_on_description
                    : R.string.active_off_description);
        });

        // Hiển thị live preview giá bán khi người dùng đang nhập
        edtPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilPrice.setError(null);
                updatePricePreview(s != null ? s.toString() : "");
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Tự xóa lỗi khi người dùng gõ
        edtProductName.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilProductName.setError(null);
            }
        });

        edtQuantity.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilQuantity.setError(null);
            }
        });

        // Nút Lưu sản phẩm
        btnSaveProduct.setOnClickListener(v -> saveProduct());
    }

    private void updatePricePreview(String text) {
        String trimmed = text.trim();
        if (trimmed.isEmpty()) {
            tvPricePreview.setVisibility(View.GONE);
            return;
        }
        try {
            long price = Long.parseLong(trimmed);
            if (price > 0 && price <= Product.MAX_PRICE) {
                tvPricePreview.setText(getString(R.string.price_preview_format,
                        FormatUtils.formatCurrency(price)));
                tvPricePreview.setVisibility(View.VISIBLE);
            } else {
                tvPricePreview.setVisibility(View.GONE);
            }
        } catch (NumberFormatException e) {
            tvPricePreview.setVisibility(View.GONE);
        }
    }

    /** Xử lý validation và lưu sản phẩm vào SQLite. */
    private void saveProduct() {
        // 1. Lấy dữ liệu người dùng nhập
        String name = edtProductName.getText() != null ? edtProductName.getText().toString().trim() : "";
        String priceStr = edtPrice.getText() != null ? edtPrice.getText().toString().trim() : "";
        String quantityStr = edtQuantity.getText() != null ? edtQuantity.getText().toString().trim() : "";
        String category = spinnerCategory.getSelectedItem() != null
                ? spinnerCategory.getSelectedItem().toString()
                : CategoryStyle.OTHER;
        boolean active = switchActive.isChecked();
        String description = edtDescription.getText() != null ? edtDescription.getText().toString().trim() : "";

        // 2. Kiểm tra validation
        boolean isValid = true;
        View firstErrorView = null;

        // Tên sản phẩm không được rỗng (kể cả chỉ toàn khoảng trắng)
        if (name.isEmpty()) {
            tilProductName.setError(getString(R.string.error_name_empty));
            isValid = false;
            firstErrorView = edtProductName;
        }

        // Giá bán
        long price = 0;
        if (priceStr.isEmpty()) {
            tilPrice.setError(getString(R.string.error_price_empty));
            isValid = false;
            if (firstErrorView == null) firstErrorView = edtPrice;
        } else {
            try {
                price = Long.parseLong(priceStr);
                if (price <= 0) {
                    tilPrice.setError(getString(R.string.error_price_not_positive));
                    isValid = false;
                    if (firstErrorView == null) firstErrorView = edtPrice;
                } else if (price > Product.MAX_PRICE) {
                    tilPrice.setError(getString(R.string.error_price_too_large,
                            FormatUtils.formatCurrency(Product.MAX_PRICE)));
                    isValid = false;
                    if (firstErrorView == null) firstErrorView = edtPrice;
                }
            } catch (NumberFormatException e) {
                tilPrice.setError(getString(R.string.error_price_invalid));
                isValid = false;
                if (firstErrorView == null) firstErrorView = edtPrice;
            }
        }

        // Số lượng
        int quantity = 0;
        if (quantityStr.isEmpty()) {
            tilQuantity.setError(getString(R.string.error_quantity_empty));
            isValid = false;
            if (firstErrorView == null) firstErrorView = edtQuantity;
        } else {
            try {
                quantity = Integer.parseInt(quantityStr);
                if (quantity < 0) {
                    tilQuantity.setError(getString(R.string.error_quantity_negative));
                    isValid = false;
                    if (firstErrorView == null) firstErrorView = edtQuantity;
                } else if (quantity > Product.MAX_QUANTITY) {
                    tilQuantity.setError(getString(R.string.error_quantity_too_large,
                            FormatUtils.formatNumber(Product.MAX_QUANTITY)));
                    isValid = false;
                    if (firstErrorView == null) firstErrorView = edtQuantity;
                }
            } catch (NumberFormatException e) {
                tilQuantity.setError(getString(R.string.error_quantity_invalid));
                isValid = false;
                if (firstErrorView == null) firstErrorView = edtQuantity;
            }
        }

        if (!isValid) {
            if (firstErrorView != null) {
                firstErrorView.requestFocus();
            }
            UiUtils.showMessage(btnSaveProduct, getString(R.string.error_form_invalid),
                    UiUtils.MessageType.WARNING);
            return;
        }

        // 3. Tạo đối tượng Product và gọi DatabaseHelper.insertProduct()
        Product product = new Product(name, price, quantity, category, active, description);
        long newId = dbHelper.insertProduct(product);

        // 4. Kiểm tra kết quả
        if (newId != -1) {
            Toast.makeText(this, R.string.add_success, Toast.LENGTH_SHORT).show();
            // Đóng AddProductActivity, quay về MainActivity (MainActivity.onResume sẽ tự cập nhật)
            finish();
        } else {
            // Thất bại: thông báo lỗi và giữ nguyên dữ liệu đã nhập
            UiUtils.showMessage(btnSaveProduct, getString(R.string.add_failed),
                    UiUtils.MessageType.WARNING);
        }
    }

    /** Lớp tiện ích rút gọn TextWatcher. */
    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }
}

