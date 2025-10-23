package vn.flower.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable; // <-- THÊM IMPORT NÀY
import org.springframework.security.web.csrf.CsrfToken; // <-- THÊM IMPORT NÀY
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import vn.flower.entities.Account;
import vn.flower.services.AccountService;
import vn.flower.services.EmailService;
import vn.flower.services.OtpService;

@Controller
@RequestMapping("/auth")
public class AuthController {

	@Autowired
	private HttpSession session;
	@Autowired
	private AccountService accountService;

	@Autowired
	private EmailService emailService;

	@Autowired
	private OtpService otpService;

	@GetMapping("/register")
	public String showRegisterForm(Model model, @Nullable CsrfToken csrfToken) { // <-- Thêm CsrfToken
		model.addAttribute("account", new Account());
		if (csrfToken != null) model.addAttribute("_csrf", csrfToken); // <-- Thêm vào Model
		return "auth/register";
	}

	@PostMapping("/register")
	public String register(@ModelAttribute("account") Account account, Model model,
	                       @Nullable CsrfToken csrfToken) { // <-- Thêm CsrfToken cho trang OTP
	    if (accountService.existsByEmail(account.getEmail())) {
	        model.addAttribute("error", "Email này đã được đăng ký!");
	        if (csrfToken != null) model.addAttribute("_csrf", csrfToken); // <-- Thêm lại cho form lỗi
	        return "auth/register";
	    }

	    String otp = otpService.generateOtp(account.getEmail());
	    emailService.sendOtp(account.getEmail(), otp);

	    session.setAttribute("pendingAccount", account);

	    model.addAttribute("email", account.getEmail());
	    model.addAttribute("message", "Đăng ký thành công! Mã OTP đã được gửi đến email của bạn.");
	    if (csrfToken != null) model.addAttribute("_csrf", csrfToken); // <-- Thêm cho form OTP
	    return "auth/verify-otp";
	}

	@PostMapping("/register/confirm")
	public String registerConfirm(@RequestParam String email,
	                              @RequestParam String otp,
	                              Model model, @Nullable CsrfToken csrfToken) { // <-- Thêm CsrfToken cho trang lỗi
	    Account pendingAccount = (Account) session.getAttribute("pendingAccount");

	    if (pendingAccount == null || !pendingAccount.getEmail().equals(email)) {
	        model.addAttribute("error", "Phiên không hợp lệ, vui lòng đăng ký lại!");
	        if (csrfToken != null) model.addAttribute("_csrf", csrfToken); // <-- Thêm cho form lỗi
	        return "auth/register";
	    }

	    if (otpService.verifyOtp(email, otp)) {
	        boolean success = accountService.register(pendingAccount);
	        session.removeAttribute("pendingAccount");

	        if (!success) {
	            model.addAttribute("error", "Email này đã được đăng ký!");
	            if (csrfToken != null) model.addAttribute("_csrf", csrfToken); // <-- Thêm cho form lỗi
	            return "auth/register";
	        }
	        // Redirect không cần token
	        return "redirect:/auth/login?verified=true"; 
	    } else {
	        model.addAttribute("email", email);
	        model.addAttribute("error", "❌ Mã OTP không hợp lệ hoặc đã hết hạn!");
	        if (csrfToken != null) model.addAttribute("_csrf", csrfToken); // <-- Thêm cho form OTP
	        return "auth/verify-otp";
	    }
	}


	@GetMapping("/login")
	public String showLoginForm(Model model, @Nullable CsrfToken csrfToken) { // <-- Thêm CsrfToken
	    if (csrfToken != null) model.addAttribute("_csrf", csrfToken); // <-- Thêm vào Model
		return "auth/login";
	}

    // Phương thức POST /auth/login được Spring Security xử lý, không cần định nghĩa ở đây
}