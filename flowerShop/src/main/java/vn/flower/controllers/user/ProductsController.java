package vn.flower.controllers.user;

import vn.flower.services.ProductService;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.flower.entities.Product;
import vn.flower.services.ProductService;

import java.math.BigDecimal;

@Controller
public class ProductsController {

  private final ProductService productService;

  public ProductsController(ProductService productService) {
    this.productService = productService;
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
    return "user/products";            // --> templates/products.html
  }

  @GetMapping("/products/{id}")
  public String detail(@PathVariable Integer id, Model model) {
    model.addAttribute("product", productService.getById(id));
    return "user/product-detail";      // --> templates/product-detail.html
  }
}
