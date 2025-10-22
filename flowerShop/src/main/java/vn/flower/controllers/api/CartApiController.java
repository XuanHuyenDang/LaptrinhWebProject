package vn.flower.controllers.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import vn.flower.api.dto.AddItemRequest;
// === THÊM IMPORT NÀY ===
import vn.flower.api.dto.BuyNowRequest; 
import vn.flower.api.dto.CartView;
import vn.flower.api.dto.CheckoutRequest;
import vn.flower.api.dto.UpdateQtyRequest;
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
    // Trả 401 nếu chưa đăng nhập
    if (auth == null || !auth.isAuthenticated() ||
        "anonymousUser".equals(String.valueOf(auth.getPrincipal()))) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Chưa đăng nhập");
    }
    String email = (auth.getPrincipal() instanceof UserDetails u)
        ? u.getUsername() : auth.getName();

    return accountRepo.findByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.UNAUTHORIZED, "Không tìm thấy tài khoản: " + email))
        .getId();
  }

  @GetMapping
  public ResponseEntity<CartView> getCart() {
    return ResponseEntity.ok(cartService.getCart(currentAccountId()));
  }

  @PostMapping("/items")
  public ResponseEntity<CartView> addItem(@RequestBody AddItemRequest req) {
    return ResponseEntity.ok(
        cartService.addItem(currentAccountId(), req.productId(), req.quantity()));
  }

  @PutMapping("/items/{productId}")
  public ResponseEntity<CartView> updateQty(
      @PathVariable Integer productId,
      @RequestBody UpdateQtyRequest req) {
    return ResponseEntity.ok(
        cartService.updateQty(currentAccountId(), productId, req.quantity()));
  }

  @DeleteMapping("/items/{productId}")
  public ResponseEntity<CartView> remove(@PathVariable Integer productId) {
    return ResponseEntity.ok(
        cartService.removeItem(currentAccountId(), productId));
  }

  /**
   * API này xử lý thanh toán cho GIỎ HÀNG (status='CART')
   */
  @PostMapping("/checkout")
  public ResponseEntity<Integer> checkout(@RequestBody CheckoutRequest req) {
    // CartService đã xử lý ShippingMethod (SAVING/FAST/EXPRESS), shipping fee, total, v.v.
    return ResponseEntity.ok(
        cartService.checkout(currentAccountId(), req));
  }

  /**
   * === API MỚI: XỬ LÝ THANH TOÁN "MUA NGAY" ===
   * Tạo một đơn hàng mới chỉ với 1 sản phẩm, không đụng đến giỏ 'CART'.
   */
  @PostMapping("/checkout/buy-now")
  public ResponseEntity<Integer> checkoutBuyNow(@RequestBody BuyNowRequest req) {
    return ResponseEntity.ok(
        cartService.checkoutBuyNow(currentAccountId(), req));
  }


  // --- Optional: trả mã lỗi & message gọn gàng ---
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<String> handleBadReq(IllegalArgumentException ex) {
    return ResponseEntity.badRequest().body(ex.getMessage());
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<String> handleState(IllegalStateException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
  }
}