package vn.flower.controllers.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable; // <-- THÊM IMPORT NÀY
import org.springframework.security.web.csrf.CsrfToken; // <-- THÊM IMPORT NÀY
import vn.flower.entities.Category;
import vn.flower.repositories.CategoryRepository;
import vn.flower.services.ProductService;
import vn.flower.services.ReviewService;
import vn.flower.entities.Product;
import vn.flower.entities.Review;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class ProductsController {

  private final ProductService productService;
  private final CategoryRepository categoryRepository;
  private final ReviewService reviewService;

  public ProductsController(ProductService productService,
                            CategoryRepository categoryRepository,
                            ReviewService reviewService) {
    this.productService = productService;
    this.categoryRepository = categoryRepository;
    this.reviewService = reviewService;
  }

  @GetMapping({"/product", "/products"})
  public String products(@RequestParam(required = false) String q,
                         @RequestParam(required = false) Integer categoryId,
                         @RequestParam(required = false) BigDecimal min,
                         @RequestParam(required = false) BigDecimal max,
                         @RequestParam(defaultValue = "") String sort,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "12") int size,
                         Model model, // <-- Giữ nguyên Model
                         @Nullable CsrfToken csrfToken) { // <-- THÊM THAM SỐ CSRF TOKEN

    Sort s = switch (sort) {
      case "asc" -> Sort.by(Sort.Direction.ASC, "price");
      case "desc" -> Sort.by(Sort.Direction.DESC, "price");
      default -> Sort.by(Sort.Direction.DESC, "id");
    };

    Pageable pageable = PageRequest.of(page, size, s);
    Page<Product> resultPage = productService.search(q, categoryId, min, max, pageable);
    List<Category> categories = categoryRepository.findAll();

    model.addAttribute("page", resultPage);
    model.addAttribute("products", resultPage.getContent());
    model.addAttribute("categories", categories);
    model.addAttribute("q", q);
    model.addAttribute("categoryId", categoryId);
    model.addAttribute("min", min);
    model.addAttribute("max", max);
    model.addAttribute("sort", sort);

    // === THÊM DÒNG NÀY ĐỂ TRUYỀN CSRF TOKEN ===
    if (csrfToken != null) model.addAttribute("_csrf", csrfToken);
    // ===========================================

    return "user/products";
  }

  @GetMapping("/products/{id}")
  public String detail(@PathVariable Integer id, Model model,
                       @Nullable CsrfToken csrfToken) { // <-- THÊM THAM SỐ CSRF TOKEN

    model.addAttribute("product", productService.getById(id));
    List<Review> reviews = reviewService.getReviewsForProduct(id);
    model.addAttribute("reviews", reviews);
    double avgRating = reviews.stream()
                             .mapToInt(Review::getRating)
                             .average()
                             .orElse(0.0);

    model.addAttribute("averageRating", avgRating);
    model.addAttribute("reviewCount", reviews.size());
    model.addAttribute("newReview", new Review());

    // === THÊM DÒNG NÀY ĐỂ TRUYỀN CSRF TOKEN ===
    if (csrfToken != null) model.addAttribute("_csrf", csrfToken);
    // ===========================================

    // model.addAttribute("related", ...);

    return "user/product-detail";
  }
}