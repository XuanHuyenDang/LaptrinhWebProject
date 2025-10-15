package vn.flower.services;

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

    public Account register(Account account) {
        if (accountRepository.existsByEmail(account.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng");
        }
        account.setPassword(passwordEncoder.encode(account.getPassword()));
        account.setRole("USER");
        return accountRepository.save(account);
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

