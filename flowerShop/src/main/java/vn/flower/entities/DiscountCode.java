package vn.flower.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "DiscountCodes")
public class DiscountCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "Code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "DiscountPercent", precision = 5, scale = 2)
    private BigDecimal discountPercent; // Giảm theo % (null nếu giảm cố định)

    @Column(name = "DiscountAmount", precision = 18, scale = 2)
    private BigDecimal discountAmount; // Giảm số tiền cố định (null nếu giảm %)

    @Column(name = "MinOrderAmount", precision = 18, scale = 2)
    private BigDecimal minOrderAmount; // Đơn hàng tối thiểu

    @Column(name = "MaxDiscountAmount", precision = 18, scale = 2)
    private BigDecimal maxDiscountAmount; // Giảm tối đa (khi dùng %)

    @Column(name = "StartDate", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "EndDate", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "MaxUsage")
    private Integer maxUsage; // Số lần dùng tối đa (null = vô hạn)

    @Column(name = "CurrentUsage", nullable = false)
    private int currentUsage = 0; // Số lần đã dùng

    @Column(name = "IsActive", nullable = false)
    private boolean isActive = true; // Trạng thái hoạt động

    @Column(name = "Description", length = 255)
    private String description;

    // Getters and Setters
    // ... (Tạo getters và setters cho tất cả các trường)

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public BigDecimal getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(BigDecimal discountPercent) { this.discountPercent = discountPercent; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public BigDecimal getMinOrderAmount() { return minOrderAmount; }
    public void setMinOrderAmount(BigDecimal minOrderAmount) { this.minOrderAmount = minOrderAmount; }
    public BigDecimal getMaxDiscountAmount() { return maxDiscountAmount; }
    public void setMaxDiscountAmount(BigDecimal maxDiscountAmount) { this.maxDiscountAmount = maxDiscountAmount; }
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    public Integer getMaxUsage() { return maxUsage; }
    public void setMaxUsage(Integer maxUsage) { this.maxUsage = maxUsage; }
    public int getCurrentUsage() { return currentUsage; }
    public void setCurrentUsage(int currentUsage) { this.currentUsage = currentUsage; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}