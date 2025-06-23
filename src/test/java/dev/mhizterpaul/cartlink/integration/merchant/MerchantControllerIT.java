package dev.mhizterpaul.cartlink.integration.merchant;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.paul.cartlink.merchant.dto.LoginRequest; // Actual DTO path
import dev.paul.cartlink.merchant.dto.SignUpRequest;  // Actual DTO path
import dev.paul.cartlink.merchant.dto.AuthResponse;    // Actual DTO path
// Assuming other DTOs like MerchantProfileUpdateRequest, PasswordResetRequestRequest etc.
// would be in dev.paul.cartlink.merchant.dto or a common dto package.
// For now, using placeholder fully qualified names if direct match not found in listing.
import dev.paul.cartlink.dto.request.MerchantProfileUpdateRequest; // Placeholder
import dev.paul.cartlink.dto.request.PasswordResetExecutionRequest; // Placeholder
import dev.paul.cartlink.dto.request.PasswordResetRequestRequest; // Placeholder
import dev.paul.cartlink.dto.request.RefreshTokenRequest; // Placeholder


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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.isA;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // Rollback database changes after each test
@DisplayName("Merchant API Integration Tests")
public class MerchantControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper; // Spring Boot auto-configures this

    private SignUpRequest validSignUpRequest;
    private LoginRequest validLoginRequest;

    @BeforeEach
    void setUp() {
        validSignUpRequest = new SignUpRequest(
                "merchant" + System.currentTimeMillis() + "@example.com", // Unique email
                "Password123!",
                "Test",
                "Merchant",
                "http://example.com/image.png"
        );
        validLoginRequest = new LoginRequest(validSignUpRequest.getEmail(), validSignUpRequest.getPassword());
    }

    @Nested
    @DisplayName("User Journey: Signup and Login")
    class SignupAndLoginJourney {

        @Test
        @DisplayName("Should allow merchant to signup, then login, then access protected profile")
        void merchantSignupLoginAndAccessProfile() throws Exception {
            // 1. Sign Up
            MvcResult signUpResult = mockMvc.perform(post("/api/v1/merchants/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validSignUpRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.merchantId").exists())
                    .andExpect(jsonPath("$.token").exists())
                    .andExpect(jsonPath("$.merchantDetails.email").value(validSignUpRequest.getEmail()))
                    .andReturn();

            String responseString = signUpResult.getResponse().getContentAsString();
            AuthResponse signUpAuthResponse = objectMapper.readValue(responseString, AuthResponse.class);
            String merchantId = signUpAuthResponse.getMerchantId();
            String token = signUpAuthResponse.getToken();

            // 2. Login with the same credentials
            mockMvc.perform(post("/api/v1/merchants/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validLoginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.merchantId").value(merchantId))
                    .andExpect(jsonPath("$.token").exists());

            // 3. Access Profile with token from signup
            mockMvc.perform(get("/api/v1/merchants/{merchantId}", merchantId)
                    .header("Authorization", "Bearer " + token)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value(validSignUpRequest.getEmail()));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/merchants/signup")
    class MerchantSignUp {
        @Test
        @DisplayName("Should return 400 Bad Request for invalid signup data (e.g., invalid email)")
        void whenInvalidEmailFormat_thenReturns400() throws Exception {
            SignUpRequest invalidEmailRequest = new SignUpRequest("invalid-email", "Password123!", "Test", "User", null);
            mockMvc.perform(post("/api/v1/merchants/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidEmailRequest)))
                    .andExpect(status().isBadRequest()); // Assuming @Valid handles this
        }
    }

    @Nested
    @DisplayName("POST /api/v1/merchants/login")
    class MerchantLogin {
        @Test
        @DisplayName("Should return 401 Unauthorized for non-existent user")
        void whenNonExistentUserLogin_thenReturns401() throws Exception {
            LoginRequest nonExistentUserLogin = new LoginRequest("nosuchuser@example.com", "password123");
            mockMvc.perform(post("/api/v1/merchants/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(nonExistentUserLogin)))
                    .andExpect(status().isUnauthorized());
        }
    }

    // --- Placeholder for other Merchant endpoint tests ---
    // PasswordResetRequest, PasswordReset, RefreshToken, GetProfile, UpdateProfile
    // Each would follow a similar pattern:
    // - Prepare request DTO.
    // - Perform mockMvc call.
    // - Assert status and response content.
    // - For protected endpoints, include Authorization header.
    // - For state-changing operations, consider verifying DB state if crucial and not covered by other BDT steps.

    // --- Notes on MerchantControllerIT ---
    // - Assumed DTOs from dev.paul.cartlink.merchant.dto. Placeholder for others.
    // - `@Transactional` will roll back DB changes.
    // - Email uniqueness is handled by adding System.currentTimeMillis().
}
