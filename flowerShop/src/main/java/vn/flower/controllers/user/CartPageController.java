package vn.flower.controllers.user;

import org.springframework.lang.Nullable; // <-- THÊM IMPORT NÀY
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.web.csrf.CsrfToken; // <-- THÊM IMPORT NÀY

@Controller
public class CartPageController {

  @GetMapping("/cart")
  public String cartPage(Model model, @Nullable CsrfToken csrfToken) { // <-- Thêm tham số
    if (csrfToken != null) {
      model.addAttribute("_csrf", csrfToken); // <-- Thêm vào model
    }
    return "user/cart";
  }
}