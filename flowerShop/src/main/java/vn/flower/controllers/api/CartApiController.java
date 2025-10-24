package vn.flower.controllers.api;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

// === ADDED MISSING IMPORTS ===
import java.math.BigDecimal;
import java.util.List;
// =============================

import vn.flower.api.dto.AddItemRequest;
import vn.flower.api.dto.BuyNowRequest;
import vn.flower.api.dto.CartView;
import vn.flower.api.dto.CheckoutRequest;
import vn.flower.api.dto.UpdateQtyRequest;
import vn.flower.api.dto.CheckoutResponse;
import vn.flower.entities.Order;
import vn.flower.repositories.AccountRepository;
import vn.flower.services.CartService;
import vn.flower.services.VnpayService;

@RestController
@RequestMapping("/api/cart")
public class CartApiController {

  private final CartService cartService;
  private final AccountRepository accountRepo;
  private final VnpayService vnpayService;

  public CartApiController(CartService cartService, AccountRepository accountRepo, VnpayService vnpayService) {
    this.cartService = cartService;
    this.accountRepo = accountRepo;
    this.vnpayService = vnpayService;
  }

  // Lấy Account ID của người dùng hiện tại
  private Integer currentAccountId() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
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

  // Lấy thông tin giỏ hàng hiện tại
  @GetMapping
  public ResponseEntity<CartView> getCart() {
    try {
        // currentAccountId() throws 401 if not logged in
        return ResponseEntity.ok(cartService.getCart(currentAccountId()));
    } catch (ResponseStatusException e) {
         // If exception is 401 (Unauthorized), return an empty cart view
         if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
             // Correctly construct an empty CartView
             return ResponseEntity.ok(new CartView(null, List.of(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
         }
         // Re-throw other ResponseStatusExceptions
         throw e;
    } catch (Exception e) {
        // Handle other unexpected errors
        System.err.println("Error fetching cart: " + e.getMessage()); // Log the error
        // Return a 500 error with an empty cart or an error message
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body(new CartView(null, List.of(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO)); // Return empty cart on error
    }
  }

  // Thêm sản phẩm vào giỏ
  @PostMapping("/items")
  public ResponseEntity<CartView> addItem(@RequestBody AddItemRequest req) {
    // currentAccountId() throws 401
    return ResponseEntity.ok(
        cartService.addItem(currentAccountId(), req.productId(), req.quantity()));
  }

  // Cập nhật số lượng sản phẩm trong giỏ
  @PutMapping("/items/{productId}")
  public ResponseEntity<CartView> updateQty(
      @PathVariable Integer productId,
      @RequestBody UpdateQtyRequest req) {
    // currentAccountId() throws 401
    return ResponseEntity.ok(
        cartService.updateQty(currentAccountId(), productId, req.quantity()));
  }

  // Xóa sản phẩm khỏi giỏ
  @DeleteMapping("/items/{productId}")
  public ResponseEntity<CartView> remove(@PathVariable Integer productId) {
    // currentAccountId() throws 401
    return ResponseEntity.ok(
        cartService.removeItem(currentAccountId(), productId));
  }

  /**
   * API xử lý thanh toán cho GIỎ HÀNG (status='CART')
   * Trả về CheckoutResponse chứa orderId và paymentUrl (nếu có).
   */
  @PostMapping("/checkout")
  public ResponseEntity<CheckoutResponse> checkout(@RequestBody CheckoutRequest req, HttpServletRequest httpServletRequest) {
    Integer accountId = currentAccountId(); // Ném 401 nếu chưa đăng nhập

    Order order = cartService.checkout(accountId, req); // Returns Order object

    String paymentUrl = null;
    String paymentMethod = order.getPaymentMethod() != null ? order.getPaymentMethod().toUpperCase() : "COD";

    if ("VNPAY".equals(paymentMethod) && "Chờ thanh toán".equals(order.getStatus())) {
        try {
            paymentUrl = vnpayService.createPaymentUrl(order, httpServletRequest);
        } catch (Exception e) {
             System.err.println("Lỗi tạo URL VNPAY cho đơn hàng " + order.getId() + ": " + e.getMessage());
             // Consider how to handle this - maybe return error or proceed without URL
             // For now, we proceed without the URL
        }
    }

    CheckoutResponse response = new CheckoutResponse(
        order.getId(),
        paymentMethod,
        paymentUrl,
        order.getStatus()
    );
    return ResponseEntity.ok(response);
  }

  /**
   * API xử lý thanh toán "MUA NGAY"
   * Trả về CheckoutResponse chứa orderId và paymentUrl (nếu có).
   */
  @PostMapping("/checkout/buy-now")
  public ResponseEntity<CheckoutResponse> checkoutBuyNow(@RequestBody BuyNowRequest req, HttpServletRequest httpServletRequest) {
    Integer accountId = currentAccountId(); // Ném 401

    Order order = cartService.checkoutBuyNow(accountId, req); // Returns Order object

    String paymentUrl = null;
    String paymentMethod = order.getPaymentMethod() != null ? order.getPaymentMethod().toUpperCase() : "COD";

    if ("VNPAY".equals(paymentMethod) && "Chờ thanh toán".equals(order.getStatus())) {
         try {
            paymentUrl = vnpayService.createPaymentUrl(order, httpServletRequest);
         } catch (Exception e) {
             System.err.println("Lỗi tạo URL VNPAY cho đơn hàng Mua Ngay " + order.getId() + ": " + e.getMessage());
             // Handle error as above
         }
    }

    CheckoutResponse response = new CheckoutResponse(
        order.getId(),
        paymentMethod,
        paymentUrl,
        order.getStatus()
    );
    return ResponseEntity.ok(response);
  }


  // --- Exception Handlers ---
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<String> handleBadReq(IllegalArgumentException ex) {
    return ResponseEntity.badRequest().body(ex.getMessage());
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<String> handleState(IllegalStateException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<String> handleResponseStatus(ResponseStatusException ex) {
      return ResponseEntity.status(ex.getStatusCode()).body(ex.getReason());
  }
}