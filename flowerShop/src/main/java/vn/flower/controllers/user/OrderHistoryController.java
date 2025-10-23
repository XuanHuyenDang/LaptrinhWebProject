// java/vn/flower/controllers/user/OrderHistoryController.java
package vn.flower.controllers.user;

// === THÊM CÁC IMPORT NÀY ===
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.flower.entities.OrderDetail;
import vn.flower.entities.Product;
import vn.flower.repositories.ProductRepository;
// ==========================

import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.flower.repositories.AccountRepository;
import vn.flower.repositories.OrderRepository;
import vn.flower.entities.Account;
import vn.flower.entities.Order;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

@Controller
public class OrderHistoryController {

  private final OrderRepository orderRepo;
  private final AccountRepository accountRepo;
  private final ProductRepository productRepo; // <-- THÊM VÀO

  // === CẬP NHẬT CONSTRUCTOR ===
  public OrderHistoryController(OrderRepository orderRepo, 
                                AccountRepository accountRepo, 
                                ProductRepository productRepo) { // <-- THÊM VÀO
    this.orderRepo = orderRepo;
    this.accountRepo = accountRepo;
    this.productRepo = productRepo; // <-- THÊM VÀO
  }

  private Optional<Integer> getCurrentAccountId() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(String.valueOf(auth.getPrincipal()))) {
        return Optional.empty();
    }
    String email = (auth.getPrincipal() instanceof UserDetails u) ? u.getUsername() : auth.getName();
    return accountRepo.findByEmail(email).map(Account::getId);
  }

  @GetMapping("/orders")
  @Transactional(readOnly = true)
  public String showOrderHistory(Model model,
                                 @Nullable CsrfToken csrfToken) { // <-- THÊM CsrfToken
    Optional<Integer> accountIdOpt = getCurrentAccountId();

    if (accountIdOpt.isEmpty()) {
        System.err.println("User not logged in for order history.");
        model.addAttribute("orders", Collections.emptyList());
    } else {
        Integer accountId = accountIdOpt.get();
        try {
            List<Order> orders =
                orderRepo.findAllByAccount_IdAndStatusNotOrderByOrderDateDesc(accountId, "CART");

            System.out.println("[DEBUG] Fetched orders for Account ID: " + accountId + ". Found: " + orders.size() + " orders (excluding CART).");
            model.addAttribute("orders", orders);

        } catch (Exception e) {
            System.err.println("Error fetching order history for accountId " + accountId + ": " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("orders", Collections.emptyList());
        }
    }
    
    model.addAttribute("now", LocalDateTime.now());
    
    // === THÊM CSRF VÀO MODEL ===
    if (csrfToken != null) model.addAttribute("_csrf", csrfToken);
    // ======================
    return "user/order-list";
  }
  
  // === PHƯƠNG THỨC MỚI ĐỂ HỦY ĐƠN ===
  @PostMapping("/orders/cancel/{orderId}")
  @Transactional // Đảm bảo tất cả các thao tác (hủy đơn + cập nhật 'sold') thành công
  public String cancelOrder(@PathVariable Integer orderId, 
                            RedirectAttributes ra) {
      
      Optional<Integer> accountIdOpt = getCurrentAccountId();
      if (accountIdOpt.isEmpty()) {
          ra.addFlashAttribute("errorMessage", "Bạn cần đăng nhập để thực hiện.");
          return "redirect:/auth/login";
      }

      Integer accountId = accountIdOpt.get();
      Optional<Order> orderOpt = orderRepo.findByIdAndAccount_Id(orderId, accountId);

      if (orderOpt.isEmpty()) {
          ra.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng.");
          return "redirect:/orders";
      }

      Order order = orderOpt.get();

      // Chỉ cho phép hủy khi "Đang xử lý"
      if (!"Đang xử lý".equals(order.getStatus())) {
          ra.addFlashAttribute("errorMessage", "Không thể hủy đơn hàng ở trạng thái " + order.getStatus());
          return "redirect:/orders";
      }

      try {
          // 1. Cập nhật trạng thái
          order.setStatus("Đã hủy");
          orderRepo.save(order);

          // 2. Hoàn trả lại số lượng 'sold' cho sản phẩm
          for (OrderDetail detail : order.getDetails()) {
              Product p = detail.getProduct();
              if (p != null) {
                  int newSold = (p.getSold() == null ? 0 : p.getSold()) - detail.getQuantity();
                  p.setSold(Math.max(0, newSold)); // Đảm bảo không bị âm
                  productRepo.save(p);
              }
          }

          ra.addFlashAttribute("successMessage", "Đã hủy đơn hàng #" + orderId + " thành công.");
      } catch (Exception e) {
          ra.addFlashAttribute("errorMessage", "Lỗi khi hủy đơn hàng: " + e.getMessage());
      }

      return "redirect:/orders";
  }
}