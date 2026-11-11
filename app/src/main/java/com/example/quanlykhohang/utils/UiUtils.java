package com.example.quanlykhohang.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;

import androidx.activity.ComponentActivity;
import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.quanlykhohang.R;
import com.google.android.material.snackbar.Snackbar;

/**
 * Các hàm hỗ trợ giao diện dùng chung cho 4 Activity:
 * hiển thị tràn viền (edge-to-edge), xử lý vùng Status Bar / Navigation Bar / bàn phím,
 * hiển thị thông báo và các hiệu ứng nhỏ.
 */
public final class UiUtils {

    /** Kiểu thông báo, quyết định màu nền của Snackbar. */
    public enum MessageType { SUCCESS, WARNING, INFO }

    /** Callback nhận kích thước vùng hệ thống (status bar, navigation bar) và bàn phím. */
    public interface InsetsCallback {
        void onInsetsChanged(@NonNull Insets systemBars, int imeBottom);
    }

    private UiUtils() {
    }

    /** Bật chế độ tràn viền: status bar trong suốt với icon sáng (nền header màu Navy). */
    public static void enableEdgeToEdge(@NonNull ComponentActivity activity) {
        EdgeToEdge.enable(activity,
                SystemBarStyle.dark(Color.TRANSPARENT),
                SystemBarStyle.light(Color.TRANSPARENT, Color.argb(0x80, 0x1B, 0x1B, 0x1B)));
    }

    /** Lắng nghe thay đổi vùng hệ thống và bàn phím trên view gốc. */
    public static void applyWindowInsets(@NonNull View root, @NonNull InsetsCallback callback) {
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());
            int imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
            callback.onInsetsChanged(bars, imeBottom);
            return insets;
        });
    }

    /**
     * Cấu hình chuẩn cho màn hình có header ở trên và form ở dưới:
     * - Header được đệm thêm chiều cao Status Bar để nội dung không bị che.
     * - View gốc được đệm dưới bằng Navigation Bar hoặc bàn phím (lấy giá trị lớn hơn),
     *   nhờ đó ScrollView co lại và ô nhập đang focus không bị bàn phím che.
     */
    public static void applyScreenInsets(@NonNull View root, @NonNull View header,
                                         @Nullable View statusBarScrim) {
        final int headerPaddingTop = header.getPaddingTop();
        applyWindowInsets(root, (bars, imeBottom) -> {
            header.setPadding(header.getPaddingLeft(), headerPaddingTop + bars.top,
                    header.getPaddingRight(), header.getPaddingBottom());
            root.setPadding(bars.left, 0, bars.right, Math.max(bars.bottom, imeBottom));
            if (statusBarScrim != null) {
                setHeight(statusBarScrim, bars.top);
            }
        });
    }

    /** Đặt chiều cao cho một view. */
    public static void setHeight(@NonNull View view, int height) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params != null && params.height != height) {
            params.height = height;
            view.setLayoutParams(params);
        }
    }

    /** Hiển thị Snackbar với màu theo loại thông báo. */
    public static void showMessage(@NonNull View anyView, @NonNull String message,
                                   @NonNull MessageType type) {
        Context context = anyView.getContext();
        int backgroundColor;
        switch (type) {
            case SUCCESS:
                backgroundColor = R.color.mint;
                break;
            case WARNING:
                backgroundColor = R.color.coral;
                break;
            default:
                backgroundColor = R.color.navy;
                break;
        }
        Snackbar snackbar = Snackbar.make(anyView, message, Snackbar.LENGTH_SHORT);
        snackbar.setBackgroundTint(ContextCompat.getColor(context, backgroundColor));
        snackbar.setTextColor(ContextCompat.getColor(context, R.color.white));
        snackbar.show();
    }

    /** Hiệu ứng "nảy" nhẹ khi số liệu thay đổi. */
    public static void pulse(@NonNull View view) {
        view.animate().cancel();
        view.animate()
                .scaleX(1.18f).scaleY(1.18f)
                .setDuration(120)
                .withEndAction(() -> view.animate().scaleX(1f).scaleY(1f).setDuration(160).start())
                .start();
    }

    /** Hiệu ứng rung ngang để nhấn mạnh lỗi. */
    public static void shake(@NonNull View view) {
        view.startAnimation(AnimationUtils.loadAnimation(view.getContext(), R.anim.shake));
    }

    /** Ẩn bàn phím ảo. */
    public static void hideKeyboard(@NonNull Activity activity) {
        View focused = activity.getCurrentFocus();
        if (focused == null) {
            return;
        }
        InputMethodManager imm =
                (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(focused.getWindowToken(), 0);
        }
    }

    /** Viết hoa chữ cái đầu: "admin" → "Admin". */
    @NonNull
    public static String capitalize(@Nullable String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }
        String value = text.trim();
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }
}

