package dev.mhizterpaul.cartlink.integration.complaint;

import com.fasterxml.jackson.databind.ObjectMapper;
// Assuming DTOs are in dev.paul.cartlink.complaint.dto or common
import dev.paul.cartlink.complaint.dto.ComplaintRequest; // Placeholder
import dev.paul.cartlink.complaint.dto.ComplaintResponse; // Placeholder

// For test data setup
import dev.paul.cartlink.merchant.model.Merchant;
import dev.paul.cartlink.merchant.repository.MerchantRepository;
import dev.paul.cartlink.customer.model.Customer;
import dev.paul.cartlink.customer.repository.CustomerRepository;
import dev.paul.cartlink.order.model.Order;
import dev.paul.cartlink.order.repository.OrderRepository;
import dev.paul.cartlink.order.model.OrderStatus;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockCookie;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Complaint Handling API Integration Tests")
public class ComplaintControllerIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private OrderRepository orderRepository;
    @Autowired private MerchantRepository merchantRepository;
    @Autowired private CustomerRepository customerRepository;

    private String testCustomerId;
    private String testOrderId;
    private MockCookie customerSessionCookie;

    @BeforeEach
    void setUpTestData() {
        Merchant merchant = merchantRepository.save(new Merchant("complaint-merchant" + System.currentTimeMillis() + "@example.com", "Pass", "CompM", "LTD"));
        Customer customer = new Customer();
        customer.setEmail("complaintcust" + System.currentTimeMillis() + "@example.com");
        Customer savedCustomer = customerRepository.save(customer);
        testCustomerId = savedCustomer.getId();

        Order order = new Order();
        order.setMerchant(merchant);
        order.setCustomer(savedCustomer);
        order.setTotalAmount(BigDecimal.valueOf(50.00));
        order.setStatus(OrderStatus.DELIVERED); // Complaint typically after delivery
        order.setOrderDate(LocalDateTime.now().minusDays(1));
        Order savedOrder = orderRepository.save(order);
        testOrderId = savedOrder.getId();

        customerSessionCookie = new MockCookie("JSESSIONID", "session-for-" + testCustomerId);
    }

    @Nested
    @DisplayName("POST /api/v1/customers/orders/{orderId}/complaint")
    class SubmitComplaint {
        @Test
        @DisplayName("Should allow authenticated customer to submit a complaint for their order")
        void whenValidComplaint_thenSubmitsSuccessfully() throws Exception {
            ComplaintRequest complaintRequest = new ComplaintRequest("Item Damaged", "The item arrived broken.", "Product Quality");

            mockMvc.perform(post("/api/v1/customers/orders/{orderId}/complaint", testOrderId)
                    .cookie(customerSessionCookie)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(complaintRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists()) // Assuming response includes complaint ID
                    .andExpect(jsonPath("$.title").value("Item Damaged"));
        }
        // Add 400 (invalid data), 401 (not auth), 403 (not owner of order), 404 (order not found) tests
    }

    @Nested
    @DisplayName("GET /api/v1/customers/orders/complaints (Get All Customer Complaints)")
    class GetCustomerComplaints {
        @Test
        @DisplayName("Should retrieve all complaints for the authenticated customer")
        void whenAuthenticated_thenReturnsCustomerComplaints() throws Exception {
            // Submit a complaint first to ensure data exists
            ComplaintRequest complaintRequest = new ComplaintRequest("Test Complaint", "Details", "Test");
            mockMvc.perform(post("/api/v1/customers/orders/{orderId}/complaint", testOrderId)
                    .cookie(customerSessionCookie)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(complaintRequest)))
                    .andExpect(status().isCreated());

            mockMvc.perform(get("/api/v1/customers/orders/complaints")
                    .cookie(customerSessionCookie))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].title").value("Test Complaint"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/customers/orders/{orderId}/complaints (Get Complaints for Specific Order)")
    class GetOrderComplaints {
        @Test
        @DisplayName("Should retrieve complaints for a specific order by authenticated customer")
        void whenAuthenticatedAndOrderExists_thenReturnsOrderComplaints() throws Exception {
             ComplaintRequest complaintRequest = new ComplaintRequest("Order Specific Complaint", "Details here.", "Order Issue");
            mockMvc.perform(post("/api/v1/customers/orders/{orderId}/complaint", testOrderId)
                    .cookie(customerSessionCookie)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(complaintRequest)))
                    .andExpect(status().isCreated());

            mockMvc.perform(get("/api/v1/customers/orders/{orderId}/complaints", testOrderId)
                    .cookie(customerSessionCookie))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].title").value("Order Specific Complaint"));
        }
    }

    // --- Notes on ComplaintControllerIT ---
    // - Targets dev.paul.cartlink.complaint.controller.ComplaintController.
    // - Uses repositories to set up Merchant, Customer, and Order data.
    // - Placeholder DTOs (ComplaintRequest, ComplaintResponse) used.
    // - Simulates customer session with MockCookie.
}
