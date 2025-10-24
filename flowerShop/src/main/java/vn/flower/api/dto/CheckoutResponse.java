package vn.flower.api.dto;

public record CheckoutResponse(
    Integer orderId,
    String paymentMethod,
    String paymentUrl, // URL thanh toán (null nếu là COD)
    String status      // Trạng thái của đơn hàng ("Đang xử lý" hoặc "Chờ thanh toán")
) {}