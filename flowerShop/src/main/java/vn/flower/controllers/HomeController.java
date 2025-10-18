package vn.flower.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.lang.Nullable;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.flower.services.ProductService;

@Controller
public class HomeController {

  private final ProductService productService;

  public HomeController(ProductService productService) {
    this.productService = productService;
  }

  @GetMapping({"/", "/index"})
  public String index(Model model,
                      @Nullable CsrfToken csrfToken,
                      HttpServletRequest req) {
    // dữ liệu hiển thị
    model.addAttribute("topSoldProducts", productService.getTop10BestSellers());
    model.addAttribute("latestProducts",  productService.getLatest5Products());
    model.addAttribute("saleProducts",    productService.getTop10SaleProducts());

    // >>> THÊM 2 DÒNG NÀY <<<
    if (csrfToken != null) model.addAttribute("_csrf", csrfToken);  // để render meta CSRF
    model.addAttribute("cartApiBase", req.getContextPath() + "/api/cart"); // để JS biết API

    return "index";
  }
}
