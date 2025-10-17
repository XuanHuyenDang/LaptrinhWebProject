package vn.flower.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import vn.flower.entities.Category;
import vn.flower.entities.Product;

public interface ProductRepository extends JpaRepository<Product, Integer> {
	List<Product> findByCategoryId(Long categoryId);
	List<Product> findByCategory_Id(Long categoryId);
	List<Product> findByProductNameContainingIgnoreCase(String keyword);
    List<Product> findByCategory_IdAndProductNameContainingIgnoreCase(Long categoryId, String keyword);
}
