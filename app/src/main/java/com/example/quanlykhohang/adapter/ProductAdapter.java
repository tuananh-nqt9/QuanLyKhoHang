package com.example.quanlykhohang.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlykhohang.R;
import com.example.quanlykhohang.model.Product;
import com.example.quanlykhohang.utils.CategoryStyle;
import com.example.quanlykhohang.utils.FormatUtils;

import java.util.ArrayList;

/**
 * Adapter cho RecyclerView hiển thị danh sách sản phẩm ở MainActivity.
 * <p>
 * Lưu ý: ArrayList ở đây chỉ dùng để chứa kết quả truy vấn từ SQLite và hiển thị lên giao diện,
 * hoàn toàn không dùng làm nơi lưu trữ dữ liệu chính.
 */
public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    public interface OnProductClickListener {
        void onProductClick(@NonNull Product product);
    }

    private final ArrayList<Product> products = new ArrayList<>();
    private final OnProductClickListener listener;

    public ProductAdapter(OnProductClickListener listener) {
        this.listener = listener;
    }

    /** Cập nhật toàn bộ danh sách sản phẩm (kết quả truy vấn mới từ SQLite). */
    public void setProducts(ArrayList<Product> newProducts) {
        products.clear();
        if (newProducts != null) {
            products.addAll(newProducts);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);
        holder.bind(product, listener);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {

        private final View cardProduct;
        private final FrameLayout iconContainer;
        private final ImageView imgCategoryIcon;
        private final TextView tvProductName;
        private final TextView tvCategoryBadge;
        private final TextView tvInactiveBadge;
        private final TextView tvProductPrice;
        private final TextView tvQuantity;
        private final TextView tvLowStockBadge;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            cardProduct = itemView.findViewById(R.id.cardProduct);
            iconContainer = itemView.findViewById(R.id.iconContainer);
            imgCategoryIcon = itemView.findViewById(R.id.imgCategoryIcon);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvCategoryBadge = itemView.findViewById(R.id.tvCategoryBadge);
            tvInactiveBadge = itemView.findViewById(R.id.tvInactiveBadge);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvLowStockBadge = itemView.findViewById(R.id.tvLowStockBadge);
        }

        void bind(Product product, OnProductClickListener listener) {
            Context context = itemView.getContext();

            tvProductName.setText(product.getName());
            tvProductPrice.setText(FormatUtils.formatCurrency(product.getPrice()));
            tvQuantity.setText("SL: " + product.getQuantity());

            // 1. Phân loại icon và màu sắc theo loại sản phẩm
            String category = product.getCategory();
            int iconRes = CategoryStyle.getIcon(category);
            int colorRes = CategoryStyle.getColor(category);
            int softColorRes = CategoryStyle.getSoftColor(category);

            int primaryCatColor = ContextCompat.getColor(context, colorRes);
            int softCatColor = ContextCompat.getColor(context, softColorRes);

            imgCategoryIcon.setImageResource(iconRes);
            imgCategoryIcon.setImageTintList(ColorStateList.valueOf(primaryCatColor));

            // Đặt nền màu nhạt cho ô icon bên trái
            GradientDrawable iconBg = new GradientDrawable();
            iconBg.setColor(softCatColor);
            iconBg.setCornerRadius(context.getResources().getDimension(R.dimen.radius_icon));
            iconContainer.setBackground(iconBg);

            // Badge loại sản phẩm
            tvCategoryBadge.setText(category);
            tvCategoryBadge.setTextColor(primaryCatColor);
            GradientDrawable badgeBg = new GradientDrawable();
            badgeBg.setColor(softCatColor);
            badgeBg.setCornerRadius(999f);
            tvCategoryBadge.setBackground(badgeBg);

            // Badge tạm ngừng kinh doanh nếu active = false
            tvInactiveBadge.setVisibility(product.isActive() ? View.GONE : View.VISIBLE);

            // 2. Trạng thái tồn kho: quantity < 5 là "Sắp hết hàng"
            if (product.isOutOfStock()) {
                // Số lượng = 0: Hết hàng (vẫn thuộc nhóm quantity < 5)
                tvLowStockBadge.setVisibility(View.VISIBLE);
                tvLowStockBadge.setText(R.string.badge_out_of_stock);
                tvLowStockBadge.setTextColor(ContextCompat.getColor(context, R.color.coral));
                tvQuantity.setTextColor(ContextCompat.getColor(context, R.color.coral));
            } else if (product.isLowStock()) {
                // 1 <= quantity < 5: Sắp hết hàng
                tvLowStockBadge.setVisibility(View.VISIBLE);
                tvLowStockBadge.setText(R.string.badge_low_stock);
                tvLowStockBadge.setTextColor(ContextCompat.getColor(context, R.color.coral));
                tvQuantity.setTextColor(ContextCompat.getColor(context, R.color.coral));
            } else {
                // quantity >= 5: Bình thường
                tvLowStockBadge.setVisibility(View.GONE);
                tvQuantity.setTextColor(ContextCompat.getColor(context, R.color.text_primary));
            }

            // 3. Sự kiện nhấn vào Card sản phẩm
            cardProduct.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductClick(product);
                }
            });
        }
    }
}

