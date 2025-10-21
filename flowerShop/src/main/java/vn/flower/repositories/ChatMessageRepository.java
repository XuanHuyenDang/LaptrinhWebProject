package vn.flower.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.flower.entities.ChatMessage;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * Tìm toàn bộ lịch sử chat giữa hai ID người dùng
     * (userId1 gửi cho userId2) HOẶC (userId2 gửi cho userId1)
     * Sắp xếp theo thời gian tạo.
     */
    @Query("SELECT m FROM ChatMessage m WHERE " +
           "(m.sender.id = :userId1 AND m.receiver.id = :userId2) OR " +
           "(m.sender.id = :userId2 AND m.receiver.id = :userId1) " +
           "ORDER BY m.createdAt ASC")
    List<ChatMessage> findConversationByIds(
        @Param("userId1") Integer userId1, 
        @Param("userId2") Integer userId2
    );
}