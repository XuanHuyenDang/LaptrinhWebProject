package vn.flower.controllers.user;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import vn.flower.api.dto.CartView;
import vn.flower.api.dto.CheckoutRequest;
import vn.flower.repositories.AccountRepository;
import vn.flower.repositories.OrderRepository;
import vn.flower.services.CartService;

@Controller
public class CheckoutController {

  private final CartService cartService;
  private final AccountRepository accountRepo;
  private final OrderRepository orderRepo;

  public CheckoutController(CartService cartService,
                            AccountRepository accountRepo,
                            OrderRepository orderRepo) {
    this.cartService = cartService;
    this.accountRepo = accountRepo;
    this.orderRepo = orderRepo;
  }

  private Integer currentAccountId() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated()) throw new IllegalStateException("Chưa đăng nhập");
    String email = (auth.getPrincipal() instanceof UserDetails u) ? u.getUsername() : auth.getName();
    return accountRepo.findByEmail(email)
        .orElseThrow(() -> new IllegalStateException("Không tìm thấy tài khoản: " + email))
        .getId();
  }

  // Hiển thị trang checkout (tóm tắt giỏ + form)
  @GetMapping("/checkout")
  public String showCheckout(Model model) {
    Integer accId = currentAccountId();
    CartView cart = cartService.getCart(accId);
    model.addAttribute("cart", cart); // subtotal, shipping, total, lines...
    model.addAttribute("form", new CheckoutRequest("", "", "", null, "COD")); // mặc định COD
    return "checkout"; // templates/checkout.html
  }

  // Submit form checkout theo tên trường: recipientName, recipientPhone, shippingAddress, note, paymentMethod
  @PostMapping("/checkout")
  public String doCheckout(@ModelAttribute("form") CheckoutRequest form,
                           RedirectAttributes ra) {
    Integer accId = currentAccountId();
    // Validate đơn giản
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

  // Trang xác nhận đơn (xem được khi là chủ đơn)
  @GetMapping("/orders/{id}")
  public String orderSuccess(@PathVariable Integer id, Model model) {
    Integer accId = currentAccountId();
    var order = orderRepo.findByIdAndAccount_Id(id, accId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng"));

    model.addAttribute("order", order);
    return "order-success"; // tạo view đơn giản hiển thị mã đơn + tổng tiền
  }
}
