package vn.flower.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "OrderDetails")
public class OrderDetail {
  @EmbeddedId
  private OrderDetailId id;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("orderId")
  @JoinColumn(name = "OrderId")
  private Order order;

  @ManyToOne(fetch = FetchType.EAGER)
  @MapsId("productId")
  @JoinColumn(name = "ProductId")
  private Product product;

  @Column(name = "Quantity", nullable = false)
  private Integer quantity;

  @Column(name = "Price", precision = 18, scale = 2, nullable = false)
  private BigDecimal price;

  // getters/setters
  public OrderDetailId getId(){ return id; }
  public void setId(OrderDetailId id){ this.id = id; }
  public Order getOrder(){ return order; }
  public void setOrder(Order order){ this.order = order; }
  public Product getProduct(){ return product; }
  public void setProduct(Product product){ this.product = product; }
  public Integer getQuantity(){ return quantity; }
  public void setQuantity(Integer quantity){ this.quantity = quantity; }
  public BigDecimal getPrice(){ return price; }
  public void setPrice(BigDecimal price){ this.price = price; }
}
