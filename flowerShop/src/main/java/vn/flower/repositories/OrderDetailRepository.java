package vn.flower.repositories;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import vn.flower.entities.OrderDetail;
import vn.flower.entities.OrderDetailId;

import java.util.List;
import java.util.Optional;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, OrderDetailId> {

  List<OrderDetail> findById_OrderId(Integer orderId);

  Optional<OrderDetail> findById_OrderIdAndId_ProductId(Integer orderId, Integer productId);

  // ✅ Xóa trực tiếp bằng JPQL, flush/clear tự động để chắc chắn phát lệnh DELETE
  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("delete from OrderDetail d where d.id.orderId = :orderId and d.id.productId = :productId")
  int deleteByOrderIdAndProductId(@Param("orderId") Integer orderId, @Param("productId") Integer productId);
}
