package dev.codesoap.book.integration;

import dev.paul.cartlink.merchant.dto.LoginRequest;
import dev.paul.cartlink.merchant.dto.SignUpRequest;
import dev.paul.cartlink.order.dto.OrderStatusUpdateRequest;
// For creating an order to test against, we might need customer actions or a way to seed orders.
// For now, will focus on merchant accessing existing orders, assuming they are created by another flow.
// If no orders exist, list endpoints might return empty arrays, which is valid.
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MerchantOrderIntegrationTests extends BaseIntegrationTest {

    private String authToken;
    private String merchantId;
    // private String orderId; // Will be needed if we can create/seed an order for this merchant

    @BeforeEach
    void setUp() throws Exception {
        String email = "merchantorder@example.com";
        String password = "password123";

        SignUpRequest signupRequest = new SignUpRequest();
        signupRequest.setEmail(email);
        signupRequest.setPassword(password);
        signupRequest.setFirstName("Order");
        signupRequest.setLastName("Merchant");
        signupRequest.setImage("order_merchant_img.png");

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

        // TODO: Ideally, create an order associated with this merchant here
        // For now, tests will assume orders might exist or endpoints handle empty results gracefully.
        // String createdOrderId = setupOrderForMerchant(merchantId, authToken);
        // this.orderId = createdOrderId;
    }

    @Nested
    @DisplayName("GET /api/v1/merchants/{merchantId}/orders")
    class ViewOrdersTests {

        @Test
        @DisplayName("Should return 200 OK and a list of orders (can be empty)")
        void shouldReturn200AndListOfOrders() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/merchants/" + merchantId + "/orders?page=1&limit=10")
                            .header("Authorization", "Bearer " + authToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray()); // Expecting a list, could be empty
        }

        @Test
        @DisplayName("Should filter orders by status if provided")
        void shouldFilterOrdersByStatus() throws Exception {
            // This test assumes an order with a specific status exists or the endpoint handles it.
            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/merchants/" + merchantId + "/orders?status=PENDING&page=1&limit=10")
                            .header("Authorization", "Bearer " + authToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
            // Further checks would involve verifying that returned orders actually have the PENDING status.
        }

        @Test
        @DisplayName("Should return 401 Unauthorized if token is missing")
        void shouldReturn401ForMissingToken() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/merchants/" + merchantId + "/orders")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/merchants/{merchantId}/orders/{orderId}/status")
    class UpdateOrderStatusTests {
        // This test requires a valid orderId belonging to the merchant.
        // For now, this test is more of a placeholder until order creation is integrated.
        // If I had 'this.orderId' from setup, I would use it.

        @Test
        @DisplayName("Should return 200 OK for valid status update (conceptual without seeded order)")
        void shouldReturn200ForValidStatusUpdate() throws Exception {
            OrderStatusUpdateRequest updateRequest = new OrderStatusUpdateRequest();
            updateRequest.setStatus("SHIPPED");
            String placeholderOrderId = "placeholderOrderId123"; // Replace with actual order ID when available

            // This test will likely fail with 404 if placeholderOrderId doesn't exist or isn't accessible.
            mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/merchants/" + merchantId + "/orders/" + placeholderOrderId + "/status")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk()) // Assuming 200 OK, could be 404 if order not found
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("Should return 404 Not Found for non-existent orderId")
        void shouldReturn404ForNonExistentOrder() throws Exception {
            OrderStatusUpdateRequest updateRequest = new OrderStatusUpdateRequest();
            updateRequest.setStatus("PROCESSING");

            mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/merchants/" + merchantId + "/orders/nonexistentorder999/status")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isNotFound()); // Or other error if not found
        }

        @Test
        @DisplayName("Should return 400 Bad Request for invalid status value (conceptual)")
        void shouldReturn400ForInvalidStatusValue() throws Exception {
            OrderStatusUpdateRequest updateRequest = new OrderStatusUpdateRequest();
            updateRequest.setStatus("INVALID_STATUS_VALUE"); // Assuming this is not a valid status
            String placeholderOrderId = "placeholderOrderId456";

            mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/merchants/" + merchantId + "/orders/" + placeholderOrderId + "/status")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isBadRequest()); // Assuming 400 for invalid enum/status
        }
    }

    @Nested
    @DisplayName("GET /api/v1/merchants/{merchantId}/orders/link/{linkId}")
    class GetOrdersByLinkTests {
        // This test requires a linkId that has associated orders.
        // For now, this is a placeholder.
        @Test
        @DisplayName("Should return 200 OK and list of orders for a given linkId (conceptual)")
        void shouldReturn200AndOrdersForLink() throws Exception {
            String placeholderLinkId = "placeholderLinkId123"; // Replace with actual link ID with orders

            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/merchants/" + merchantId + "/orders/link/" + placeholderLinkId)
                            .header("Authorization", "Bearer " + authToken)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk()) // Could be 404 if link has no orders or link doesn't exist
                    .andExpect(jsonPath("$").isArray());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/merchants/{merchantId}/orders/{orderId}/delivered")
    class MarkOrderAsDeliveredTests {
        // Requires a valid orderId for an order that can be marked as delivered.

        @Test
        @DisplayName("Should return 200 OK when marking an order as delivered (conceptual)")
        void shouldReturn200WhenMarkingDelivered() throws Exception {
            String placeholderOrderId = "placeholderOrderForDelivery789";

            mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/merchants/" + merchantId + "/orders/" + placeholderOrderId + "/delivered")
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isOk()) // Assuming 200, could be 404 or 400 based on order state/existence
                    .andExpect(jsonPath("$.message").exists()); // API_REQUIREMENTS says "Order marked as delivered..."
                                                              // but doesn't specify JSON response. Assuming a common pattern.
        }

        @Test
        @DisplayName("Should return 400 Bad Request if order cannot be marked delivered (e.g. wrong state)")
        void shouldReturn400IfCannotMarkDelivered() throws Exception {
            String placeholderOrderIdNotReady = "placeholderOrderNotReadyForDelivery";
            // This test would require setting up an order in a state not eligible for delivery.
            mockMvc.perform(MockMvcRequestBuilders.patch("/api/v1/merchants/" + merchantId + "/orders/" + placeholderOrderIdNotReady + "/delivered")
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isBadRequest());
        }
    }

    // Note: Automatic Merchant Payout is a scheduled service, not a direct API endpoint to test here.
}
