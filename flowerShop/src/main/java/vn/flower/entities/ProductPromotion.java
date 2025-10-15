package vn.flower.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "Product_Promotions")
public class ProductPromotion {
  @EmbeddedId
  private ProductPromotionId id;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("productId")
  @JoinColumn(name = "ProductId")
  private Product product;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("promotionId")
  @JoinColumn(name = "PromotionId")
  private Promotion promotion;

  // getters/setters
  public ProductPromotionId getId(){ return id; }
  public void setId(ProductPromotionId id){ this.id = id; }
  public Product getProduct(){ return product; }
  public void setProduct(Product product){ this.product = product; }
  public Promotion getPromotion(){ return promotion; }
  public void setPromotion(Promotion promotion){ this.promotion = promotion; }
}
