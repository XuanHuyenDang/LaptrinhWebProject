package vn.flower.controllers.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import vn.flower.entities.Account;
import vn.flower.repositories.AccountRepository;
import vn.flower.services.WishlistService; // <-- SỬ DỤNG SERVICE
import vn.flower.util.AuthUtils;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistApiController {

    @Autowired 
    private WishlistService wishlistService; // <-- SỬ DỤNG SERVICE
    
    @Autowired 
    private AccountRepository accountRepository;

    // Hàm helper lấy AccountId
    private Integer currentAccountId() {
        String email = AuthUtils.currentUsername();
        if (email == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Chưa đăng nhập");
        }
        return accountRepository.findByEmail(email)
            .map(Account::getId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Không tìm thấy tài khoản"));
    }

    /**
     * Lấy danh sách ID sản phẩm đã yêu thích
     */
    @GetMapping("/ids")
    public ResponseEntity<List<Integer>> getWishlistIds() {
        try {
            List<Integer> ids = wishlistService.getWishlistProductIds(currentAccountId());
            return ResponseEntity.ok(ids);
        } catch (ResponseStatusException e) {
            // Nếu chưa đăng nhập, trả về mảng rỗng thay vì lỗi 401
            return ResponseEntity.ok(List.of()); 
        }
    }

    /**
     * Thêm/Xóa một sản phẩm khỏi wishlist
     * Trả về "added" hoặc "removed"
     */
    @PostMapping("/toggle/{productId}")
    public ResponseEntity<Map<String, String>> toggleWishlist(@PathVariable Integer productId) {
        Integer accountId = currentAccountId(); // Sẽ ném 401 nếu chưa đăng nhập
        
        try {
            String status = wishlistService.toggleWishlist(accountId, productId);
            return ResponseEntity.ok(Map.of("status", status));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", e.getMessage()));
        }
    }
    
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> handleAuthException(ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
    }
}