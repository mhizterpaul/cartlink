package dev.mhizterpaul.cartlink.integration.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import dev.paul.cartlink.product.dto.ProductResponse; // Corrected import
import dev.paul.cartlink.merchant.controller.CouponController;
import dev.paul.cartlink.merchant.dto.CouponCreateRequest;
import dev.paul.cartlink.merchant.dto.CouponDetailsResponse;
import dev.paul.cartlink.merchant.dto.CouponIdResponse;
import dev.paul.cartlink.merchant.model.Coupon;
// For test data setup
import dev.paul.cartlink.merchant.model.Merchant;
import dev.paul.cartlink.merchant.repository.CouponRepository;
import dev.paul.cartlink.merchant.repository.MerchantRepository;
import dev.paul.cartlink.product.model.Product;
import dev.paul.cartlink.product.repository.ProductRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Coupon Management API Integration Tests")
public class CouponControllerIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper; // Already configured with JavaTimeModule by Spring Boot

    @Autowired
    private MerchantRepository merchantRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CouponRepository couponRepository; // Assuming this exists

    private Long testMerchantId;
    private Long testProductId;
    private Long testCouponId;

    @BeforeEach
    void setUpTestData() {
        objectMapper.registerModule(new JavaTimeModule()); // Ensure for local objectMapper if not relying on Spring's
                                                           // global one

        Merchant merchant = new Merchant("coupon-merchant" + System.currentTimeMillis() + "@example.com", "Pass",
                "CouponM", "LTD");
        Merchant savedMerchant = merchantRepository.save(merchant);
        testMerchantId = savedMerchant.getId();

        Product product = new Product();
        product.setName("Coupon Product");
        product.setPrice(100.00);
        product.setStock(10);
        product.setMerchant(savedMerchant);
        Product savedProduct = productRepository.save(product);
        testProductId = savedProduct.getId();

        // Create a sample coupon for GET/DELETE tests
        Coupon coupon = new Coupon();
        coupon.setProduct(savedProduct);
        coupon.setMerchant(savedMerchant);
        coupon.setDiscount(10.0); // Assuming discount is BigDecimal
        coupon.setValidFrom(LocalDateTime.now().minusDays(1).toInstant(ZoneOffset.UTC));
        coupon.setValidUntil(LocalDateTime.now().plusDays(10).toInstant(ZoneOffset.UTC));
        coupon.setMaxUsage(100);
        // set other mandatory coupon fields
        Coupon savedCoupon = couponRepository.save(coupon);
        testCouponId = savedCoupon.getId();
    }

    @Nested
    @DisplayName("POST /api/v1/merchants/{merchantId}/products/{productId}/coupons")
    @WithMockUser(username = "test-coupon-merchant", roles = { "MERCHANT" })
    class CreateCoupon {
        @Test
        @DisplayName("Should create a new coupon for a product and return 201 Created")
        void whenValidCouponData_thenCreatesCoupon() throws Exception {
            CouponCreateRequest request = new CouponCreateRequest(
                    15.0,
                    "2024-09-01T00:00:00Z",
                    "2024-09-30T23:59:59Z",
                    200,
                    50);

            mockMvc.perform(
                    post("/api/v1/merchants/{merchantId}/products/{productId}/coupons", testMerchantId, testProductId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.couponId").exists());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/merchants/{merchantId}/products/{productId}/coupons")
    @WithMockUser(username = "test-coupon-merchant", roles = { "MERCHANT" })
    class GetCouponsForProduct {
        @Test
        @DisplayName("Should retrieve coupons for a specific product")
        void whenProductHasCoupons_thenReturnsCouponList() throws Exception {
            mockMvc.perform(
                    get("/api/v1/merchants/{merchantId}/products/{productId}/coupons", testMerchantId, testProductId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].couponId").value(testCouponId)); // Assuming the pre-created coupon is
                                                                               // listed
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/merchants/{merchantId}/products/coupons/{couponId}")
    @WithMockUser(username = "test-coupon-merchant", roles = { "MERCHANT" })
    class DeleteCoupon {
        @Test
        @DisplayName("Should delete an existing coupon and return 200 OK")
        void whenCouponExists_thenDeletesCoupon() throws Exception {
            mockMvc.perform(
                    delete("/api/v1/merchants/{merchantId}/products/coupons/{couponId}", testMerchantId, testCouponId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    // --- Notes on CouponControllerIT ---
    // - Assumes CouponController, CouponService, Coupon model, CouponRepository,
    // and DTOs exist.
    // These are placeholders as they were not found in the initial 'ls'.
    // - Test data setup includes creating a Merchant, Product, and a Coupon.
    // - Uses @WithMockUser for merchant authentication.
}
