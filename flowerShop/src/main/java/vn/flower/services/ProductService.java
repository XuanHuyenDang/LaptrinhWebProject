package vn.flower.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.flower.entities.Product;
import java.math.BigDecimal;

public interface ProductService {
  Page<Product> search(String q, Integer categoryId, BigDecimal min, BigDecimal max, Pageable pageable);
  Product getById(Integer id);
}
