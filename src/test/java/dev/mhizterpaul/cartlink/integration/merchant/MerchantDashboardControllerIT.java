package dev.mhizterpaul.cartlink.integration.merchant;

import com.fasterxml.jackson.databind.ObjectMapper;
// Assuming DTOs are in dev.paul.cartlink.merchant.dto or common
import dev.paul.cartlink.merchant.dto.DashboardStatsResponse; // Placeholder
import dev.paul.cartlink.merchant.dto.SalesDataResponse; // Placeholder
import dev.paul.cartlink.merchant.dto.TrafficDataResponse; // Placeholder

// For test data setup
import dev.paul.cartlink.merchant.model.Merchant;
import dev.paul.cartlink.merchant.repository.MerchantRepository;
// May need Order, ProductLink, etc. repositories if dashboard data relies on them directly
// and can't be easily set up via API calls for the purpose of dashboard verification.

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
import java.util.Collections;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Merchant Dashboard API Integration Tests")
public class MerchantDashboardControllerIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MerchantRepository merchantRepository;
    // Autowire other repositories like OrderRepository, ProductLinkRepository if
    // needed for direct data setup for dashboard tests

    private Long testMerchantId;

    @BeforeEach
    void setUpTestData() {
        Merchant merchant = new Merchant();
        merchant.setEmail("dashboardmerchant" + System.currentTimeMillis() + "@example.com");
        merchant.setPassword("password");
        merchant.setFirstName("DashboardTest");
        Merchant savedMerchant = merchantRepository.save(merchant);
        testMerchantId = savedMerchant.getId();

        // Potentially create some orders, products, links, and track some analytics
        // events here
        // if the dashboard relies on fresh data and can't be tested with zero/empty
        // state.
        // For now, tests will check structure assuming services might return zero/empty
        // data.
    }

    @Nested
    @DisplayName("GET /api/v1/merchants/{merchantId}/dashboard/stats")
    @WithMockUser(username = "test-dashboard-merchant", roles = { "MERCHANT" }) // Ensure username matches if service
                                                                                // uses principal name
    class GetDashboardStats {
        @Test
        @DisplayName("Should return 200 OK with dashboard statistics structure")
        void whenAuthenticated_thenReturnsDashboardStats() throws Exception {
            // Test assumes the service layer will calculate stats. We check for structure.
            // If specific stats are expected (e.g. for a new merchant), assertions can be
            // more precise.
            mockMvc.perform(get("/api/v1/merchants/{merchantId}/dashboard/stats", testMerchantId)
            // If @WithMockUser principal name is used by controller/service for merchantId,
            // ensure it aligns or pass the correct merchantId.
            // .header("Authorization", "Bearer <token_for_testMerchantId>") // Alternative
            // auth
            )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalSales").isNumber())
                    .andExpect(jsonPath("$.totalOrders").isNumber())
                    .andExpect(jsonPath("$.todaySales").isNumber())
                    .andExpect(jsonPath("$.totalCustomers").isNumber())
                    .andExpect(jsonPath("$.analytics.totalSalesChange").isNumber());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/merchants/{merchantId}/dashboard/sales-data")
    @WithMockUser(username = "test-dashboard-merchant", roles = { "MERCHANT" })
    class GetSalesData {
        @Test
        @DisplayName("Should return 200 OK with sales data structure for a valid period")
        void whenParamsProvided_thenReturnsSalesData() throws Exception {
            mockMvc.perform(get("/api/v1/merchants/{merchantId}/dashboard/sales-data", testMerchantId)
                    .param("period", "week")
                    .param("startDate", "2024-01-01")
                    .param("endDate", "2024-01-07"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
            // Further assertions on array content if specific data is set up and expected
        }
    }

    @Nested
    @DisplayName("GET /api/v1/merchants/{merchantId}/dashboard/traffic-data")
    @WithMockUser(username = "test-dashboard-merchant", roles = { "MERCHANT" })
    class GetTrafficData {
        @Test
        @DisplayName("Should return 200 OK with traffic data structure")
        void whenAuthenticated_thenReturnsTrafficData() throws Exception {
            mockMvc.perform(get("/api/v1/merchants/{merchantId}/dashboard/traffic-data", testMerchantId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
            // Further assertions on array content if specific data is set up and expected
        }
    }

    // --- Notes on MerchantDashboardControllerIT ---
    // - Targets dev.paul.cartlink.merchant.controller.MerchantDashboardController.
    // - Assumes a MerchantDashboardService or similar handles the logic.
    // - Placeholder DTOs used.
    // - Data setup for dashboard might be complex if it requires many underlying
    // entities (orders, clicks).
    // Current tests focus on reachability and basic structure for a new/empty
    // merchant.
}
