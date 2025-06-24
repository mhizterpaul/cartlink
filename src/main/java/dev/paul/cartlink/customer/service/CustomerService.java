package dev.paul.cartlink.customer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.paul.cartlink.customer.model.Customer;
import dev.paul.cartlink.customer.repository.CustomerRepository;
import dev.paul.cartlink.security.service.SecurityService;
import dev.paul.cartlink.order.model.Order;
import dev.paul.cartlink.order.model.OrderStatus;
import dev.paul.cartlink.order.repository.OrderRepository;
import dev.paul.cartlink.customer.dto.CustomerProfileUpdateRequest;

import java.util.Map;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public CustomerService(CustomerRepository customerRepository, OrderRepository orderRepository) {
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public Customer registerCustomer(Customer customer) {
        // Validate required fields
        if (customer.getEmail() == null || customer.getPassword() == null) {
            throw new IllegalArgumentException("Email and password are required");
        }
        // Set address fields directly (no Address object)
        // Save customer
        return customerRepository.save(customer);
    }

    public Customer getCustomerById(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
    }

    // Add profile update method
    public Customer updateProfile(CustomerProfileUpdateRequest request) {
        // Find customer by email or authentication context (for demo, by email)
        Customer customer = customerRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setPhoneNumber(request.getPhoneNumber());
        // Set address fields directly from request
        customer.setStreet(request.getStreet());
        customer.setCity(request.getCity());
        customer.setState(request.getState());
        customer.setCountry(request.getCountry());
        customer.setPostalCode(request.getPostalCode());
        return customerRepository.save(customer);
    }

    // Get or create customer from map (for guest checkout)
    public Customer getOrCreateCustomer(Map<String, Object> customerMap) {
        String email = (String) customerMap.get("email");
        return customerRepository.findByEmail(email).orElseGet(() -> {
            Customer customer = new Customer();
            customer.setEmail(email);
            customer.setFirstName((String) customerMap.get("firstName"));
            customer.setLastName((String) customerMap.get("lastName"));
            customer.setPhoneNumber((String) customerMap.get("phoneNumber"));
            // Address
            Map<String, String> addressMap = (Map<String, String>) customerMap.get("address");
            if (addressMap != null) {
                customer.setStreet(addressMap.get("street"));
                customer.setCity(addressMap.get("city"));
                customer.setState(addressMap.get("state"));
                customer.setCountry(addressMap.get("country"));
                customer.setPostalCode(addressMap.get("postalCode"));
            }
            return customerRepository.save(customer);
        });
    }

    // Generate JWT for customer (real implementation)
    public String generateJwtForCustomer(Customer customer) {
        return securityService.generateToken(customer);
    }

    // Find customer by email (returns null if not found)
    public Customer findByEmail(String email) {
        return customerRepository.findByEmail(email).orElse(null);
    }

    // Parse JWT and return authenticated customer (real JWT validation)
    public Customer getCustomerFromJwt(String jwt) {
        if (!securityService.validateToken(jwt))
            return null;
        String email = securityService.getEmailFromToken(jwt);
        if (email == null)
            return null;
        return customerRepository.findByEmail(email).orElse(null);
    }

    @Transactional
    public void deleteCustomerById(Long customerId) {
        customerRepository.deleteById(customerId);
    }

    // Update createOrderForCustomer to take Customer and Order
    @Transactional
    public Order createOrderForCustomer(Customer customer, Order order) {
        // Set customer on order
        order.setCustomer(customer);
        // Set default status and paid if not set
        if (order.getStatus() == null)
            order.setStatus(OrderStatus.PENDING);
        if (order.getPaid() == null)
            order.setPaid(false);
        return orderRepository.save(order);
    }

    // Authenticate customer with email and password
    public Customer authenticate(String email, String password) {
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        if (customer.getPassword() == null || !passwordEncoder.matches(password, customer.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        return customer;
    }
}