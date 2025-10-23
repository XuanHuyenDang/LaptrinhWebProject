package vn.flower.controllers.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import vn.flower.entities.Order;
import vn.flower.repositories.OrderRepository;
import vn.flower.services.OrderReturnService;
import vn.flower.util.AuthUtils;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/orders/return")
public class OrderReturnController {

    @Autowired
    private OrderReturnService orderReturnService;
    @Autowired
    private OrderRepository orderRepository; // Chỉ dùng để lấy thông tin hiển thị

    /**
     * Hiển thị form yêu cầu trả hàng
     */
    @GetMapping("/{orderId}")
    public String showReturnForm(@PathVariable Integer orderId, Model model, RedirectAttributes ra) {
        String email = AuthUtils.currentUsername();
        if (email == null) return "redirect:/auth/login";

        // Lấy thông tin order để hiển thị
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng"));
        
        // Kiểm tra (dù service cũng kiểm tra) để user không thấy form nếu sai
        if (order.getCompletedDate() == null || LocalDateTime.now().isAfter(order.getCompletedDate().plusDays(7))) {
             ra.addFlashAttribute("errorMessage", "Đơn hàng này đã quá hạn trả hàng.");
             return "redirect:/orders";
        }
        if (!"Hoàn tất".equals(order.getStatus())) {
             ra.addFlashAttribute("errorMessage", "Chỉ đơn hàng 'Hoàn tất' mới có thể trả.");
             return "redirect:/orders";
        }

        model.addAttribute("order", order);
        return "user/order-return-form";
    }

    /**
     * Xử lý POST yêu cầu trả hàng
     */
    @PostMapping
    public String processReturnRequest(
            @RequestParam("orderId") Integer orderId,
            @RequestParam("reason") String reason,
            @RequestParam("evidenceFile") MultipartFile evidenceFile,
            RedirectAttributes ra) {
        
        try {
            orderReturnService.createReturnRequest(orderId, reason, evidenceFile);
            ra.addFlashAttribute("successMessage", "Đã gửi yêu cầu trả hàng thành công. Vui lòng chờ admin xử lý.");
        } catch (Exception e) {
            // Chuyển hướng lại form nếu có lỗi
            ra.addFlashAttribute("errorMessage", "Gửi yêu cầu thất bại: " + e.getMessage());
            return "redirect:/orders/return/" + orderId;
        }

        return "redirect:/orders"; // Về trang lịch sử đơn hàng
    }
}