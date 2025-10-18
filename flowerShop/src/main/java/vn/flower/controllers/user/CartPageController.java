package vn.flower.controllers.user;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.web.csrf.CsrfToken;

@Controller
public class CartPageController {

  @GetMapping("/cart")
  public String cartPage(Model model, CsrfToken csrfToken) {
    if (csrfToken != null) {
      model.addAttribute("_csrf", csrfToken); // <-- để Thymeleaf dùng
    }
    return "user/cart"; // đúng với đường dẫn templates/user/cart.html
  }
}
