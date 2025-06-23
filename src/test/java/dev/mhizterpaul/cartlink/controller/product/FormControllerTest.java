package dev.mhizterpaul.cartlink.controller.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.mhizterpaul.cartlink.product.service.FormService;
// Assuming ProductFormRequest DTO is in dev.mhizterpaul.cartlink.product.dto or a common dto package
import dev.mhizterpaul.cartlink.product.dto.ProductFormRequest; // Placeholder

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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Product Form Generator API Endpoints (FormController)")
public class FormControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private FormService formService; // Actual service from dev.mhizterpaul.cartlink.product.service

    @InjectMocks
    private FormController formController; // Actual controller from dev.mhizterpaul.cartlink.product.controller

    private final String MERCHANT_ID = "test-merchant-id";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(formController).build();
    }

    @Nested
    @DisplayName("POST /api/v1/merchants/{merchantId}/products/form")
    class GenerateProductForm {

        @Test
        @DisplayName("Should return 200 OK with HTML form for a valid category and product type")
        void whenValidCategoryAndProductType_thenReturns200AndHtmlForm() throws Exception {
            ProductFormRequest formRequest = new ProductFormRequest("Electronics", "Smartphone", "TechBrand", "SuperPhone", "A great phone");
            String expectedHtmlForm = "<html><body>Generated Form for Smartphone</body></html>";

            when(formService.generateProductForm(eq(MERCHANT_ID), any(ProductFormRequest.class))).thenReturn(expectedHtmlForm);

            mockMvc.perform(post("/api/v1/merchants/{merchantId}/products/form", MERCHANT_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(formRequest)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                    .andExpect(content().string(expectedHtmlForm));
        }

        @Test
        @DisplayName("Should return 400 Bad Request for an invalid or unsupported category")
        void whenInvalidCategory_thenReturns400() throws Exception {
            ProductFormRequest formRequest = new ProductFormRequest("UnsupportedCategory", "Smartphone", "TechBrand", "SuperPhone", "A great phone");
            when(formService.generateProductForm(eq(MERCHANT_ID), any(ProductFormRequest.class)))
                .thenThrow(new IllegalArgumentException("Invalid category: UnsupportedCategory"));

            mockMvc.perform(post("/api/v1/merchants/{merchantId}/products/form", MERCHANT_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(formRequest)))
                    .andExpect(status().isBadRequest());
        }
        // Add 401, other 400 tests
    }

    // --- Notes on FormControllerTest ---
    // Assumptions:
    // 1. FormController and FormService are correctly mapped from dev.mhizterpaul.cartlink.product.*
    // 2. ProductFormRequest DTO exists (placeholder path used).
    // Inadequacies & Edge Cases:
    // - Testing specific HTML content structure.
    // - Schema reuse logic within FormService.
}
