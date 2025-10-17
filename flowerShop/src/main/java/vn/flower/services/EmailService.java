package vn.flower.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    // Gửi mã OTP
    public void sendOtp(String toEmail, String otp) {
        String subject = "Xác nhận đăng ký - Florio Flower Shop";
        String body = "Xin chào,\n\nMã OTP xác nhận đăng ký của bạn là: " + otp +
                "\n\nMã này có hiệu lực trong 3 phút.\n\nCảm ơn bạn đã sử dụng dịch vụ Florio 🌸";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }
}
