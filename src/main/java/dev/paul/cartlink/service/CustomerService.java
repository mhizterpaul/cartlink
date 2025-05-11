package dev.paul.cartlink.service;

import dev.paul.cartlink.model.Customer;
import dev.paul.cartlink.repository.CustomerRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityService securityService;

    public CustomerService(CustomerRepository customerRepository,
            PasswordEncoder passwordEncoder,
            SecurityService securityService) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.securityService = securityService;
    }

    @Transactional
    public Customer registerCustomer(Customer customer) {
        if (customerRepository.existsByEmail(customer.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        return customerRepository.save(customer);
    }

    public Customer getCustomerById(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
    }
}