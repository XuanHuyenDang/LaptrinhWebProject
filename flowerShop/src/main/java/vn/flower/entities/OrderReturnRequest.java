package vn.flower.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "OrderReturnRequests")
public class OrderReturnRequest {

    public enum ReturnStatus {
        PENDING,  // Đang chờ xử lý
        APPROVED, // Đã chấp nhận
        REJECTED  // Đã từ chối
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Liên kết OneToOne với Order
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OrderId", nullable = false, unique = true)
    private Order order;

    @Column(name = "RequestDate", nullable = false)
    private LocalDateTime requestDate;

    @Column(name = "Reason", columnDefinition = "NVARCHAR(MAX)", nullable = false)
    private String reason;

    @Column(name = "EvidenceUrl", length = 500)
    private String evidenceUrl; // Đường dẫn tới file ảnh/video

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", length = 20, nullable = false)
    private ReturnStatus status = ReturnStatus.PENDING;

    @Column(name = "AdminNotes", columnDefinition = "NVARCHAR(MAX)")
    private String adminNotes; // Ghi chú của admin khi duyệt

    @Column(name = "ProcessedDate")
    private LocalDateTime processedDate; // Ngày admin duyệt

    // Getters and Setters
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    public LocalDateTime getRequestDate() { return requestDate; }
    public void setRequestDate(LocalDateTime requestDate) { this.requestDate = requestDate; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getEvidenceUrl() { return evidenceUrl; }
    public void setEvidenceUrl(String evidenceUrl) { this.evidenceUrl = evidenceUrl; }
    public ReturnStatus getStatus() { return status; }
    public void setStatus(ReturnStatus status) { this.status = status; }
    public String getAdminNotes() { return adminNotes; }
    public void setAdminNotes(String adminNotes) { this.adminNotes = adminNotes; }
    public LocalDateTime getProcessedDate() { return processedDate; }
    public void setProcessedDate(LocalDateTime processedDate) { this.processedDate = processedDate; }
}