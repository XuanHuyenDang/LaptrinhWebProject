package vn.flower.controllers.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import vn.flower.api.dto.*;
import vn.flower.repositories.AccountRepository;
import vn.flower.services.CartService;

@RestController
@RequestMapping("/api/cart")
public class CartApiController {

  private final CartService cartService;
  private final AccountRepository accountRepo;

  public CartApiController(CartService cartService, AccountRepository accountRepo) {
    this.cartService = cartService;
    this.accountRepo = accountRepo;
  }

  private Integer currentAccountId() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated()) throw new IllegalStateException("Chưa đăng nhập");
    String email = (auth.getPrincipal() instanceof UserDetails u) ? u.getUsername() : auth.getName();
    return accountRepo.findByEmail(email)
        .orElseThrow(() -> new IllegalStateException("Không tìm thấy tài khoản: " + email))
        .getId();
  }

  @GetMapping
  public ResponseEntity<CartView> getCart() {
    return ResponseEntity.ok(cartService.getCart(currentAccountId()));
  }

  @PostMapping("/items")
  public ResponseEntity<CartView> addItem(@RequestBody AddItemRequest req) {
    return ResponseEntity.ok(cartService.addItem(currentAccountId(), req.productId(), req.quantity()));
  }

  @PutMapping("/items/{productId}")
  public ResponseEntity<CartView> updateQty(@PathVariable Integer productId, @RequestBody UpdateQtyRequest req) {
    return ResponseEntity.ok(cartService.updateQty(currentAccountId(), productId, req.quantity()));
  }

  // ✅ Gọi hàm removeItem riêng, không đi vòng updateQty(0)
  @DeleteMapping("/items/{productId}")
  public ResponseEntity<CartView> remove(@PathVariable Integer productId) {
    return ResponseEntity.ok(cartService.removeItem(currentAccountId(), productId));
  }

  @PostMapping("/checkout")
  public ResponseEntity<Integer> checkout(@RequestBody CheckoutRequest req) {
    return ResponseEntity.ok(cartService.checkout(currentAccountId(), req));
  }
}
