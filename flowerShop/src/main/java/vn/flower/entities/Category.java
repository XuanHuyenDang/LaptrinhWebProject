package vn.flower.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "Categories")
public class Category {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "CategoryName", nullable = false, unique = true, length = 255)
  private String categoryName;

  @Column(name = "CategoryDescription", length = 500)
  private String categoryDescription;

  // getters/setters
  public Integer getId(){ return id; }
  public void setId(Integer id){ this.id = id; }
  public String getCategoryName(){ return categoryName; }
  public void setCategoryName(String categoryName){ this.categoryName = categoryName; }
  public String getCategoryDescription(){ return categoryDescription; }
  public void setCategoryDescription(String categoryDescription){ this.categoryDescription = categoryDescription; }
}
