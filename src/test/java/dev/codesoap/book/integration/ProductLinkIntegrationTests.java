package dev.codesoap.book.integration;

import dev.paul.cartlink.merchant.dto.LoginRequest;
import dev.paul.cartlink.merchant.dto.SignUpRequest;
import dev.paul.cartlink.product.dto.ProductCreateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ProductLinkIntegrationTests extends BaseIntegrationTest {

    private String authToken;
    private String merchantId;
    private String productId;

    @BeforeEach
    void setUp() throws Exception {
        // Merchant Signup
        String email = "productlinkmerchant@example.com";
        String password = "password123";
        SignUpRequest signupRequest = new SignUpRequest();
        signupRequest.setEmail(email);
        signupRequest.setPassword(password);
        signupRequest.setFirstName("Link");
        signupRequest.setLastName("Merchant");
        signupRequest.setImage("link_img.png");

        MvcResult signUpResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/merchants/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        String signUpResponse = signUpResult.getResponse().getContentAsString();
        merchantId = objectMapper.readTree(signUpResponse).get("merchantId").asText();

        // Merchant Login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);
        MvcResult loginResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/merchants/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        String loginResponse = loginResult.getResponse().getContentAsString();
        authToken = objectMapper.readTree(loginResponse).get("token").asText();

        // Create a Product for link generation
        Map<String, Object> productRequestMap = new HashMap<>();
        productRequestMap.put("name", "Linkable Product");
        productRequestMap.put("brand", "LinkBrand");
        productRequestMap.put("category", "LinkCategory");
        productRequestMap.put("description", "A product to generate links for");
        productRequestMap.put("price", 49.99);
        productRequestMap.put("stock", 20);

        MvcResult productResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/merchants/products") // Corrected endpoint
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequestMap)))
                .andExpect(status().isOk()) // MerchantProductController returns 200 OK
                .andReturn();
        String productResponse = productResult.getResponse().getContentAsString();
        // ProductLink endpoints use {productId} which refers to the global Product's ID
        productId = objectMapper.readTree(productResponse).get("productId").asText();
    }

    @Nested
    @DisplayName("POST /api/v1/merchants/{merchantId}/products/{productId}/generate-link")
    class GenerateLinkTests {

        @Test
        @DisplayName("Should return 201 Created with linkId and URL for a valid product")
        void shouldReturn201WithLinkDetails() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/merchants/" + merchantId + "/products/" + productId + "/generate-link")
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.linkId").exists())
                    .andExpect(jsonPath("$.url").exists());
        }

        @Test
        @DisplayName("Should return 404 Not Found if product does not exist")
        void shouldReturn404ForNonExistentProduct() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/merchants/" + merchantId + "/products/nonexistentproductid/generate-link")
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isNotFound()); // Or appropriate error for non-existent product
        }

        @Test
        @DisplayName("Should return 401 Unauthorized if token is missing")
        void shouldReturn401ForMissingToken() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/merchants/" + merchantId + "/products/" + productId + "/generate-link"))
                    // No Authorization header
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/merchants/{merchantId}/products/links")
    class GetAllLinksTests {

        @BeforeEach
        void generateALink() throws Exception {
            // Ensure at least one link exists
            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/merchants/" + merchantId + "/products/" + productId + "/generate-link")
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("Should return 200 OK and a list of links")
        void shouldReturn200AndListOfLinks() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/merchants/" + merchantId + "/products/links?page=1&limit=10")
                            .header("Authorization", "Bearer " + authToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].linkId").exists())
                    .andExpect(jsonPath("$[0].productId").value(productId));
        }

        @Test
        @DisplayName("Should return 401 Unauthorized if token is missing")
        void shouldReturn401ForMissingToken() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/merchants/" + merchantId + "/products/links")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/merchants/{merchantId}/products/links/{linkId}/analytics")
    class GetLinkAnalyticsTests {
        private String linkId;

        @BeforeEach
        void generateLinkAndGetId() throws Exception {
            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/merchants/" + merchantId + "/products/" + productId + "/generate-link")
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isCreated())
                    .andReturn();
            String response = result.getResponse().getContentAsString();
            linkId = objectMapper.readTree(response).get("linkId").asText();
        }

        @Test
        @DisplayName("Should return 200 OK and analytics data for a valid link")
        void shouldReturn200AndAnalyticsData() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/merchants/" + merchantId + "/products/links/" + linkId + "/analytics?startDate=2023-01-01&endDate=2023-12-31")
                            .header("Authorization", "Bearer " + authToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalClicks").isNumber()) // Basic check for analytics structure
                    .andExpect(jsonPath("$.bounceRate").isNumber());
        }

        @Test
        @DisplayName("Should return 404 Not Found if link does not exist")
        void shouldReturn404ForNonExistentLink() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/merchants/" + merchantId + "/products/links/nonexistentlinkid/analytics")
                            .header("Authorization", "Bearer " + authToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/merchants/{merchantId}/products/links/{linkId}/traffic")
    class GetLinkTrafficSourcesTests {
        private String linkId;

        @BeforeEach
        void generateLinkAndGetId() throws Exception {
            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/merchants/" + merchantId + "/products/" + productId + "/generate-link")
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isCreated())
                    .andReturn();
            String response = result.getResponse().getContentAsString();
            linkId = objectMapper.readTree(response).get("linkId").asText();
        }

        @Test
        @DisplayName("Should return 200 OK and traffic source data")
        void shouldReturn200AndTrafficData() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/merchants/" + merchantId + "/products/links/" + linkId + "/traffic")
                            .header("Authorization", "Bearer " + authToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
            // Further checks can be added if specific traffic data is seeded/mocked
            // e.g., .andExpect(jsonPath("$[0].source").exists());
        }

        @Test
        @DisplayName("Should return 404 Not Found if link does not exist for traffic")
        void shouldReturn404ForNonExistentLinkTraffic() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/merchants/" + merchantId + "/products/links/nonexistentlinkid/traffic")
                            .header("Authorization", "Bearer " + authToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }
}
