package vn.flower.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import vn.flower.entities.Product;
import vn.flower.repositories.ProductRepository;

import java.math.BigDecimal;
import java.util.List;

// === THÊM IMPORT NÀY ===
import static vn.flower.repositories.ProductSpecifications.*;
// ========================

@Service
public class ProductServiceImpl implements ProductService {

  private final ProductRepository productRepository;

  public ProductServiceImpl(ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  @Override
  public Page<Product> search(String q, Integer categoryId, BigDecimal min, BigDecimal max, Pageable pageable) {
    // === SỬA DÒNG NÀY ===
    // Sử dụng nameOrDescriptionLike thay vì chỉ nameLike
    Specification<Product> spec = Specification.where(nameOrDescriptionLike(q))
    // ====================
        .and(categoryIdEq(categoryId))
        .and(priceGte(min))
        .and(priceLte(max));
    return productRepository.findAll(spec, pageable);
  }

  @Override
  public Product getById(Integer id) {
    return productRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));
  }

  @Override
  public List<Product> getTop10BestSellers() {
      return productRepository.findTop10ByStatusTrueOrderBySoldDescIdDesc();
  }

  @Override
  public List<Product> getLatest5Products() {
    return productRepository.findTop5ByStatusTrueOrderByIdDesc();
  }

  @Override
  public List<Product> getTop10SaleProducts() {
    return productRepository.findTop10ByStatusTrueAndSalePriceIsNotNullOrderByIdDesc();
  }
}
