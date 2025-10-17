package vn.flower.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import vn.flower.entities.Product;

public interface ProductRepository
    extends JpaRepository<Product, Integer>, JpaSpecificationExecutor<Product> {

    List<Product> findByCategoryId(Long categoryId);
    List<Product> findByCategory_Id(Long categoryId);
    List<Product> findByProductNameContainingIgnoreCase(String keyword);
    List<Product> findByCategory_IdAndProductNameContainingIgnoreCase(Long categoryId, String keyword);
    List<Product> findTop10ByStatusTrueOrderBySoldDescIdDesc();
    List<Product> findTop5ByStatusTrueOrderByIdDesc();
    List<Product> findTop10ByStatusTrueAndSalePriceIsNotNullOrderByIdDesc();
}
