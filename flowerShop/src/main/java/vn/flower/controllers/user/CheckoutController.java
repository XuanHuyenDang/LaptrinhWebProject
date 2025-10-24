// java/vn/flower/controllers/user/CheckoutController.java
package vn.flower.controllers.user;

import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException; // **Đảm bảo import này có**
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import vn.flower.api.dto.CartLine;
import vn.flower.entities.Product;
import vn.flower.entities.Order;   // **Make sure Order is imported**
import vn.flower.repositories.ProductRepository;
import vn.flower.entities.Account; // Import Account
import vn.flower.repositories.AccountRepository;
import vn.flower.repositories.OrderRepository;
import vn.flower.services.CartService;
import vn.flower.api.dto.CartView;
import vn.flower.api.dto.ShippingMethod;
import vn.flower.api.dto.CheckoutRequest;
import vn.flower.api.dto.BuyNowRequest; // **Import BuyNowRequest**
import vn.flower.services.VnpayService; // **THÊM IMPORT**
import jakarta.servlet.http.HttpServletRequest; // **THÊM IMPORT**

@Controller
public class CheckoutController {

  private final CartService cartService;
  private final AccountRepository accountRepo;
  private final OrderRepository orderRepo;
  private final ProductRepository productRepo;
  private final VnpayService vnpayService; // **INJECT VNPAY SERVICE**

  public CheckoutController(CartService cartService,
                            AccountRepository accountRepo,
                            OrderRepository orderRepo,
                            ProductRepository productRepo,
                            VnpayService vnpayService // **THÊM VÀO CONSTRUCTOR**
                            ) {
    this.cartService = cartService;
    this.accountRepo = accountRepo;
    this.orderRepo = orderRepo;
    this.productRepo = productRepo;
    this.vnpayService = vnpayService; // **GÁN VNPAY SERVICE**
  }

  // Helper to get current Account ID
   private Integer currentAccountId() {
     var auth = SecurityContextHolder.getContext().getAuthentication();
     // Use equalsIgnoreCase for comparing string literals safely
     if (auth == null || !auth.isAuthenticated() || "anonymousUser".equalsIgnoreCase(String.valueOf(auth.getPrincipal()))) {
         throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Chưa đăng nhập");
     }
     String email = (auth.getPrincipal() instanceof UserDetails u) ? u.getUsername() : auth.getName();
     return accountRepo.findByEmail(email)
         .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Không tìm thấy tài khoản: " + email))
         .getId();
   }

   // Show Checkout Page (GET request)
   @GetMapping("/checkout")
   public String showCheckout(
       @RequestParam(required = false) Boolean buyNow,
       @RequestParam(required = false) Integer productId,
       @RequestParam(required = false) Integer qty,
       Model model,
       @Nullable CsrfToken csrfToken) {

     Integer accId = currentAccountId(); // Throws 401 if not logged in
     CartView cart;
     boolean isBuyNowFlow = (buyNow != null && buyNow && productId != null && qty != null && qty > 0);
     Account currentAccount = accountRepo.findById(Long.valueOf(accId)).orElse(null); // Get account info

     if (isBuyNowFlow) {
        Product p = productRepo.findById(productId)
           .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sản phẩm không tồn tại: ID " + productId));

        if (p.getStatus() == null || !p.getStatus()) {
             // Redirect back or show error if product is out of stock
             // For simplicity, throwing exception here. Consider redirecting with flash attribute.
             throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sản phẩm '" + p.getProductName() + "' đã hết hàng.");
        }

        BigDecimal price = p.getSalePrice() != null ? p.getSalePrice() : p.getPrice();
        BigDecimal lineTotal = price.multiply(BigDecimal.valueOf(qty));

        CartLine line = new CartLine(p.getId(), p.getProductName(), qty, price, lineTotal);

        // Calculate shipping and total for Buy Now using CartService helper
        // Assuming default FAST shipping for Buy Now view calculation
        BigDecimal shipping = cartService.shippingFeeFor(ShippingMethod.FAST, lineTotal);
        BigDecimal total = lineTotal.add(shipping);

        // Create a temporary CartView for Buy Now scenario
        cart = new CartView(null, List.of(line), lineTotal, shipping, total);

        model.addAttribute("isBuyNow", true);
        model.addAttribute("buyNowProductId", productId);
        model.addAttribute("buyNowQty", qty);
     } else {
       // Fetch the actual cart from the service
       cart = cartService.getCart(accId);
       // Check if the cart is empty and redirect if necessary
       if (cart.lines() == null || cart.lines().isEmpty()) {
          // Redirect to products page with a message
          return "redirect:/products?message=emptyCart";
       }
       model.addAttribute("isBuyNow", false);
     }

     // Add cart data to the model
     model.addAttribute("cart", cart);

     // Create and pre-fill the checkout form DTO
     CheckoutRequest form = new CheckoutRequest(
         currentAccount != null ? currentAccount.getFullName() : "",
         currentAccount != null ? currentAccount.getPhoneNumber() : "",
         currentAccount != null ? currentAccount.getAddress() : "",
         null, // Note initially empty
         "COD", // Default payment method
         ShippingMethod.FAST // Default shipping method
     );
     model.addAttribute("form", form);
     model.addAttribute("account", currentAccount); // Pass the account object for potential use in Thymeleaf

     // Add CSRF token if present
     if (csrfToken != null) model.addAttribute("_csrf", csrfToken);

     // Return the checkout view template name
     return "checkout";
   }


  // Handle Checkout Form Submission (POST request)
  // This method might be less relevant if using the API + JS approach primarily
  @PostMapping("/checkout")
  public String doCheckout(@ModelAttribute("form") CheckoutRequest form,
                           RedirectAttributes ra,
                           // Include Buy Now parameters if this form handles both flows
                           @RequestParam(required = false) Boolean isBuyNow,
                           @RequestParam(required = false) Integer buyNowProductId,
                           @RequestParam(required = false) Integer buyNowQty
                           /*, HttpServletRequest httpServletRequest */) { // Add request if generating VNPAY URL here
    Integer accId = currentAccountId(); // Throws 401 if not logged in

    // Basic form validation
    if (form.recipientName() == null || form.recipientName().isBlank()
        || form.recipientPhone() == null || form.recipientPhone().isBlank()
        || form.shippingAddress() == null || form.shippingAddress().isBlank()) {
      ra.addFlashAttribute("error", "Vui lòng nhập đầy đủ Họ tên, SĐT, Địa chỉ.");
      // Add Buy Now parameters back to redirect attributes if it was a Buy Now flow
      if (Boolean.TRUE.equals(isBuyNow)) {
          ra.addAttribute("buyNow", true);
          ra.addAttribute("productId", buyNowProductId);
          ra.addAttribute("qty", buyNowQty);
      }
      return "redirect:/checkout";
    }

    try {
        Order order; // Declare Order variable

        // Determine if it's Buy Now or regular checkout
        if (Boolean.TRUE.equals(isBuyNow) && buyNowProductId != null && buyNowQty != null) {
            // === Buy Now Flow ===
            BuyNowRequest buyNowReq = new BuyNowRequest(form, buyNowProductId, buyNowQty);
            order = cartService.checkoutBuyNow(accId, buyNowReq); // Call Buy Now service method
        } else {
            // === Regular Cart Checkout Flow ===
            order = cartService.checkout(accId, form); // Call regular checkout service method
        }

        // --- **FIXED HERE** ---
        Integer orderId = order.getId(); // Get the ID from the returned Order object
        // --- **END FIX** ---

        // Check payment method and redirect accordingly
        if ("VNPAY".equalsIgnoreCase(order.getPaymentMethod()) && "Chờ thanh toán".equals(order.getStatus())) {
            // VNPAY: Redirect logic ideally handled by API/JS.
            // If handling here, you'd generate the URL and redirect.
            // String vnpayUrl = vnpayService.createPaymentUrl(order, httpServletRequest);
            // return "redirect:" + vnpayUrl;
            // For now, redirect to order details page, JS should handle VNPAY redirect
             ra.addFlashAttribute("infoMessage", "Đơn hàng #" + orderId + " đang chờ thanh toán VNPAY.");
             return "redirect:/orders/" + orderId;
        } else {
            // COD or BANK: Redirect to the order success page
            ra.addFlashAttribute("success", "Đặt hàng thành công! Mã đơn #" + orderId);
            return "redirect:/orders/" + orderId;
        }

    } catch (IllegalArgumentException | IllegalStateException e) {
        // Handle specific business logic errors (e.g., empty cart, item out of stock)
        ra.addFlashAttribute("error", "Đặt hàng thất bại: " + e.getMessage());
        // Add Buy Now parameters back to redirect attributes
         if (Boolean.TRUE.equals(isBuyNow)) {
            ra.addAttribute("buyNow", true);
            ra.addAttribute("productId", buyNowProductId);
            ra.addAttribute("qty", buyNowQty);
         }
        return "redirect:/checkout";
    } catch (Exception e) {
        // Handle unexpected errors
        System.err.println("Lỗi checkout không mong muốn: " + e.getMessage()); // Log error
        ra.addFlashAttribute("error", "Đã xảy ra lỗi không mong muốn khi đặt hàng. Vui lòng thử lại.");
         // Add Buy Now parameters back to redirect attributes
         if (Boolean.TRUE.equals(isBuyNow)) {
            ra.addAttribute("buyNow", true);
            ra.addAttribute("productId", buyNowProductId);
            ra.addAttribute("qty", buyNowQty);
         }
        return "redirect:/checkout";
    }
  }


  // View Order Success/Details Page (GET request)
  @GetMapping("/orders/{id}")
  public String orderSuccess(@PathVariable Integer id, Model model, HttpServletRequest httpServletRequest /* **THÊM HttpServletRequest** */) {
       Integer accId = currentAccountId(); // Throws 401 if not logged in
       // Find the order by ID and ensure it belongs to the current user
       Order order = orderRepo.findByIdAndAccount_Id(id, accId)
           .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng #" + id + " hoặc bạn không có quyền xem đơn hàng này."));

       model.addAttribute("order", order);

       // *** LOGIC THÊM NÚT THANH TOÁN LẠI VNPAY ***
       if ("Chờ thanh toán".equals(order.getStatus()) && "VNPAY".equalsIgnoreCase(order.getPaymentMethod())) {
           model.addAttribute("pendingVnpayPayment", true);
           try {
               // Tạo lại URL thanh toán VNPAY
               String paymentUrl = vnpayService.createPaymentUrl(order, httpServletRequest);
               model.addAttribute("vnpayPaymentUrl", paymentUrl);
           } catch (Exception e) {
               System.err.println("Lỗi tạo lại URL VNPAY cho đơn hàng " + id + ": " + e.getMessage());
               // Thêm thuộc tính lỗi vào model để hiển thị thông báo trên trang
               model.addAttribute("vnpayError", "Không thể tạo liên kết thanh toán VNPAY. Vui lòng thử lại sau.");
           }
       }
       // *** KẾT THÚC LOGIC THÊM ***

       return "user/order-success"; // Return the success/details view template
  }
}