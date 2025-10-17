package vn.flower.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.flower.entities.Order;

public interface AdminOrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByStatus(String status);
    List<Order> findByAccountId(Integer accountId);
    @Query("SELECT o FROM Order o WHERE o.account.fullName = :name")
    List<Order> findByCustomerName(@Param("name") String name);
    List<Order> findTop5ByOrderByOrderDateDesc();
}
