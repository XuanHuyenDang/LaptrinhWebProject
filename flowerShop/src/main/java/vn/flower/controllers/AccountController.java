package vn.flower.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.flower.entities.Account;
import vn.flower.services.AccountService;
import vn.flower.services.EmailService;
import vn.flower.services.OtpService;

@Controller
@RequestMapping("/auth")
public class AccountController {

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

	// Xử lý đăng ký
	@PostMapping("/register")
	public String register(@ModelAttribute Account account, Model model) {
		String otp = otpService.generateOtp(account.getEmail());
		emailService.sendOtp(account.getEmail(), otp);
		model.addAttribute("email", account.getEmail());
		return "verify-otp"; // verify-otp.html
	}

	// Xác nhận OTP
	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam String email, @RequestParam String otp, @ModelAttribute Account account,
			Model model) {
		if (otpService.verifyOtp(email, otp)) {
			accountService.register(account);
			model.addAttribute("message", "Kích hoạt tài khoản thành công!");
			return "login"; // chuyển đến trang đăng nhập
		} else {
			model.addAttribute("error", "OTP không hợp lệ hoặc đã hết hạn!");
			return "verify-otp";
		}
	}

	// Trang đăng nhập
	@GetMapping("/login")
	public String showLoginForm() {
		return "auth/login";
	}


}
