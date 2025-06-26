package dev.codesoap.book.integration;

import dev.paul.cartlink.merchant.dto.LoginRequest;
import dev.paul.cartlink.merchant.dto.SignUpRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.isA;

public class MerchantDashboardIntegrationTests extends BaseIntegrationTest {

    private String authToken;
    private String merchantId;

    @BeforeEach
    void setUp() throws Exception {
        String email = "dashboardmerchant@example.com";
        String password = "password123";

        SignUpRequest signupRequest = new SignUpRequest();
        signupRequest.setEmail(email);
        signupRequest.setPassword(password);
        signupRequest.setFirstName("Dashboard");
        signupRequest.setLastName("User");
        signupRequest.setImage("dash_img.png");

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
    @DisplayName("GET /api/v1/merchants/{merchantId}/dashboard/stats")
    class GetDashboardStatsTests {

        @Test
        @DisplayName("Should return 200 OK and dashboard statistics")
        void shouldReturn200AndDashboardStats() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/merchants/" + merchantId + "/dashboard/stats")
                            .header("Authorization", "Bearer " + authToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalSales").isNumber())
                    .andExpect(jsonPath("$.totalOrders").isNumber())
                    .andExpect(jsonPath("$.todaySales").isNumber())
                    .andExpect(jsonPath("$.totalCustomers").isNumber())
                    .andExpect(jsonPath("$.analytics.totalSalesChange").isNumber());
        }

        @Test
        @DisplayName("Should return 401 Unauthorized if token is missing")
        void shouldReturn401ForMissingToken() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/merchants/" + merchantId + "/dashboard/stats")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/merchants/{merchantId}/dashboard/sales-data")
    class GetSalesDataTests {

        @Test
        @DisplayName("Should return 200 OK and sales data for specified period")
        void shouldReturn200AndSalesData() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/merchants/" + merchantId + "/dashboard/sales-data?period=month&startDate=2023-01-01&endDate=2023-01-31")
                            .header("Authorization", "Bearer " + authToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].startDate").exists()) // Assuming at least one entry for valid request
                    .andExpect(jsonPath("$[0].totalSales").isNumber());
        }

        @Test
        @DisplayName("Should return 400 Bad Request if period or dates are missing/invalid (conceptual)")
        void shouldReturn400ForMissingParams() throws Exception {
            // Test missing 'period'
            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/merchants/" + merchantId + "/dashboard/sales-data?startDate=2023-01-01&endDate=2023-01-31")
                            .header("Authorization", "Bearer " + authToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            // Test invalid 'period'
             mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/merchants/" + merchantId + "/dashboard/sales-data?period=INVALID&startDate=2023-01-01&endDate=2023-01-31")
                            .header("Authorization", "Bearer " + authToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/merchants/{merchantId}/dashboard/traffic-data")
    class GetTrafficDataTests {

        @Test
        @DisplayName("Should return 200 OK and traffic data")
        void shouldReturn200AndTrafficData() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/merchants/" + merchantId + "/dashboard/traffic-data")
                            .header("Authorization", "Bearer " + authToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].source").exists()) // Assuming at least one traffic source
                    .andExpect(jsonPath("$[0].clicks").isNumber());
        }

        @Test
        @DisplayName("Should return 401 Unauthorized if token is missing")
        void shouldReturn401ForMissingToken() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/merchants/" + merchantId + "/dashboard/traffic-data")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }
}
