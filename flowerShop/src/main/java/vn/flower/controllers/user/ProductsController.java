package vn.flower.controllers.user;

// === THÊM CÁC IMPORT NÀY ===
import org.springframework.beans.factory.annotation.Autowired; // Hoặc dùng constructor injection
import vn.flower.entities.Category;
import vn.flower.repositories.CategoryRepository;
import java.util.List;
// ==========================

import vn.flower.services.ProductService;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.flower.entities.Product;
// Bỏ import vn.flower.services.ProductService; dư thừa

import java.math.BigDecimal;

@Controller
public class ProductsController {

  private final ProductService productService;
  // === TIÊM CATEGORY REPOSITORY ===
  private final CategoryRepository categoryRepository;
  // ==============================

  // === SỬA CONSTRUCTOR ĐỂ NHẬN REPOSITORY ===
  public ProductsController(ProductService productService, CategoryRepository categoryRepository) {
    this.productService = productService;
    this.categoryRepository = categoryRepository; // Gán giá trị
  }
  // ========================================

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
      default -> Sort.by(Sort.Direction.DESC, "id"); // Sắp xếp mặc định theo ID mới nhất
    };

    Pageable pageable = PageRequest.of(page, size, s);
    Page<Product> resultPage = productService.search(q, categoryId, min, max, pageable);

    // === LẤY DANH SÁCH CATEGORIES ===
    List<Category> categories = categoryRepository.findAll();
    // ================================

    // === GỬI DỮ LIỆU SANG TEMPLATE ===
    model.addAttribute("page", resultPage); // Đối tượng Page chứa thông tin phân trang
    model.addAttribute("products", resultPage.getContent()); // Danh sách sản phẩm của trang hiện tại
    model.addAttribute("categories", categories); // <-- GỬI CATEGORIES SANG
    model.addAttribute("q", q);
    model.addAttribute("categoryId", categoryId); // Giữ lại ID đã chọn
    model.addAttribute("min", min);
    model.addAttribute("max", max);
    model.addAttribute("sort", sort);
    // ================================

    return "user/products"; // -> templates/user/products.html
  }

  @GetMapping("/products/{id}")
  public String detail(@PathVariable Integer id, Model model) {
    Product product = productService.getById(id);
    model.addAttribute("product", product);
    // (Optionally add related products here if needed)
    return "user/product-detail"; // -> templates/user/product-detail.html
  }
}
