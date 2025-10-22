package vn.flower.controllers.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.flower.entities.Account;
import vn.flower.repositories.AccountRepository;
import vn.flower.services.AccountService; // Đảm bảo AccountService có phương thức update

import java.security.Principal;

@Controller
@RequestMapping("/account") // Base path for account related pages
public class AccountController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountService accountService; // Inject AccountService

    // Lấy thông tin tài khoản đang đăng nhập
    private Account getCurrentAccount(Principal principal) {
        if (principal == null) {
            return null; // Should not happen if security is configured correctly
        }
        String email = principal.getName();
        return accountRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Cannot find account for email: " + email));
    }

    // Hiển thị trang tài khoản
    @GetMapping
    public String showAccountPage(Model model, Principal principal) {
        Account currentAccount = getCurrentAccount(principal);
        model.addAttribute("account", currentAccount);
        // Create a separate object for the form to avoid password issues if needed
        model.addAttribute("accountUpdateForm", currentAccount);
        return "user/account"; // Path to the HTML template
    }

    // Xử lý cập nhật thông tin tài khoản
    @PostMapping("/update")
    public String updateAccount(@ModelAttribute("accountUpdateForm") Account updatedAccount,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        Account currentAccount = getCurrentAccount(principal);

        // Chỉ cập nhật các trường cho phép thay đổi
        currentAccount.setFullName(updatedAccount.getFullName());
        currentAccount.setPhoneNumber(updatedAccount.getPhoneNumber());
        currentAccount.setAddress(updatedAccount.getAddress());

        try {
            // Gọi service để lưu (Giả sử AccountService có phương thức save hoặc update)
            accountRepository.save(currentAccount); // Or use accountService.updateAccountDetails(currentAccount);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi cập nhật: " + e.getMessage());
        }

        return "redirect:/account"; // Redirect back to the account page
    }

    // Có thể thêm các mapping khác ở đây (ví dụ: đổi mật khẩu)
}
