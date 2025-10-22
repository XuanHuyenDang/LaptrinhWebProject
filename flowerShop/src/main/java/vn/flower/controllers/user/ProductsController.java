package vn.flower.controllers.user;

// === IMPORT ĐẦY ĐỦ ===
import org.springframework.beans.factory.annotation.Autowired;
import vn.flower.entities.Category;
import vn.flower.repositories.CategoryRepository;
import vn.flower.services.ProductService;
import vn.flower.services.ReviewService; // <-- Thêm lại
import vn.flower.entities.Product;
import vn.flower.entities.Review;     // <-- Thêm lại
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
// =====================

@Controller
public class ProductsController {

  private final ProductService productService;
  private final CategoryRepository categoryRepository; // <-- Thêm
  private final ReviewService reviewService; 

  // === SỬA CONSTRUCTOR ĐỂ NHẬN ĐỦ 3 SERVICE/REPOSITORY ===
  public ProductsController(ProductService productService, 
                            CategoryRepository categoryRepository, // <-- Thêm
                            ReviewService reviewService) { 
    this.productService = productService;
    this.categoryRepository = categoryRepository; // <-- Thêm
    this.reviewService = reviewService; 
  }
  // ====================================================

  @GetMapping({"/product", "/products"})
  public String products(@RequestParam(required = false) String q,
                         @RequestParam(required = false) Integer categoryId,
                         @RequestParam(required = false) BigDecimal min,
                         @RequestParam(required = false) BigDecimal max,
                         @RequestParam(defaultValue = "") String sort,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "12") int size,
                         Model model) {

    Sort s = switch (sort) {
      case "asc" -> Sort.by(Sort.Direction.ASC, "price");
      case "desc" -> Sort.by(Sort.Direction.DESC, "price");
      default -> Sort.by(Sort.Direction.DESC, "id");
    };

    Pageable pageable = PageRequest.of(page, size, s);
    Page<Product> resultPage = productService.search(q, categoryId, min, max, pageable);

    // === LẤY DANH SÁCH CATEGORIES (ĐÃ THÊM LẠI) ===
    List<Category> categories = categoryRepository.findAll();
    // ==============================================

    // === GỬI DỮ LIỆU SANG TEMPLATE (ĐÃ THÊM LẠI CATEGORIES) ===
    model.addAttribute("page", resultPage); // Đối tượng Page chứa thông tin phân trang
    model.addAttribute("products", resultPage.getContent()); // Danh sách sản phẩm của trang hiện tại
    model.addAttribute("categories", categories); // <-- GỬI CATEGORIES SANG
    model.addAttribute("q", q);
    model.addAttribute("categoryId", categoryId); // Giữ lại ID đã chọn
    model.addAttribute("min", min);
    model.addAttribute("max", max);
    model.addAttribute("sort", sort);
    // ======================================================

    return "user/products"; // -> templates/user/products.html
  }

  // === LOGIC REVIEW CHO TRANG DETAIL (ĐÃ GIỮ NGUYÊN) ===
  @GetMapping("/products/{id}")
  public String detail(@PathVariable Integer id, Model model) {
    
    // 1. Lấy thông tin sản phẩm
    model.addAttribute("product", productService.getById(id));

    // 2. Lấy danh sách reviews
    List<Review> reviews = reviewService.getReviewsForProduct(id);
    model.addAttribute("reviews", reviews);

    // 3. Tính toán rating trung bình và tổng số
    double avgRating = reviews.stream()
                             .mapToInt(Review::getRating)
                             .average()
                             .orElse(0.0);
                             
    model.addAttribute("averageRating", avgRating);
    model.addAttribute("reviewCount", reviews.size());

    // 4. Chuẩn bị 1 object rỗng cho form
    model.addAttribute("newReview", new Review());

    // (Logic sản phẩm liên quan của bạn nếu có)
    // model.addAttribute("related", ...);

    return "user/product-detail"; // --> templates/user/product-detail.html
  }
  // =================================================
}