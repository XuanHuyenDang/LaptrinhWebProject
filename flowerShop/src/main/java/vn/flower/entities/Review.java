package vn.flower.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Reviews")
public class Review {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "ProductId")
  private Product product;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "AccountId")
  private Account account;

  @Column(name = "Rating", nullable = false)
  private Integer rating; // 1..5

  @Column(name = "Comment", columnDefinition = "NVARCHAR(MAX)")
  private String comment;

  @Column(name = "CreatedAt")
  private LocalDateTime createdAt;

  // getters/setters
  public Integer getId(){ return id; }
  public void setId(Integer id){ this.id = id; }
  public Product getProduct(){ return product; }
  public void setProduct(Product product){ this.product = product; }
  public Account getAccount(){ return account; }
  public void setAccount(Account account){ this.account = account; }
  public Integer getRating(){ return rating; }
  public void setRating(Integer rating){ this.rating = rating; }
  public String getComment(){ return comment; }
  public void setComment(String comment){ this.comment = comment; }
  public LocalDateTime getCreatedAt(){ return createdAt; }
  public void setCreatedAt(LocalDateTime createdAt){ this.createdAt = createdAt; }
}
