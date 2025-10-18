package vn.flower.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // ✅ dùng Spring Transactional

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import vn.flower.api.dto.CartLine;
import vn.flower.api.dto.CartView;
import vn.flower.api.dto.CheckoutRequest;

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

  private static final String STATUS_CART = "CART";
  private static final BigDecimal DEFAULT_SHIP = new BigDecimal("30000");

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

  // ✅ Hàm xóa riêng, dùng JPQL DELETE (nhanh, dứt khoát, có flush auto)
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

  @Transactional
  public Integer checkout(Integer accountId, CheckoutRequest req) {
    Order cart = orderRepo.findFirstByAccount_IdAndStatusOrderByIdDesc(accountId, STATUS_CART)
        .orElseThrow(() -> new IllegalStateException("Không có giỏ hàng"));

    List<OrderDetail> lines = odRepo.findById_OrderId(cart.getId());
    if (lines.isEmpty()) throw new IllegalStateException("Giỏ hàng trống");

    var totals = calcTotals(lines, DEFAULT_SHIP);

    cart.setRecipientName(req.recipientName());
    cart.setRecipientPhone(req.recipientPhone());
    cart.setShippingAddress(req.shippingAddress());
    cart.setNote(req.note());
    cart.setPaymentMethod(req.paymentMethod());
    cart.setShippingFee(totals.shipping());
    cart.setTotalAmount(totals.total());
    cart.setOrderDate(LocalDateTime.now());
    cart.setStatus("PENDING");

    // ➕ NEW: tăng sold cho sản phẩm
    for (OrderDetail d : lines) {
      Product p = d.getProduct();
      p.setSold((p.getSold() == null ? 0 : p.getSold()) + d.getQuantity());
      productRepo.save(p);
    }

    orderRepo.save(cart);
    return cart.getId();
  }

  private record Totals(BigDecimal subtotal, BigDecimal shipping, BigDecimal total) {}

  private CartView toView(Integer orderId) {
    List<OrderDetail> lines = odRepo.findById_OrderId(orderId);
    var totals = calcTotals(lines, DEFAULT_SHIP);

    var viewLines = lines.stream().map(l ->
        new CartLine(
            l.getProduct().getId(),
            l.getProduct().getProductName(),
            l.getQuantity(),
            l.getPrice(),
            l.getPrice().multiply(BigDecimal.valueOf(l.getQuantity()))
        )).collect(Collectors.toList());

    return new CartView(orderId, viewLines, totals.subtotal(), totals.shipping(), totals.total());
  }

  private Totals calcTotals(List<OrderDetail> lines, BigDecimal ship) {
    BigDecimal subtotal = lines.stream()
        .map(l -> l.getPrice().multiply(BigDecimal.valueOf(l.getQuantity())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal shipping = subtotal.compareTo(BigDecimal.ZERO) > 0 ? ship : BigDecimal.ZERO;
    BigDecimal total = subtotal.add(shipping);
    return new Totals(subtotal, shipping, total);
  }
}
