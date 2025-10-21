package vn.flower.controllers;

import org.springframework.beans.factory.annotation.Autowired; // THÊM
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.flower.entities.Review;
import vn.flower.services.ReviewService;
import vn.flower.repositories.ReviewRepository; // THÊM

import java.security.Principal;

@Controller
public class ReviewController {

    private final ReviewService reviewService;
    
    // (Bỏ qua @Autowired nếu dùng constructor)
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/reviews/add")
    public String addReview(@ModelAttribute("newReview") Review review,
                            @RequestParam("productId") Integer productId,
                            Principal principal,
                            RedirectAttributes ra) {
        // ... (Giữ nguyên logic addReview)
        if (principal == null) {
            return "redirect:/auth/login";
        }
        if (review.getComment() == null || review.getComment().trim().isEmpty() || review.getRating() == null) {
             ra.addFlashAttribute("reviewError", "Vui lòng chọn số sao và viết bình luận.");
             return "redirect:/products/" + productId;
        }
        try {
            String userEmail = principal.getName();
            reviewService.saveReview(review, userEmail, productId);
            ra.addFlashAttribute("reviewSuccess", "Cảm ơn bạn đã gửi đánh giá!");
        } catch (Exception e) {
            ra.addFlashAttribute("reviewError", "Gửi đánh giá thất bại: " + e.getMessage());
        }
        return "redirect:/products/" + productId;
    }

    // === THÊM ENDPOINT NÀY ===
    @PostMapping("/reviews/delete")
    public String deleteReview(@RequestParam("reviewId") Integer reviewId,
                               @RequestParam("productId") Integer productId,
                               Principal principal,
                               RedirectAttributes ra) {

        if (principal == null) {
            return "redirect:/auth/login";
        }

        try {
            String userEmail = principal.getName();
            // Gọi service để xóa (service sẽ kiểm tra quyền sở hữu)
            reviewService.deleteReview(reviewId, userEmail);
            ra.addFlashAttribute("reviewSuccess", "Đã xóa bình luận của bạn.");
            
        } catch (IllegalAccessException e) {
            // Bắt lỗi nếu xóa không đúng chủ
            ra.addFlashAttribute("reviewError", "Bạn không có quyền xóa bình luận này.");
        } catch (Exception e) {
            // Các lỗi khác (ví dụ: reviewId không tồn tại)
            ra.addFlashAttribute("reviewError", "Xóa thất bại: " + e.getMessage());
        }

        // Redirect lại đúng trang sản phẩm
        return "redirect:/products/" + productId;
    }
}