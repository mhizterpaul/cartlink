package dev.codesoap.book.integration;

import dev.paul.cartlink.customer.dto.RefundRequest; // Path from API_REQUIREMENTS: dev.paul.cartlink.customer.dto.RefundRequest
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.hasSize;

public class RefundManagementIntegrationTests extends BaseIntegrationTest {

    private MockHttpSession customerSession;
    private String sampleOrderIdForRefund = "orderForRefund456"; // Placeholder

    @BeforeEach
    void setUpCustomerSessionAndOrder() throws Exception {
        customerSession = new MockHttpSession();
        // Conceptual: Login customer
        // Conceptual: Create an order for this customer (sampleOrderIdForRefund) that is eligible for refund
    }

    @Nested
    @DisplayName("POST /api/v1/customers/orders/{orderId}/refund")
    class SubmitRefundRequestTests {

        @Test
        @DisplayName("Should return 201 Created for valid refund request submission")
        void shouldReturn201ForValidRefundRequest() throws Exception {
            RefundRequest refundRequest = new RefundRequest();
            refundRequest.setReason("Product damaged upon arrival.");
            // refundRequest.setOrderId(sampleOrderIdForRefund); // If DTO requires it

            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/customers/orders/" + sampleOrderIdForRefund + "/refund")
                            .session(customerSession)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(refundRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.reason").value("Product damaged upon arrival."))
                    .andExpect(jsonPath("$.status").value("REQUESTED")); // Assuming a default status like REQUESTED or PENDING
        }

        @Test
        @DisplayName("Should return 400 Bad Request if reason is missing")
        void shouldReturn400ForMissingReason() throws Exception {
            RefundRequest refundRequest = new RefundRequest();
            // Missing reason

            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/customers/orders/" + sampleOrderIdForRefund + "/refund")
                            .session(customerSession)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(refundRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 404 Not Found if orderId does not exist or not eligible for refund")
        void shouldReturn404ForNonExistentOrIneligibleOrder() throws Exception {
            RefundRequest refundRequest = new RefundRequest();
            refundRequest.setReason("Refund for non-existent order.");

            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/customers/orders/nonexistentOrderForRefund789/refund")
                            .session(customerSession)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(refundRequest)))
                    .andExpect(status().isNotFound()); // Or 400/422 if order exists but not refundable
        }
    }

    @Nested
    @DisplayName("GET /api/v1/customers/orders/refunds")
    class GetCustomerRefundsTests {

        @BeforeEach
        void submitARefundRequest() throws Exception {
            // Ensure at least one refund request exists for the customer
            RefundRequest refundRequest = new RefundRequest();
            refundRequest.setReason("Test refund for listing.");
            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/customers/orders/" + sampleOrderIdForRefund + "/refund")
                            .session(customerSession)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(refundRequest)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("Should return 200 OK and a list of customer's refund requests")
        void shouldReturn200AndListOfRefunds() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/customers/orders/refunds")
                            .session(customerSession)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].reason").value("Test refund for listing."));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/customers/orders/{orderId}/refunds")
    class GetOrderRefundsTests {
        String specificOrderForRefund = "specificOrderRefundXYZ";

        @BeforeEach
        void submitRefundForSpecificOrder() throws Exception {
            RefundRequest refundRequest = new RefundRequest();
            refundRequest.setReason("Refund for specific order XYZ.");
            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/customers/orders/" + specificOrderForRefund + "/refund")
                            .session(customerSession)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(refundRequest)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("Should return 200 OK and refund requests for a specific order")
        void shouldReturn200AndOrderRefunds() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/customers/orders/" + specificOrderForRefund + "/refunds")
                            .session(customerSession)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].reason").value("Refund for specific order XYZ."));
        }

        @Test
        @DisplayName("Should return empty list if order has no refund requests (or 404 if order not found)")
        void shouldReturnEmptyListIfNoRefundsForOrder() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/customers/orders/orderWithNoRefundsABC/refunds")
                            .session(customerSession)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk()) // Assuming 200 OK with empty list
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }
}
