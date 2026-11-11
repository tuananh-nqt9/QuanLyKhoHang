package com.example.quanlykhohang;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quanlykhohang.database.DatabaseHelper;
import com.example.quanlykhohang.utils.UiUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Màn hình Đăng nhập (Màn hình 1).
 * <p>
 * Yêu cầu:
 * <ul>
 *     <li>Không hardcode tài khoản trong code của Activity.</li>
 *     <li>Gọi {@link DatabaseHelper#checkLogin(String, String)} để kiểm tra với bảng users trong SQLite.</li>
 *     <li>Validation: nếu rỗng hiển thị lỗi; nếu sai tài khoản hiển thị chính xác: "Sai tên đăng nhập hoặc mật khẩu".</li>
 *     <li>Đăng nhập thành công: chuyển sang {@link MainActivity} bằng Intent và gọi {@link #finish()}.</li>
 * </ul>
 */
public class LoginActivity extends AppCompatActivity {

    public static final String EXTRA_LOGGED_IN_USER = "extra_logged_in_user";

    private View rootScrollView;
    private View statusBarScrim;
    private TextInputLayout tilUsername;
    private TextInputLayout tilPassword;
    private TextInputEditText edtUsername;
    private TextInputEditText edtPassword;
    private TextView tvErrorMessage;
    private MaterialButton btnLogin;
    private View demoAccountLayout;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UiUtils.enableEdgeToEdge(this);
        setContentView(R.layout.activity_login);

        dbHelper = DatabaseHelper.getInstance(this);

        initViews();
        setupInsets();
        setupListeners();
    }

    private void initViews() {
        rootScrollView = findViewById(R.id.rootScrollView);
        statusBarScrim = findViewById(R.id.statusBarScrim);
        tilUsername = findViewById(R.id.tilUsername);
        tilPassword = findViewById(R.id.tilPassword);
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);
        btnLogin = findViewById(R.id.btnLogin);
        demoAccountLayout = findViewById(R.id.demoAccountLayout);
    }

    private void setupInsets() {
        UiUtils.applyWindowInsets(rootScrollView, (bars, imeBottom) -> {
            UiUtils.setHeight(statusBarScrim, bars.top);
            rootScrollView.setPadding(bars.left, 0, bars.right, Math.max(bars.bottom, imeBottom));
        });
    }

    private void setupListeners() {
        // Nhấn nút Đăng nhập
        btnLogin.setOnClickListener(v -> performLogin());

        // Nhấn nút Done trên bàn phím ảo ở ô mật khẩu để đăng nhập luôn
        edtPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                performLogin();
                return true;
            }
            return false;
        });

        // Tự động xóa thông báo lỗi khi người dùng bắt đầu gõ lại
        TextWatcher clearErrorWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                clearErrors();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
        edtUsername.addTextChangedListener(clearErrorWatcher);
        edtPassword.addTextChangedListener(clearErrorWatcher);

        // Chạm vào khối tài khoản mẫu để tự động điền nhanh cho việc kiểm thử
        demoAccountLayout.setOnClickListener(v -> {
            edtUsername.setText(getString(R.string.demo_username));
            edtPassword.setText(getString(R.string.demo_password));
            edtPassword.setSelection(edtPassword.getText() != null ? edtPassword.getText().length() : 0);
            clearErrors();
        });
    }

    /** Xử lý logic đăng nhập: validation -> truy vấn SQLite -> điều hướng. */
    private void performLogin() {
        clearErrors();

        String username = edtUsername.getText() != null ? edtUsername.getText().toString().trim() : "";
        String password = edtPassword.getText() != null ? edtPassword.getText().toString() : "";

        boolean hasError = false;

        if (username.isEmpty()) {
            tilUsername.setError(getString(R.string.error_username_empty));
            hasError = true;
        }

        if (password.isEmpty()) {
            tilPassword.setError(getString(R.string.error_password_empty));
            hasError = true;
        }

        if (hasError) {
            showGeneralError(getString(R.string.error_login_empty));
            return;
        }

        // BẮT BUỘC: Kiểm tra tài khoản bằng truy vấn bảng users trong SQLite
        boolean isValid = dbHelper.checkLogin(username, password);

        if (isValid) {
            UiUtils.hideKeyboard(this);

            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra(EXTRA_LOGGED_IN_USER, username);
            startActivity(intent);

            // Gọi finish() để khi ở MainActivity nhấn nút Back sẽ không quay về LoginActivity
            finish();
        } else {
            // Hiển thị chính xác thông báo theo yêu cầu đề bài
            showGeneralError(getString(R.string.error_login_invalid));
            UiUtils.shake(findViewById(R.id.loginCard));
        }
    }

    private void showGeneralError(String message) {
        tvErrorMessage.setText(message);
        tvErrorMessage.setVisibility(View.VISIBLE);
    }

    private void clearErrors() {
        tilUsername.setError(null);
        tilPassword.setError(null);
        tvErrorMessage.setVisibility(View.GONE);
    }
}

