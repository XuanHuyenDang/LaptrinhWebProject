package vn.flower.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import vn.flower.entities.Account;
import vn.flower.entities.Product;
import vn.flower.entities.Wishlist;
import vn.flower.entities.WishlistId;
import vn.flower.repositories.ProductRepository;
import vn.flower.repositories.WishlistRepository;
import vn.flower.repositories.AccountRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WishlistServiceImpl implements WishlistService {

    @Autowired
    private WishlistRepository wishlistRepository;
    
    @Autowired
    private AccountRepository accountRepository; // Dùng để lấy proxy
    
    @Autowired
    private ProductRepository productRepository; // Dùng để lấy proxy

    @Override
    @Transactional(readOnly = true)
    public List<Product> getWishlistProducts(Integer accountId) {
        List<Wishlist> wishlistItems = wishlistRepository.findByAccountIdWithProduct(accountId);
        return wishlistItems.stream()
            .map(Wishlist::getProduct)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Integer> getWishlistProductIds(Integer accountId) {
        return wishlistRepository.findProductIdsByAccountId(accountId);
    }

    @Override
    @Transactional
    public String toggleWishlist(Integer accountId, Integer productId) {
        if (accountId == null || productId == null) {
            throw new IllegalArgumentException("Account ID và Product ID không được rỗng");
        }
        
        WishlistId id = new WishlistId(accountId, productId);
        
        if (wishlistRepository.existsById(id)) {
            // Đã tồn tại -> Xóa
            wishlistRepository.deleteById(id);
            return "removed";
        } else {
            // Chưa tồn tại -> Thêm mới
            Wishlist newItem = new Wishlist();
            newItem.setId(id);
            newItem.setCreatedAt(LocalDateTime.now());

            // Tạo proxy cho Account và Product để tránh truy vấn DB
            Account accRef = accountRepository.getReferenceById(Long.valueOf(accountId));
            Product prodRef = productRepository.getReferenceById(productId);
            
            newItem.setAccount(accRef);
            newItem.setProduct(prodRef);

            wishlistRepository.save(newItem);
            return "added";
        }
    }
}