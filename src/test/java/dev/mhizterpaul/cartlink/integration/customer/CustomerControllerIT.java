package dev.mhizterpaul.cartlink.integration.customer;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.paul.cartlink.customer.service.SignUpRequest; // Path from ls output (customer.service)
// Assuming other DTOs like CustomerProfileUpdateRequest, CustomerAuthResponse, OrderHistoryResponse
// would be in dev.paul.cartlink.customer.dto or a common dto package.
import dev.paul.cartlink.dto.request.CustomerProfileUpdateRequest; // Placeholder
import dev.paul.cartlink.dto.response.CustomerAuthResponse; // Placeholder
import dev.paul.cartlink.dto.response.OrderHistoryResponse; // Placeholder

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.mock.web.MockCookie; // For sending cookies

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Customer API Integration Tests")
public class CustomerControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private SignUpRequest validCustomerSignUpRequest;
    // Placeholder for Address DTO, assuming it's nested or a separate class
    private SignUpRequest.AddressDto validAddressDto;


    @BeforeEach
    void setUp() {
        // Assuming SignUpRequest has a nested AddressDto or similar structure
        // If AddressDto is separate, it would need to be imported.
        // For now, this is a placeholder as its exact structure isn't in the ls output.
        validAddressDto = new SignUpRequest.AddressDto("123 Test St", "Testville", "TS", "Testland", "12345");

        validCustomerSignUpRequest = new SignUpRequest(
                "customer" + System.currentTimeMillis() + "@example.com",
                "CustFirst",
                "CustLast",
                "+12345678901",
                validAddressDto
        );
    }

    @Nested
    @DisplayName("POST /api/v1/customers/signup")
    class CustomerSignUp {
        @Test
        @DisplayName("Should allow customer to signup and return JWT and customer details")
        void whenValidSignup_thenReturns201AndAuthResponse() throws Exception {
            mockMvc.perform(post("/api/v1/customers/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCustomerSignUpRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.token").exists())
                    .andExpect(jsonPath("$.customerDetails.email").value(validCustomerSignUpRequest.getEmail()));
        }

        @Test
        @DisplayName("Should return 400 for signup with invalid data (e.g., missing email)")
        void whenInvalidSignup_thenReturns400() throws Exception {
            // Create a request with invalid data, e.g. null email
            SignUpRequest invalidRequest = new SignUpRequest(null, "First", "Last", "123", null);
            mockMvc.perform(post("/api/v1/customers/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest()); // Assuming @Valid or service layer validation
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/customers/profile")
    class UpdateCustomerProfile {
        // This requires an authenticated customer session (cookie)

        @Test
        @DisplayName("Should allow authenticated customer to update their profile")
        void whenAuthenticated_andValidUpdateData_thenReturns200() throws Exception {
            // Step 1: Signup to get a session/cookie (or obtain one through login if separate)
            // For simplicity in this isolated test, we'll assume a cookie representing an authenticated user
            // In a full BDT, you'd get this from a login step.

            // Assume signup creates a session cookie named "JSESSIONID" (common default)
            // This part would ideally be preceded by a login/signup that establishes the cookie.
            // For this test, we'll manually create a placeholder cookie.
            // A more robust way would be to perform a login and extract the cookie.

            // First, sign up a user to ensure the customer exists for profile update.
            // (This also helps if the profile update endpoint requires the user to exist)
             mockMvc.perform(post("/api/v1/customers/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCustomerSignUpRequest)))
                    .andExpect(status().isCreated());

            // Placeholder for actual cookie obtained after login/signup
            // In a real test, you'd capture the cookie from the signup/login response
            MockCookie sessionCookie = new MockCookie("JSESSIONID", "mock-customer-session-id-for-update");

            CustomerProfileUpdateRequest.AddressDto updateAddressDto = new CustomerProfileUpdateRequest.AddressDto("789 New St", "NewCity", "NC", "NewLand", "67890");
            CustomerProfileUpdateRequest profileUpdateRequest = new CustomerProfileUpdateRequest(
                    "UpdatedFirst", "UpdatedLast", "+9876543210", updateAddressDto
            );

            mockMvc.perform(put("/api/v1/customers/profile")
                    .cookie(sessionCookie) // Send the session cookie
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(profileUpdateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/customers/orders/history")
    class GetOrderHistory {
        @Test
        @DisplayName("Should allow authenticated customer to retrieve order history")
        void whenAuthenticated_thenReturnsOrderHistory() throws Exception {
            // Similar to profile update, requires an authenticated session.
            // Sign up user first (or ensure user exists and is logged in)
            mockMvc.perform(post("/api/v1/customers/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validCustomerSignUpRequest)))
                    .andExpect(status().isCreated());

            MockCookie sessionCookie = new MockCookie("JSESSIONID", "mock-customer-session-id-for-history");

            mockMvc.perform(get("/api/v1/customers/orders/history")
                    .cookie(sessionCookie) // Send the session cookie
                    .param("page", "1")
                    .param("limit", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orders").isArray()); // Assuming 'orders' is the list property
        }
    }

    // --- Notes on CustomerControllerIT ---
    // - Assumed SignUpRequest DTO from dev.paul.cartlink.customer.service.
    // - Other DTOs (CustomerProfileUpdateRequest, CustomerAuthResponse, OrderHistoryResponse) are placeholders.
    // - Cookie-based authentication is simulated. A more robust approach for obtaining cookies would be
    //   to perform a login (if available and sets a cookie) and capture the cookie from its response.
    // - `@Transactional` ensures data from one test (like signup) doesn't interfere with others.
}
