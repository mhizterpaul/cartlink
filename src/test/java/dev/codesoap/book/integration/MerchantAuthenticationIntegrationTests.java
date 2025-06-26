package dev.codesoap.book.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.paul.cartlink.merchant.dto.LoginRequest;
import dev.paul.cartlink.merchant.dto.SignUpRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MerchantAuthenticationIntegrationTests extends BaseIntegrationTest {

    @Nested
    @DisplayName("POST /api/v1/merchants/signup")
    class MerchantSignupTests {

        @Test
        @DisplayName("Should return 201 Created for valid signup request")
        void shouldReturn201ForValidSignup() throws Exception {
            SignUpRequest signupRequest = new SignUpRequest(); // Ensured PascalCase for type
            signupRequest.setEmail("testmerchant@example.com");
            signupRequest.setPassword("password123");
            signupRequest.setFirstName("Test");
            signupRequest.setLastName("Merchant");
            signupRequest.setImage("image_url");

            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/merchants/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(signupRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.merchantId").exists())
                    .andExpect(jsonPath("$.token").exists())
                    .andExpect(jsonPath("$.merchantDetails.email").value("testmerchant@example.com"));
        }

        @Test
        @DisplayName("Should return 400 Bad Request for invalid signup request (e.g., missing email)")
        void shouldReturn400ForInvalidSignupMissingEmail() throws Exception {
            SignUpRequest signupRequest = new SignUpRequest(); // Ensured PascalCase for type
            // Missing email
            signupRequest.setPassword("password123");
            signupRequest.setFirstName("Test");
            signupRequest.setLastName("Merchant");

            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/merchants/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(signupRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 Bad Request for invalid signup request (e.g., invalid email format)")
        void shouldReturn400ForInvalidSignupInvalidEmail() throws Exception {
            SignUpRequest signupRequest = new SignUpRequest(); // Ensured PascalCase for type
            signupRequest.setEmail("invalid-email");
            signupRequest.setPassword("password123");
            signupRequest.setFirstName("Test");
            signupRequest.setLastName("Merchant");

            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/merchants/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(signupRequest)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/merchants/login")
    class MerchantLoginTests {

        @Test
        @DisplayName("Should return 200 OK and token for valid login")
        void shouldReturn200AndTokenForValidLogin() throws Exception {
            // First, sign up a merchant to ensure the user exists
            SignUpRequest signupRequest = new SignUpRequest(); // Ensuring correct casing
            signupRequest.setEmail("loginmerchant@example.com");
            signupRequest.setPassword("password123");
            signupRequest.setFirstName("Login");
            signupRequest.setLastName("Merchant");
            signupRequest.setImage("image_url");

            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/merchants/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(signupRequest)))
                    .andExpect(status().isCreated());

            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setEmail("loginmerchant@example.com");
            loginRequest.setPassword("password123");

            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/merchants/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.merchantId").exists())
                    .andExpect(jsonPath("$.token").exists())
                    .andExpect(jsonPath("$.merchantDetails.email").value("loginmerchant@example.com"));
        }

        @Test
        @DisplayName("Should return 401 Unauthorized for invalid credentials (wrong password)")
        void shouldReturn401ForInvalidPassword() throws Exception {
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setEmail("loginmerchant@example.com"); // User from previous test
            loginRequest.setPassword("wrongpassword");

            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/merchants/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 401 Unauthorized for non-existent user")
        void shouldReturn401ForNonExistentUser() throws Exception {
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setEmail("nonexistent@example.com");
            loginRequest.setPassword("password123");

            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/merchants/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isUnauthorized());
        }
    }

    // TODO: Add tests for Password Reset (POST /api/v1/merchants/password-reset-request and POST /api/v1/merchants/password-reset)
    // TODO: Add tests for Refresh Token (POST /api/v1/merchants/refresh-token)
    // TODO: Add tests for Get Profile (GET /api/v1/merchants/{merchantId}) - Requires authentication
    // TODO: Add tests for Update Profile (PUT /api/v1/merchants/{merchantId}) - Requires authentication


    @Nested
    @DisplayName("POST /api/v1/merchants/password-reset-request")
    class MerchantPasswordResetRequestTests {

        @Test
        @DisplayName("Should return 200 OK for valid password reset request")
        void shouldReturn200ForValidPasswordResetRequest() throws Exception {
            // Requires a merchant to exist
            SignUpRequest signupRequest = new SignUpRequest(); // Ensuring correct casing
            signupRequest.setEmail("resetrequest@example.com");
            signupRequest.setPassword("password123");
            signupRequest.setFirstName("Reset");
            signupRequest.setLastName("Request");
            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/merchants/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(signupRequest)))
                    .andExpect(status().isCreated());

            dev.paul.cartlink.merchant.dto.PasswordResetRequestRequest resetRequest = new dev.paul.cartlink.merchant.dto.PasswordResetRequestRequest();
            resetRequest.setEmail("resetrequest@example.com");

            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/merchants/password-reset-request")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(resetRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("Should return 404 Not Found for non-existent email")
        void shouldReturn404ForNonExistentEmail() throws Exception {
            dev.paul.cartlink.merchant.dto.PasswordResetRequestRequest resetRequest = new dev.paul.cartlink.merchant.dto.PasswordResetRequestRequest();
            resetRequest.setEmail("nonexistentemail@example.com");

            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/merchants/password-reset-request")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(resetRequest)))
                    .andExpect(status().isNotFound());
        }
    }

    // NOTE: Testing POST /api/v1/merchants/password-reset is more complex as it requires a valid resetToken.
    // This token is typically sent via email or another out-of-band mechanism in a real application.
    // For integration testing, this might require mocking the token generation/validation or having a way to retrieve it.
    // For now, I'll add a placeholder and may need to adjust based on how token generation is handled.

    @Nested
    @DisplayName("POST /api/v1/merchants/refresh-token")
    class MerchantRefreshTokenTests {
        // Testing refresh token also requires obtaining a valid refresh token first,
        // which is usually part of the login response.
        // I will assume the login response includes a refreshToken field.
        // This test will likely fail if 'refreshToken' is not part of the AuthResponse from login.

        @Test
        @DisplayName("Should return 200 OK and new token for valid refresh token")
        void shouldReturn200AndNewTokenForValidRefreshToken() throws Exception {
            // 1. Signup and Login to get a refresh token
            SignUpRequest signupRequest = new SignUpRequest(); // Ensuring correct casing
            signupRequest.setEmail("refreshtokentest@example.com");
            signupRequest.setPassword("password123");
            signupRequest.setFirstName("Refresh");
            signupRequest.setLastName("Token");
            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/merchants/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(signupRequest)))
                    .andExpect(status().isCreated());

            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setEmail("refreshtokentest@example.com");
            loginRequest.setPassword("password123");

            String loginResponse = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/merchants/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            // Assuming AuthResponse has getRefreshToken()
            // Adjust this based on the actual structure of AuthResponse
            String refreshToken = objectMapper.readTree(loginResponse).get("refreshToken").asText();


            dev.paul.cartlink.merchant.dto.RefreshTokenRequest refreshTokenRequest = new dev.paul.cartlink.merchant.dto.RefreshTokenRequest();
            refreshTokenRequest.setRefreshToken(refreshToken);

            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/merchants/refresh-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").exists());
        }
    }


    // For Get Profile and Update Profile, we need a valid token.
    // These tests will first log in, extract the token, and then use it.

    private String obtainAuthToken(String email, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);

        String responseString = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/merchants/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Assuming the token is directly in the response or within a sub-object like 'token' or 'accessToken'
        // And that AuthResponse from login contains a "token" field for the access token.
        return objectMapper.readTree(responseString).get("token").asText();
    }

     private String signUpAndReturnMerchantId(String email, String password, String firstName, String lastName) throws Exception {
        SignUpRequest signupRequest = new SignUpRequest(); // Ensuring correct casing
        signupRequest.setEmail(email);
        signupRequest.setPassword(password);
        signupRequest.setFirstName(firstName);
        signupRequest.setLastName(lastName);
        signupRequest.setImage("default_image.png");


        String responseString = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/merchants/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(responseString).get("merchantId").asText();
    }


    @Nested
    @DisplayName("GET /api/v1/merchants/{merchantId}")
    class GetMerchantProfileTests {

        @Test
        @DisplayName("Should return 200 OK and profile for authenticated merchant")
        void shouldReturn200AndProfileForAuthenticatedMerchant() throws Exception {
            String merchantEmail = "getprofile@example.com";
            String merchantPassword = "password123";
            String merchantId = signUpAndReturnMerchantId(merchantEmail, merchantPassword, "GetProfile", "User");
            String authToken = obtainAuthToken(merchantEmail, merchantPassword);

            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/merchants/" + merchantId)
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.merchantProfile.email").value(merchantEmail));
        }

        @Test
        @DisplayName("Should return 401 Unauthorized if no token is provided")
        void shouldReturn401WhenNoTokenProvided() throws Exception {
            String merchantEmail = "getprofileunauth@example.com";
            String merchantPassword = "password123";
            String merchantId = signUpAndReturnMerchantId(merchantEmail, merchantPassword, "GetProfileUnauth", "User");

            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/merchants/" + merchantId))
                    .andExpect(status().isUnauthorized());
        }

         @Test
        @DisplayName("Should return 401 Unauthorized if invalid token is provided")
        void shouldReturn401WhenInvalidTokenProvided() throws Exception {
            String merchantEmail = "getprofileinvalidtoken@example.com";
            String merchantPassword = "password123";
            String merchantId = signUpAndReturnMerchantId(merchantEmail, merchantPassword, "GetProfileInvalid", "User");

            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/merchants/" + merchantId)
                            .header("Authorization", "Bearer " + "invalidtoken"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/merchants/{merchantId}")
    class UpdateMerchantProfileTests {

        @Test
        @DisplayName("Should return 200 OK for successful profile update")
        void shouldReturn200ForSuccessfulProfileUpdate() throws Exception {
            String merchantEmail = "updateprofile@example.com";
            String merchantPassword = "password123";
            String merchantId = signUpAndReturnMerchantId(merchantEmail, merchantPassword, "UpdateProfile", "User");
            String authToken = obtainAuthToken(merchantEmail, merchantPassword);

            dev.paul.cartlink.merchant.dto.MerchantProfileUpdateRequest updateRequest = new dev.paul.cartlink.merchant.dto.MerchantProfileUpdateRequest();
            updateRequest.setFirstName("UpdatedFirstName");
            updateRequest.setLastName("UpdatedLastName");
            updateRequest.setPhoneNumber("+2348011111111");

            mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/merchants/" + merchantId)
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").exists());

            // Optionally, verify the update by fetching the profile again
            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/merchants/" + merchantId)
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.merchantProfile.firstName").value("UpdatedFirstName"))
                    .andExpect(jsonPath("$.merchantProfile.lastName").value("UpdatedLastName"));
        }

        @Test
        @DisplayName("Should return 401 Unauthorized if no token is provided")
        void shouldReturn401WhenNoTokenProvidedForUpdate() throws Exception {
             String merchantEmail = "updateprofileunauth@example.com";
            String merchantPassword = "password123";
            String merchantId = signUpAndReturnMerchantId(merchantEmail, merchantPassword, "UpdateProfileUnauth", "User");


            dev.paul.cartlink.merchant.dto.MerchantProfileUpdateRequest updateRequest = new dev.paul.cartlink.merchant.dto.MerchantProfileUpdateRequest();
            updateRequest.setFirstName("UpdatedFirstName");

            mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/merchants/" + merchantId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isUnauthorized());
        }

        // Consider adding tests for updating with invalid data (e.g., invalid phone number format)
        // if validation is in place.
    }
}
