package vn.flower.services;

import vn.flower.entities.Product;
import java.util.List;

/**
 * Interface cho logic nghiệp vụ liên quan đến Wishlist
 */
public interface WishlistService {

    /**
     * Lấy danh sách đầy đủ các sản phẩm mà người dùng đã yêu thích
     * @param accountId ID của tài khoản
     * @return Danh sách các đối tượng Product
     */
    List<Product> getWishlistProducts(Integer accountId);

    /**
     * Lấy danh sách chỉ gồm ID của các sản phẩm đã yêu thích
     * @param accountId ID của tài khoản
     * @return Danh sách các Product ID
     */
    List<Integer> getWishlistProductIds(Integer accountId);

    /**
     * Thêm hoặc xóa một sản phẩm khỏi danh sách yêu thích
     * @param accountId ID của tài khoản
     * @param productId ID của sản phẩm
     * @return "added" nếu thêm thành công, "removed" nếu xóa thành công
     */
    String toggleWishlist(Integer accountId, Integer productId);
}