package vn.flower.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Accounts")
public class Account {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "FullName", nullable = false, length = 100)
  private String fullName;

  @Column(name = "Email", nullable = false, unique = true, length = 100)
  private String email;

  @Column(name = "PhoneNumber", length = 20)
  private String phoneNumber;

  @Column(name = "Address", length = 255)
  private String address;

  @Column(name = "Password", nullable = false, length = 255)
  private String password; // đã hash

  @Column(name = "Role", nullable = false, length = 20)
  private String role; // 'customer' | 'admin'

  @Column(name = "CreatedAt")
  private LocalDateTime createdAt;

  // getters/setters
  public Integer getId(){ return id; }
  public void setId(Integer id){ this.id = id; }
  public String getFullName(){ return fullName; }
  public void setFullName(String fullName){ this.fullName = fullName; }
  public String getEmail(){ return email; }
  public void setEmail(String email){ this.email = email; }
  public String getPhoneNumber(){ return phoneNumber; }
  public void setPhoneNumber(String phoneNumber){ this.phoneNumber = phoneNumber; }
  public String getAddress(){ return address; }
  public void setAddress(String address){ this.address = address; }
  public String getPassword(){ return password; }
  public void setPassword(String password){ this.password = password; }
  public String getRole(){ return role; }
  public void setRole(String role){ this.role = role; }
  public LocalDateTime getCreatedAt(){ return createdAt; }
  public void setCreatedAt(LocalDateTime createdAt){ this.createdAt = createdAt; }
}
