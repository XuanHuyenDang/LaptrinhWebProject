package vn.flower.services;

import vn.flower.entities.CustomerDTO;
import vn.flower.repositories.AccountRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CustomerService {

    private final AccountRepository accountRepository;

    public CustomerService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public List<CustomerDTO> getAllCustomers() {
        return accountRepository.findAllCustomersWithOrderCount();
    }
}
