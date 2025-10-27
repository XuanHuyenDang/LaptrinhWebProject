package vn.flower.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.flower.entities.DiscountCode;
import vn.flower.repositories.DiscountCodeRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DiscountCodeService {

    @Autowired
    private DiscountCodeRepository discountCodeRepository;

    public List<DiscountCode> getAllDiscountCodes() {
        return discountCodeRepository.findAll();
    }

    public Optional<DiscountCode> findById(Integer id) {
        return discountCodeRepository.findById(id);
    }

    public DiscountCode save(DiscountCode discountCode) {
        // Có thể thêm validation ở đây trước khi lưu
        return discountCodeRepository.save(discountCode);
    }

    public void deleteById(Integer id) {
        discountCodeRepository.deleteById(id);
    }

    public Optional<DiscountCode> findByCode(String code) {
        return discountCodeRepository.findByCodeIgnoreCase(code);
    }

    /**
     * Kiểm tra xem mã giảm giá có hợp lệ để áp dụng không.
     * @param code Mã code cần kiểm tra
     * @param orderSubtotal Tổng tiền hàng (chưa bao gồm ship, chưa giảm giá)
     * @return DiscountCode nếu hợp lệ
     * @throws IllegalArgumentException Nếu mã không hợp lệ với lý do cụ thể
     */
    public DiscountCode validateDiscountCode(String code, BigDecimal orderSubtotal) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập mã giảm giá.");
        }

        DiscountCode dc = discountCodeRepository.findByCodeIgnoreCase(code.trim())
                .orElseThrow(() -> new IllegalArgumentException("Mã giảm giá không tồn tại hoặc không chính xác."));

        if (!dc.isActive()) {
            throw new IllegalArgumentException("Mã giảm giá này đã bị vô hiệu hóa.");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(dc.getStartDate())) {
            throw new IllegalArgumentException("Mã giảm giá chưa đến ngày bắt đầu sử dụng.");
        }
        if (now.isAfter(dc.getEndDate())) {
            throw new IllegalArgumentException("Mã giảm giá đã hết hạn sử dụng.");
        }

        if (dc.getMaxUsage() != null && dc.getCurrentUsage() >= dc.getMaxUsage()) {
            throw new IllegalArgumentException("Mã giảm giá đã hết lượt sử dụng.");
        }

        if (dc.getMinOrderAmount() != null && orderSubtotal.compareTo(dc.getMinOrderAmount()) < 0) {
            throw new IllegalArgumentException("Đơn hàng chưa đạt giá trị tối thiểu ("
                    + formatVND(dc.getMinOrderAmount()) + ") để áp dụng mã này.");
        }

        return dc;
    }

    /**
     * Tính toán số tiền được giảm dựa trên mã và tổng tiền hàng.
     * @param dc Mã giảm giá đã được validate
     * @param orderSubtotal Tổng tiền hàng
     * @return Số tiền được giảm
     */
    public BigDecimal calculateDiscountAmount(DiscountCode dc, BigDecimal orderSubtotal) {
        BigDecimal discount = BigDecimal.ZERO;

        if (dc.getDiscountPercent() != null) { // Giảm theo %
            discount = orderSubtotal.multiply(dc.getDiscountPercent()).divide(BigDecimal.valueOf(100));
            // Kiểm tra giới hạn giảm tối đa
            if (dc.getMaxDiscountAmount() != null && discount.compareTo(dc.getMaxDiscountAmount()) > 0) {
                discount = dc.getMaxDiscountAmount();
            }
        } else if (dc.getDiscountAmount() != null) { // Giảm cố định
            discount = dc.getDiscountAmount();
            // Đảm bảo không giảm nhiều hơn tổng tiền hàng
            if (discount.compareTo(orderSubtotal) > 0) {
                discount = orderSubtotal;
            }
        }
        return discount.max(BigDecimal.ZERO); // Đảm bảo không âm
    }

    // Helper format tiền tệ (có thể chuyển vào Util class)
    private String formatVND(BigDecimal amount) {
         try { return java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("vi", "VN")).format(amount); }
         catch(Exception e) { return amount + "đ"; }
    }
}