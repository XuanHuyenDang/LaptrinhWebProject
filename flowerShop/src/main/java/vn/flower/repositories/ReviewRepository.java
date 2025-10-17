package vn.flower.repositories;

import vn.flower.entities.Review;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {


    // Lọc theo tên sản phẩm A->Z
    List<Review> findAllByOrderByProductProductNameAsc();

    // Lọc theo tên khách hàng A->Z
    @Query(value = """
            SELECT r FROM Review r
            JOIN r.account a
            ORDER BY
              TRIM(
                SUBSTRING(
                  a.fullName,
                  LENGTH(a.fullName) - LOCATE(' ', REVERSE(a.fullName)) + 2
                )
              )
            ASC
            """)
    List<Review> findAllByOrderByAccountFullNameAsc();

    // Tìm theo cả tên sản phẩm hoặc tên khách hàng
    List<Review> findByProductProductNameContainingIgnoreCaseOrAccountFullNameContainingIgnoreCase(String productName, String fullName);
}

