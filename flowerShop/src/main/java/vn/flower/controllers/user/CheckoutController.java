package vn.flower.controllers.user;

import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable; // <-- THÊM IMPORT NÀY
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.csrf.CsrfToken; // <-- THÊM IMPORT NÀY
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import vn.flower.api.dto.CartLine;
import vn.flower.entities.Product;
import vn.flower.repositories.ProductRepository;

import vn.flower.api.dto.CartView;
import vn.flower.api.dto.ShippingMethod;
import vn.flower.api.dto.CheckoutRequest;
import vn.flower.repositories.AccountRepository;
import vn.flower.repositories.OrderRepository;
import vn.flower.services.CartService;

@Controller
public class CheckoutController {

  private final CartService cartService;
  private final AccountRepository accountRepo;
  private final OrderRepository orderRepo;
  private final ProductRepository productRepo;

  public CheckoutController(CartService cartService,
                            AccountRepository accountRepo,
                            OrderRepository orderRepo,
                            ProductRepository productRepo) {
    this.cartService = cartService;
    this.accountRepo = accountRepo;
    this.orderRepo = orderRepo;
    this.productRepo = productRepo;
  }

  private Integer currentAccountId() {
    // ... (giữ nguyên)
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated()) throw new IllegalStateException("Chưa đăng nhập");
    String email = (auth.getPrincipal() instanceof UserDetails u) ? u.getUsername() : auth.getName();
    return accountRepo.findByEmail(email)
        .orElseThrow(() -> new IllegalStateException("Không tìm thấy tài khoản: " + email))
        .getId();
  }

  @GetMapping("/checkout")
  public String showCheckout(
      @RequestParam(required = false) Boolean buyNow,
      @RequestParam(required = false) Integer productId,
      @RequestParam(required = false) Integer qty,
      Model model,
      @Nullable CsrfToken csrfToken) { // <-- THÊM THAM SỐ CSRF TOKEN

    Integer accId = currentAccountId();
    CartView cart;
    boolean isBuyNowFlow = (buyNow != null && buyNow && productId != null && qty != null && qty > 0);

    if (isBuyNowFlow) {
      // ... (logic Buy Now giữ nguyên) ...
       Product p = productRepo.findById(productId)
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sản phẩm không tồn tại"));
      
      BigDecimal price = p.getSalePrice() != null ? p.getSalePrice() : p.getPrice();
      BigDecimal lineTotal = price.multiply(BigDecimal.valueOf(qty));

      CartLine line = new CartLine(p.getId(), p.getProductName(), qty, price, lineTotal);
      
      BigDecimal shipping = new BigDecimal("30000"); 
      if(lineTotal.compareTo(new BigDecimal("500000")) >= 0) {
          shipping = BigDecimal.ZERO; 
      }
      BigDecimal total = lineTotal.add(shipping);
      
      cart = new CartView(null, List.of(line), lineTotal, shipping, total);

      model.addAttribute("isBuyNow", true);
      model.addAttribute("buyNowProductId", productId);
      model.addAttribute("buyNowQty", qty);
    } else {
      cart = cartService.getCart(accId);
      if (cart.lines().isEmpty()) {
         return "redirect:/products";
      }
      model.addAttribute("isBuyNow", false);
    }
    
    model.addAttribute("cart", cart);
    model.addAttribute("form", new CheckoutRequest("", "", "", null, "COD", ShippingMethod.FAST)); 
    
    // === THÊM DÒNG NÀY ĐỂ TRUYỀN CSRF TOKEN ===
    if (csrfToken != null) model.addAttribute("_csrf", csrfToken);
    // ===========================================
    
    return "checkout"; 
  }


  // Phương thức POST này có thể không cần thiết nếu bạn chỉ dùng API
  @PostMapping("/checkout")
  public String doCheckout(@ModelAttribute("form") CheckoutRequest form,
                           RedirectAttributes ra) {
    // ... (giữ nguyên)
    Integer accId = currentAccountId();
    if (form.recipientName() == null || form.recipientName().isBlank()
        || form.recipientPhone() == null || form.recipientPhone().isBlank()
        || form.shippingAddress() == null || form.shippingAddress().isBlank()) {
      ra.addFlashAttribute("error", "Vui lòng nhập đầy đủ Họ tên, SĐT, Địa chỉ.");
      return "redirect:/checkout";
    }

    Integer orderId = cartService.checkout(accId, form);
    ra.addFlashAttribute("success", "Đặt hàng thành công! Mã đơn #" + orderId);
    return "redirect:/orders/" + orderId;
  }

  // Phương thức xem chi tiết đơn hàng giữ nguyên
  @GetMapping("/orders/{id}")
  public String orderSuccess(@PathVariable Integer id, Model model) {
      // ... (giữ nguyên)
       Integer accId = currentAccountId();
       var order = orderRepo.findByIdAndAccount_Id(id, accId)
           .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng"));

       model.addAttribute("order", order);
       return "user/order-success"; 
  }
}