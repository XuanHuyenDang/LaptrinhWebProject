package vn.flower.controllers.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable; // <-- THÊM IMPORT NÀY
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken; // <-- THÊM IMPORT NÀY
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.flower.entities.Account;
import vn.flower.repositories.AccountRepository;
import vn.flower.services.AccountService;

import java.security.Principal;

@Controller
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountService accountService;

    private Account getCurrentAccount(Principal principal) {
        if (principal == null) return null;
        String email = principal.getName();
        return accountRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Cannot find account for email: " + email));
    }

    @GetMapping
    public String showAccountPage(Model model, Principal principal,
                                  @Nullable CsrfToken csrfToken) { // <-- THÊM THAM SỐ CSRF TOKEN
        Account currentAccount = getCurrentAccount(principal);
        model.addAttribute("account", currentAccount);
        model.addAttribute("accountUpdateForm", currentAccount);

        // === THÊM DÒNG NÀY ĐỂ TRUYỀN CSRF TOKEN ===
        if (csrfToken != null) model.addAttribute("_csrf", csrfToken);
        // ===========================================

        return "user/account";
    }

    @PostMapping("/update")
    public String updateAccount(@ModelAttribute("accountUpdateForm") Account updatedAccount,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        Account currentAccount = getCurrentAccount(principal);

        currentAccount.setFullName(updatedAccount.getFullName());
        currentAccount.setPhoneNumber(updatedAccount.getPhoneNumber());
        currentAccount.setAddress(updatedAccount.getAddress());

        try {
            accountRepository.save(currentAccount);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi cập nhật: " + e.getMessage());
        }

        return "redirect:/account";
    }
}