package dev.paul.cartlink.customer.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import dev.paul.cartlink.customer.model.Customer;
import dev.paul.cartlink.customer.service.CustomerService;

import java.util.Map;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerCustomer(@RequestBody Customer customer) {
        try {
            Customer registeredCustomer = customerService.registerCustomer(customer);
            return ResponseEntity.ok(Map.of(
                    "customerId", registeredCustomer.getCustomerId(),
                    "message", "Customer registered successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<?> getCustomer(@PathVariable Long customerId) {
        try {
            Customer customer = customerService.getCustomerById(customerId);
            // Create a response without sensitive information
            Map<String, Object> response = Map.of(
                    "customerId", customer.getCustomerId(),
                    "email", customer.getEmail(),
                    "firstName", customer.getFirstName(),
                    "lastName", customer.getLastName(),
                    "phoneNumber", customer.getPhoneNumber());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}