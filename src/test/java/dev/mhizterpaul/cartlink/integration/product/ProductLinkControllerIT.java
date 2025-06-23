package dev.mhizterpaul.cartlink.integration.product;

import com.fasterxml.jackson.databind.ObjectMapper;
// Assuming DTOs like GeneratedLinkResponse are in dev.paul.cartlink.product.dto or common
import dev.paul.cartlink.product.dto.GeneratedLinkResponse; // Placeholder

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

// For BDT setup: Potentially create a product first to generate a link for it
import dev.paul.cartlink.product.model.Product; // Actual model
import dev.paul.cartlink.product.repository.ProductRepository; // Actual repository
import dev.paul.cartlink.merchant.model.Merchant; // Actual model
import dev.paul.cartlink.merchant.repository.MerchantRepository; // Actual repository

import java.util.Map;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Product Link Controller Integration Tests (Generation & Listing)")
public class ProductLinkControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository; // To help setup test data

    @Autowired
    private MerchantRepository merchantRepository; // To help setup test data

    private String testMerchantId;
    private String testProductId;

    @BeforeEach
    void setUpTestData() {
        // Create a dummy merchant for association (if your Product model requires it)
        Merchant merchant = new Merchant();
        merchant.setEmail("linktestmerchant" + System.currentTimeMillis() + "@example.com");
        merchant.setPassword("password"); // Ensure this meets any validation rules
        merchant.setFirstName("LinkTest");
        merchant.setLastName("Merchant");
        Merchant savedMerchant = merchantRepository.save(merchant);
        testMerchantId = savedMerchant.getId();

        // Create a product to generate a link for
        Product product = new Product();
        product.setName("Linkable Product");
        product.setPrice(java.math.BigDecimal.valueOf(29.99));
        product.setStock(100);
        product.setMerchant(savedMerchant);
        // Set other mandatory fields for Product if any
        Product savedProduct = productRepository.save(product);
        testProductId = savedProduct.getId();
    }

    @Nested
    @DisplayName("POST /api/v1/merchants/{merchantId}/products/{productId}/generate-link")
    @WithMockUser(username = "test-merchant", roles = {"MERCHANT"})
    class GenerateLink {
        @Test
        @DisplayName("Should generate a new link for an existing product and return 201 Created")
        void whenProductExists_thenGeneratesLink() throws Exception {
            mockMvc.perform(post("/api/v1/merchants/{merchantId}/products/{productId}/generate-link", testMerchantId, testProductId))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.linkId").exists())
                    .andExpect(jsonPath("$.url").exists());
        }

        @Test
        @DisplayName("Should return 404 Not Found when trying to generate link for non-existent product")
        void whenProductNotFound_thenReturns404() throws Exception {
            mockMvc.perform(post("/api/v1/merchants/{merchantId}/products/{productId}/generate-link", testMerchantId, "non-existent-product-id"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/merchants/{merchantId}/products/links")
    @WithMockUser(username = "test-merchant", roles = {"MERCHANT"})
    class GetAllLinks {
        @Test
        @DisplayName("Should return 200 OK with a list of links for the merchant")
        void whenLinksExist_thenReturnsLinkList() throws Exception {
            // First, generate a link to ensure there's something to list
            mockMvc.perform(post("/api/v1/merchants/{merchantId}/products/{productId}/generate-link", testMerchantId, testProductId))
                    .andExpect(status().isCreated());

            mockMvc.perform(get("/api/v1/merchants/{merchantId}/products/links", testMerchantId)
                    .param("page", "0") // Spring Data JPA pagination is often 0-indexed
                    .param("limit", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].productId").value(testProductId)); // Verify one of the fields
        }
    }

    // --- Notes on ProductLinkControllerIT ---
    // - Targets dev.paul.cartlink.product.controller.ProductLinkController.
    // - Uses actual Product and Merchant repositories for test data setup.
    // - GeneratedLinkResponse DTO is a placeholder.
    // - Assumes merchant authentication is handled by @WithMockUser.
}
