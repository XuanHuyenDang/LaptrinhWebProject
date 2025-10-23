package vn.flower.controllers.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable; // <-- THÊM IMPORT NÀY
import org.springframework.security.web.csrf.CsrfToken; // <-- THÊM IMPORT NÀY
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.flower.repositories.WishlistRepository;
import vn.flower.util.AuthUtils;
import vn.flower.repositories.AccountRepository;
import vn.flower.entities.Product; // Import Product

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class WishlistPageController {

    @Autowired private WishlistRepository wishlistRepository;
    @Autowired private AccountRepository accountRepository;

    @GetMapping("/wishlist")
    public String showWishlist(Model model,
                               @Nullable CsrfToken csrfToken) { // <-- THÊM THAM SỐ CSRF TOKEN
        String email = AuthUtils.currentUsername();
        if (email == null) return "redirect:/auth/login";

        Integer accountId = accountRepository.findByEmail(email).map(vn.flower.entities.Account::getId).orElse(null);
        if (accountId == null) return "redirect:/auth/login";

        List<vn.flower.entities.Wishlist> wishlistItems = wishlistRepository.findByAccountIdWithProduct(accountId);
        List<Product> products = wishlistItems.stream() // Sử dụng Product đã import
            .map(vn.flower.entities.Wishlist::getProduct)
            .collect(Collectors.toList());

        model.addAttribute("products", products);

        // === THÊM DÒNG NÀY ĐỂ TRUYỀN CSRF TOKEN ===
        if (csrfToken != null) model.addAttribute("_csrf", csrfToken);
        // ===========================================

        return "user/wishlist";
    }
}