package dev.codesoap.book.integration;

import dev.paul.cartlink.merchant.dto.LoginRequest;
import dev.paul.cartlink.merchant.dto.SignUpRequest;
// Assuming a DTO for form generation request, let's define a simple Map for now or create a DTO if specific fields are confirmed.
// For now, using a Map<String, String> for the request body.
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.containsString;


public class ProductFormIntegrationTests extends BaseIntegrationTest {

    private String authToken;
    private String merchantId;

    @BeforeEach
    void setUp() throws Exception {
        String email = "productformmerchant@example.com";
        String password = "password123";

        SignUpRequest signupRequest = new SignUpRequest();
        signupRequest.setEmail(email);
        signupRequest.setPassword(password);
        signupRequest.setFirstName("Form");
        signupRequest.setLastName("Merchant");
        signupRequest.setImage("form_img.png");

        String signUpResponse = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/merchants/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        merchantId = objectMapper.readTree(signUpResponse).get("merchantId").asText();

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);

        String loginResponse = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/merchants/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        authToken = objectMapper.readTree(loginResponse).get("token").asText();
    }

    @Nested
    @DisplayName("POST /api/v1/merchants/{merchantId}/products/form")
    class GenerateProductFormTests {

        @Test
        @DisplayName("Should return 200 OK and HTML form for valid category and product type")
        void shouldReturn200AndHtmlForm() throws Exception {
            Map<String, String> formRequest = new HashMap<>();
            formRequest.put("category", "Electronics");
            formRequest.put("productType", "Smartphone");
            formRequest.put("brand", "TechBrand");
            formRequest.put("name", "SuperPhone X");
            formRequest.put("description", "Latest model smartphone");

            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/merchants/" + merchantId + "/products/form")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(formRequest)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                    .andExpect(content().string(containsString("<form")))
                    .andExpect(content().string(containsString("SuperPhone X"))); // Check if product name is reflected
        }

        @Test
        @DisplayName("Should return 400 Bad Request for invalid or unsupported category")
        void shouldReturn400ForInvalidCategory() throws Exception {
            Map<String, String> formRequest = new HashMap<>();
            formRequest.put("category", "UnsupportedCategory"); // Invalid category
            formRequest.put("productType", "Gadget");
            formRequest.put("brand", "Generic");
            formRequest.put("name", "Some Gadget");
            formRequest.put("description", "A generic gadget");

            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/merchants/" + merchantId + "/products/form")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(formRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 401 Unauthorized if token is missing")
        void shouldReturn401ForMissingToken() throws Exception {
            Map<String, String> formRequest = new HashMap<>();
            formRequest.put("category", "Electronics");
            formRequest.put("productType", "Laptop");

            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/merchants/" + merchantId + "/products/form")
                            // No Authorization header
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(formRequest)))
                    .andExpect(status().isUnauthorized());
        }
    }
}
