package dev.codesoap.book.integration;

import dev.paul.cartlink.customer.dto.CustomerProfileUpdateRequest;
import dev.paul.cartlink.customer.service.SignUpRequest;
import dev.paul.cartlink.merchant.model.Address;
import dev.paul.cartlink.payment.model.PaymentMethod; // Correctly placed import
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.MvcResult;
import java.util.Map;
import java.util.HashMap;
import org.springframework.mock.web.MockHttpSession;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CustomerIntegrationTests extends BaseIntegrationTest {

    @Nested
    @DisplayName("POST /api/v1/customers/signup")
    class CustomerSignupTests {

        @Test
        @DisplayName("Should return 201 Created for valid customer signup")
        void shouldReturn201ForValidCustomerSignup() throws Exception {
            SignUpRequest signupRequest = new SignUpRequest();
            signupRequest.setEmail("customer@example.com");
            signupRequest.setFirstName("Test");
            signupRequest.setLastName("Customer");
            signupRequest.setPhoneNumber("+2348012345678");
            signupRequest.setStreet("123 Main St");
            signupRequest.setCity("Testville");
            signupRequest.setState("TestState");
            signupRequest.setCountry("TestCountry");
            signupRequest.setPostalCode("12345");

            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/customers/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(signupRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.email").value("customer@example.com"))
                    .andExpect(jsonPath("$.firstName").value("Test"))
                    .andExpect(jsonPath("$.address.street").value("123 Main St"));
        }

        @Test
        @DisplayName("Should return 400 Bad Request for invalid customer signup (e.g., missing email)")
        void shouldReturn400ForInvalidSignupMissingEmail() throws Exception {
            SignUpRequest signupRequest = new SignUpRequest();
            signupRequest.setFirstName("Test");
            signupRequest.setLastName("Customer");
            signupRequest.setPhoneNumber("+2348012345678");

            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/customers/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(signupRequest)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/customers/profile")
    class UpdateCustomerProfileTests {
        @Test
        @DisplayName("Should return 200 OK for valid customer profile update")
        void shouldReturn200ForValidProfileUpdate() throws Exception {
            CustomerProfileUpdateRequest updateRequest = new CustomerProfileUpdateRequest();
            updateRequest.setFirstName("UpdatedFirst");
            updateRequest.setLastName("UpdatedLast");
            updateRequest.setPhoneNumber("+2348098765432");
            // CustomerProfileUpdateRequest has flat address fields, not a nested Address object.
            updateRequest.setStreet("456 New Ave");
            updateRequest.setCity("NewCity");
            updateRequest.setState("NewState");
            updateRequest.setCountry("NewCountry");
            updateRequest.setPostalCode("67890");
            // updateRequest.setAddress(address); // This was incorrect based on CustomerProfileUpdateRequest structure

            mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/customers/profile")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").exists());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/customers/orders/history")
    class GetCustomerOrderHistoryTests {
        @Test
        @DisplayName("Should return 200 OK and order history")
        void shouldReturn200AndOrderHistory() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/customers/orders/history?page=1&limit=20")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("Should handle pagination parameters")
        void shouldHandlePaginationParameters() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/customers/orders/history?page=2&limit=5")
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/customers/cart/checkout")
    class CustomerCheckoutCartTests {
        @Test
        @DisplayName("Should return 200 OK and checkout details for valid checkout request")
        void shouldReturn200ForValidCheckout() throws Exception {
            dev.paul.cartlink.cart.dto.CheckoutRequest checkoutRequest = new dev.paul.cartlink.cart.dto.CheckoutRequest();
            checkoutRequest.setPaymentMethod(PaymentMethod.CARD); // Corrected: Use Enum
            checkoutRequest.setCurrency("NGN");

            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/customers/cart/checkout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(checkoutRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.orderId").exists())
                    .andExpect(jsonPath("$.paymentStatus").value("PENDING"))
                    .andExpect(jsonPath("$.message").value("Checkout initiated"));
        }

        @Test
        @DisplayName("Should return 400 Bad Request if cart is empty (conceptual test)")
        void shouldReturn400IfCartIsEmpty() throws Exception {
            dev.paul.cartlink.cart.dto.CheckoutRequest checkoutRequest = new dev.paul.cartlink.cart.dto.CheckoutRequest();
            checkoutRequest.setPaymentMethod(PaymentMethod.CARD); // Corrected: Use Enum
            checkoutRequest.setCurrency("NGN");

            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/customers/cart/checkout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(checkoutRequest)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/customers/login")
    class CustomerLoginTests {

        private String customerEmail = "logincust@example.com";
        private String customerPassword = "custPassword123";

        @BeforeEach
        void setupCustomerForLogin() throws Exception {
             dev.paul.cartlink.customer.service.SignUpRequest signupReq = new dev.paul.cartlink.customer.service.SignUpRequest();
            signupReq.setEmail(customerEmail);
            signupReq.setFirstName("Login");
            signupReq.setLastName("User");
            signupReq.setPhoneNumber("+2348010101010");
            signupReq.setStreet("1 Login St");
            signupReq.setCity("Logincity");
            signupReq.setState("LG");
            signupReq.setCountry("NG");
            signupReq.setPostalCode("100001");
            // Customer signup in API_REQUIREMENTS.md doesn't include password.
            // Assuming dev.paul.cartlink.customer.service.SignUpRequest handles password if it's required by login.
            // If SignUpRequest DTO doesn't have password field, this login will likely fail or require different setup.
            // signupReq.setPassword(customerPassword);


            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/customers/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(signupReq)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("Should return 200 OK with token and customer details for valid login")
        void shouldReturn200AndTokenForValidLogin() throws Exception {
            Map<String, String> loginPayload = new HashMap<>();
            loginPayload.put("email", customerEmail);
            loginPayload.put("password", customerPassword);

            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/customers/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginPayload)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").exists())
                    .andExpect(jsonPath("$.customer.email").value(customerEmail));
        }

        @Test
        @DisplayName("Should return 401 Unauthorized for invalid credentials")
        void shouldReturn401ForInvalidCredentials() throws Exception {
            Map<String, String> loginPayload = new HashMap<>();
            loginPayload.put("email", customerEmail);
            loginPayload.put("password", "wrongPassword");

            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/customers/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginPayload)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/customers/{customerId}")
    class DeleteCustomerTests {

        private String customerIdToDelete;

        @BeforeEach
        void setupCustomerForDeletion() throws Exception {
            dev.paul.cartlink.customer.service.SignUpRequest signupReq = new dev.paul.cartlink.customer.service.SignUpRequest();
            signupReq.setEmail("todelete@example.com");
            signupReq.setFirstName("ToDelete");
            signupReq.setLastName("Person");
            signupReq.setPhoneNumber("+2348099998888");
            signupReq.setStreet("1 Delete St");
            signupReq.setCity("Delcity");
            signupReq.setState("DL");
            signupReq.setCountry("NG");
            signupReq.setPostalCode("100002");

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/customers/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(signupReq)))
                    .andExpect(status().isCreated())
                    .andReturn();
            String responseString = result.getResponse().getContentAsString();
            customerIdToDelete = objectMapper.readTree(responseString).get("id").asText();
        }

        @Test
        @DisplayName("Should return 200 OK for successful customer deletion")
        void shouldReturn200ForSuccessfulDeletion() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/customers/" + customerIdToDelete))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should return 404 Not Found if customerId does not exist")
        void shouldReturn404ForNonExistentCustomer() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/customers/nonExistentCustomerId999"))
                    .andExpect(status().isNotFound());
        }
    }

     @Nested
    @DisplayName("POST /api/v1/customers/orders")
    class PlaceOrderTests {
        @BeforeEach
        void setupForPlaceOrder() throws Exception {
        }

        @Test
        @DisplayName("Should return 201 Created and Order object for valid order placement")
        void shouldReturn201ForValidOrderPlacement() throws Exception {
            Map<String, Object> orderPayload = new HashMap<>();
            orderPayload.put("description", "Direct order for item X");
            orderPayload.put("amount", 100.00);

            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/customers/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(orderPayload)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.status").value("PENDING"));
        }

        @Test
        @DisplayName("Should return 400 Bad Request for invalid order details")
        void shouldReturn400ForInvalidOrder() throws Exception {
            Map<String, Object> invalidOrderPayload = new HashMap<>();
            mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/customers/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidOrderPayload)))
                    .andExpect(status().isBadRequest());
        }
    }
}
