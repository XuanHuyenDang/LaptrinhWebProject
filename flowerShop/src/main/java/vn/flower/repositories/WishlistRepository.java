package vn.flower.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.flower.entities.Wishlist;
import vn.flower.entities.WishlistId;

import java.util.List;

public interface WishlistRepository extends JpaRepository<Wishlist, WishlistId> {
    
    // Lấy tất cả mục wishlist (bao gồm thông tin Product) cho một tài khoản
    // Dùng 'fetch' để tránh lỗi N+1 query khi truy cập product
    @Query("SELECT w FROM Wishlist w JOIN FETCH w.product WHERE w.account.id = :accountId ORDER BY w.createdAt DESC")
    List<Wishlist> findByAccountIdWithProduct(Integer accountId);

    // Chỉ lấy danh sách ID sản phẩm
    @Query("SELECT w.product.id FROM Wishlist w WHERE w.account.id = :accountId")
    List<Integer> findProductIdsByAccountId(Integer accountId);
}