package vn.flower.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
// Removed unused imports like ResponseStatusException, HttpStatus, Account, ChatMessage, LocalDateTime

import vn.flower.api.dto.ChatMessageDTO;
// Removed AccountRepository and ChatMessageRepository imports
import vn.flower.services.ChatService; // <-- ADD ChatService import

import java.security.Principal;

@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    // === USE ChatService INSTEAD OF REPOSITORIES ===
    @Autowired
    private ChatService chatService; 
    // Removed ChatMessageRepository and AccountRepository

    private static final String ADMIN_USERNAME = "admin@flowershop.com"; 

    @MessageMapping("/chat.private")
    public void sendPrivateMessage(@Payload ChatMessageDTO chatMessage, Principal principal) {
        
        String senderEmail = principal.getName();
        String recipientEmail;

        // 1. Determine recipient
        if (senderEmail.equals(ADMIN_USERNAME)) {
            recipientEmail = chatMessage.recipient();
            if (recipientEmail == null || recipientEmail.isBlank()) {
                System.err.println("Admin sent message without recipient.");
                // Optional: Send error back to admin
                return;
            }
        } else {
            recipientEmail = ADMIN_USERNAME;
        }
        
        // 2. === SAVE MESSAGE USING ChatService ===
        try {
            chatService.saveChatMessage(senderEmail, recipientEmail, chatMessage.content());
        } catch (Exception e) {
            System.err.println("Error saving chat message: " + e.getMessage());
            // Optional: Send error message back to sender via WebSocket
            messagingTemplate.convertAndSendToUser(senderEmail, "/queue/private", 
                new ChatMessageDTO("SYSTEM", null, "Error sending: " + e.getMessage(), ChatMessageDTO.MessageType.CHAT));
            return; 
        }

        // 3. Create DTO to send via WebSocket
        ChatMessageDTO messageToSend = new ChatMessageDTO(
            senderEmail,
            recipientEmail,
            chatMessage.content(),
            ChatMessageDTO.MessageType.CHAT
        );

        // 4. Send message via WebSocket (unchanged)
        messagingTemplate.convertAndSendToUser(recipientEmail, "/queue/private", messageToSend);
        messagingTemplate.convertAndSendToUser(senderEmail, "/queue/private", messageToSend);
    }
}