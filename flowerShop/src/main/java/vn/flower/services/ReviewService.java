package vn.flower.services;

import vn.flower.entities.Review;
import java.util.List;

public interface ReviewService {
    
    /**
     * Lấy danh sách đánh giá cho một sản phẩm.
     */
    List<Review> getReviewsForProduct(Integer productId);

    /**
     * Lưu một đánh giá mới.
     * @param review Đối tượng Review từ form
     * @param userEmail Email của người dùng đang đăng nhập
     * @param productId ID của sản phẩm được đánh giá
     * @return Review đã được lưu
     */
    Review saveReview(Review review, String userEmail, Integer productId);

    // === THÊM PHƯƠNG THỨC NÀY ===
    /**
     * Xóa một đánh giá.
     * @param reviewId ID của đánh giá cần xóa
     * @param userEmail Email của người dùng yêu cầu xóa (để xác thực)
     * @throws IllegalAccessException Nếu người dùng không phải là chủ sở hữu
     */
    void deleteReview(Integer reviewId, String userEmail) throws IllegalAccessException;
}