package dev.codesoap.book.integration;

import dev.paul.cartlink.merchant.dto.CouponCreateRequest;
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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.hasSize;


public class CouponManagementIntegrationTests extends BaseIntegrationTest {

    private String authToken;
    private String merchantId;
    private String productId;

    @BeforeEach
    void setUp() throws Exception {
        // Merchant Signup
        String email = "couponmerchant@example.com";
        String password = "password123";
        SignUpRequest signupRequest = new SignUpRequest();
        signupRequest.setEmail(email);
        signupRequest.setPassword(password);
        signupRequest.setFirstName("Coupon");
        signupRequest.setLastName("Master");
        signupRequest.setImage("coupon_img.png");

        MvcResult signUpResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/merchants/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated())
                .andReturn();
        merchantId = objectMapper.readTree(signUpResult.getResponse().getContentAsString()).get("merchantId").asText();

        // Merchant Login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);
        MvcResult loginResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/merchants/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        authToken = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();

        // Create a Product to associate coupons with
        Map<String, Object> productRequestMap = new HashMap<>();
        productRequestMap.put("name", "Couponable Product");
        productRequestMap.put("brand", "CouponBrand");
        productRequestMap.put("category", "CouponCategory");
        productRequestMap.put("description", "A product for testing coupons");
        productRequestMap.put("price", 100.00);
        productRequestMap.put("stock", 50);

        MvcResult productResult = mockMvc.perform(MockMvcRequestBuilders.post("/api/merchants/products") // Corrected endpoint
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequestMap)))
                .andExpect(status().isOk()) // MerchantProductController returns 200 OK
                .andReturn();
        // Assuming the response contains "productId" for the underlying Product's ID,
        // or "merchantProductId" if we need the MerchantProduct ID.
        // The Coupon endpoints in API_REQUIREMENTS.md use {productId} in path, suggesting the global Product ID.
        productId = objectMapper.readTree(productResult.getResponse().getContentAsString()).get("productId").asText();
    }

    @Nested
    @DisplayName("POST /api/v1/merchants/{merchantId}/products/{productId}/coupons")
    class CreateCouponTests {

        @Test
        @DisplayName("Should return 201 Created for valid coupon creation")
        void shouldReturn201ForValidCouponCreation() throws Exception {
            CouponCreateRequest couponRequest = new CouponCreateRequest();
            couponRequest.setDiscount(15.0); // 15% or 15 units, depends on interpretation
            couponRequest.setValidFrom(Instant.now().toString());
            couponRequest.setValidUntil(Instant.now().plus(30, ChronoUnit.DAYS).toString());
            couponRequest.setMaxUsage(100);
            couponRequest.setMaxUsers(50); // Assuming this means max distinct users that can use it.

            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/merchants/" + merchantId + "/products/" + productId + "/coupons")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(couponRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.couponId").exists());
        }

        @Test
        @DisplayName("Should return 400 Bad Request if discount is missing or invalid")
        void shouldReturn400ForInvalidDiscount() throws Exception {
            CouponCreateRequest couponRequest = new CouponCreateRequest();
            // Missing discount
            couponRequest.setValidFrom(Instant.now().toString());
            couponRequest.setValidUntil(Instant.now().plus(30, ChronoUnit.DAYS).toString());

            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/merchants/" + merchantId + "/products/" + productId + "/coupons")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(couponRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 404 Not Found if product does not exist")
        void shouldReturn404ForNonExistentProduct() throws Exception {
            CouponCreateRequest couponRequest = new CouponCreateRequest();
            couponRequest.setDiscount(10.0);
            couponRequest.setValidFrom(Instant.now().toString());
            couponRequest.setValidUntil(Instant.now().plus(30, ChronoUnit.DAYS).toString());

            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/merchants/" + merchantId + "/products/nonexistentProductIdForCoupon/coupons")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(couponRequest)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/merchants/{merchantId}/products/{productId}/coupons")
    class GetCouponsForProductTests {

        @BeforeEach
        void createACoupon() throws Exception {
            CouponCreateRequest couponRequest = new CouponCreateRequest();
            couponRequest.setDiscount(20.0);
            couponRequest.setValidFrom(Instant.now().toString());
            couponRequest.setValidUntil(Instant.now().plus(10, ChronoUnit.DAYS).toString());
            couponRequest.setMaxUsage(50);
            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/merchants/" + merchantId + "/products/" + productId + "/coupons")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(couponRequest)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("Should return 200 OK and a list of coupons for the product")
        void shouldReturn200AndListOfCoupons() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/merchants/" + merchantId + "/products/" + productId + "/coupons")
                            .header("Authorization", "Bearer " + authToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].couponId").exists())
                    .andExpect(jsonPath("$[0].discount").value(20.0));
        }

        @Test
        @DisplayName("Should return 404 Not Found if product does not exist")
        void shouldReturn404ForNonExistentProduct() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/merchants/" + merchantId + "/products/nonexistentProductIdForCouponList/coupons")
                            .header("Authorization", "Bearer " + authToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/merchants/{merchantId}/products/coupons/{couponId}")
    class DeleteCouponTests {
        private String couponIdToDelete;

        @BeforeEach
        void createCouponForDeletion() throws Exception {
            CouponCreateRequest couponRequest = new CouponCreateRequest();
            couponRequest.setDiscount(5.0);
            couponRequest.setValidFrom(Instant.now().toString());
            couponRequest.setValidUntil(Instant.now().plus(5, ChronoUnit.DAYS).toString());
            couponRequest.setMaxUsage(10);

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/merchants/" + merchantId + "/products/" + productId + "/coupons")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(couponRequest)))
                    .andExpect(status().isCreated())
                    .andReturn();
            couponIdToDelete = objectMapper.readTree(result.getResponse().getContentAsString()).get("couponId").asText();
        }

        @Test
        @DisplayName("Should return 200 OK for successful coupon deletion")
        void shouldReturn200ForSuccessfulDeletion() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/merchants/" + merchantId + "/products/coupons/" + couponIdToDelete)
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("Should return 404 Not Found if coupon does not exist")
        void shouldReturn404ForNonExistentCoupon() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/merchants/" + merchantId + "/products/coupons/nonexistentCouponId999")
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isNotFound());
        }
    }
}
