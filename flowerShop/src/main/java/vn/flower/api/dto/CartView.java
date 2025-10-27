package vn.flower.api.dto;

import vn.flower.entities.DiscountCode; // Thêm import
import java.math.BigDecimal;
import java.util.List;

public record CartView(
    Integer orderId,
    List<CartLine> lines,
    BigDecimal subtotal,    // Tổng tiền hàng
    BigDecimal shipping,    // Phí ship
    BigDecimal discount,    // Số tiền giảm giá
    BigDecimal total,       // Tổng cuối cùng = subtotal + shipping - discount
    DiscountCodeDTO appliedDiscountCode // <- Thay thế bằng DTO // Mã giảm giá đã áp dụng (có thể null)
) {}