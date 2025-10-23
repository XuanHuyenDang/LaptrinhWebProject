// vn/flower/controllers/user/OrderHistoryController.java
package vn.flower.controllers.user;

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

  public OrderHistoryController(OrderRepository orderRepo, AccountRepository accountRepo) {
    this.orderRepo = orderRepo;
    this.accountRepo = accountRepo;
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
  public String showOrderHistory(Model model) {
    Optional<Integer> accountIdOpt = getCurrentAccountId();

    if (accountIdOpt.isEmpty()) {
        System.err.println("User not logged in for order history.");
        model.addAttribute("orders", Collections.emptyList());
    } else {
        Integer accountId = accountIdOpt.get();
        try {
            // === KEEP USING THIS LINE (returns List<Order>) ===
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
    
 // === THÊM DÒNG NÀY ===
    // Truyền thời gian hiện tại để UI so sánh
    model.addAttribute("now", LocalDateTime.now());
    // ======================
    return "user/order-list";
  }
}