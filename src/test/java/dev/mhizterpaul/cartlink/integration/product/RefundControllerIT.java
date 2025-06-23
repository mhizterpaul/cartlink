package dev.mhizterpaul.cartlink.integration.product; // Controller is in product package

import com.fasterxml.jackson.databind.ObjectMapper;
// DTOs likely in customer.dto or common, as service is in customer.service
import dev.paul.cartlink.customer.dto.RefundRequest; // Placeholder
import dev.paul.cartlink.customer.dto.RefundResponse; // Placeholder

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
@DisplayName("Refund Management API Integration Tests (product.RefundController)")
public class RefundControllerIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private MerchantRepository merchantRepository;
    @Autowired
    private CustomerRepository customerRepository;

    private Long testCustomerId;
    private Long testOrderId;
    private MockCookie customerSessionCookie;

    @BeforeEach
    void setUpTestData() {
        Merchant merchant = merchantRepository.save(new Merchant(
                "refund-merchant" + System.currentTimeMillis() + "@example.com", "Pass", "RefundM", "LTD"));
        Customer customer = new Customer();
        customer.setEmail("refundcust" + System.currentTimeMillis() + "@example.com");
        Customer savedCustomer = customerRepository.save(customer);
        testCustomerId = savedCustomer.getId();

        Order order = new Order();
        order.setMerchant(merchant);
        order.setCustomer(savedCustomer);
        order.setTotalAmount(BigDecimal.valueOf(30.00));
        order.setStatus(OrderStatus.PAID); // Order status suitable for refund request
        order.setOrderDate(LocalDateTime.now().minusDays(2));
        Order savedOrder = orderRepository.save(order);
        testOrderId = savedOrder.getId();

        customerSessionCookie = new MockCookie("JSESSIONID", "session-for-" + testCustomerId);
    }

    @Nested
    @DisplayName("POST /api/v1/customers/orders/{orderId}/refund")
    class SubmitRefundRequest {
        @Test
        @DisplayName("Should allow authenticated customer to submit a refund request for their order")
        void whenValidRefundRequest_thenSubmitsSuccessfully() throws Exception {
            RefundRequest refundRequest = new RefundRequest(
                    testOrderId, // orderId from created order
                    "Item defective", // reason
                    30.00, // amount (matches order amount)
                    "1234567890", // accountNumber (dummy)
                    "Test Bank", // bankName (dummy)
                    "John Doe" // accountName (dummy)
            );

            mockMvc.perform(post("/api/v1/customers/orders/{orderId}/refund", testOrderId)
                    .cookie(customerSessionCookie)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(refundRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.reason").value("Item defective"));
        }
        // Add 400 (invalid data), 401 (not auth), 403 (not owner), 404 (order not
        // found) tests
    }

    @Nested
    @DisplayName("GET /api/v1/customers/orders/refunds (Get All Customer Refunds)")
    class GetCustomerRefunds {
        @Test
        @DisplayName("Should retrieve all refund requests for the authenticated customer")
        void whenAuthenticated_thenReturnsCustomerRefunds() throws Exception {
            // Submit a refund request first
            RefundRequest refundRequest = new RefundRequest(
                    testOrderId,
                    "Another reason",
                    30.00,
                    "1234567890",
                    "Test Bank",
                    "John Doe");
            mockMvc.perform(post("/api/v1/customers/orders/{orderId}/refund", testOrderId)
                    .cookie(customerSessionCookie)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(refundRequest)))
                    .andExpect(status().isCreated());

            mockMvc.perform(get("/api/v1/customers/orders/refunds")
                    .cookie(customerSessionCookie))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].reason").value("Another reason"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/customers/orders/{orderId}/refunds (Get Refunds for Specific Order)")
    class GetOrderRefunds {
        @Test
        @DisplayName("Should retrieve refunds for a specific order by authenticated customer")
        void whenAuthenticatedAndOrderExists_thenReturnsOrderRefunds() throws Exception {
            RefundRequest refundRequest = new RefundRequest(
                    testOrderId,
                    "Order specific refund reason",
                    30.00,
                    "1234567890",
                    "Test Bank",
                    "John Doe");
            mockMvc.perform(post("/api/v1/customers/orders/{orderId}/refund", testOrderId)
                    .cookie(customerSessionCookie)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(refundRequest)))
                    .andExpect(status().isCreated());

            mockMvc.perform(get("/api/v1/customers/orders/{orderId}/refunds", testOrderId)
                    .cookie(customerSessionCookie))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].reason").value("Order specific refund reason"));
        }
    }

    // --- Notes on RefundControllerIT ---
    // - Targets dev.paul.cartlink.product.controller.RefundController.
    // - Mocks dev.paul.cartlink.customer.service.RefundService.
    // - Uses repositories for Merchant, Customer, Order setup.
    // - Placeholder DTOs (RefundRequestDto, RefundResponseDto) used, likely from
    // customer.dto.
}
