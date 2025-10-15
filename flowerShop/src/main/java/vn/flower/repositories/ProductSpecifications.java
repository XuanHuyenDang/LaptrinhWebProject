package vn.flower.repositories;

import org.springframework.data.jpa.domain.Specification;
import vn.flower.entities.Product;

import java.math.BigDecimal;

public final class ProductSpecifications {
  private ProductSpecifications(){}

  public static Specification<Product> nameLike(String q) {
    return (root, query, cb) ->
        (q==null || q.isBlank()) ? cb.conjunction()
        : cb.like(cb.lower(root.get("productName")), "%"+q.toLowerCase()+"%");
  }

  public static Specification<Product> categoryIdEq(Integer categoryId) {
    return (root, query, cb) ->
        (categoryId==null) ? cb.conjunction()
        : cb.equal(root.get("category").get("id"), categoryId);
  }

  public static Specification<Product> priceGte(BigDecimal min) {
    return (root, query, cb) ->
        (min==null) ? cb.conjunction()
        : cb.greaterThanOrEqualTo(root.get("price"), min);
  }

  public static Specification<Product> priceLte(BigDecimal max) {
    return (root, query, cb) ->
        (max==null) ? cb.conjunction()
        : cb.lessThanOrEqualTo(root.get("price"), max);
  }
}
