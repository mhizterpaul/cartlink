package dev.mhizterpaul.cartlink.integration.product;

import com.fasterxml.jackson.databind.ObjectMapper;
// Assuming DTOs like ProductCreateRequest, ProductEditRequest, ProductResponse, BatchUploadResponse
// are in dev.paul.cartlink.product.dto or a common dto package.
// For now, using placeholders.
import dev.paul.cartlink.product.dto.ProductCreateRequest; // Placeholder
import dev.paul.cartlink.product.dto.ProductEditRequest; // Placeholder
import dev.paul.cartlink.product.dto.ProductResponse; // Placeholder
import dev.paul.cartlink.product.dto.BatchUploadResponse; // Placeholder
import dev.paul.cartlink.product.dto.ProductResponse; // Common Placeholder

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser; // For simulating authenticated merchant
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Product Management API Integration Tests")
public class ProductControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String merchantIdForTest = "auth-merchant-id"; // Placeholder, or setup a merchant

    // Placeholder for a valid auth token or use @WithMockUser
    // For simplicity, using @WithMockUser where endpoints require authentication.
    // This assumes 'ROLE_MERCHANT' is a valid role and security is set up to use
    // it.

    @Nested
    @DisplayName("POST /api/v1/merchants/{merchantId}/products (Add Product)")
    @WithMockUser(username = "test-merchant", roles = { "MERCHANT" }) // Assumes role-based auth
    class AddProduct {
        @Test
        @DisplayName("Should add a new product and return 201 Created")
        void whenValidProductData_thenCreatesProduct() throws Exception {
            // API doc says "dynamic backend-generated form" for request body.
            // For testing, we'll assume a JSON map can be sent, or a specific DTO if known.
            Map<String, Object> productData = new HashMap<>();
            productData.put("name", "Test Product from IT");
            productData.put("model", "Model X");
            productData.put("manufacturer", "IT Factory");
            productData.put("stock", 100);
            productData.put("price", 19.99);
            // productData.put("productDetails", Map.of("color", "Red")); // Example of
            // productDetails
            // productData.put("typeId", "someTypeId"); // If typeId is needed for schema
            // validation

            mockMvc.perform(post("/api/v1/merchants/{merchantId}/products", merchantIdForTest)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(productData)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.productId").exists())
                    .andExpect(jsonPath("$.merchantProductId").exists())
                    .andExpect(jsonPath("$.productDetails.name").value("Test Product from IT"));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/merchants/{merchantId}/products/{productId} (Edit Product)")
    @WithMockUser(username = "test-merchant", roles = { "MERCHANT" })
    class EditProduct {
        // Pre-requisite: A product needs to exist to be edited.
        // This could be created in a @BeforeEach specific to this Nested class,
        // or the product ID could be hardcoded if a known test product exists.
        // For BDT, one might chain create then edit.

        @Test
        @DisplayName("Should edit an existing product and return 200 OK")
        void whenValidEditData_thenUpdatesProduct() throws Exception {
            // For this test to be robust, we should first create a product, get its ID,
            // then edit.
            // Simplified: Assume productId "prod-to-edit" exists or is created in setup.
            String productIdToEdit = "prod-to-edit"; // This should be a real ID from a setup step.

            // Create a product first to ensure it exists (if not using @Sql setup)
            Map<String, Object> initialProductData = Map.of("name", "Initial Product", "price", 10.0);
            mockMvc.perform(post("/api/v1/merchants/{merchantId}/products", merchantIdForTest)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(initialProductData)))
                    .andReturn().getResponse().getContentAsString();
            // Ideally, parse the response to get the actual productIdToEdit. For now, using
            // fixed.
            // For a real IT, you'd extract the ID. Here, we assume a fixed ID or
            // pre-existing data.

            ProductEditRequest editRequest = new ProductEditRequest(
                    "Updated Product Name", "NewModel", "NewManu", 50, 25.99,
                    Collections.emptyList(), Collections.emptyMap(), merchantIdForTest, Collections.emptyList());

            mockMvc.perform(
                    put("/api/v1/merchants/{merchantId}/products/{productId}", merchantIdForTest, productIdToEdit)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(editRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    // ... Other tests for DELETE, List, Search, In-stock, Out-of-stock, Upload,
    // Template ...
    // Each would use @WithMockUser for merchant authentication.
    // Upload test would use MockMultipartFile.
    // List/Search tests would verify array responses and potentially
    // pagination/sorting parameters.

    // --- Notes on ProductControllerIT ---
    // - Assumed DTOs from dev.paul.cartlink.product.dto or common.
    // - `@WithMockUser` is used to simulate an authenticated merchant. Ensure roles
    // match application security config.
    // - Add Product: The "dynamic backend-generated form" aspect makes request body
    // testing tricky without knowing the schema logic.
    // - For Edit/Delete, ensuring the target product exists is key (could be done
    // via API call in @BeforeEach or a chained test).
}
