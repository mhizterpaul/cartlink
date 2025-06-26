package dev.codesoap.book.integration;

// Assuming Payment DTOs might be in dev.paul.cartlink.payment.dto
// For request params of POST /api/v1/payments/initiate, they are sent as request parameters, not a JSON body.
// The response is a Payment entity.
import dev.paul.cartlink.payment.model.PaymentMethod; // Assuming enum exists
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;


import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PaymentIntegrationTests extends BaseIntegrationTest {

    // These tests might require an order to be created first to get a valid orderId.
    // For now, using placeholders.
    private String sampleOrderIdForPayment = "orderForPayment789";
    private String sampleTxRef = "TX-ORDER-MOCK-001";

    @BeforeEach
    void setUp() {
        // Conceptual: Create an order to get a real 'sampleOrderIdForPayment'
        // This order should have an amount and currency associated with it.
        // For example, after a customer checkout process.
    }

    @Nested
    @DisplayName("POST /api/v1/payments/initiate")
    class InitiatePaymentTests {

        @Test
        @DisplayName("Should return 200 OK and Payment entity for valid initiation request")
        void shouldReturn200AndPaymentEntity() throws Exception {
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("orderId", sampleOrderIdForPayment);
            params.add("method", PaymentMethod.CARD.toString()); // Assuming CARD is a valid enum value
            params.add("amount", "1500.00");
            params.add("currency", "NGN");
            params.add("txRef", sampleTxRef);

            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/payments/initiate")
                            .params(params) // Sent as request parameters
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED) // Standard for params
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.paymentId").exists())
                    .andExpect(jsonPath("$.order.id").value(Integer.parseInt(sampleOrderIdForPayment.replaceAll("[^0-9]", "")))) // Assuming order.id is part of response and matches
                    .andExpect(jsonPath("$.method").value("CARD"))
                    .andExpect(jsonPath("$.status").value("PENDING")) // As per API_REQUIREMENTS example
                    .andExpect(jsonPath("$.amount").value(1500.00))
                    .andExpect(jsonPath("$.txRef").value(sampleTxRef));
        }

        @Test
        @DisplayName("Should return 400 Bad Request if orderId is missing or invalid")
        void shouldReturn400ForMissingOrderId() throws Exception {
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            // Missing orderId
            params.add("method", "CARD");
            params.add("amount", "100.00");
            params.add("currency", "NGN");
            params.add("txRef", "TX-FAIL-002");

            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/payments/initiate")
                            .params(params)
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 Bad Request if order is not found (as per API doc)")
        void shouldReturn400IfOrderNotFound() throws Exception {
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("orderId", "nonExistentOrder9999");
            params.add("method", "CARD");
            params.add("amount", "50.00");
            params.add("currency", "NGN");
            params.add("txRef", "TX-NOTFOUND-003");

            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/payments/initiate")
                            .params(params)
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().isBadRequest()); // API doc says 400 for order not found
        }
    }

    @Nested
    @DisplayName("POST /api/v1/payments/refund/{orderId}")
    class RefundPaymentTests {

        @Test
        @DisplayName("Should return 200 OK when refund process is triggered successfully")
        void shouldReturn200ForSuccessfulRefundTrigger() throws Exception {
            // This test assumes 'sampleOrderIdForPayment' is an order eligible for refund.
            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/payments/refund/" + sampleOrderIdForPayment)
                            .accept(MediaType.APPLICATION_JSON)) // Assuming it might return JSON success message
                    .andExpect(status().isOk());
                    // .andExpect(jsonPath("$.message").value("Refund process triggered")); // Or similar
        }

        @Test
        @DisplayName("Should return appropriate error (e.g., 404 or 400) if order is not found or not eligible for refund")
        void shouldReturnErrorForNonExistentOrIneligibleOrder() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/payments/refund/nonExistentOrderForRefund111")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound()); // Assuming 404 for not found, could be 400/422 if ineligible
        }
    }
    // Note: Automatic Merchant Payout is a scheduled service, not a direct API endpoint to test here.
}
