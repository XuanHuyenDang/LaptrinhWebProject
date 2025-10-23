package vn.flower.entities;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class WishlistId implements Serializable {
  
  @Column(name = "AccountId")
  private Integer accountId;

  @Column(name = "ProductId")
  private Integer productId;

  public WishlistId() {}
  
  public WishlistId(Integer accountId, Integer productId){
    this.accountId = accountId; 
    this.productId = productId;
  }
  
  // Getters, Setters, equals() và hashCode()

  public Integer getAccountId() { return accountId; }
  public void setAccountId(Integer accountId) { this.accountId = accountId; }
  public Integer getProductId() { return productId; }
  public void setProductId(Integer productId) { this.productId = productId; }

  @Override
  public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof WishlistId that)) return false;
      return Objects.equals(accountId, that.accountId) &&
             Objects.equals(productId, that.productId);
  }

  @Override
  public int hashCode() {
      return Objects.hash(accountId, productId);
  }
}