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
import vn.flower.repositories.OrderDetailRepository; // Đảm bảo import này có
import vn.flower.repositories.OrderRepository;
import vn.flower.repositories.ProductRepository;

@Service
public class CartService {

  private final OrderRepository orderRepo;
  private final OrderDetailRepository odRepo;
  private final ProductRepository productRepo;

  public CartService(OrderRepository orderRepo,
                     OrderDetailRepository odRepo,
                     ProductRepository productRepo) {
    this.orderRepo = orderRepo;
    this.odRepo = odRepo;
    this.productRepo = productRepo;
  }

  // --- **FIXED HERE: Changed private to public** ---
  /**
   * Calculates the shipping fee based on the method and subtotal.
   * Made public so CheckoutController can use it for Buy Now view calculation.
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
  // --- **END FIX** ---

  private BigDecimal calcSubtotal(List<OrderDetail> lines) {
	  if (lines == null) return BigDecimal.ZERO; // Add null check
      return lines.stream()
          .filter(l -> l != null && l.getPrice() != null && l.getQuantity() != null) // Add null checks for safety
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
    // Let Spring manage flushing at the end of the transaction unless explicitly needed
    // odRepo.flush();

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
      // odRepo.flush();
    } else {
      if (line.getProduct() != null && (line.getProduct().getStatus() == null || !line.getProduct().getStatus())) {
          throw new IllegalStateException("Sản phẩm '" + line.getProduct().getProductName() + "' hiện đã hết hàng.");
      }
      line.setQuantity(qty);
      odRepo.save(line);
      // odRepo.flush();
    }
    return toView(cart.getId());
  }

  @Transactional
  public CartView removeItem(Integer accountId, Integer productId) {
    Order cart = getOrCreateCart(accountId);
    Integer orderId = cart.getId(); // Lấy orderId từ cart

    // === THAY ĐỔI Ở ĐÂY ===
    // Sử dụng phương thức xóa tùy chỉnh thay vì deleteById
    int deletedCount = odRepo.deleteByOrderIdAndProductId(orderId, productId);
    // Bạn có thể kiểm tra deletedCount nếu cần (ví dụ: > 0 là xóa thành công)
    // odRepo.flush(); // Thường không cần flush thủ công trong @Transactional
    // ======================

    return toView(cart.getId()); // Trả về view giỏ hàng đã cập nhật
  }


  @Transactional(readOnly = true)
  public CartView getCart(Integer accountId) {
    return orderRepo.findFirstByAccount_IdAndStatusOrderByIdDesc(accountId, STATUS_CART)
        .map(cart -> toView(cart.getId()))
        .orElseGet(() -> new CartView(null, List.of(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO));
  }

  @Transactional
  public Order checkout(Integer accountId, CheckoutRequest req) {
    Order cart = orderRepo.findFirstByAccount_IdAndStatusOrderByIdDesc(accountId, STATUS_CART)
        .orElseThrow(() -> new IllegalStateException("Không có giỏ hàng hoặc giỏ hàng không hợp lệ."));

    List<OrderDetail> lines = odRepo.findById_OrderId(cart.getId());
    if (lines.isEmpty()) throw new IllegalStateException("Giỏ hàng trống, không thể thanh toán.");

    for (OrderDetail line : lines) {
        Product p = line.getProduct();
        if (p == null || p.getStatus() == null || !p.getStatus()) {
            throw new IllegalStateException("Sản phẩm '" + (p != null ? p.getProductName() : "ID " + line.getId().getProductId()) + "' trong giỏ đã hết hàng.");
        }
    }

    BigDecimal subtotal = calcSubtotal(lines);
    BigDecimal shippingFee = shippingFeeFor(req.shippingMethod(), subtotal);
    Totals totals = calcTotals(lines, shippingFee);

    cart.setRecipientName(req.recipientName());
    cart.setRecipientPhone(req.recipientPhone());
    cart.setShippingAddress(req.shippingAddress());
    cart.setNote(req.note());
    cart.setPaymentMethod(req.paymentMethod());
    cart.setShippingMethod(req.shippingMethod());
    cart.setShippingFee(totals.shipping());
    cart.setTotalAmount(totals.total());
    cart.setOrderDate(LocalDateTime.now());

    if ("VNPAY".equalsIgnoreCase(req.paymentMethod())) {
        cart.setStatus("Chờ thanh toán");
    } else {
        cart.setStatus("Đang xử lý");
        for (OrderDetail d : lines) {
          Product p = d.getProduct();
          if (p != null) {
              p.setSold((p.getSold() == null ? 0 : p.getSold()) + d.getQuantity());
              // No need to save product here if OrderDetail cascades (check Order entity relationship)
              // productRepo.save(p);
          }
        }
    }

    return orderRepo.save(cart); // Return the saved/updated order
  }

  @Transactional
  public Order checkoutBuyNow(Integer accountId, BuyNowRequest req) {
    if (req.quantity() <= 0) {
        throw new IllegalArgumentException("Số lượng sản phẩm phải lớn hơn 0.");
    }

    Product p = productRepo.findById(req.productId())
        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + req.productId()));
    if (p.getStatus() == null || !p.getStatus()) {
        throw new IllegalStateException("Sản phẩm '" + p.getProductName() + "' hiện đã hết hàng.");
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

    BigDecimal subtotal = stampedPrice.multiply(BigDecimal.valueOf(req.quantity()));
    BigDecimal shippingFee = shippingFeeFor(checkoutInfo.shippingMethod(), subtotal);
    BigDecimal total = subtotal.add(shippingFee);

    order.setShippingFee(shippingFee);
    order.setTotalAmount(total);
    order.setDetails(List.of(line)); // Associate the detail with the order

    if ("VNPAY".equalsIgnoreCase(checkoutInfo.paymentMethod())) {
        order.setStatus("Chờ thanh toán");
    } else {
        order.setStatus("Đang xử lý");
        p.setSold((p.getSold() == null ? 0 : p.getSold()) + req.quantity());
        // productRepo.save(p); // Saving Order might cascade save Product if configured
    }

    return orderRepo.save(order); // Return the saved order
  }


  private record Totals(BigDecimal subtotal, BigDecimal shipping, BigDecimal total) {}

  // Convert OrderDetails to CartView
  private CartView toView(Integer orderId) {
      // Find order first to get shipping method
      Order order = orderRepo.findById(orderId).orElse(null);
      if (order == null) { // Handle case where order might not exist (e.g., after deletion)
          return new CartView(orderId, List.of(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
      }

	  List<OrderDetail> lines = odRepo.findById_OrderId(orderId);
	  BigDecimal subtotal = calcSubtotal(lines);
      // Use the actual shipping method from the order
      BigDecimal shipping = shippingFeeFor(order.getShippingMethod(), subtotal);
	  BigDecimal total = subtotal.add(shipping);

	  var viewLines = lines.stream()
          .filter(l -> l != null && l.getProduct() != null) // Filter out potential nulls
          .map(l ->
	      new CartLine(
	          l.getProduct().getId(),
	          l.getProduct().getProductName(),
	          l.getQuantity(),
	          l.getPrice(),
	          l.getPrice().multiply(BigDecimal.valueOf(l.getQuantity()))
	      )).collect(Collectors.toList());

	  return new CartView(orderId, viewLines, subtotal, shipping, total);
	}

  // Calculate totals based on lines and provided shipping fee
  private Totals calcTotals(List<OrderDetail> lines, BigDecimal calculatedShippingFee) {
    BigDecimal subtotal = calcSubtotal(lines);
    BigDecimal shipping = subtotal.compareTo(BigDecimal.ZERO) > 0 ? calculatedShippingFee : BigDecimal.ZERO;
    BigDecimal total = subtotal.add(shipping);
    return new Totals(subtotal, shipping, total);
  }
}