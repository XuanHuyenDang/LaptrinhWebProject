// java/vn/flower/api/dto/DiscountCodeDTO.java
package vn.flower.api.dto;
import java.math.BigDecimal;

public record DiscountCodeDTO(
    String code,
    String description,
    BigDecimal discountPercent,
    BigDecimal discountAmount
    // Thêm các trường khác nếu frontend cần
) {}