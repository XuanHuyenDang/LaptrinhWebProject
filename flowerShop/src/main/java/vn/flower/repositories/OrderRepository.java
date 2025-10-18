package vn.flower.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.flower.entities.Order;

public interface OrderRepository extends JpaRepository<Order, Integer> {
  Optional<Order> findFirstByAccount_IdAndStatusOrderByIdDesc(Integer accountId, String status);
}
