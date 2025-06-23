package dev.mhizterpaul.cartlink.controller.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.mhizterpaul.cartlink.product.service.ProductLinkService;
// Assuming DTOs like GeneratedLinkResponse are in dev.mhizterpaul.cartlink.product.dto or a common dto package
import dev.mhizterpaul.cartlink.product.dto.GeneratedLinkResponse; // Placeholder

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

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
@DisplayName("Product Link Controller API Endpoints (for link generation and listing)")
public class ProductLinkControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ProductLinkService productLinkService;

    @InjectMocks
    private ProductLinkController productLinkController;

    private final String MERCHANT_ID = "test-merchant-id";
    private final String PRODUCT_ID = "test-product-id";
    private final String LINK_ID = "test-link-id";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productLinkController).build();
    }

    @Nested
    @DisplayName("POST /api/v1/merchants/{merchantId}/products/{productId}/generate-link")
    class GenerateLink {
        @Test
        @DisplayName("Should return 201 Created with linkId and URL on successful link generation")
        void whenValidRequest_thenReturns201AndLinkDetails() throws Exception {
            GeneratedLinkResponse response = new GeneratedLinkResponse("new-link-id", "https://cart.link/new-link-id", PRODUCT_ID); // Added productId to match typical response
            when(productLinkService.generateLink(MERCHANT_ID, PRODUCT_ID)).thenReturn(response);

            mockMvc.perform(post("/api/v1/merchants/{merchantId}/products/{productId}/generate-link", MERCHANT_ID, PRODUCT_ID))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.linkId").value("new-link-id"))
                    .andExpect(jsonPath("$.url").value("https://cart.link/new-link-id"));
        }
        // Add 401, 404 tests
    }

    @Nested
    @DisplayName("GET /api/v1/merchants/{merchantId}/products/links")
    class GetAllLinks {
        @Test
        @DisplayName("Should return 200 OK with a list of links and handle pagination")
        void whenAuthenticated_thenReturns200AndLinkList() throws Exception {
            GeneratedLinkResponse link = new GeneratedLinkResponse(LINK_ID, "https://cart.link/" + LINK_ID, PRODUCT_ID);
            List<GeneratedLinkResponse> linkList = Collections.singletonList(link);
            // Assuming the service method for listing links might be on ProductLinkService
            when(productLinkService.getAllLinksByMerchant(eq(MERCHANT_ID), anyInt(), anyInt())).thenReturn(linkList);

            mockMvc.perform(get("/api/v1/merchants/{merchantId}/products/links", MERCHANT_ID)
                    .param("page", "1")
                    .param("limit", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].linkId").value(LINK_ID));
        }
        // Add 401 test
    }

    // --- Notes on ProductLinkControllerTest ---
    // This controller handles /generate-link and /links (listing of links).
    // Analytics-related link endpoints (/analytics, /traffic) will be in LinkAnalyticsControllerTest.
    // Assumptions:
    // 1. ProductLinkController and ProductLinkService are from dev.mhizterpaul.cartlink.product.*
    // 2. GeneratedLinkResponse DTO exists (placeholder path used).
    // Inadequacies & Edge Cases:
    // - Product not found for link generation.
    // - No links found for a merchant.
}
