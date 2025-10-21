package vn.flower.controllers.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import vn.flower.api.dto.ChatMessageDTO;
import vn.flower.entities.Account;
// Removed ChatMessage import
import vn.flower.repositories.AccountRepository; 
// Removed ChatMessageRepository import
import vn.flower.services.ChatService; // <-- ADD ChatService import

import java.util.List;
// Removed Collectors import

@RestController
@RequestMapping("/api/chat")
public class ChatApiController {

    // === USE ChatService INSTEAD OF REPOSITORIES ===
    @Autowired
    private ChatService chatService; 
    @Autowired
    private AccountRepository accountRepo; // Keep AccountRepo for finding users

    private static final String ADMIN_USERNAME = "admin@flowershop.com";

    private Account getCurrentAccount() {
        // ... (Keep this method as is)
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(String.valueOf(auth.getPrincipal()))) {
          throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Chưa đăng nhập");
        }
        String email = (auth.getPrincipal() instanceof UserDetails u) ? u.getUsername() : auth.getName();
        return accountRepo.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Không tìm thấy tài khoản: " + email));
    }
    
    // Removed convertToDTO method, it's now in ChatService

    /**
     * API for Customer: Get their chat history with Admin
     */
    @GetMapping("/history")
    public ResponseEntity<List<ChatMessageDTO>> getCustomerHistory() {
        Account customer = getCurrentAccount();
        Account admin = chatService.findAdminAccount(); // Use service helper

        // === CALL ChatService TO GET HISTORY ===
        List<ChatMessageDTO> dtos = chatService.getConversation(customer.getId(), admin.getId());
            
        return ResponseEntity.ok(dtos);
    }

    /**
     * API for Admin: Get chat history with a specific Customer
     */
    @GetMapping("/history/{partnerEmail}")
    public ResponseEntity<List<ChatMessageDTO>> getAdminHistory(@PathVariable String partnerEmail) {
        Account admin = getCurrentAccount();
        Account partner = accountRepo.findByEmail(partnerEmail)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found: " + partnerEmail));

        // === CALL ChatService TO GET HISTORY ===
        List<ChatMessageDTO> dtos = chatService.getConversation(admin.getId(), partner.getId());
            
        return ResponseEntity.ok(dtos);
    }

    /**
     * API for Admin: Get list of all users who have chatted with Admin
     */
    @GetMapping("/users")
    public ResponseEntity<List<String>> getChattedUsers() {
        Account admin = getCurrentAccount();
        if (!admin.getEmail().equals(ADMIN_USERNAME)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only Admin access allowed");
        }
        
        // === CALL ChatService TO GET USER LIST ===
        List<String> userEmails = chatService.getChattedUsers(admin.getId());
        return ResponseEntity.ok(userEmails);
    }
}