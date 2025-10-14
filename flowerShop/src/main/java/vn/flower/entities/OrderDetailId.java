package vn.flower.entities;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class OrderDetailId implements Serializable {
  @Column(name = "OrderId")
  private Integer orderId;

  @Column(name = "ProductId")
  private Integer productId;

  public OrderDetailId() {}
  public OrderDetailId(Integer orderId, Integer productId){
    this.orderId = orderId; this.productId = productId;
  }

  public Integer getOrderId(){ return orderId; }
  public void setOrderId(Integer orderId){ this.orderId = orderId; }
  public Integer getProductId(){ return productId; }
  public void setProductId(Integer productId){ this.productId = productId; }

  @Override public boolean equals(Object o){
    if(this == o) return true;
    if(!(o instanceof OrderDetailId that)) return false;
    return Objects.equals(orderId, that.orderId)
        && Objects.equals(productId, that.productId);
  }
  @Override public int hashCode(){ return Objects.hash(orderId, productId); }
}
