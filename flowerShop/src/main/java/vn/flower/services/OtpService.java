package vn.flower.services;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.springframework.stereotype.Service;

@Service
public class OtpService {

    // Bộ nhớ tạm chứa OTP
    private Map<String, OtpInfo> otpStorage = new HashMap<>();

    // Sinh mã OTP ngẫu nhiên và lưu kèm thời gian hết hạn
    public String generateOtp(String email) {
        String otp = String.format("%06d", new Random().nextInt(999999));
        otpStorage.put(email, new OtpInfo(otp, LocalDateTime.now().plusMinutes(3))); // hết hạn sau 3 phút
        return otp;
    }

    // Kiểm tra OTP hợp lệ hay không
    public boolean verifyOtp(String email, String otp) {
        OtpInfo info = otpStorage.get(email);
        if (info == null) return false;

        boolean isValid = info.getOtp().equals(otp) && LocalDateTime.now().isBefore(info.getExpireTime());
        if (isValid) otpStorage.remove(email); // dùng 1 lần rồi xóa
        return isValid;
    }

    // Lớp lưu OTP và thời gian hết hạn
    private static class OtpInfo {
        private String otp;
        private LocalDateTime expireTime;

        public OtpInfo(String otp, LocalDateTime expireTime) {
            this.otp = otp;
            this.expireTime = expireTime;
        }
        public String getOtp() { return otp; }
        public LocalDateTime getExpireTime() { return expireTime; }
    }
}
