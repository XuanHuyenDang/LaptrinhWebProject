package vn.flower.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.flower.entities.Product;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {
  Page<Product> search(String q, Integer categoryId, BigDecimal min, BigDecimal max, Pageable pageable);
  Product getById(Integer id);
  List<Product> getTop10BestSellers();
  List<Product> getLatest5Products();
  List<Product> getTop10SaleProducts();
}
