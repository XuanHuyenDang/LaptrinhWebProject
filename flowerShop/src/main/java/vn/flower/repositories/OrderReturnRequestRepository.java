package vn.flower.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // Đảm bảo đã import
import vn.flower.entities.OrderReturnRequest;
import vn.flower.entities.OrderReturnRequest.ReturnStatus;

import java.util.List;
import java.util.Optional; // Import Optional nếu dùng phương thức findById...Eagerly

public interface OrderReturnRequestRepository extends JpaRepository<OrderReturnRequest, Long> {

    // Tìm các yêu cầu theo trạng thái (có thể giữ lại nếu cần)
    List<OrderReturnRequest> findByStatusOrderByRequestDateDesc(ReturnStatus status);

    // === PHƯƠNG THỨC QUAN TRỌNG CẦN CÓ ===
    /**
     * Tìm yêu cầu trả hàng theo trạng thái và tải luôn Order và Account liên kết.
     * Điều này tránh lỗi LazyInitializationException ở tầng view.
     */
    @Query("SELECT r FROM OrderReturnRequest r JOIN FETCH r.order o JOIN FETCH o.account WHERE r.status = :status ORDER BY r.requestDate DESC")
    List<OrderReturnRequest> findByStatusWithOrderAndAccountEagerly(ReturnStatus status);
    // =====================================

    // (Tùy chọn) Phương thức tương tự cho findById để dùng trong trang chi tiết
    @Query("SELECT r FROM OrderReturnRequest r JOIN FETCH r.order o JOIN FETCH o.account WHERE r.id = :id")
    Optional<OrderReturnRequest> findByIdWithOrderAndAccountEagerly(Long id);

    // Tìm theo Order ID
    boolean existsByOrderId(Integer orderId);
}