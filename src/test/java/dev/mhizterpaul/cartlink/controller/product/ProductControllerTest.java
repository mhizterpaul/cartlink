package dev.mhizterpaul.cartlink.controller.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.mhizterpaul.cartlink.product.service.ProductService;
// Assuming DTOs like ProductEditRequest, ProductResponse, BatchUploadResponse are in dev.mhizterpaul.cartlink.product.dto or a common dto package
import dev.mhizterpaul.cartlink.product.dto.ProductEditRequest; // Placeholder
import dev.mhizterpaul.cartlink.product.dto.ProductResponse;   // Placeholder
import dev.mhizterpaul.cartlink.product.dto.BatchUploadResponse; // Placeholder
import dev.mhizterpaul.cartlink.dto.response.SimpleSuccessResponse; // Common placeholder

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
@DisplayName("Product Management API Endpoints")
public class ProductControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private final String MERCHANT_ID = "test-merchant-id";
    private final String PRODUCT_ID = "test-product-id";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController).build();
    }

    @Nested
    @DisplayName("POST /api/v1/merchants/{merchantId}/products")
    class AddProduct {
        @Test
        @DisplayName("Should return 201 Created with product details on successful addition")
        void whenValidProductDataAndAuthenticated_thenReturns201AndProductDetails() throws Exception {
            Map<String, Object> productRequestData = Map.of("name", "New Gadget", "price", 99.99, "typeId", "electronics-001");
            // Assuming ProductResponse structure based on API_REQUIREMENTS and typical patterns
            ProductResponse productResponse = new ProductResponse(PRODUCT_ID, "merchant-prod-id", productRequestData, MERCHANT_ID, Collections.emptyList(), null, null, null, null, null, null, null);

            when(productService.addProduct(eq(MERCHANT_ID), any(Map.class))).thenReturn(productResponse);

            mockMvc.perform(post("/api/v1/merchants/{merchantId}/products", MERCHANT_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(productRequestData)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.productId").value(PRODUCT_ID))
                    .andExpect(jsonPath("$.productDetails.name").value("New Gadget"));
        }
        // Add 401, 400 tests
    }

    @Nested
    @DisplayName("PUT /api/v1/merchants/{merchantId}/products/{productId}")
    class EditProduct {
        @Test
        @DisplayName("Should return 200 OK with success message on successful product edit")
        void whenValidProductUpdateAndAuthenticated_thenReturns200AndSuccess() throws Exception {
            ProductEditRequest editRequest = new ProductEditRequest("Updated Gadget", "ModelX", "ManuY", 50, 89.99, Collections.emptyList(), Collections.emptyMap(), MERCHANT_ID, Collections.emptyList());
            SimpleSuccessResponse successResponse = new SimpleSuccessResponse(true, "Product updated successfully.");
            when(productService.editProduct(eq(MERCHANT_ID), eq(PRODUCT_ID), any(ProductEditRequest.class))).thenReturn(successResponse);

            mockMvc.perform(put("/api/v1/merchants/{merchantId}/products/{productId}", MERCHANT_ID, PRODUCT_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(editRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
        // Add 404, 401 tests
    }

    @Nested
    @DisplayName("DELETE /api/v1/merchants/{merchantId}/products/{productId}")
    class DeleteProduct {
        @Test
        @DisplayName("Should return 200 OK with success and productId on successful deletion")
        void whenValidProductDeleteAndAuthenticated_thenReturns200AndSuccess() throws Exception {
            // API Doc says: { success, productId } - using SimpleSuccessResponse and adapting message
            SimpleSuccessResponse response = new SimpleSuccessResponse(true, PRODUCT_ID);
            when(productService.deleteProduct(eq(MERCHANT_ID), eq(PRODUCT_ID))).thenReturn(response);

            mockMvc.perform(delete("/api/v1/merchants/{merchantId}/products/{productId}", MERCHANT_ID, PRODUCT_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value(PRODUCT_ID)); // Assuming service response message contains productId
        }
         // Add 401, 404 tests
    }

    @Nested
    @DisplayName("GET /api/v1/merchants/{merchantId}/products")
    class ListProducts {
        @Test
        @DisplayName("Should return 200 OK with a list of products and handle pagination/sorting")
        void whenAuthenticated_thenReturns200AndProductList() throws Exception {
            ProductResponse product = new ProductResponse(PRODUCT_ID, null, Map.of("name", "Gadget 1"), MERCHANT_ID, null, null, null, null, null, null, null, null);
            List<ProductResponse> productList = Collections.singletonList(product);
            when(productService.listProducts(eq(MERCHANT_ID), eq(1), eq(10), eq("price"), eq("asc"))).thenReturn(productList);

            mockMvc.perform(get("/api/v1/merchants/{merchantId}/products", MERCHANT_ID)
                    .param("page", "1").param("limit", "10").param("sort", "price").param("order", "asc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].productId").value(PRODUCT_ID));
        }
        // Add 401 test
    }

    @Nested
    @DisplayName("POST /api/v1/merchants/{merchantId}/products/upload")
    class BatchUploadProducts {
        @Test
        @DisplayName("Should return 201 Created with success and count on successful CSV/Excel upload")
        void whenValidFileUploadedAndAuthenticated_thenReturns201AndSuccessCount() throws Exception {
            MockMultipartFile file = new MockMultipartFile("file", "products.csv", MediaType.TEXT_PLAIN_VALUE, "name,price\nProductA,10.0".getBytes());
            BatchUploadResponse response = new BatchUploadResponse(true, 1);
            when(productService.batchUploadProducts(eq(MERCHANT_ID), any())).thenReturn(response);

            mockMvc.perform(multipart("/api/v1/merchants/{merchantId}/products/upload", MERCHANT_ID).file(file))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.addedCount").value(1));
        }
        // Add 401 test
    }

    // ... other nested classes for Search, In-Stock, Out-of-Stock, Download Template ...

    // --- Notes on ProductControllerTest ---
    // Assumptions:
    // 1. ProductController in dev.mhizterpaul.cartlink.product.controller, ProductService in dev.mhizterpaul.cartlink.product.service.
    // 2. DTOs like ProductEditRequest, ProductResponse, BatchUploadResponse are in dev.mhizterpaul.cartlink.product.dto (or common).
    // Inadequacies & Edge Cases:
    // - Dynamic schema validation for "Add Product".
    // - Thorough testing of all query params for ListProducts and SearchProducts.
}
