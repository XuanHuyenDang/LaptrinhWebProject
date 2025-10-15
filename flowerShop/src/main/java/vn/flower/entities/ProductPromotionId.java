package vn.flower.entities;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ProductPromotionId implements Serializable {
  @Column(name = "ProductId")
  private Integer productId;

  @Column(name = "PromotionId")
  private Integer promotionId;

  public ProductPromotionId() {}
  public ProductPromotionId(Integer productId, Integer promotionId){
    this.productId = productId; this.promotionId = promotionId;
  }

  public Integer getProductId(){ return productId; }
  public void setProductId(Integer productId){ this.productId = productId; }
  public Integer getPromotionId(){ return promotionId; }
  public void setPromotionId(Integer promotionId){ this.promotionId = promotionId; }

  @Override public boolean equals(Object o){
    if(this == o) return true;
    if(!(o instanceof ProductPromotionId that)) return false;
    return Objects.equals(productId, that.productId)
        && Objects.equals(promotionId, that.promotionId);
  }
  @Override public int hashCode(){ return Objects.hash(productId, promotionId); }
}
