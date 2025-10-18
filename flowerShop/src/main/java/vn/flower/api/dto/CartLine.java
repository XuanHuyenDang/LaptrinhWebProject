package vn.flower.api.dto;

import java.math.BigDecimal;

public record CartLine(
    Integer productId,
    String productName,
    Integer quantity,
    BigDecimal price,
    BigDecimal lineTotal
) {}
