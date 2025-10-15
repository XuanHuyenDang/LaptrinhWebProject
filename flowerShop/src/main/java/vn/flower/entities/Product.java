package vn.flower.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "Products")
public class Product {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "ProductName", nullable = false, length = 255)
  private String productName;

  @Column(name = "ProductDescription", columnDefinition = "NVARCHAR(MAX)")
  private String productDescription;

  @Column(name = "ImageUrl", length = 500)
  private String imageUrl;

  @Column(name = "Price", precision = 18, scale = 2, nullable = false)
  private BigDecimal price;

  @Column(name = "SalePrice", precision = 18, scale = 2)
  private BigDecimal salePrice;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "CategoryId")
  private Category category;

  // getters/setters
  public Integer getId(){ return id; }
  public void setId(Integer id){ this.id = id; }
  public String getProductName(){ return productName; }
  public void setProductName(String productName){ this.productName = productName; }
  public String getProductDescription(){ return productDescription; }
  public void setProductDescription(String productDescription){ this.productDescription = productDescription; }
  public String getImageUrl(){ return imageUrl; }
  public void setImageUrl(String imageUrl){ this.imageUrl = imageUrl; }
  public BigDecimal getPrice(){ return price; }
  public void setPrice(BigDecimal price){ this.price = price; }
  public BigDecimal getSalePrice(){ return salePrice; }
  public void setSalePrice(BigDecimal salePrice){ this.salePrice = salePrice; }
  public Category getCategory(){ return category; }
  public void setCategory(Category category){ this.category = category; }
}
