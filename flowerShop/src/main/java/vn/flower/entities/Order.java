package vn.flower.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Orders")
public class Order {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "AccountId")
  private Account account;

  @Column(name = "RecipientName", nullable = false, length = 100)
  private String recipientName;

  @Column(name = "ShippingAddress", nullable = false, length = 500)
  private String shippingAddress;

  @Column(name = "RecipientPhone", nullable = false, length = 20)
  private String recipientPhone;

  @Column(name = "Note", length = 500)
  private String note;

  @Column(name = "OrderDate")
  private LocalDateTime orderDate;

  @Column(name = "ShippingFee", precision = 18, scale = 2)
  private BigDecimal shippingFee;

  @Column(name = "TotalAmount", precision = 18, scale = 2, nullable = false)
  private BigDecimal totalAmount;

  @Column(name = "PaymentMethod", length = 100)
  private String paymentMethod;

  @Column(name = "Status", length = 50)
  private String status;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  private List<OrderDetail> details = new ArrayList<>();


  // getters/setters
  public Integer getId(){ return id; }
  public void setId(Integer id){ this.id = id; }
  public Account getAccount(){ return account; }
  public void setAccount(Account account){ this.account = account; }
  public String getRecipientName(){ return recipientName; }
  public void setRecipientName(String recipientName){ this.recipientName = recipientName; }
  public String getShippingAddress(){ return shippingAddress; }
  public void setShippingAddress(String shippingAddress){ this.shippingAddress = shippingAddress; }
  public String getRecipientPhone(){ return recipientPhone; }
  public void setRecipientPhone(String recipientPhone){ this.recipientPhone = recipientPhone; }
  public String getNote(){ return note; }
  public void setNote(String note){ this.note = note; }
  public LocalDateTime getOrderDate(){ return orderDate; }
  public void setOrderDate(LocalDateTime orderDate){ this.orderDate = orderDate; }
  public BigDecimal getShippingFee(){ return shippingFee; }
  public void setShippingFee(BigDecimal shippingFee){ this.shippingFee = shippingFee; }
  public BigDecimal getTotalAmount(){ return totalAmount; }
  public void setTotalAmount(BigDecimal totalAmount){ this.totalAmount = totalAmount; }
  public String getPaymentMethod(){ return paymentMethod; }
  public void setPaymentMethod(String paymentMethod){ this.paymentMethod = paymentMethod; }
  public String getStatus(){ return status; }
  public void setStatus(String status){ this.status = status; }
  public List<OrderDetail> getDetails(){ return details; }
  public void setDetails(List<OrderDetail> details){ this.details = details; }
}
