package dev.codesoap.book.integration;

import dev.paul.cartlink.complaint.dto.ComplaintRequest;
// Assuming customer session is needed, similar to CustomerOrderIntegrationTests
// Also, submitting a complaint requires a valid orderId.
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


public class ComplaintHandlingIntegrationTests extends BaseIntegrationTest {

    private MockHttpSession customerSession;
    private String sampleOrderId = "orderForComplaint123"; // Placeholder

    @BeforeEach
    void setUpCustomerSessionAndOrder() throws Exception {
        customerSession = new MockHttpSession();
        // Conceptual: Login customer to establish session
        // Conceptual: Create an order for this customer to get a valid sampleOrderId

        // For now, sampleOrderId is a placeholder. Tests involving it might fail
        // if the order doesn't exist or isn't associated with the customer session.
    }

    @Nested
    @DisplayName("POST /api/v1/customers/orders/{orderId}/complaint")
    class SubmitComplaintTests {

        @Test
        @DisplayName("Should return 201 Created for valid complaint submission")
        void shouldReturn201ForValidComplaint() throws Exception {
            ComplaintRequest complaintRequest = new ComplaintRequest();
            complaintRequest.setTitle("Missing Item");
            complaintRequest.setDescription("One of the items in my order was missing.");
            complaintRequest.setCategory("Shipment Error");
            // complaintRequest.setOrderId(sampleOrderId); // If DTO requires it explicitly

            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/customers/orders/" + sampleOrderId + "/complaint")
                            .session(customerSession)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(complaintRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.title").value("Missing Item"))
                    .andExpect(jsonPath("$.complaintStatus").value("PENDING")); // Assuming a default status like PENDING
        }

        @Test
        @DisplayName("Should return 400 Bad Request if title or description is missing")
        void shouldReturn400ForMissingFields() throws Exception {
            ComplaintRequest complaintRequest = new ComplaintRequest();
            // Missing title and description
            complaintRequest.setCategory("General");

            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/customers/orders/" + sampleOrderId + "/complaint")
                            .session(customerSession)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(complaintRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 404 Not Found if orderId does not exist")
        void shouldReturn404ForNonExistentOrder() throws Exception {
            ComplaintRequest complaintRequest = new ComplaintRequest();
            complaintRequest.setTitle("Test Complaint");
            complaintRequest.setDescription("For a non-existent order.");
            complaintRequest.setCategory("Test");

            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/customers/orders/nonexistentorder999/complaint")
                            .session(customerSession)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(complaintRequest)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/customers/orders/complaints")
    class GetCustomerComplaintsTests {

        @BeforeEach
        void submitAComplaint() throws Exception {
            // Ensure at least one complaint exists for the customer
            ComplaintRequest complaintRequest = new ComplaintRequest();
            complaintRequest.setTitle("Test Complaint for Listing");
            complaintRequest.setDescription("This is a test complaint.");
            complaintRequest.setCategory("Testing");
            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/customers/orders/" + sampleOrderId + "/complaint")
                            .session(customerSession)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(complaintRequest)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("Should return 200 OK and a list of customer's complaints")
        void shouldReturn200AndListOfComplaints() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/customers/orders/complaints")
                            .session(customerSession)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(1))) // Assuming one complaint was created
                    .andExpect(jsonPath("$[0].title").value("Test Complaint for Listing"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/customers/orders/{orderId}/complaints")
    class GetOrderComplaintsTests {

        @BeforeEach
        void submitComplaintForSpecificOrder() throws Exception {
            ComplaintRequest complaintRequest = new ComplaintRequest();
            complaintRequest.setTitle("Specific Order Complaint");
            complaintRequest.setDescription("Complaint for order " + sampleOrderId);
            complaintRequest.setCategory("OrderSpecific");
            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/customers/orders/" + sampleOrderId + "/complaint")
                            .session(customerSession)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(complaintRequest)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("Should return 200 OK and complaints for a specific order")
        void shouldReturn200AndOrderComplaints() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/customers/orders/" + sampleOrderId + "/complaints")
                            .session(customerSession)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(1))) // Assuming one complaint for this order
                    .andExpect(jsonPath("$[0].title").value("Specific Order Complaint"));
        }

        @Test
        @DisplayName("Should return empty list if order has no complaints")
        void shouldReturnEmptyListIfNoComplaintsForOrder() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/customers/orders/orderwithnocomplaints777/complaints")
                            .session(customerSession)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk()) // Or 404 if order itself doesn't exist
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }
}
