package vn.flower.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Promotions")
public class Promotion {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "PromotionName", nullable = false, length = 255)
  private String promotionName;

  @Column(name = "DiscountPercent", precision = 5, scale = 2, nullable = false)
  private BigDecimal discountPercent;

  @Column(name = "StartDate", nullable = false)
  private LocalDateTime startDate;

  @Column(name = "EndDate", nullable = false)
  private LocalDateTime endDate;

  // getters/setters
  public Integer getId(){ return id; }
  public void setId(Integer id){ this.id = id; }
  public String getPromotionName(){ return promotionName; }
  public void setPromotionName(String promotionName){ this.promotionName = promotionName; }
  public BigDecimal getDiscountPercent(){ return discountPercent; }
  public void setDiscountPercent(BigDecimal discountPercent){ this.discountPercent = discountPercent; }
  public LocalDateTime getStartDate(){ return startDate; }
  public void setStartDate(LocalDateTime startDate){ this.startDate = startDate; }
  public LocalDateTime getEndDate(){ return endDate; }
  public void setEndDate(LocalDateTime endDate){ this.endDate = endDate; }
}
