package vn.flower.services;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.springframework.stereotype.Service;

@Service
public class OtpService {
    private final Map<String, String> otpStorage = new HashMap<>();
    private final Map<String, Long> otpExpiry = new HashMap<>();

    public String generateOtp(String email) {
        String otp = String.valueOf(new Random().nextInt(900000) + 100000);
        otpStorage.put(email, otp);
        otpExpiry.put(email, System.currentTimeMillis() + 5 * 60 * 1000);
        return otp;
    }

    public boolean verifyOtp(String email, String otp) {
        if (!otpStorage.containsKey(email)) return false;
        if (System.currentTimeMillis() > otpExpiry.get(email)) return false;
        return otpStorage.get(email).equals(otp);
    }
}

