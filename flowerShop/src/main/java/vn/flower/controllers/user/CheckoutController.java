package vn.flower.controllers.user;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// === THÊM CÁC IMPORT NÀY ===
import java.math.BigDecimal;
import java.util.List;
import vn.flower.api.dto.CartLine;
import vn.flower.entities.Product;
import vn.flower.repositories.ProductRepository;
// ==========================

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
  
  // === THÊM PRODUCT REPO ===
  private final ProductRepository productRepo; 

  public CheckoutController(CartService cartService,
                            AccountRepository accountRepo,
                            OrderRepository orderRepo,
                            // === THÊM VÀO CONSTRUCTOR ===
                            ProductRepository productRepo) { 
    this.cartService = cartService;
    this.accountRepo = accountRepo;
    this.orderRepo = orderRepo;
    this.productRepo = productRepo; 
  }

  private Integer currentAccountId() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated()) throw new IllegalStateException("Chưa đăng nhập");
    String email = (auth.getPrincipal() instanceof UserDetails u) ? u.getUsername() : auth.getName();
    return accountRepo.findByEmail(email)
        .orElseThrow(() -> new IllegalStateException("Không tìm thấy tài khoản: " + email))
        .getId();
  }

  // === SỬA LẠI HOÀN TOÀN PHƯƠNG THỨC NÀY ===
  @GetMapping("/checkout")
  public String showCheckout(
      // Các tham số mới cho "Mua ngay"
      @RequestParam(required = false) Boolean buyNow,
      @RequestParam(required = false) Integer productId,
      @RequestParam(required = false) Integer qty,
      Model model) {

    Integer accId = currentAccountId();
    CartView cart; // CartView có thể là thật (từ DB) hoặc "ảo" (từ Mua ngay)
    boolean isBuyNowFlow = (buyNow != null && buyNow && productId != null && qty != null && qty > 0);

    if (isBuyNowFlow) {
      // --- LUỒNG MUA NGAY ---
      // 1. Tạo một CartView "ảo" (không lưu DB)
      Product p = productRepo.findById(productId)
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sản phẩm không tồn tại"));
      
      BigDecimal price = p.getSalePrice() != null ? p.getSalePrice() : p.getPrice();
      BigDecimal lineTotal = price.multiply(BigDecimal.valueOf(qty));

      CartLine line = new CartLine(p.getId(), p.getProductName(), qty, price, lineTotal);
      
      // Giả định phí ship mặc định (FAST), JS sẽ tính lại khi người dùng thay đổi
      BigDecimal shipping = new BigDecimal("30000"); 
      if(lineTotal.compareTo(new BigDecimal("500000")) >= 0) {
          shipping = BigDecimal.ZERO; // Miễn phí ship nếu đủ 500k
      }
      BigDecimal total = lineTotal.add(shipping);
      
      // Tạo CartView "ảo" với orderId = null (vì chưa có order)
      cart = new CartView(null, List.of(line), lineTotal, shipping, total);

      // Gửi thông tin "Mua ngay" sang view để JS sử dụng
      model.addAttribute("isBuyNow", true);
      model.addAttribute("buyNowProductId", productId);
      model.addAttribute("buyNowQty", qty);

    } else {
      // --- LUỒNG GIỎ HÀNG THÔNG THƯỜNG ---
      cart = cartService.getCart(accId);
      if (cart.lines().isEmpty()) {
         // Nếu giỏ hàng thật trống, quay về trang sản phẩm
         return "redirect:/products";
      }
      model.addAttribute("isBuyNow", false);
    }
    
    model.addAttribute("cart", cart); // Gửi CartView (thật hoặc ảo)
    // Form này chỉ dùng cho Thymeleaf, JS sẽ đọc giá trị từ input
    model.addAttribute("form", new CheckoutRequest("", "", "", null, "COD", ShippingMethod.FAST)); 
    return "checkout"; // templates/checkout.html
  }


  // Phương thức POST này chỉ xử lý cho luồng giỏ hàng (nếu dùng form-submit, không phải API)
  // Chúng ta đang dùng API nên phương thức này ít quan trọng, nhưng để lại cũng không sao.
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

    // Logic POST này là logic thanh toán giỏ hàng (không phải "Mua ngay")
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
    return "user/order-success"; 
  }
}