package vn.flower.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    public void sendOtp(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("🌸 Mã xác thực StarShop");
        message.setText("Xin chào,\n\nMã OTP của bạn là: " + otp +
                        "\n\nMã có hiệu lực trong 5 phút.\nTrân trọng,\nĐội ngũ StarShop.");
        mailSender.send(message);
    }
}

