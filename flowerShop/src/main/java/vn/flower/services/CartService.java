package vn.flower.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // ✅ dùng Spring Transactional

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

// === THÊM IMPORT NÀY ===
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
  
  

  private BigDecimal shippingFeeFor(ShippingMethod method, BigDecimal subtotal) {
	  if (subtotal == null || subtotal.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO; // Nếu không có tiền hàng thì 0đ ship
	  ShippingMethod m = (method != null) ? method : ShippingMethod.FAST;

	  return switch (m) {
	    case SAVING   -> new BigDecimal("20000");                           // tiết kiệm: 20k
	    case EXPRESS  -> new BigDecimal("60000");                           // hỏa tốc: 60k
	    case FAST     -> (subtotal.compareTo(new BigDecimal("500000"))>=0)  // nhanh: >= 500k miễn phí
	                    ? BigDecimal.ZERO : new BigDecimal("30000");
	  };
	}
  
  private BigDecimal calcSubtotal(List<OrderDetail> lines) {
	  return lines.stream()
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
          return orderRepo.save(o);
        });
  }

  @Transactional
  public CartView addItem(Integer accountId, Integer productId, Integer qty) {
    if (qty == null || qty <= 0) qty = 1;

    Order cart = getOrCreateCart(accountId);
    Product p = productRepo.findById(productId)
        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm"));

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
    odRepo.flush(); // ✅ chắc chắn ghi xuống DB trước khi đọc lại

    return toView(cart.getId());
  }

  @Transactional
  public CartView updateQty(Integer accountId, Integer productId, Integer qty) {
    Order cart = getOrCreateCart(accountId);
    OrderDetailId id = new OrderDetailId(cart.getId(), productId);
    OrderDetail line = odRepo.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Mục không tồn tại"));

    if (qty == null || qty <= 0) {
      odRepo.delete(line);   // remove entity
      odRepo.flush();        // ✅ ép DELETE ngay
    } else {
      line.setQuantity(qty);
      odRepo.save(line);
      odRepo.flush();        // ✅ ép UPDATE ngay
    }
    return toView(cart.getId());
  }

  @Transactional
  public CartView removeItem(Integer accountId, Integer productId) {
    Order cart = getOrCreateCart(accountId);
    odRepo.deleteByOrderIdAndProductId(cart.getId(), productId); // trả về số dòng bị xóa
    return toView(cart.getId());
  }

  @Transactional(readOnly = true)
  public CartView getCart(Integer accountId) {
    Order cart = getOrCreateCart(accountId);
    return toView(cart.getId());
  }

  /**
   * Thanh toán giỏ hàng (status='CART') hiện có.
   */
  @Transactional
  public Integer checkout(Integer accountId, CheckoutRequest req) {
    Order cart = orderRepo.findFirstByAccount_IdAndStatusOrderByIdDesc(accountId, STATUS_CART)
        .orElseThrow(() -> new IllegalStateException("Không có giỏ hàng"));

    List<OrderDetail> lines = odRepo.findById_OrderId(cart.getId());
    if (lines.isEmpty()) throw new IllegalStateException("Giỏ hàng trống");

    // === SỬA LỖI LOGIC: Tính phí ship dựa trên req, không dùng DEFAULT_SHIP ===
    BigDecimal subtotal = calcSubtotal(lines);
    BigDecimal shippingFee = shippingFeeFor(req.shippingMethod(), subtotal);
    var totals = calcTotals(lines, shippingFee);
    // ===================================================================

    cart.setRecipientName(req.recipientName());
    cart.setRecipientPhone(req.recipientPhone());
    cart.setShippingAddress(req.shippingAddress());
    cart.setNote(req.note());
    cart.setPaymentMethod(req.paymentMethod());
    cart.setShippingMethod(req.shippingMethod()); // <-- Lưu phương thức ship
    cart.setShippingFee(totals.shipping());
    cart.setTotalAmount(totals.total());
    cart.setOrderDate(LocalDateTime.now());
    cart.setStatus("Đang xử lý");

    // ➕ NEW: tăng sold cho sản phẩm
    for (OrderDetail d : lines) {
      Product p = d.getProduct();
      p.setSold((p.getSold() == null ? 0 : p.getSold()) + d.getQuantity());
      productRepo.save(p);
    }

    orderRepo.save(cart);
    return cart.getId();
  }

  /**
   * === PHƯƠNG THỨC MỚI CHO "MUA NGAY" ===
   * Tạo một đơn hàng hoàn toàn mới (không đụng đến giỏ status='CART')
   * chỉ với 1 sản phẩm duy nhất.
   */
  @Transactional
  public Integer checkoutBuyNow(Integer accountId, BuyNowRequest req) {
    if (req.quantity() <= 0) {
        throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
    }

    // 1. Lấy thông tin
    Product p = productRepo.findById(req.productId())
        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm"));
    Account acc = new Account();
    acc.setId(accountId);
    CheckoutRequest checkoutInfo = req.checkout(); // Thông tin người nhận

    // 2. Tạo Order MỚI
    Order order = new Order();
    order.setAccount(acc);
    order.setStatus("Đang xử lý"); // Đặt hàng thẳng, không qua 'CART'
    order.setOrderDate(LocalDateTime.now());

    // 3. Set thông tin người nhận
    order.setRecipientName(checkoutInfo.recipientName());
    order.setRecipientPhone(checkoutInfo.recipientPhone());
    order.setShippingAddress(checkoutInfo.shippingAddress());
    order.setNote(checkoutInfo.note());
    order.setPaymentMethod(checkoutInfo.paymentMethod());
    order.setShippingMethod(checkoutInfo.shippingMethod());

    // 4. Tạo OrderDetail MỚI
    OrderDetail line = new OrderDetail();
    BigDecimal stampedPrice = p.getSalePrice() != null ? p.getSalePrice() : p.getPrice();
    
    // OrderId sẽ được set tự động khi save
    line.setId(new OrderDetailId(null, p.getId())); 
    line.setOrder(order);
    line.setProduct(p);
    line.setQuantity(req.quantity());
    line.setPrice(stampedPrice);

    // 5. Tính toán tổng tiền
    BigDecimal subtotal = stampedPrice.multiply(BigDecimal.valueOf(req.quantity()));
    BigDecimal shippingFee = shippingFeeFor(checkoutInfo.shippingMethod(), subtotal);
    BigDecimal total = subtotal.add(shippingFee);

    order.setShippingFee(shippingFee);
    order.setTotalAmount(total);

    // 6. Tăng số lượng đã bán
    p.setSold((p.getSold() == null ? 0 : p.getSold()) + req.quantity());
    productRepo.save(p);

    // 7. Lưu Order (và OrderDetail qua cascade)
    // Phải thêm line vào list của order để cascade hoạt động
    order.setDetails(List.of(line));
    Order savedOrder = orderRepo.save(order);

    return savedOrder.getId();
  }


  private record Totals(BigDecimal subtotal, BigDecimal shipping, BigDecimal total) {}

  private CartView toView(Integer orderId) {
	  List<OrderDetail> lines = odRepo.findById_OrderId(orderId);
	  BigDecimal subtotal = calcSubtotal(lines);
	  // Mặc định là FAST, nhưng khi checkout thật sẽ tính lại
	  BigDecimal shipping = shippingFeeFor(ShippingMethod.FAST, subtotal); 
	  BigDecimal total = subtotal.add(shipping);

	  var viewLines = lines.stream().map(l ->
	      new CartLine(
	          l.getProduct().getId(),
	          l.getProduct().getProductName(),
	          l.getQuantity(),
	          l.getPrice(),
	          l.getPrice().multiply(BigDecimal.valueOf(l.getQuantity()))
	      )).collect(Collectors.toList());

	  return new CartView(orderId, viewLines, subtotal, shipping, total);
	}

  // Sửa lại hàm này để nhận phí ship từ bên ngoài
  private Totals calcTotals(List<OrderDetail> lines, BigDecimal ship) {
    BigDecimal subtotal = lines.stream()
        .map(l -> l.getPrice().multiply(BigDecimal.valueOf(l.getQuantity())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal shipping = subtotal.compareTo(BigDecimal.ZERO) > 0 ? ship : BigDecimal.ZERO;
    BigDecimal total = subtotal.add(shipping);
    return new Totals(subtotal, shipping, total);
  }
}