package vn.flower.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Import Transactional
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import vn.flower.api.dto.ChatMessageDTO;
import vn.flower.entities.Account;
import vn.flower.entities.ChatMessage;
import vn.flower.repositories.AccountRepository;
import vn.flower.repositories.ChatMessageRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {

    @Autowired
    private ChatMessageRepository chatMessageRepo;

    @Autowired
    private AccountRepository accountRepo;
    
    // Email admin (must match controller and frontend)
    private static final String ADMIN_USERNAME = "admin@flowershop.com";

    // Helper to convert Entity to DTO
    private ChatMessageDTO convertToDTO(ChatMessage entity) {
        // Accessing sender/receiver happens here, INSIDE the transaction
        return new ChatMessageDTO(
            entity.getSender().getEmail(),    
            entity.getReceiver().getEmail(),  
            entity.getMessageContent(),
            ChatMessageDTO.MessageType.CHAT
        );
    }
    
    /**
     * Saves a chat message sent via WebSocket.
     * This method ensures sender/receiver exist before saving.
     */
    @Transactional // Ensures atomicity for finding accounts and saving message
    public void saveChatMessage(String senderEmail, String recipientEmail, String content) throws Exception {
         Account senderAcc = accountRepo.findByEmail(senderEmail)
            .orElseThrow(() -> new Exception("Sender not found: " + senderEmail));
        Account receiverAcc = accountRepo.findByEmail(recipientEmail)
            .orElseThrow(() -> new Exception("Recipient not found: " + recipientEmail));

        ChatMessage messageEntity = new ChatMessage();
        messageEntity.setSender(senderAcc);
        messageEntity.setReceiver(receiverAcc);
        messageEntity.setMessageContent(content);
        messageEntity.setCreatedAt(LocalDateTime.now());
        messageEntity.setIsRead(false); 

        chatMessageRepo.save(messageEntity);
    }


    /**
     * Gets chat history between two users.
     * Transactional keeps the session open for lazy loading.
     */
    @Transactional(readOnly = true) // readOnly = true for query performance
    public List<ChatMessageDTO> getConversation(Integer userId1, Integer userId2) {
        List<ChatMessage> messages = chatMessageRepo.findConversationByIds(userId1, userId2);
        
        // Conversion happens within the transaction
        return messages.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Gets the list of users who have chatted with the admin.
     */
    @Transactional(readOnly = true)
    public List<String> getChattedUsers(Integer adminId) {
         return accountRepo.findDistinctChatPartners(adminId);
    }
    
    /**
     * Helper to find Admin Account
     */
    public Account findAdminAccount() {
         return accountRepo.findByEmail(ADMIN_USERNAME)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Admin account not found"));
    }
}