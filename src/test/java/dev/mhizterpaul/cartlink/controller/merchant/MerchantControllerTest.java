package dev.mhizterpaul.cartlink.controller.merchant;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.mhizterpaul.cartlink.merchant.dto.AuthResponse;
import dev.mhizterpaul.cartlink.merchant.dto.LoginRequest;
import dev.mhizterpaul.cartlink.merchant.dto.SignUpRequest;
// Assuming other DTOs like MerchantProfileUpdateRequest, PasswordResetRequestRequest etc.
// would be in dev.mhizterpaul.cartlink.merchant.dto or a common dto package.
// For now, using placeholder fully qualified names if direct match not found in listing.
import dev.mhizterpaul.cartlink.dto.request.MerchantProfileUpdateRequest; // Placeholder if not in merchant.dto
import dev.mhizterpaul.cartlink.dto.request.PasswordResetExecutionRequest; // Placeholder
import dev.mhizterpaul.cartlink.dto.request.PasswordResetRequestRequest; // Placeholder
import dev.mhizterpaul.cartlink.dto.request.RefreshTokenRequest; // Placeholder
import dev.mhizterpaul.cartlink.dto.response.MerchantProfileResponse; // Placeholder
import dev.mhizterpaul.cartlink.dto.response.PasswordResetResponse; // Placeholder
import dev.mhizterpaul.cartlink.dto.response.RefreshTokenResponse; // Placeholder
import dev.mhizterpaul.cartlink.dto.response.SuccessMessageResponse; // Placeholder

import dev.mhizterpaul.cartlink.merchant.service.MerchantService;

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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Merchant API Endpoints")
public class MerchantControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private MerchantService merchantService;

    @InjectMocks
    private MerchantController merchantController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(merchantController).build();
    }

    @Nested
    @DisplayName("POST /api/v1/merchants/signup")
    class MerchantSignUp {

        @Test
        @DisplayName("Should return 201 Created with merchant details and token on successful signup")
        void whenValidSignupRequest_thenReturns201AndMerchantDetails() throws Exception {
            SignUpRequest signUpRequest = new SignUpRequest("test@example.com", "password123", "Test", "User", "image_url.png");
            AuthResponse.MerchantDetailsDTO merchantDetailsDTO = new AuthResponse.MerchantDetailsDTO("test@example.com", "Test", "User", "image_url.png", null, null); // Assuming structure
            AuthResponse authResponse = new AuthResponse("generated-merchant-id", "jwt-token-123", merchantDetailsDTO);

            when(merchantService.signUp(any(SignUpRequest.class))).thenReturn(authResponse);

            mockMvc.perform(post("/api/v1/merchants/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signUpRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.merchantId").value("generated-merchant-id"))
                    .andExpect(jsonPath("$.token").value("jwt-token-123"))
                    .andExpect(jsonPath("$.merchantDetails.email").value("test@example.com"))
                    .andExpect(jsonPath("$.merchantDetails.firstName").value("Test"));
        }

        @Test
        @DisplayName("Should return 400 Bad Request for invalid signup data")
        void whenInvalidSignupRequest_thenReturns400() throws Exception {
            SignUpRequest signUpRequest = new SignUpRequest(null, "password123", "Test", "User", "image_url.png");
            when(merchantService.signUp(any(SignUpRequest.class))).thenThrow(new IllegalArgumentException("Invalid data"));

            mockMvc.perform(post("/api/v1/merchants/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signUpRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 500 Internal Server Error when signup fails due to server issue")
        void whenSignupFailsDueToServerIssue_thenReturns500() throws Exception {
            SignUpRequest signUpRequest = new SignUpRequest("test@example.com", "password123", "Test", "User", "image_url.png");
            when(merchantService.signUp(any(SignUpRequest.class))).thenThrow(new RuntimeException("Database error"));

            mockMvc.perform(post("/api/v1/merchants/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signUpRequest)))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/merchants/login")
    class MerchantLogin {
        @Test
        @DisplayName("Should return 200 OK with merchant details and token on successful login")
        void whenValidLoginRequest_thenReturns200AndToken() throws Exception {
            LoginRequest loginRequest = new LoginRequest("test@example.com", "password123");
            AuthResponse.MerchantDetailsDTO merchantDetailsDTO = new AuthResponse.MerchantDetailsDTO("test@example.com", "Test", "User", null, null, null);
            AuthResponse authResponse = new AuthResponse("existing-merchant-id", "new-jwt-token", merchantDetailsDTO);
            when(merchantService.login(any(LoginRequest.class))).thenReturn(authResponse);

            mockMvc.perform(post("/api/v1/merchants/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.merchantId").value("existing-merchant-id"))
                    .andExpect(jsonPath("$.token").value("new-jwt-token"));
        }

        @Test
        @DisplayName("Should return 401 Unauthorized for invalid credentials")
        void whenInvalidCredentials_thenReturns401() throws Exception {
            LoginRequest loginRequest = new LoginRequest("test@example.com", "wrongpassword");
            when(merchantService.login(any(LoginRequest.class))).thenThrow(new org.springframework.security.authentication.BadCredentialsException("Invalid credentials"));

            mockMvc.perform(post("/api/v1/merchants/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ... other @Nested classes for PasswordResetRequest, PasswordReset, RefreshToken, GetProfile, UpdateProfile ...
    // These will also need their DTO imports checked against dev.mhizterpaul.cartlink.merchant.dto or a common DTO package.

    // --- Notes on MerchantControllerTest ---
    // Assumptions:
    // 1. MerchantController and MerchantService exist in dev.mhizterpaul.cartlink.merchant.*
    // 2. DTOs like SignUpRequest, LoginRequest, AuthResponse are in dev.mhizterpaul.cartlink.merchant.dto.
    //    Other DTOs (MerchantProfileUpdateRequest, PasswordReset* etc.) are assumed or placeholders.
    // 3. JWT based authentication is handled.
    // Inadequacies & Edge Cases:
    // - Full BDT flow: signup -> login -> get profile -> update profile -> get profile (verify update).
    // - Detailed validation checks for each field in requests.
    // - Token expiration/renewal scenarios.
}
