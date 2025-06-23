package dev.mhizterpaul.cartlink.controller.customer;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.mhizterpaul.cartlink.customer.service.CustomerService;
import dev.mhizterpaul.cartlink.customer.service.SignUpRequest; // This was listed under customer.service, assuming it's the DTO for customer signup
// Assuming other DTOs like CustomerProfileUpdateRequest, CustomerAuthResponse, OrderHistoryResponse
// would be in dev.mhizterpaul.cartlink.customer.dto or a common dto package.
// Using placeholders for now.
import dev.mhizterpaul.cartlink.dto.request.CustomerProfileUpdateRequest; // Placeholder
import dev.mhizterpaul.cartlink.dto.response.CustomerAuthResponse; // Placeholder
import dev.mhizterpaul.cartlink.dto.response.OrderHistoryResponse; // Placeholder
import dev.mhizterpaul.cartlink.dto.response.SuccessMessageResponse; // Placeholder


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Collections;
import javax.servlet.http.Cookie;


@ExtendWith(MockitoExtension.class)
@DisplayName("Customer API Endpoints")
public class CustomerControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private CustomerController customerController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(customerController).build();
    }

    @Nested
    @DisplayName("POST /api/v1/customers/signup")
    class CustomerSignUp {

        @Test
        @DisplayName("Should return 201 Created with JWT and customer details on successful signup")
        void whenValidSignupRequest_thenReturns201AndCustomerDetails() throws Exception {
            // Assuming SignUpRequest under customer.service is the DTO.
            // This is unusual, DTOs are typically in a 'dto' package.
            // Adjust if dev.mhizterpaul.cartlink.customer.dto.SignUpRequest is found later.
            SignUpRequest signUpRequest = new SignUpRequest("newcust@example.com", "New", "Customer", "+2349000000000", null); // Address simplified

            CustomerAuthResponse.CustomerDetails customerDetails = new CustomerAuthResponse.CustomerDetails("newcust@example.com", "New", "Customer", "+2349000000000", null);
            CustomerAuthResponse authResponse = new CustomerAuthResponse("jwt-customer-token", customerDetails);

            when(customerService.signUp(any(SignUpRequest.class))).thenReturn(authResponse);

            mockMvc.perform(post("/api/v1/customers/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signUpRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.token").value("jwt-customer-token"))
                    .andExpect(jsonPath("$.customerDetails.email").value("newcust@example.com"));
        }

        @Test
        @DisplayName("Should return 400 Bad Request for invalid signup data")
        void whenInvalidSignupRequest_thenReturns400() throws Exception {
            SignUpRequest signUpRequest = new SignUpRequest(null, "New", "Customer", "+2349000000000", null);
            when(customerService.signUp(any(SignUpRequest.class))).thenThrow(new IllegalArgumentException("Invalid data"));

            mockMvc.perform(post("/api/v1/customers/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signUpRequest)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/customers/profile")
    class UpdateCustomerProfile {
        private final String MOCK_SESSION_ID = "mock-customer-session-id";

        @Test
        @DisplayName("Should return 200 OK with success message on successful profile update")
        void whenValidProfileUpdateRequest_thenReturns200AndSuccessMessage() throws Exception {
            CustomerProfileUpdateRequest.AddressDto addressDto = new CustomerProfileUpdateRequest.AddressDto("456 Updated St", "NewCity", "NewState", "NewCountry", "54321");
            CustomerProfileUpdateRequest updateRequest = new CustomerProfileUpdateRequest("UpdatedFirst", "UpdatedLast", "+2348012345678", addressDto);

            SuccessMessageResponse successResponse = new SuccessMessageResponse(true, "Profile updated successfully.");
            when(customerService.updateProfile(anyString(), any(CustomerProfileUpdateRequest.class))).thenReturn(successResponse);

            mockMvc.perform(put("/api/v1/customers/profile")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest))
                    .cookie(new Cookie("JSESSIONID", MOCK_SESSION_ID)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
        // Add 400, 401/403 tests
    }

    @Nested
    @DisplayName("GET /api/v1/customers/orders/history")
    class GetOrderHistory {
        private final String MOCK_SESSION_ID = "mock-customer-session-id";

        @Test
        @DisplayName("Should return 200 OK with list of past orders")
        void whenAuthenticatedCustomerRequestsOrderHistory_thenReturns200AndOrderList() throws Exception {
            OrderHistoryResponse.OrderSummary summary = new OrderHistoryResponse.OrderSummary("order123", null, 0.0, null, null);
            OrderHistoryResponse response = new OrderHistoryResponse(Collections.singletonList(summary), 1, 1, 10, 1L); // Added totalElements

            when(customerService.getOrderHistory(anyString(), anyInt(), anyInt())).thenReturn(response);

            mockMvc.perform(get("/api/v1/customers/orders/history?page=1&limit=10")
                    .cookie(new Cookie("JSESSIONID", MOCK_SESSION_ID)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orders").isArray())
                    .andExpect(jsonPath("$.orders[0].orderId").value("order123"));
        }
        // Add 401/403 test
    }

    // --- Notes on CustomerControllerTest ---
    // Assumptions:
    // 1. CustomerController in dev.mhizterpaul.cartlink.customer.controller and CustomerService in dev.mhizterpaul.cartlink.customer.service.
    // 2. SignUpRequest DTO is dev.mhizterpaul.cartlink.customer.service.SignUpRequest (unusual location). Other DTOs are placeholders.
    // 3. Customer tracking via cookies is handled.
    // Inadequacies & Edge Cases:
    // - Confirm actual DTO locations and structures.
    // - Test specific field validations for signup and profile update.
}
