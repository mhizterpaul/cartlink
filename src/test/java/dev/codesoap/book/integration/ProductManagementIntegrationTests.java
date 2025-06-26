package dev.codesoap.book.integration;

import dev.paul.cartlink.merchant.dto.LoginRequest;
import dev.paul.cartlink.merchant.dto.SignUpRequest;
// ProductCreateRequest is not suitable for direct product addition via MerchantProductController.
// ProductEditRequest defines fields that might be used in the Map<String, Object> for updates.
import dev.paul.cartlink.product.dto.ProductEditRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.hamcrest.Matchers.hasSize; // Added for hasSize matcher
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ProductManagementIntegrationTests extends BaseIntegrationTest {

    private String authToken;
    private String merchantId;

    @BeforeEach
    void setUp() throws Exception {
        // Create and login a merchant for these tests
        String email = "productmerchant@example.com";
        String password = "password123";

        SignUpRequest signupRequest = new SignUpRequest();
        signupRequest.setEmail(email);
        signupRequest.setPassword(password);
        signupRequest.setFirstName("Product");
        signupRequest.setLastName("Merchant");
        signupRequest.setImage("img.png");

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
    @DisplayName("POST /api/merchants/products") // Corrected path, merchantId from auth
    class AddProductTests {

        @Test
        @DisplayName("Should return 200 OK for valid product addition") // MerchantProductController returns 200 OK
        void shouldReturn200ForValidProductAddition() throws Exception {
            Map<String, Object> productRequest = new HashMap<>();
            productRequest.put("name", "Test Product");
            productRequest.put("brand", "TestBrand");
            productRequest.put("category", "Electronics");
            productRequest.put("description", "A great test product");
            productRequest.put("price", 19.99);
            productRequest.put("stock", 100);
            // Add other fields as expected by MerchantProductService.addMerchantProduct

            mockMvc.perform(MockMvcRequestBuilders.post("/api/merchants/products") // Corrected path
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(productRequest)))
                    .andExpect(status().isOk()) // MerchantProductController.addMerchantProduct returns 200 OK
                    .andExpect(jsonPath("$.productId").exists())
                    .andExpect(jsonPath("$.merchantProductId").exists())
                    .andExpect(jsonPath("$.message").value("Merchant product added successfully"));
        }

        @Test
        @DisplayName("Should return 401 Unauthorized if token is invalid or missing")
        void shouldReturn401ForUnauthorized() throws Exception {
            Map<String, Object> productRequest = new HashMap<>();
            productRequest.put("name", "Unauthorized Product");
            productRequest.put("price", 10.00);
            productRequest.put("stock", 10);

            mockMvc.perform(MockMvcRequestBuilders.post("/api/merchants/products") // Corrected path
                            // No Authorization header
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(productRequest)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/merchants/{merchantId}/products/{productId}")
    class EditProductTests {

        private String productId;

        @BeforeEach
        void createProductForEditing() throws Exception {
            Map<String, Object> createRequestMap = new HashMap<>();
            createRequestMap.put("name", "Editable Product");
            createRequestMap.put("brand", "EditBrand");
            createRequestMap.put("category", "Editing");
            createRequestMap.put("description", "Initial description for editing");
            createRequestMap.put("price", 29.99);
            createRequestMap.put("stock", 50);

            String response = mockMvc.perform(MockMvcRequestBuilders.post("/api/merchants/products")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequestMap)))
                    .andExpect(status().isOk()) // MerchantProductController returns 200 OK for add
                    .andReturn().getResponse().getContentAsString();
            // Assuming the response from addMerchantProduct contains "merchantProductId" for the created MerchantProduct's ID
            productId = objectMapper.readTree(response).get("merchantProductId").asText();
        }

        @Test
        @DisplayName("Should return 200 OK for valid product edit")
        void shouldReturn200ForValidProductEdit() throws Exception {
            Map<String, Object> productEditMap = new HashMap<>();
            productEditMap.put("name", "Updated Test Product");
            productEditMap.put("price", 25.99);
            productEditMap.put("stock", 90);
            productEditMap.put("model", "ModelX"); // Corresponds to ProductEditRequest fields
            productEditMap.put("manufacturer", "ManuCorp");
            productEditMap.put("description", "Updated product description"); // Assuming description can be updated
            // productEditMap.put("specifications", Map.of("color", "blue")); // Example for specifications

            // The endpoint is PUT /api/merchants/products/{merchantProductId}
            mockMvc.perform(MockMvcRequestBuilders.put("/api/merchants/products/" + productId)
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(productEditMap)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("Should return 404 Not Found for non-existent product")
        void shouldReturn404ForNonExistentProduct() throws Exception {
            ProductEditRequest productEditRequest = new ProductEditRequest();
            productEditRequest.setName("Non-Existent Product Update");

            mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/merchants/" + merchantId + "/products/nonexistentproductid")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(productEditRequest)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/merchants/{merchantId}/products/{productId}")
    class DeleteProductTests {
        private String productIdToDelete;

        @BeforeEach
        void createProductForDeletion() throws Exception {
            Map<String, Object> createRequestMap = new HashMap<>();
            createRequestMap.put("name", "Deletable Product");
            createRequestMap.put("brand", "DeleteBrand");
            createRequestMap.put("category", "Deleting");
            createRequestMap.put("description", "To be deleted");
            createRequestMap.put("price", 9.99);
            createRequestMap.put("stock", 5);

            String response = mockMvc.perform(MockMvcRequestBuilders.post("/api/merchants/products")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequestMap)))
                    .andExpect(status().isOk()) // MerchantProductController returns 200 OK for add
                    .andReturn().getResponse().getContentAsString();
            // Assuming the response from addMerchantProduct contains "merchantProductId"
            productIdToDelete = objectMapper.readTree(response).get("merchantProductId").asText();
        }

        @Test
        @DisplayName("Should return 200 OK for successful product deletion")
        void shouldReturn200ForSuccessfulDeletion() throws Exception {
            // Endpoint is DELETE /api/merchants/products/{merchantProductId}
            mockMvc.perform(MockMvcRequestBuilders.delete("/api/merchants/products/" + productIdToDelete)
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isOk())
                    // MerchantProductController delete returns: Map.of("message", "Merchant product deleted successfully")
                    .andExpect(jsonPath("$.message").value("Merchant product deleted successfully"));
        }

        @Test
        @DisplayName("Should return 404 Not Found when trying to delete a non-existent product")
        void shouldReturn404ForDeletingNonExistentProduct() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.delete("/api/merchants/products/nonexistentproductidfordelete")
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isNotFound()); // Or 400 if controller handles it that way before service
        }
    }

    @Nested
    @DisplayName("GET /api/v1/merchants/{merchantId}/products")
    class ListProductsTests {

        @Test
        @DisplayName("Should return 200 OK and a list of products")
        void shouldReturn200AndListOfProducts() throws Exception {
            // Add a product first to ensure the list is not empty
            Map<String, Object> productRequestMap = new HashMap<>();
            productRequestMap.put("name", "Listable Product");
            productRequestMap.put("brand", "ListBrand");
            productRequestMap.put("category", "Listing");
            productRequestMap.put("description", "A product for listing");
            productRequestMap.put("price", 1.99);
            productRequestMap.put("stock", 10);

            mockMvc.perform(MockMvcRequestBuilders.post("/api/merchants/products")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(productRequestMap)))
                    .andExpect(status().isOk()); // MerchantProductController returns 200 OK

            // Endpoint is GET /api/merchants/products (merchant from auth)
            // API_REQUIREMENTS.md shows /api/v1/merchants/{merchantId}/products?page=1&limit=20&sort=price&order=asc
            // Assuming the /api/merchants/products (without merchantId in path) also supports pagination/sorting
            mockMvc.perform(MockMvcRequestBuilders.get("/api/merchants/products?page=1&limit=10")
                            .header("Authorization", "Bearer " + authToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].product.name").value("Listable Product")); // Response is List<MerchantProduct>, so product details are nested
        }

        @Test
        @DisplayName("Should handle sorting and pagination parameters")
        void shouldHandleSortingAndPagination() throws Exception {
            // This test assumes the /api/merchants/products endpoint supports these params.
            mockMvc.perform(MockMvcRequestBuilders.get("/api/merchants/products?page=1&limit=5&sort=price&order=asc")
                            .header("Authorization", "Bearer " + authToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
            // Further checks could verify sorting order if multiple products with different prices are seeded.
        }
    }

    // TODO: Add tests for Search Products (GET /api/v1/merchants/{merchantId}/products/search?query=...)
    // TODO: Add tests for Get In-stock Products (GET /api/v1/merchants/{merchantId}/products/in-stock)
    // TODO: Add tests for Get Out-of-stock Products (GET /api/v1/merchants/{merchantId}/products/out-of-stock)
    // TODO: Add tests for Batch Upload Products (POST /api/v1/merchants/{merchantId}/products/upload) - Requires file upload handling
    // TODO: Add tests for Download Product Catalogue (GET /api/v1/merchants/{merchantId}/products/template) - Requires file download check

    @Nested
    @DisplayName("GET /api/merchants/products/search (Extra Endpoint)")
    // API_REQUIREMENTS.md lists this under "Merchant Product Endpoints (Extra)" without {merchantId}
    // but also under "Product Management" as /api/v1/merchants/{merchantId}/products/search.
    // Assuming the one with {merchantId} is the primary one if they conflict.
    // For now, testing the one from "Extra" section: /api/merchants/products/search?query=...
    // This implies it might be a global search or merchant is inferred from auth.
    class SearchMerchantProductsExtraTests {

        @BeforeEach
        void addSearchableProduct() throws Exception {
            Map<String, Object> productMap = new HashMap<>();
            productMap.put("name", "UniqueSearchableProductName");
            productMap.put("brand", "SearchBrand");
            productMap.put("category", "SearchCategory");
            productMap.put("description", "A very unique product for searching.");
            productMap.put("price", 99.99);
            productMap.put("stock", 5);

            mockMvc.perform(MockMvcRequestBuilders.post("/api/merchants/products") // Use correct endpoint
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(productMap)))
                    .andExpect(status().isOk()); // Corrected expected status
        }

        @Test
        @DisplayName("Should return 200 OK and list of products matching query")
        void shouldReturn200AndMatchingProducts() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get("/api/merchants/products/search?query=UniqueSearchableProductName")
                            .header("Authorization", "Bearer " + authToken) // Assuming merchant auth is still needed
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].name").value("UniqueSearchableProductName"));
        }

        @Test
        @DisplayName("Should return empty list if no products match query")
        void shouldReturnEmptyListForNoMatch() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get("/api/merchants/products/search?query=NonExistentNameQueryXYZ")
                            .header("Authorization", "Bearer " + authToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }


    @Nested
    @DisplayName("GET /api/merchants/products/in-stock (Extra Endpoint)")
    // Similar to search, this is listed under "Extra" without {merchantId}.
    // Assuming merchant context is from auth.
    class InStockMerchantProductsExtraTests {

         @BeforeEach
        void addInStockProduct() throws Exception {
            Map<String, Object> productMap = new HashMap<>();
            productMap.put("name", "AlwaysInStockProduct");
            productMap.put("brand", "StockBrand");
            productMap.put("category", "StockCategory");
            productMap.put("description", "This product is in stock.");
            productMap.put("price", 10.00);
            productMap.put("stock", 100); // Definitely in stock

            mockMvc.perform(MockMvcRequestBuilders.post("/api/merchants/products")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(productMap)))
                    .andExpect(status().isOk()); // Corrected expected status
        }

        @Test
        @DisplayName("Should return 200 OK and list of in-stock products")
        void shouldReturn200AndInStockProducts() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get("/api/merchants/products/in-stock")
                            .header("Authorization", "Bearer " + authToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].name").value("AlwaysInStockProduct"))
                    .andExpect(jsonPath("$[0].stock").value(100));
        }
    }

    // POST /api/merchants/products/batch-upload for "Merchant Product Endpoints (Extra)"
    // is similar to /api/v1/merchants/{merchantId}/products/upload (CSV/Excel).
    // Testing file uploads requires MockMultipartHttpServletRequestBuilder.
    // Will add a placeholder TODO for this as it's more involved.
    // TODO: Test POST /api/merchants/products/batch-upload (list of product objects)
    // TODO: Test POST /api/v1/merchants/{merchantId}/products/upload (CSV/Excel file upload)
    // TODO: Test GET /api/v1/merchants/{merchantId}/products/template (Download Product Catalogue)


    // Original Product Management Endpoints (if different from "Extra")
    // These were covered by earlier tests if paths are equivalent.
    // GET /api/v1/merchants/{merchantId}/products/search?query=... (already have similar above)
    // GET /api/v1/merchants/{merchantId}/products/in-stock
    // GET /api/v1/merchants/{merchantId}/products/out-of-stock

    @Nested
    @DisplayName("GET /api/merchants/products/in-stock (from MerchantProductController)")
    class GetInStockProductsTests {
         @BeforeEach
        void ensureProducts() throws Exception {
            Map<String, Object> inStockMap = new HashMap<>();
            inStockMap.put("name", "StandardInStock");
            inStockMap.put("brand", "InStockBrand");
            inStockMap.put("category", "InStockCat");
            inStockMap.put("description", "This is in stock");
            inStockMap.put("price", 5.0);
            inStockMap.put("stock", 10);
            mockMvc.perform(MockMvcRequestBuilders.post("/api/merchants/products")
                            .header("Authorization", "Bearer " + authToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(inStockMap)))
                    .andExpect(status().isOk());

            Map<String, Object> outOfStockMap = new HashMap<>();
            outOfStockMap.put("name", "StandardOutOfStock");
            outOfStockMap.put("brand", "OutOfStockBrand");
            outOfStockMap.put("category", "OutOfStockCat");
            outOfStockMap.put("description", "This is out of stock");
            outOfStockMap.put("price", 15.0);
            outOfStockMap.put("stock", 0);
            mockMvc.perform(MockMvcRequestBuilders.post("/api/merchants/products")
                            .header("Authorization", "Bearer " + authToken).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(outOfStockMap)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should return 200 OK and list of in-stock products for the merchant")
        void shouldReturnInStockProducts() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get("/api/merchants/products/in-stock") // Path from MerchantProductController
                            .header("Authorization", "Bearer " + authToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    // Check that the response contains the in-stock product and not the out-of-stock one.
                    // The actual product structure in response is MerchantProduct, which has a nested Product object.
                    .andExpect(jsonPath("$[?(@.product.name == 'StandardInStock')]", hasSize(1)))
                    .andExpect(jsonPath("$[?(@.product.name == 'StandardOutOfStock')]", hasSize(0)));
        }
    }

    // Removed GetOutOfStockProductsTests as MerchantProductController does not have a dedicated /out-of-stock endpoint.
    // This functionality would need to be tested by filtering the main GET /api/merchants/products list if supported.
}
