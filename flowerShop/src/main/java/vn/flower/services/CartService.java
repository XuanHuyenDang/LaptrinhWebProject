package vn.flower.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import vn.flower.api.dto.BuyNowRequest;
import vn.flower.api.dto.CartLine;
import vn.flower.api.dto.CartView;
import vn.flower.api.dto.CheckoutRequest;
import vn.flower.api.dto.ShippingMethod;
import vn.flower.entities.Account;
import vn.flower.entities.Order;
import vn.flower.entities.OrderDetail;
import vn.flower.entities.OrderDetailId;
import vn.flower.entities.Product;
import vn.flower.repositories.OrderDetailRepository;
import vn.flower.repositories.OrderRepository;
import vn.flower.repositories.ProductRepository;
import vn.flower.entities.DiscountCode; // Thêm import
import vn.flower.services.DiscountCodeService; // Thêm import
import vn.flower.repositories.DiscountCodeRepository; // Thêm import nếu cần save DiscountCode trực tiếp
import vn.flower.api.dto.DiscountCodeDTO; // Thêm DTO cho DiscountCode

@Service
public class CartService {

    private final OrderRepository orderRepo;
    private final OrderDetailRepository odRepo;
    private final ProductRepository productRepo;
    private final DiscountCodeService discountCodeService; // Thêm service
    // Optional: Thêm DiscountCodeRepository nếu bạn muốn save DiscountCode trực tiếp từ CartService
    // private final DiscountCodeRepository discountCodeRepo;

    // Cập nhật constructor
    public CartService(OrderRepository orderRepo,
                     OrderDetailRepository odRepo,
                     ProductRepository productRepo,
                     DiscountCodeService discountCodeService
                     /*, DiscountCodeRepository discountCodeRepo */ ) {
        this.orderRepo = orderRepo;
        this.odRepo = odRepo;
        this.productRepo = productRepo;
        this.discountCodeService = discountCodeService;
        // this.discountCodeRepo = discountCodeRepo; // Gán nếu dùng
    }

    /**
     * Calculates the shipping fee based on the method and subtotal.
     */
    public BigDecimal shippingFeeFor(ShippingMethod method, BigDecimal subtotal) {
      if (subtotal == null || subtotal.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;
      ShippingMethod m = (method != null) ? method : ShippingMethod.FAST;

      return switch (m) {
        case SAVING   -> new BigDecimal("20000");
        case EXPRESS  -> new BigDecimal("60000");
        case FAST     -> (subtotal.compareTo(new BigDecimal("500000"))>=0)
                        ? BigDecimal.ZERO : new BigDecimal("30000");
      };
    }

    private BigDecimal calcSubtotal(List<OrderDetail> lines) {
      if (lines == null) return BigDecimal.ZERO;
      return lines.stream()
          .filter(l -> l != null && l.getPrice() != null && l.getQuantity() != null)
          .map(l -> l.getPrice().multiply(BigDecimal.valueOf(l.getQuantity())))
          .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static final String STATUS_CART = "CART";

    @Transactional
    public Order getOrCreateCart(Integer accountId) {
        return orderRepo.findFirstByAccount_IdAndStatusOrderByIdDesc(accountId, STATUS_CART)
            .orElseGet(() -> {
              Order o = new Order();
              Account acc = new Account();
              acc.setId(accountId);
              o.setAccount(acc);
              o.setStatus(STATUS_CART);
              o.setOrderDate(LocalDateTime.now());
              o.setRecipientName("");
              o.setRecipientPhone("");
              o.setShippingAddress("");
              o.setNote(null);
              o.setPaymentMethod(null);
              o.setShippingFee(BigDecimal.ZERO);
              o.setTotalAmount(BigDecimal.ZERO);
              o.setShippingMethod(ShippingMethod.FAST);
              o.setDiscountAmount(BigDecimal.ZERO); // Khởi tạo discount
              o.setDiscountCode(null);           // Khởi tạo discount code
              return orderRepo.save(o);
            });
    }

    @Transactional
    public CartView addItem(Integer accountId, Integer productId, Integer qty) {
        if (qty == null || qty <= 0) qty = 1;

        Order cart = getOrCreateCart(accountId);
        Product p = productRepo.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm"));

        if (p.getStatus() == null || !p.getStatus()) {
            throw new IllegalStateException("Sản phẩm '" + p.getProductName() + "' hiện đã hết hàng.");
        }

        BigDecimal stampedPrice = p.getSalePrice() != null ? p.getSalePrice() : p.getPrice();

        OrderDetailId id = new OrderDetailId(cart.getId(), productId);
        OrderDetail line = odRepo.findById(id).orElse(null);

        if (line == null) {
          line = new OrderDetail();
          line.setId(id);
          line.setOrder(cart);
          line.setProduct(p);
          line.setQuantity(qty);
          line.setPrice(stampedPrice);
        } else {
          line.setQuantity(line.getQuantity() + qty);
        }
        odRepo.save(line);

        // Khi thêm/sửa sp trong giỏ, cần tính lại discount nếu có
        recalculateDiscount(cart);

        return toView(cart.getId());
    }

    @Transactional
    public CartView updateQty(Integer accountId, Integer productId, Integer qty) {
        Order cart = getOrCreateCart(accountId);
        OrderDetailId id = new OrderDetailId(cart.getId(), productId);
        OrderDetail line = odRepo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Mục không tồn tại trong giỏ hàng"));

        if (qty == null || qty <= 0) {
          odRepo.delete(line);
        } else {
          if (line.getProduct() != null && (line.getProduct().getStatus() == null || !line.getProduct().getStatus())) {
              throw new IllegalStateException("Sản phẩm '" + line.getProduct().getProductName() + "' hiện đã hết hàng.");
          }
          line.setQuantity(qty);
          odRepo.save(line);
        }

        // Khi thêm/sửa sp trong giỏ, cần tính lại discount nếu có
        recalculateDiscount(cart);

        return toView(cart.getId());
    }

    @Transactional
    public CartView removeItem(Integer accountId, Integer productId) {
        Order cart = getOrCreateCart(accountId);
        Integer orderId = cart.getId();

        int deletedCount = odRepo.deleteByOrderIdAndProductId(orderId, productId);

        // Khi thêm/sửa sp trong giỏ, cần tính lại discount nếu có
        recalculateDiscount(cart);

        return toView(cart.getId());
    }

    @Transactional(readOnly = true)
    public CartView getCart(Integer accountId) {
        return orderRepo.findFirstByAccount_IdAndStatusOrderByIdDesc(accountId, STATUS_CART)
            .map(cart -> toView(cart.getId()))
            // Trả về CartView rỗng với discount = 0 và code = null
            .orElseGet(() -> new CartView(null, List.of(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, null));
    }

    // --- Chức năng Discount Code ---
    @Transactional
    public CartView applyDiscountCode(Integer accountId, String code) {
        Order cart = getOrCreateCart(accountId);
        List<OrderDetail> lines = odRepo.findById_OrderId(cart.getId());
        BigDecimal subtotal = calcSubtotal(lines);

        // Validate mã giảm giá
        DiscountCode dc = discountCodeService.validateDiscountCode(code, subtotal);

        // Tính toán số tiền giảm
        BigDecimal discountAmount = discountCodeService.calculateDiscountAmount(dc, subtotal);

        // Cập nhật giỏ hàng (Order)
        cart.setDiscountCode(dc);
        cart.setDiscountAmount(discountAmount);

        orderRepo.save(cart); // Lưu thay đổi vào giỏ hàng

        return toView(cart.getId()); // Trả về view đã cập nhật
    }

    @Transactional
    public CartView removeDiscountCode(Integer accountId) {
        Order cart = getOrCreateCart(accountId);
        cart.setDiscountCode(null);
        cart.setDiscountAmount(BigDecimal.ZERO);
        orderRepo.save(cart);
        return toView(cart.getId());
    }

    /**
     * Tính toán lại số tiền giảm giá cho giỏ hàng hiện tại nếu có mã được áp dụng.
     * Cần gọi hàm này sau khi thêm/sửa/xóa sản phẩm khỏi giỏ.
     */
    private void recalculateDiscount(Order cart) {
        if (cart.getDiscountCode() != null) {
            DiscountCode dc = cart.getDiscountCode();
            // Lấy lại danh sách lines mới nhất từ DB trong cùng transaction
            List<OrderDetail> currentLines = odRepo.findById_OrderId(cart.getId());
            BigDecimal currentSubtotal = calcSubtotal(currentLines);

            // Kiểm tra lại điều kiện tối thiểu một cách an toàn
            if (dc.getMinOrderAmount() != null && currentSubtotal.compareTo(dc.getMinOrderAmount()) < 0) {
               // Nếu không đủ điều kiện nữa -> Xóa mã và discount
               cart.setDiscountCode(null);
               cart.setDiscountAmount(BigDecimal.ZERO);
               System.out.println("Mã giảm giá '" + dc.getCode() + "' đã bị xóa do không đủ điều kiện đơn hàng tối thiểu.");
            } else {
                BigDecimal newDiscountAmount = discountCodeService.calculateDiscountAmount(dc, currentSubtotal);
                cart.setDiscountAmount(newDiscountAmount);
            }
            // Không cần save cart ở đây vì hàm gọi (addItem, updateQty, removeItem) sẽ save
        } else {
             // Đảm bảo discount là 0 nếu không có code
             cart.setDiscountAmount(BigDecimal.ZERO);
        }
    }
    // --- Kết thúc chức năng Discount Code ---

    @Transactional
    public Order checkout(Integer accountId, CheckoutRequest req) {
        Order cart = orderRepo.findFirstByAccount_IdAndStatusOrderByIdDesc(accountId, STATUS_CART)
            .orElseThrow(() -> new IllegalStateException("Không có giỏ hàng hoặc giỏ hàng không hợp lệ."));

        List<OrderDetail> lines = odRepo.findById_OrderId(cart.getId());
        if (lines.isEmpty()) throw new IllegalStateException("Giỏ hàng trống, không thể thanh toán.");

        // Kiểm tra lại tình trạng hàng tồn
        for (OrderDetail line : lines) {
            Product p = line.getProduct();
            if (p == null || p.getStatus() == null || !p.getStatus()) {
                throw new IllegalStateException("Sản phẩm '" + (p != null ? p.getProductName() : "ID " + line.getId().getProductId()) + "' trong giỏ đã hết hàng.");
            }
        }

        // Tính toán lại lần cuối trước khi checkout
        BigDecimal subtotal = calcSubtotal(lines);
        BigDecimal shippingFee = shippingFeeFor(req.shippingMethod(), subtotal);
        // Kiểm tra lại mã giảm giá và tính toán discount lần cuối
        BigDecimal discount = BigDecimal.ZERO;
        DiscountCode appliedCode = cart.getDiscountCode(); // Lấy mã đã lưu trong giỏ
        if(appliedCode != null) {
            try {
                // Validate lại lần cuối (quan trọng)
                appliedCode = discountCodeService.validateDiscountCode(appliedCode.getCode(), subtotal);
                discount = discountCodeService.calculateDiscountAmount(appliedCode, subtotal);
            } catch (IllegalArgumentException e) {
                 // Nếu mã không còn hợp lệ -> Xóa khỏi đơn hàng và báo lỗi? Hoặc tiếp tục ko giảm giá?
                 // Tạm thời: Xóa mã và tiếp tục checkout không giảm giá.
                 System.err.println("Mã giảm giá '" + appliedCode.getCode() + "' không còn hợp lệ khi checkout: " + e.getMessage());
                 appliedCode = null; // Xóa mã
                 discount = BigDecimal.ZERO; // Không giảm giá
                 cart.setDiscountCode(null);
                 cart.setDiscountAmount(BigDecimal.ZERO);
                 // Có thể ném lỗi để báo user: throw new IllegalStateException("Mã giảm giá không còn hợp lệ: " + e.getMessage());
            }
        } else {
             cart.setDiscountAmount(BigDecimal.ZERO); // Đảm bảo discount là 0 nếu không có code
        }

        BigDecimal finalTotal = subtotal.add(shippingFee).subtract(discount).max(BigDecimal.ZERO); // Tính total cuối

        // Cập nhật thông tin đơn hàng
        cart.setRecipientName(req.recipientName());
        cart.setRecipientPhone(req.recipientPhone());
        cart.setShippingAddress(req.shippingAddress());
        cart.setNote(req.note());
        cart.setPaymentMethod(req.paymentMethod());
        cart.setShippingMethod(req.shippingMethod());
        cart.setShippingFee(shippingFee); // Lưu phí ship đã tính
        cart.setDiscountAmount(discount); // Lưu discount đã tính (hoặc 0)
        cart.setTotalAmount(finalTotal);  // Lưu total cuối cùng
        cart.setOrderDate(LocalDateTime.now());
        cart.setDiscountCode(appliedCode); // Lưu lại code đã áp dụng (hoặc null)

        // Xử lý trạng thái và số lượng bán
        if ("VNPAY".equalsIgnoreCase(req.paymentMethod())) {
            cart.setStatus("Chờ thanh toán");
            // Không trừ sold và không tăng usage của discount code vội
        } else {
            cart.setStatus("Đang xử lý");
            // Trừ sold
            for (OrderDetail d : lines) {
              Product p = d.getProduct();
              if (p != null) {
                  p.setSold((p.getSold() == null ? 0 : p.getSold()) + d.getQuantity());
                  // productRepo.save(p); // Thường không cần nếu cascade
              }
            }
            // Tăng usage của discount code
            if (appliedCode != null) {
                appliedCode.setCurrentUsage(appliedCode.getCurrentUsage() + 1);
                // discountCodeRepo.save(appliedCode); // Lưu nếu không cascade hoặc để @Transactional quản lý
            }
        }

        return orderRepo.save(cart); // Lưu đơn hàng (trạng thái CART -> Đang xử lý/Chờ thanh toán)
    }

    @Transactional
    public Order checkoutBuyNow(Integer accountId, BuyNowRequest req) {
        // ... (phần kiểm tra product, tạo Order, OrderDetail giữ nguyên) ...
        Product p = productRepo.findById(req.productId())
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + req.productId()));
        if (p.getStatus() == null || !p.getStatus()) {
            throw new IllegalStateException("Sản phẩm '" + p.getProductName() + "' hiện đã hết hàng.");
        }
        if (req.quantity() <= 0) {
            throw new IllegalArgumentException("Số lượng sản phẩm phải lớn hơn 0.");
        }

        Account acc = new Account();
        acc.setId(accountId);
        CheckoutRequest checkoutInfo = req.checkout();

        Order order = new Order();
        order.setAccount(acc);
        order.setOrderDate(LocalDateTime.now());
        order.setRecipientName(checkoutInfo.recipientName());
        order.setRecipientPhone(checkoutInfo.recipientPhone());
        order.setShippingAddress(checkoutInfo.shippingAddress());
        order.setNote(checkoutInfo.note());
        order.setPaymentMethod(checkoutInfo.paymentMethod());
        order.setShippingMethod(checkoutInfo.shippingMethod());

        OrderDetail line = new OrderDetail();
        BigDecimal stampedPrice = p.getSalePrice() != null ? p.getSalePrice() : p.getPrice();

        line.setId(new OrderDetailId(null, p.getId())); // OrderId will be set by JPA
        line.setOrder(order);
        line.setProduct(p);
        line.setQuantity(req.quantity());
        line.setPrice(stampedPrice);

        // Tính subtotal, shipping
        BigDecimal subtotal = stampedPrice.multiply(BigDecimal.valueOf(req.quantity()));
        BigDecimal shippingFee = shippingFeeFor(checkoutInfo.shippingMethod(), subtotal);

        // Xử lý discount code cho Buy Now (nếu có trong CheckoutRequest)
        BigDecimal discount = BigDecimal.ZERO;
        DiscountCode appliedCode = null;

        // *** FIXED AGAIN: Removed the problematic line referencing discountCode() ***
        String discountCodeString = null; // Assume no discount code for Buy Now unless explicitly passed

        // If you decide to support discount codes for Buy Now:
        // 1. Add `String discountCode` to CheckoutRequest record.
        // 2. Modify BuyNowRequest record to include it if necessary, or modify how CheckoutRequest is created/passed.
        // 3. Uncomment and modify the line below if you add discountCode to CheckoutRequest:
        // String discountCodeString = checkoutInfo.discountCode();

        if (discountCodeString != null && !discountCodeString.isBlank()) {
           try {
               appliedCode = discountCodeService.validateDiscountCode(discountCodeString, subtotal);
               discount = discountCodeService.calculateDiscountAmount(appliedCode, subtotal);
           } catch (IllegalArgumentException e) {
               // Bỏ qua mã không hợp lệ hoặc ném lỗi tùy logic
               System.err.println("Mã giảm giá mua ngay không hợp lệ: " + e.getMessage());
               // throw new IllegalStateException("Mã giảm giá không hợp lệ: " + e.getMessage());
           }
        }

        BigDecimal total = subtotal.add(shippingFee).subtract(discount).max(BigDecimal.ZERO);

        // Lưu thông tin vào Order
        order.setShippingFee(shippingFee);
        order.setDiscountCode(appliedCode);
        order.setDiscountAmount(discount);
        order.setTotalAmount(total);
        order.setDetails(List.of(line)); // Gắn detail vào order

        // Xử lý trạng thái, sold, usage
        if ("VNPAY".equalsIgnoreCase(checkoutInfo.paymentMethod())) {
            order.setStatus("Chờ thanh toán");
        } else {
            order.setStatus("Đang xử lý");
            p.setSold((p.getSold() == null ? 0 : p.getSold()) + req.quantity());
            if (appliedCode != null) {
                appliedCode.setCurrentUsage(appliedCode.getCurrentUsage() + 1);
            }
        }

        return orderRepo.save(order); // Lưu order mới
    }


    private record Totals(BigDecimal subtotal, BigDecimal shipping, BigDecimal total) {}

    // Convert Order to CartView
    private CartView toView(Integer orderId) {
        Order order = orderRepo.findById(orderId).orElse(null);
        if (order == null) {
            // Đảm bảo trả về DTO null nếu không có order
            return new CartView(orderId, List.of(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, null);
        }

        List<OrderDetail> lines = odRepo.findById_OrderId(orderId);
        BigDecimal subtotal = calcSubtotal(lines);
        BigDecimal shipping = shippingFeeFor(order.getShippingMethod(), subtotal);
        BigDecimal discount = order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO; // Lấy discount từ Order
        BigDecimal total = subtotal.add(shipping).subtract(discount).max(BigDecimal.ZERO);

        // Chuyển đổi DiscountCode entity sang DTO
        DiscountCode dcEntity = order.getDiscountCode();
        DiscountCodeDTO dcDto = null;
        if (dcEntity != null) {
            // *** Quan trọng: Đảm bảo proxy được khởi tạo TRƯỚC KHI đọc dữ liệu ***
            // Cách 1: Dùng Hibernate.initialize() (Cần @Transactional ở phương thức gọi getCart)
            // Hibernate.initialize(dcEntity); // Nếu getCart() có @Transactional(readOnly=true) thì dùng cách này

            // Cách 2: Truy cập một trường để kích hoạt tải (ít tin cậy hơn)
            // dcEntity.getCode(); // Ví dụ

            // *** Tạo DTO sau khi đảm bảo entity đã được tải ***
             dcDto = new DiscountCodeDTO(
                 dcEntity.getCode(),
                 dcEntity.getDescription(),
                 dcEntity.getDiscountPercent(),
                 dcEntity.getDiscountAmount()
                 // Map các trường khác nếu cần
             );
        }

        var viewLines = lines.stream()
                .filter(l -> l != null && l.getProduct() != null)
                .map(l -> {
                    Product p = l.getProduct(); // Lấy product
                    return new CartLine(
                        p.getId(),
                        p.getProductName(),
                        l.getQuantity(),
                        l.getPrice(),
                        l.getPrice().multiply(BigDecimal.valueOf(l.getQuantity())),
                        p.getImageUrl() // <-- LẤY imageUrl TỪ PRODUCT
                    );
                }).collect(Collectors.toList());

        // Trả về CartView với DTO
        return new CartView(orderId, viewLines, subtotal, shipping, discount, total, dcDto);
    }

    // Calculate totals (không thay đổi)
    private Totals calcTotals(List<OrderDetail> lines, BigDecimal calculatedShippingFee) {
        BigDecimal subtotal = calcSubtotal(lines);
        BigDecimal shipping = subtotal.compareTo(BigDecimal.ZERO) > 0 ? calculatedShippingFee : BigDecimal.ZERO;
        BigDecimal total = subtotal.add(shipping);
        return new Totals(subtotal, shipping, total);
    }
}