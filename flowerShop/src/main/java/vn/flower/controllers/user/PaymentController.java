package vn.flower.controllers.user;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import vn.flower.entities.Order;
import vn.flower.entities.OrderDetail;
import vn.flower.entities.Product;
import vn.flower.repositories.OrderRepository;
import vn.flower.repositories.ProductRepository;
import vn.flower.services.VnpayService;

import java.util.Optional;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private VnpayService vnpayService;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private ProductRepository productRepository;

    // Đây là Return URL
    @GetMapping("/vnpay-return")
    @Transactional
    public String vnpayReturn(HttpServletRequest request, Model model) {
        boolean isValidSignature = vnpayService.validateSignature(request);
        
        String vnp_TxnRef = request.getParameter("vnp_TxnRef"); // Mã đơn hàng
        String vnp_ResponseCode = request.getParameter("vnp_ResponseCode"); // Mã kết quả
        String vnp_TransactionStatus = request.getParameter("vnp_TransactionStatus"); // Trạng thái giao dịch

        if (isValidSignature) {
            if ("00".equals(vnp_ResponseCode) && "00".equals(vnp_TransactionStatus)) {
                // Thanh toán thành công
                try {
                    Integer orderId = Integer.parseInt(vnp_TxnRef);
                    Optional<Order> orderOpt = orderRepository.findById(orderId);
                    
                    if (orderOpt.isPresent()) {
                        Order order = orderOpt.get();
                        
                        // Chỉ cập nhật nếu đơn hàng đang "Chờ thanh toán"
                        if ("Chờ thanh toán".equals(order.getStatus())) {
                            order.setStatus("Đang xử lý");
                            orderRepository.save(order);

                            // Cập nhật số lượng 'sold'
                            for (OrderDetail detail : order.getDetails()) {
                                Product p = detail.getProduct();
                                if (p != null) {
                                    int newSold = (p.getSold() == null ? 0 : p.getSold()) + detail.getQuantity();
                                    p.setSold(Math.max(0, newSold));
                                    productRepository.save(p);
                                }
                            }
                        }
                        
                        // Chuyển hướng đến trang thành công
                        return "redirect:/orders/" + order.getId();
                        
                    } else {
                         model.addAttribute("errorMessage", "Không tìm thấy đơn hàng.");
                    }
                } catch (Exception e) {
                     model.addAttribute("errorMessage", "Lỗi xử lý đơn hàng: " + e.getMessage());
                }
            } else {
                // Thanh toán thất bại
                model.addAttribute("errorMessage", "Thanh toán VNPAY thất bại. Mã lỗi: " + vnp_ResponseCode);
            }
        } else {
            model.addAttribute("errorMessage", "Chữ ký không hợp lệ.");
        }

        // Trả về một trang thông báo lỗi chung
        // Bạn nên tạo một trang Thymeleaf mới `user/payment-error.html`
        return "user/order-success"; // Tạm thời redirect về đây, bạn nên tạo trang lỗi riêng
    }
    
    // IPN URL (VNPAY gọi ngầm)
    // Thực tế, logic xác nhận đơn hàng NÊN nằm ở IPN vì nó ổn định hơn Return URL.
    // Tuy nhiên, để đơn giản, ví dụ này xử lý ở Return URL.
    // Trong dự án thực tế, bạn PHẢI xử lý logic cập nhật đơn hàng ở IPN.
    @GetMapping("/vnpay-ipn")
    public String vnpayIpn(HttpServletRequest request) {
        // Logic xử lý IPN tương tự như vnpayReturn
        // ...
        // Sau khi xử lý xong, trả về mã theo yêu cầu của VNPAY
        // return "RspCode=00|Message=Confirm Success";
        return "redirect:/"; // Tạm thời
    }
}