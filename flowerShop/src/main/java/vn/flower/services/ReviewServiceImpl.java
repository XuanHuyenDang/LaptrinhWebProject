package vn.flower.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.flower.entities.Account;
import vn.flower.entities.Product;
import vn.flower.entities.Review;
import vn.flower.repositories.AccountRepository;
import vn.flower.repositories.ProductRepository;
import vn.flower.repositories.ReviewRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final AccountRepository accountRepository;
    private final ProductRepository productRepository;

    public ReviewServiceImpl(ReviewRepository reviewRepository,
                             AccountRepository accountRepository,
                             ProductRepository productRepository) {
        this.reviewRepository = reviewRepository;
        this.accountRepository = accountRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Review> getReviewsForProduct(Integer productId) {
        return reviewRepository.findByProductIdOrderByIdDesc(productId);
    }

    @Override
    @Transactional
    public Review saveReview(Review review, String userEmail, Integer productId) {
        // ... (Giữ nguyên logic saveReview)
        Account account = accountRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản với email: " + userEmail));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + productId));
        review.setAccount(account);
        review.setProduct(product);
        review.setCreatedAt(LocalDateTime.now());
        if (review.getRating() < 1) review.setRating(1);
        if (review.getRating() > 5) review.setRating(5);
        return reviewRepository.save(review);
    }

    // === THÊM PHƯƠNG THỨC NÀY ===
    @Override
    @Transactional
    public void deleteReview(Integer reviewId, String userEmail) throws IllegalAccessException {
        // 1. Lấy review
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đánh giá với ID: " + reviewId));

        // 2. Lấy email của chủ sở hữu
        String ownerEmail = review.getAccount().getEmail();

        // 3. So sánh với người dùng hiện tại
        if (!ownerEmail.equals(userEmail)) {
            // Nếu không khớp, ném lỗi
            throw new IllegalAccessException("Bạn không có quyền xóa đánh giá này.");
        }

        // 4. Nếu khớp, tiến hành xóa
        reviewRepository.delete(review);
    }
}