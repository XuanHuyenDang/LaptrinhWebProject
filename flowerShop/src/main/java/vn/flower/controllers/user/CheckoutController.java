package vn.flower.controllers.user;

import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import vn.flower.api.dto.CartLine;
import vn.flower.entities.Product;
import vn.flower.entities.Order;
import vn.flower.repositories.ProductRepository;
import vn.flower.entities.Account;
import vn.flower.repositories.AccountRepository;
import vn.flower.repositories.OrderRepository;
import vn.flower.services.CartService;
import vn.flower.api.dto.CartView;
import vn.flower.api.dto.ShippingMethod;
import vn.flower.api.dto.CheckoutRequest; // Make sure this is imported
import vn.flower.api.dto.BuyNowRequest;
import vn.flower.services.VnpayService;
import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.Hibernate;
import org.springframework.transaction.annotation.Transactional;

@Controller
public class CheckoutController {

    private final CartService cartService;
    private final AccountRepository accountRepo;
    private final OrderRepository orderRepo;
    private final ProductRepository productRepo;
    private final VnpayService vnpayService;

    public CheckoutController(CartService cartService,
                              AccountRepository accountRepo,
                              OrderRepository orderRepo,
                              ProductRepository productRepo,
                              VnpayService vnpayService) {
        this.cartService = cartService;
        this.accountRepo = accountRepo;
        this.orderRepo = orderRepo;
        this.productRepo = productRepo;
        this.vnpayService = vnpayService;
    }

    // Helper to get current Account ID
    private Integer currentAccountId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
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
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sản phẩm '" + p.getProductName() + "' đã hết hàng.");
            }

            BigDecimal price = p.getSalePrice() != null ? p.getSalePrice() : p.getPrice();
            BigDecimal lineTotal = price.multiply(BigDecimal.valueOf(qty));

            CartLine line = new CartLine(p.getId(), p.getProductName(), qty, price, lineTotal);

            // Calculate shipping and total for Buy Now using CartService helper
            BigDecimal shipping = cartService.shippingFeeFor(ShippingMethod.FAST, lineTotal); // Assuming default FAST
            BigDecimal discount = BigDecimal.ZERO; // No discount applied in Buy Now view initially
            BigDecimal total = lineTotal.add(shipping).subtract(discount).max(BigDecimal.ZERO);

            cart = new CartView(null, List.of(line), lineTotal, shipping, discount, total, null); // Added discount (0) and null code

            model.addAttribute("isBuyNow", true);
            model.addAttribute("buyNowProductId", productId);
            model.addAttribute("buyNowQty", qty);
        } else {
            // Fetch the actual cart from the service
            cart = cartService.getCart(accId);
            if (cart.lines() == null || cart.lines().isEmpty()) {
                return "redirect:/products?message=emptyCart";
            }
            model.addAttribute("isBuyNow", false);
        }

        model.addAttribute("cart", cart);

        // *** FIXED CONSTRUCTOR CALL ***
        // Create and pre-fill the checkout form DTO based on the CheckoutRequest record definition
        CheckoutRequest form = new CheckoutRequest(
                currentAccount != null ? currentAccount.getFullName() : "",
                currentAccount != null ? currentAccount.getPhoneNumber() : "",
                currentAccount != null ? currentAccount.getAddress() : "",
                null, // note
                "COD", // paymentMethod
                ShippingMethod.FAST // shippingMethod
                // Removed the extra null for discountCode as it's not in the original record definition
        );
        model.addAttribute("form", form);
        model.addAttribute("account", currentAccount);

        if (csrfToken != null) model.addAttribute("_csrf", csrfToken);

        return "checkout";
    }


    // Handle Checkout Form Submission (POST request)
    @PostMapping("/checkout")
    public String doCheckout(@ModelAttribute("form") CheckoutRequest form,
                             RedirectAttributes ra,
                             @RequestParam(required = false) Boolean isBuyNow,
                             @RequestParam(required = false) Integer buyNowProductId,
                             @RequestParam(required = false) Integer buyNowQty,
                             HttpServletRequest httpServletRequest) { // Add request for VNPAY
        Integer accId = currentAccountId();

        if (form.recipientName() == null || form.recipientName().isBlank()
                || form.recipientPhone() == null || form.recipientPhone().isBlank()
                || form.shippingAddress() == null || form.shippingAddress().isBlank()) {
            ra.addFlashAttribute("error", "Vui lòng nhập đầy đủ Họ tên, SĐT, Địa chỉ.");
            if (Boolean.TRUE.equals(isBuyNow)) {
                ra.addAttribute("buyNow", true);
                ra.addAttribute("productId", buyNowProductId);
                ra.addAttribute("qty", buyNowQty);
            }
            return "redirect:/checkout";
        }

        try {
            Order order;

            if (Boolean.TRUE.equals(isBuyNow) && buyNowProductId != null && buyNowQty != null) {
                // Ensure BuyNowRequest constructor takes CheckoutRequest, productId, quantity
                BuyNowRequest buyNowReq = new BuyNowRequest(form, buyNowProductId, buyNowQty);
                order = cartService.checkoutBuyNow(accId, buyNowReq);
            } else {
                order = cartService.checkout(accId, form);
            }

            Integer orderId = order.getId();

            if ("VNPAY".equalsIgnoreCase(order.getPaymentMethod()) && "Chờ thanh toán".equals(order.getStatus())) {
                try {
                    String vnpayUrl = vnpayService.createPaymentUrl(order, httpServletRequest);
                    return "redirect:" + vnpayUrl; // Redirect directly to VNPAY
                } catch (Exception e) {
                    System.err.println("Lỗi tạo URL VNPAY khi checkout: " + e.getMessage());
                    ra.addFlashAttribute("error", "Lỗi tạo link thanh toán VNPAY. Vui lòng thử lại hoặc chọn phương thức khác.");
                    return "redirect:/orders/" + orderId;
                }
            } else {
                // COD or BANK: Redirect to the order success page
                // Success message implicitly handled by showing order details
                return "redirect:/orders/" + orderId;
            }

        } catch (IllegalArgumentException | IllegalStateException e) {
            ra.addFlashAttribute("error", "Đặt hàng thất bại: " + e.getMessage());
            if (Boolean.TRUE.equals(isBuyNow)) {
                ra.addAttribute("buyNow", true);
                ra.addAttribute("productId", buyNowProductId);
                ra.addAttribute("qty", buyNowQty);
            }
            return "redirect:/checkout";
        } catch (Exception e) {
            System.err.println("Lỗi checkout không mong muốn: " + e.getMessage());
             e.printStackTrace(); // Print stack trace for debugging
            ra.addFlashAttribute("error", "Đã xảy ra lỗi không mong muốn khi đặt hàng. Vui lòng thử lại.");
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
    @Transactional(readOnly = true) // Giữ nguyên annotation này
    public String orderSuccess(@PathVariable Integer id, Model model, HttpServletRequest httpServletRequest) {
        Integer accId = currentAccountId();
        Order order = orderRepo.findByIdAndAccount_Id(id, accId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng #" + id + " hoặc bạn không có quyền xem đơn hàng này."));

        // *** THÊM ĐOẠN NÀY ĐỂ KHỞI TẠO EXPLICITLY ***
        if (order.getDiscountCode() != null) {
            Hibernate.initialize(order.getDiscountCode());
        }
        // *******************************************

        model.addAttribute("order", order); // Truyền order đã (có thể) được khởi tạo discountCode

        // Logic hiển thị nút "Thanh toán ngay" cho VNPAY (giữ nguyên)
        if ("Chờ thanh toán".equals(order.getStatus()) && "VNPAY".equalsIgnoreCase(order.getPaymentMethod())) {
            model.addAttribute("pendingVnpayPayment", true);
            try {
                String paymentUrl = vnpayService.createPaymentUrl(order, httpServletRequest);
                model.addAttribute("vnpayPaymentUrl", paymentUrl);
            } catch (Exception e) {
                System.err.println("Lỗi tạo lại URL VNPAY cho đơn hàng " + id + ": " + e.getMessage());
                model.addAttribute("vnpayError", "Không thể tạo liên kết thanh toán VNPAY. Vui lòng thử lại sau.");
            }
        }

        return "user/order-success";
    }
}