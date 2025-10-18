package vn.flower.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record CartView(
    Integer orderId,
    List<CartLine> lines,
    BigDecimal subtotal,
    BigDecimal shipping,
    BigDecimal total
) {}
