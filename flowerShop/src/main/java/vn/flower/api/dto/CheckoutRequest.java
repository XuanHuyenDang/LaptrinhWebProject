package vn.flower.api.dto;

public record CheckoutRequest(
    String recipientName,
    String recipientPhone,
    String shippingAddress,
    String note,
    String paymentMethod,         // "COD" | "BANK"
    ShippingMethod shippingMethod // SAVING | FAST | EXPRESS
) {}
