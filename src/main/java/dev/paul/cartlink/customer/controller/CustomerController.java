package dev.paul.cartlink.customer.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import dev.paul.cartlink.customer.dto.CustomerProfileUpdateRequest;
import dev.paul.cartlink.customer.model.Customer;
import dev.paul.cartlink.customer.service.CustomerService;
import dev.paul.cartlink.order.service.OrderService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final OrderService orderService;

    public CustomerController(CustomerService customerService, OrderService orderService) {
        this.customerService = customerService;
        this.orderService = orderService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerCustomer(@RequestBody Map<String, Object> request) {
        try {
            String email = (String) request.get("email");
            String firstName = (String) request.get("firstName");
            String lastName = (String) request.get("lastName");
            String phoneNumber = (String) request.get("phoneNumber");
            Map<String, String> addressMap = (Map<String, String>) request.get("address");
            Customer customer = new Customer();
            customer.setEmail(email);
            customer.setFirstName(firstName);
            customer.setLastName(lastName);
            customer.setPhoneNumber(phoneNumber);
            if (addressMap != null) {
                customer.setStreet(addressMap.get("street"));
                customer.setCity(addressMap.get("city"));
                customer.setState(addressMap.get("state"));
                customer.setCountry(addressMap.get("country"));
                customer.setPostalCode(addressMap.get("postalCode"));
            }
            Customer registeredCustomer = customerService.registerCustomer(customer);
            return ResponseEntity.status(201).body(registeredCustomer);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestHeader("Authorization") String authHeader,
            @RequestBody CustomerProfileUpdateRequest request) {
        try {
            String jwt = authHeader.replace("Bearer ", "");
            Customer authenticated = customerService.getCustomerFromJwt(jwt);
            if (authenticated == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }
            // Only allow update for self
            if (!authenticated.getEmail().equals(request.getEmail())) {
                return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
            }
            Customer updated = customerService.updateProfile(request);
            return ResponseEntity.ok(Map.of("success", true, "message", "Profile updated successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/orders/history")
    public ResponseEntity<?> getOrderHistory(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @AuthenticationPrincipal Customer customer) {
        var orders = orderService.getCustomerOrderHistory(customer, page, limit);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<?> getCustomer(@PathVariable Long customerId) {
        try {
            Customer customer = customerService.getCustomerById(customerId);
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

    // Customer login endpoint (returns JWT)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        try {
            String email = credentials.get("email");
            String password = credentials.get("password");
            // Authenticate customer (implement password check as needed)
            Customer customer = customerService.authenticate(email, password);
            String jwt = customerService.generateJwtForCustomer(customer);
            return ResponseEntity.ok(Map.of("token", jwt, "customer", customer));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }
    }

    @DeleteMapping("/{customerId}")
    public ResponseEntity<?> deleteCustomer(@RequestHeader("Authorization") String authHeader,
            @PathVariable Long customerId) {
        try {
            String jwt = authHeader.replace("Bearer ", "");
            Customer authenticated = customerService.getCustomerFromJwt(jwt);
            if (authenticated == null || !authenticated.getCustomerId().equals(customerId)) {
                return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
            }
            customerService.deleteCustomerById(customerId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Customer deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}