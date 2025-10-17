package vn.flower.controllers;

import org.springframework.beans.factory.annotation.Autowired;
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

	// Trang đăng ký
	@GetMapping("/register")
	public String showRegisterForm(Model model) {
		model.addAttribute("account", new Account());
		return "auth/register"; // register.html
	}
	@PostMapping("/register")
	public String register(@ModelAttribute("account") Account account, Model model) {
	    if (accountService.existsByEmail(account.getEmail())) {
	        model.addAttribute("error", "Email này đã được đăng ký!");
	        return "auth/register";
	    }

	    // Sinh OTP
	    String otp = otpService.generateOtp(account.getEmail());
	    emailService.sendOtp(account.getEmail(), otp);

	    // 👉 Lưu thông tin tạm vào session
	    session.setAttribute("pendingAccount", account);

	    model.addAttribute("email", account.getEmail());
	    model.addAttribute("message", "Đăng ký thành công! Mã OTP đã được gửi đến email của bạn.");
	    return "auth/verify-otp";
	}

	@PostMapping("/register/confirm")
	public String registerConfirm(@RequestParam String email,
	                              @RequestParam String otp,
	                              Model model) {
	    // ✅ Lấy lại account đã lưu trong session
	    Account pendingAccount = (Account) session.getAttribute("pendingAccount");

	    if (pendingAccount == null || !pendingAccount.getEmail().equals(email)) {
	        model.addAttribute("error", "Phiên không hợp lệ, vui lòng đăng ký lại!");
	        return "auth/register";
	    }

	    if (otpService.verifyOtp(email, otp)) {
	        boolean success = accountService.register(pendingAccount);
	        session.removeAttribute("pendingAccount"); // Xóa session

	        if (!success) {
	            model.addAttribute("error", "Email này đã được đăng ký!");
	            return "auth/register";
	        }
	        model.addAttribute("message", "✅ Kích hoạt tài khoản thành công! Vui lòng đăng nhập.");
	        return "redirect:/auth/login?verified=true";
	    } else {
	        model.addAttribute("email", email);
	        model.addAttribute("error", "❌ Mã OTP không hợp lệ hoặc đã hết hạn!");
	        return "auth/verify-otp";
	    }
	}



	// Trang đăng nhập
	@GetMapping("/login")
	public String showLoginForm() {
		return "auth/login";
	}


}
