// vn/flower/repositories/OrderRepository.java
package vn.flower.repositories;

import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.flower.entities.Order;

public interface OrderRepository extends JpaRepository<Order, Integer> {

  Optional<Order> findFirstByAccount_IdAndStatusOrderByIdDesc(Integer accountId, String status);

  Optional<Order> findByIdAndAccount_Id(Integer id, Integer accountId);
  List<Order> findAllByAccount_IdOrderByOrderDateDesc(Integer accountId);
  List<Order> findAllByAccount_IdAndStatusNotOrderByOrderDateDesc(Integer accountId, String statusNot);
  // ✅ dùng cho trang lịch sử (nếu bạn muốn)
  List<Order> findByAccount_IdOrderByOrderDateDesc(Integer accountId);
  
  
}
