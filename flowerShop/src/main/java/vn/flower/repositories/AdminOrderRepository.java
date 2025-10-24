package vn.flower.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.flower.entities.Order;

public interface AdminOrderRepository extends JpaRepository<Order, Long> {
    // Giữ nguyên các phương thức cũ
    List<Order> findByStatus(String status);
    List<Order> findByAccountId(Integer accountId);
    @Query("SELECT o FROM Order o WHERE o.account.fullName LIKE %:name%") // Sửa lại query tìm theo tên KH
    List<Order> findByCustomerName(@Param("name") String name);
    List<Order> findTop5ByOrderByOrderDateDesc();
    long countByStatus(String status); // Đếm theo trạng thái

    // Bỏ các phương thức liên quan đến StatusNotIn
    // List<Order> findByPaymentMethodIgnoreCaseAndStatusNotIn(String paymentMethod, List<String> excludedStatuses);
    // long countByPaymentMethodIgnoreCaseAndStatusNotIn(String paymentMethod, List<String> excludedStatuses);

    // *** THÊM CÁC PHƯƠNG THỨC MỚI ĐỂ LỌC THEO PAYMENT METHOD ***
    List<Order> findByPaymentMethodIgnoreCase(String paymentMethod);
    long countByPaymentMethodIgnoreCase(String paymentMethod);
    // *** KẾT THÚC PHẦN THÊM MỚI ***
}