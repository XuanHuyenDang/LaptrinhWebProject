package vn.flower.api.dto;

// Thêm trường 'recipient' (người nhận)
public record ChatMessageDTO(
    String sender,
    String recipient, // <-- THÊM TRƯỜNG NÀY (email của người nhận)
    String content,
    MessageType type
) {
    public enum MessageType {
        CHAT,
        JOIN,
        LEAVE
    }
}