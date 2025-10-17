package vn.flower.services;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import vn.flower.entities.Account;
import vn.flower.repositories.AccountRepository;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public boolean existsByEmail(String email) {
        return accountRepository.existsByEmail(email);
    }
    
    public boolean register(Account account) {
        // Kiểm tra email đã tồn tại chưa
        if (accountRepository.findByEmail(account.getEmail()).isPresent()) {
            return false;
        }

        // Gán giá trị mặc định
        account.setPassword(passwordEncoder.encode(account.getPassword())); // mã hoá mật khẩu
        account.setRole("customer");
        account.setCreatedAt(LocalDateTime.now());

        accountRepository.save(account);
        return true;
    }
    

    public Account login(String email, String password) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"));
        if (!passwordEncoder.matches(password, account.getPassword())) {
            throw new RuntimeException("Sai mật khẩu");
        }
        return account;
    }
}

