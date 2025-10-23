package vn.flower.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import vn.flower.entities.Account;
import vn.flower.entities.Order;
import vn.flower.entities.OrderReturnRequest;
import vn.flower.entities.OrderReturnRequest.ReturnStatus;
import vn.flower.repositories.AccountRepository;
import vn.flower.repositories.OrderRepository;
import vn.flower.repositories.OrderReturnRequestRepository;
import vn.flower.util.AuthUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class OrderReturnService {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderReturnRequestRepository returnRequestRepository;
    @Autowired
    private AccountRepository accountRepository;

    // Định nghĩa thư mục lưu file bằng chứng
    // Đảm bảo đường dẫn này tồn tại và ứng dụng có quyền ghi
    private final String UPLOAD_DIR = "src/main/resources/static/uploads/returns";

    private Account getCurrentAccount() {
        String email = AuthUtils.currentUsername();
        if (email == null) throw new IllegalStateException("Chưa đăng nhập");
        return accountRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalStateException("Không tìm thấy tài khoản"));
    }

    /**
     * Logic nghiệp vụ cho User tạo yêu cầu trả hàng
     */
    @Transactional
    public void createReturnRequest(Integer orderId, String reason, MultipartFile evidenceFile) throws Exception {
        Account currentUser = getCurrentAccount();

        Order order = orderRepository.findByIdAndAccount_Id(orderId, currentUser.getId())
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng hoặc đơn hàng không thuộc về bạn."));

        // 1. Kiểm tra trạng thái đơn hàng
        if (!"Hoàn tất".equals(order.getStatus())) {
            throw new IllegalStateException("Chỉ có thể yêu cầu trả hàng cho đơn đã 'Hoàn tất'.");
        }

        // 2. Kiểm tra ngày hoàn tất
        if (order.getCompletedDate() == null) {
            // Nếu CompletedDate là null nhưng trạng thái là "Hoàn tất", có thể cập nhật ngay bây giờ? Hoặc báo lỗi?
            // Hiện tại, báo lỗi, giả định rằng nó lẽ ra phải được đặt trước đó.
            throw new IllegalStateException("Đơn hàng 'Hoàn tất' nhưng chưa có ngày hoàn tất. Vui lòng liên hệ Admin.");
        }

        // 3. Kiểm tra thời hạn 7 ngày
        LocalDateTime deadline = order.getCompletedDate().plusDays(7);
        if (LocalDateTime.now().isAfter(deadline)) {
            throw new IllegalStateException("Đã quá 7 ngày kể từ khi đơn hàng hoàn tất ("
                + order.getCompletedDate().toLocalDate() + "), không thể trả hàng.");
        }

        // 4. Kiểm tra xem đã yêu cầu trả hàng chưa
        if (returnRequestRepository.existsByOrderId(orderId)) {
            throw new IllegalStateException("Bạn đã gửi yêu cầu trả hàng cho đơn này rồi.");
        }
         // Kiểm tra cơ bản cho lý do
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập lý do trả hàng.");
        }

        // 5. Xử lý upload file
        String savedFileName = null;
        if (evidenceFile != null && !evidenceFile.isEmpty()) {
             // Thêm kiểm tra kích thước, loại file nếu cần
            long maxSize = 10 * 1024 * 1024; // Ví dụ: giới hạn 10MB
            if (evidenceFile.getSize() > maxSize) {
                throw new IllegalArgumentException("Kích thước file bằng chứng không được vượt quá 10MB.");
            }
            // Thêm kiểm tra mime type nếu cần
            savedFileName = saveEvidenceFile(evidenceFile);
        } else {
             // Yêu cầu bằng chứng là bắt buộc hay tùy chọn tùy thuộc vào logic nghiệp vụ
             // Nếu bắt buộc:
             throw new IllegalArgumentException("Vui lòng cung cấp bằng chứng (hình ảnh/video).");
             // Nếu tùy chọn, chỉ cần tiếp tục mà không lưu file.
        }

        // 6. Tạo yêu cầu mới
        OrderReturnRequest returnRequest = new OrderReturnRequest();
        returnRequest.setOrder(order);
        returnRequest.setReason(reason.trim()); // Trim lý do
        returnRequest.setEvidenceUrl(savedFileName); // Lưu đường dẫn tương đối (có thể là null nếu tùy chọn)
        returnRequest.setRequestDate(LocalDateTime.now());
        returnRequest.setStatus(ReturnStatus.PENDING);

        // Liên kết yêu cầu với đơn hàng TRƯỚC KHI lưu yêu cầu (nếu dùng mappedBy)
        order.setReturnRequest(returnRequest);

        returnRequestRepository.save(returnRequest);

        // 7. Cập nhật trạng thái Order
        order.setStatus("Yêu cầu trả hàng");
        orderRepository.save(order); // Lưu lại đơn hàng để cập nhật trạng thái và có thể là liên kết returnRequest
    }

    /**
     * Helper lưu file và trả về đường dẫn web tương đối
     */
    private String saveEvidenceFile(MultipartFile file) throws Exception {
        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) dir.mkdirs();

        String originalFileName = file.getOriginalFilename();
        // Xử lý tên file cơ bản (cân nhắc các phương pháp mạnh mẽ hơn)
        originalFileName = originalFileName.replaceAll("[^a-zA-Z0-9.\\-]", "_");
        String extension = "";
        int dotIndex = originalFileName.lastIndexOf(".");
        if (dotIndex > 0) {
            extension = originalFileName.substring(dotIndex);
        }
        // Sử dụng UUID + phần mở rộng đã xử lý cho tên file
        String uniqueFileName = UUID.randomUUID().toString() + extension;

        Path filePath = Paths.get(UPLOAD_DIR, uniqueFileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Trả về đường dẫn web tương đối
        return "uploads/returns/" + uniqueFileName;
    }

    // --- Các hàm cho Admin ---

    /**
     * Lấy danh sách yêu cầu trả hàng đang chờ xử lý (tải kèm Order và Account).
     */
    @Transactional(readOnly = true)
    public List<OrderReturnRequest> getPendingReturnRequests() {
        // Sử dụng phương thức mới để fetch eagerly
        return returnRequestRepository.findByStatusWithOrderAndAccountEagerly(ReturnStatus.PENDING);
    }

    /**
     * Lấy chi tiết một yêu cầu trả hàng theo ID (tải kèm Order và Account).
     */
    @Transactional(readOnly = true)
    public OrderReturnRequest getReturnRequestById(Long requestId) {
        // Sử dụng phương thức eager loading findById... nếu đã tạo trong repository
        return returnRequestRepository.findByIdWithOrderAndAccountEagerly(requestId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy yêu cầu trả hàng ID: " + requestId));
    }

    /**
     * Xử lý (duyệt/từ chối) một yêu cầu trả hàng.
     */
    @Transactional
    public void processReturnRequest(Long requestId, boolean approve, String adminNotes) {
        OrderReturnRequest returnRequest = getReturnRequestById(requestId); // Đảm bảo eager loading hoạt động ở đây
        Order order = returnRequest.getOrder();

        if (returnRequest.getStatus() != ReturnStatus.PENDING) {
            throw new IllegalStateException("Yêu cầu này đã được xử lý hoặc không ở trạng thái chờ.");
        }
         if (adminNotes == null || adminNotes.trim().isEmpty()) {
             throw new IllegalArgumentException("Vui lòng nhập ghi chú của Admin.");
         }

        returnRequest.setAdminNotes(adminNotes.trim());
        returnRequest.setProcessedDate(LocalDateTime.now());

        if (approve) {
            returnRequest.setStatus(ReturnStatus.APPROVED);
            order.setStatus("Đã trả hàng"); // Hoặc "Đã hoàn tiền" - điều chỉnh nếu cần
            // Thêm logic xử lý hoàn tiền nếu có
        } else {
            returnRequest.setStatus(ReturnStatus.REJECTED);
            order.setStatus("Đã từ chối trả hàng"); // Trạng thái cho biết bị từ chối
            // Tùy chọn: chuyển trạng thái về 'Hoàn tất'?
            // order.setStatus("Hoàn tất");
        }

        // Không cần lưu returnRequest một cách tường minh nếu cascade được cấu hình trên Order
        // Nhưng lưu order là cần thiết để cập nhật trạng thái của nó
        orderRepository.save(order);
         // Lưu returnRequest một cách tường minh sẽ an toàn hơn nếu cascade không phải là ALL hoặc PERSIST/MERGE từ Order
         // returnRequestRepository.save(returnRequest);
    }
}