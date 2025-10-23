package vn.flower.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Wishlist")
public class Wishlist {
  
  @EmbeddedId
  private WishlistId id;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("accountId") // Ánh xạ tới 'accountId' trong WishlistId
  @JoinColumn(name = "AccountId")
  private Account account;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("productId") // Ánh xạ tới 'productId' trong WishlistId
  @JoinColumn(name = "ProductId")
  private Product product;
  
  @Column(name = "CreatedAt")
  private LocalDateTime createdAt;

  // Getters, Setters...
  public WishlistId getId() { return id; }
  public void setId(WishlistId id) { this.id = id; }
  public Account getAccount() { return account; }
  public void setAccount(Account account) { this.account = account; }
  public Product getProduct() { return product; }
  public void setProduct(Product product) { this.product = product; }
  public LocalDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}