package vn.flower.controllers.user;

import vn.flower.services.ProductService;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.flower.entities.Product;
import vn.flower.entities.Review; // <-- THÊM IMPORT
import vn.flower.services.ReviewService; // <-- THÊM IMPORT

import java.math.BigDecimal;
import java.util.List; // <-- THÊM IMPORT

@Controller
public class ProductsController {

  private final ProductService productService;
  private final ReviewService reviewService; // <-- INJECT REVIEW SERVICE

  public ProductsController(ProductService productService, ReviewService reviewService) {
    this.productService = productService;
    this.reviewService = reviewService; // <-- THÊM
  }

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

    Page<Product> result = productService.search(q, categoryId, min, max, PageRequest.of(page, size, s));

    model.addAttribute("page", result);
    model.addAttribute("products", result.getContent());
    model.addAttribute("q", q);
    model.addAttribute("categoryId", categoryId);
    model.addAttribute("min", min);
    model.addAttribute("max", max);
    model.addAttribute("sort", sort);
    return "user/products";            // --> templates/user/products.html
  }

  @GetMapping("/products/{id}")
  public String detail(@PathVariable Integer id, Model model) {
    
    // 1. Lấy thông tin sản phẩm (như cũ)
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

    return "user/product-detail";      // --> templates/user/product-detail.html
  }
}