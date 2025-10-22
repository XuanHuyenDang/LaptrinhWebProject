package vn.flower.api.dto;

/**
 * DTO này bao gồm thông tin thanh toán (từ CheckoutRequest)
 * và thông tin sản phẩm (productId, quantity) cho luồng "Mua ngay".
 */
public record BuyNowRequest(
    CheckoutRequest checkout,
    Integer productId,
    Integer quantity
) {}