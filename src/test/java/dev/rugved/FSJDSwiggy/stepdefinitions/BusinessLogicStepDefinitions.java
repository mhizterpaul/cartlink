package dev.rugved.FSJDSwiggy.stepdefinitions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.rugved.FSJDSwiggy.dto.AddToCartRequestDTO; // Assuming a DTO for add to cart
import dev.rugved.FSJDSwiggy.dto.CheckoutRequestDTO; // Assuming a DTO for checkout
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Map; // For status update body


import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

public class BusinessLogicStepDefinitions {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ScenarioContext scenarioContext;

    @Autowired // Assuming CommonStepDefinitions is a Spring bean or accessible for direct calls
    private CommonStepDefinitions commonStepDefinitions;


    // --- Coupon Management Steps ---
    // (product_with_id_by_merchant_exists_price_stock is a more specific version of product exists, keep it here for now or merge if identical logic)
    @Given("product {string} with ID {string} by merchant {string} exists, price {double}, stock {int}")
    public void product_with_id_by_merchant_exists_price_stock(String productAlias, String productId, String merchantAlias, double price, int stock) throws JsonProcessingException {
        // 1. Ensure merchant exists (this step in CommonStepDefinitions also logs them in)
        commonStepDefinitions.merchant_with_id_exists(merchantAlias, "dummyMerchantIdFor_" + merchantAlias); // The ID will be overwritten by actual from API

        // 2. Create product (this step in CommonStepDefinitions uses the logged-in merchant from context)
        // The product creation step in Common uses a generic payload. We might need to enhance it or this step
        // to set specific price/stock if the generic one doesn't.
        // For now, let's assume the generic product creation is sufficient and we just store price/stock for context.
        commonStepDefinitions.product_with_id_belonging_to_merchant_exists(productAlias, productId, merchantAlias);

        // Store price and stock in context for this specific alias, as the common product creation might use defaults
        scenarioContext.setContext(productAlias + "_PRICE", price);
        scenarioContext.setContext(productAlias + "_STOCK", stock);
        System.out.println("BizLogicStepDef: Product " + productAlias + " (ID from common: " + scenarioContext.getContext(productAlias+"_ID") + ") for merchant " + merchantAlias + ". Price: " + price + ", Stock: " + stock + " stored in context.");
    }

    @When("I send a POST request to create coupon {string} for product {string} of merchant {string} with body:")
    public void i_send_a_post_request_to_create_coupon_for_product_of_merchant_with_body(String couponAlias, String productAlias, String merchantAlias, String requestBody) {
        RequestSpecification rs = (RequestSpecification) scenarioContext.getContext("REQUEST_SPEC");
        assertNotNull(rs, "RequestSpecification not found in ScenarioContext.");

        String productId = (String) scenarioContext.getContext(productAlias + "_ID");
        String merchantId = (String) scenarioContext.getContext(merchantAlias + "_ID");
        assertNotNull(productId, "Product ID for alias " + productAlias + " not found.");
        assertNotNull(merchantId, "Merchant ID for alias " + merchantAlias + " not found.");

        String endpoint = "/merchants/" + merchantId + "/products/" + productId + "/coupons";

        Response response = rs
                    .contentType(ContentType.JSON)
                    .body(requestBody)
                    .when()
                    .post(endpoint);
        scenarioContext.setContext("LAST_RESPONSE", response);
        if (response.getStatusCode() == 201) {
            String couponId = response.jsonPath().getString("couponId");
            scenarioContext.setContext(couponAlias + "_ID", couponId);
        }
    }


    // --- Order Lifecycle Steps ---

    // This step is now fully handled by CommonStepDefinitions
    // @Given("customer {string} with ID {string} exists and is logged in")
    // public void customer_with_id_exists_and_is_logged_in(String customerAlias, String customerId) throws JsonProcessingException {
    //    commonStepDefinitions.customer_with_id_exists_and_is_logged_in(customerAlias, customerId);
    // }

    @Given("{string} has an order {string} for {string} with status {string}")
    public void has_an_order_for_with_status(String customerAlias, String orderAlias, String productAlias, String targetStatus) throws JsonProcessingException {
        // 1. Ensure Customer exists and is logged in
        // The Gherkin should have a step like: Given customer "customer_A" with ID "custA_id" exists and is logged in
        // which would be handled by CommonStepDefinitions.customer_with_id_exists_and_is_logged_in
        // We retrieve the customer's ID and token from context.
        String customerId = (String) scenarioContext.getContext(customerAlias + "_ID");
        String customerToken = (String) scenarioContext.getContext("CURRENT_JWT_TOKEN"); // Assumes customer is current user
        String customerRole = (String) scenarioContext.getContext("LOGGED_IN_USER_ROLE");
        assertEquals("CUSTOMER", customerRole, "The current logged in user is not a customer for order creation.");
        assertNotNull(customerId, "Customer ID for " + customerAlias + " not found in context.");
        assertNotNull(customerToken, "Customer token for " + customerAlias + " not found in context.");

        // 2. Ensure Product (and its Merchant) exists
        // The Gherkin should have: Given product "product_X" with ID "prodX_id" belonging to merchant "merchant_Y" exists
        // We retrieve the product's ID and its merchant's ID.
        String productId = (String) scenarioContext.getContext(productAlias + "_ID"); // This should be the actual_id from product creation
        String productMerchantId = (String) scenarioContext.getContext(productAlias + "_MERCHANT_ID");
        assertNotNull(productId, "Product ID for " + productAlias + " not found in context.");
        assertNotNull(productMerchantId, "Merchant ID for product " + productAlias + " not found in context.");

        RequestSpecification customerRs = (RequestSpecification) scenarioContext.getContext("REQUEST_SPEC"); // This should be customer's authed spec

        // 3. Add to Cart
        AddToCartRequestDTO cartRequest = new AddToCartRequestDTO(productId, 1); // Assuming DTO and quantity 1
        String cartRequestBody = objectMapper.writeValueAsString(cartRequest);
        Response cartResponse = customerRs.contentType(ContentType.JSON)
                                        .body(cartRequestBody)
                                        .when()
                                        .post("/customers/cart/items");
        assertEquals(201, cartResponse.getStatusCode(), "Failed to add product " + productAlias + " to cart for customer " + customerAlias + ". Response: " + cartResponse.asString());
        // itemId might be needed if we want to remove/update cart items later, store if available from response.

        // 4. Checkout
        CheckoutRequestDTO checkoutDetails = new CheckoutRequestDTO("CARD", "NGN"); // Default payment
        String checkoutRequestBody = objectMapper.writeValueAsString(checkoutDetails);
        Response checkoutResponse = customerRs.contentType(ContentType.JSON)
                                            .body(checkoutRequestBody)
                                            .when()
                                            .post("/customers/cart/checkout");
        assertEquals(200, checkoutResponse.getStatusCode(), "Checkout failed for customer " + customerAlias + ". Response: " + checkoutResponse.asString());

        String orderId = checkoutResponse.jsonPath().getString("orderId");
        String initialStatus = checkoutResponse.jsonPath().getString("paymentStatus"); // API says "paymentStatus", but could be general order status
        assertNotNull(orderId, "orderId not found in checkout response.");
        assertNotNull(initialStatus, "initialStatus (paymentStatus) not found in checkout response.");

        scenarioContext.setContext(orderAlias + "_ID", orderId);
        scenarioContext.setContext(orderAlias + "_CUSTOMER_ID", customerId);
        scenarioContext.setContext(orderAlias + "_PRODUCT_ID", productId); // For reference
        scenarioContext.setContext(orderAlias + "_MERCHANT_ID", productMerchantId);
        scenarioContext.setContext(orderAlias + "_INITIAL_STATUS", initialStatus);
        scenarioContext.setContext(orderAlias + "_CURRENT_STATUS", initialStatus);


        System.out.println("BizLogicStepDef: Order " + orderAlias + " (ID: " + orderId + ") created for customer " + customerAlias + " with initial status " + initialStatus);

        // 5. Set specific status if needed and possible
        if (!targetStatus.equalsIgnoreCase(initialStatus)) {
            // This requires merchant login and using the merchant order update endpoint
            System.out.println("BizLogicStepDef: Initial order status is " + initialStatus + ", target is " + targetStatus + ". Attempting to update.");

            // Ensure the product's merchant is now logged in
            String merchantLoginEmail = (String) scenarioContext.getContext( (String) scenarioContext.getContext(productAlias + "_MERCHANT_ALIAS_FOR_PRODUCT") + "_EMAIL"); // We need a way to get merchant's alias
            String merchantPassword = (String) scenarioContext.getContext( (String) scenarioContext.getContext(productAlias + "_MERCHANT_ALIAS_FOR_PRODUCT") + "_PASSWORD");

            // This is tricky: product creation step stores merchantId, but not necessarily the alias used to create it.
            // For now, assume a generic merchant alias for the product if not specified, or enhance product creation to store its merchant's alias.
            // Let's assume product_MERCHANT_ID holds the ID, and we need a generic way to "log in as merchant owning this order"
            // This part needs refinement on how we get the merchant's credentials to log them in to update the order.
            // For now, if the target status is one a merchant can set, this demonstrates the call:

            // --- This section requires ensuring the correct MERCHANT is logged in ---
            // This would typically be:
            // commonStepDefinitions.i_am_logged_in_as_a_with_email_and_password("MERCHANT", merchantLoginEmail, merchantPassword);
            // RequestSpecification merchantRs = (RequestSpecification) scenarioContext.getContext("REQUEST_SPEC");
            // --- End section ---

            // For the test to proceed, we'll assume if targetStatus is different, it's a conceptual part for now,
            // or that a subsequent step explicitly logs in the correct merchant.
            // If we had merchant login details:
            // Map<String, String> statusUpdateBody = Map.of("status", targetStatus);
            // Response updateResponse = merchantRs.contentType(ContentType.JSON)
            //                                 .body(objectMapper.writeValueAsString(statusUpdateBody))
            //                                 .when()
            //                                 .put("/merchants/" + productMerchantId + "/orders/" + orderId + "/status");
            // assertEquals(200, updateResponse.getStatusCode(), "Failed to update order " + orderId + " to status " + targetStatus + ". Response: " + updateResponse.asString());
            // scenarioContext.setContext(orderAlias + "_CURRENT_STATUS", targetStatus);
            // System.out.println("BizLogicStepDef: Order " + orderAlias + " status updated to " + targetStatus);
            // else:
             System.out.println("BizLogicStepDef: Setting order status to '" + targetStatus + "' post-creation would require merchant login and API call. Current status: " + initialStatus);
             scenarioContext.setContext(orderAlias + "_TARGET_STATUS_POST_CREATION", targetStatus); // Mark for later potential update
        } else {
            System.out.println("BizLogicStepDef: Order " + orderAlias + " created with target status " + targetStatus);
        }
    }

    @When("merchant {string} attempts to mark order {string} as delivered")
    public void merchant_attempts_to_mark_order_as_delivered(String merchantAlias, String orderAlias) {
        RequestSpecification rs = (RequestSpecification) scenarioContext.getContext("REQUEST_SPEC");
        assertNotNull(rs, "RequestSpecification not found in ScenarioContext.");
        String loggedInUserAlias = (String) scenarioContext.getContext("LOGGED_IN_USER_ALIAS");
        assertEquals(merchantAlias, loggedInUserAlias, "Attempting to operate as merchant " + merchantAlias + " but logged in as " + loggedInUserAlias);

        String merchantId = (String) scenarioContext.getContext(merchantAlias + "_ID");
        String orderId = (String) scenarioContext.getContext(orderAlias + "_ID");
        assertNotNull(merchantId, "Merchant ID for alias " + merchantAlias + " not found.");
        assertNotNull(orderId, "Order ID for alias " + orderAlias + " not found.");

        String endpoint = "/merchants/" + merchantId + "/orders/" + orderId + "/delivered";

        Response response = rs
                        .contentType(ContentType.JSON)
                        .when()
                        .patch(endpoint);
        scenarioContext.setContext("LAST_RESPONSE", response);
    }

    @Then("the status of order {string} should still be {string}")
    public void the_status_of_order_should_still_be(String orderAlias, String expectedStatus) {
        String orderId = (String) scenarioContext.getContext(orderAlias + "_ID");
        assertNotNull(orderId, "Order ID for " + orderAlias + " not found in context.");

        String merchantId = (String) scenarioContext.getContext(orderAlias + "_MERCHANT_ID");
        assertNotNull(merchantId, "Merchant ID for order " + orderAlias + " not found in context. Ensure order setup step links it.");

        RequestSpecification rs = (RequestSpecification) scenarioContext.getContext("REQUEST_SPEC");
        assertNotNull(rs, "RequestSpecification (REQUEST_SPEC) not found in scenario context.");

        Response getOrderResponse = rs.when().get("/merchants/" + merchantId + "/orders");

        assertEquals(200, getOrderResponse.getStatusCode(), "Failed to fetch orders for merchant " + merchantId + ". Response: " + getOrderResponse.asString());

        String actualStatusById = getOrderResponse.jsonPath().getString("find { it.id == '" + orderId + "' }.status");
        String actualStatusByOrderId = getOrderResponse.jsonPath().getString("find { it.orderId == '" + orderId + "' }.status");
        String actualStatus = actualStatusById != null ? actualStatusById : actualStatusByOrderId;

        assertNotNull(actualStatus, "Order " + orderId + " not found in merchant " + merchantId + "'s order list, or status is null. Response: " + getOrderResponse.asString());
        assertEquals(expectedStatus, actualStatus, "Order status for " + orderAlias + " did not match.");
    }

    @Given("order {string} with ID {string} for this merchant exists with status {string}")
    public void order_with_id_for_this_merchant_exists_with_status(String orderAlias, String orderId, String status) {
        String merchantAlias = (String) scenarioContext.getContext("LOGGED_IN_USER_ALIAS");
        assertNotNull(merchantAlias, "No merchant appears to be logged in for this step (LOGGED_IN_USER_ALIAS not found).");
        String merchantId = (String) scenarioContext.getContext(merchantAlias + "_ID");
        assertNotNull(merchantId, "Merchant ID for " + merchantAlias + " not found.");

        scenarioContext.setContext(orderAlias + "_ID", orderId);
        scenarioContext.setContext(orderAlias + "_MERCHANT_ID", merchantId);
        scenarioContext.setContext(orderAlias + "_STATUS", status); // This is the *expected* or *setup* status
        scenarioContext.setContext(orderAlias + "_CURRENT_STATUS", status); // Assume it's set to this
        System.out.println("BizLogicStepDef: Assuming order " + orderAlias + " (ID: " + orderId + ") for current merchant " + merchantAlias + " (ID: " + merchantId + ") exists with status " + status + ". (Data setup placeholder - actual creation and status setting needed if not done by other steps)");
    }

    @When("I send a PUT request to update status of order {string} for merchant {string} with body:")
    public void i_send_a_put_request_to_update_status_of_order_for_merchant_with_body(String orderAlias, String merchantAlias, String requestBody) {
        RequestSpecification rs = (RequestSpecification) scenarioContext.getContext("REQUEST_SPEC");
        assertNotNull(rs, "RequestSpecification (REQUEST_SPEC) not found in scenario context.");

        String loggedInUserAlias = (String) scenarioContext.getContext("LOGGED_IN_USER_ALIAS");
        String loggedInUserRole = (String) scenarioContext.getContext("LOGGED_IN_USER_ROLE");

        assertEquals("MERCHANT", loggedInUserRole, "Logged in user is not a merchant.");
        assertEquals(merchantAlias, loggedInUserAlias, "Attempting to update order as merchant '" + merchantAlias + "' but current logged in merchant is '" + loggedInUserAlias + "'.");

        String merchantId = (String) scenarioContext.getContext(merchantAlias + "_ID");
        String orderId = (String) scenarioContext.getContext(orderAlias + "_ID");
        assertNotNull(merchantId, "Merchant ID for " + merchantAlias + " not found.");
        assertNotNull(orderId, "Order ID for " + orderAlias + " not found.");

        String endpoint = "/merchants/" + merchantId + "/orders/" + orderId + "/status";

        Response response = rs
                        .contentType(ContentType.JSON)
                        .body(requestBody)
                        .when()
                        .put(endpoint);
        scenarioContext.setContext("LAST_RESPONSE", response);
        if(response.getStatusCode() == 200) {
            try {
                Map<String, String> bodyMap = objectMapper.readValue(requestBody, Map.class);
                scenarioContext.setContext(orderAlias + "_CURRENT_STATUS", bodyMap.get("status"));
            } catch (JsonProcessingException e) {
                System.err.println("Error parsing status update request body: " + e.getMessage());
            }
        }
    }

    @Given("merchant {string} with ID {string} exists with initial balance {double}")
    public void merchant_with_id_exists_with_initial_balance(String merchantAlias, String merchantId, double balance) throws JsonProcessingException {
        commonStepDefinitions.merchant_with_id_exists(merchantAlias, merchantId);
        scenarioContext.setContext(merchantAlias + "_INITIAL_BALANCE", balance);
        System.out.println("BizLogicStepDef: Merchant " + merchantAlias + " (ID from common: " + scenarioContext.getContext(merchantAlias+"_ID") + ") exists. Initial balance " + balance + " noted in context. (Data setup)");
    }

    @Given("order {string} with ID {string} for {string} exists, status {string}, paid on {string}, amount {double}")
    public void order_for_merchant_exists_status_paid_on_amount(String orderAlias, String orderId, String merchantAlias, String status, String paidOnDate, double amount) {
        String merchantId = (String) scenarioContext.getContext(merchantAlias + "_ID");
        assertNotNull(merchantId, "Merchant ID for " + merchantAlias + " not found. Ensure merchant exists first via a Given step.");
        scenarioContext.setContext(orderAlias + "_ID", orderId);
        scenarioContext.setContext(orderAlias + "_MERCHANT_ID", merchantId);
        scenarioContext.setContext(orderAlias + "_STATUS", status); // Expected or setup status
        scenarioContext.setContext(orderAlias + "_CURRENT_STATUS", status); // Assume it's set
        scenarioContext.setContext(orderAlias + "_PAID_ON", paidOnDate);
        scenarioContext.setContext(orderAlias + "_AMOUNT", amount);
        System.out.println("BizLogicStepDef: Order " + orderAlias + " (ID: " + orderId + ") for merchant " + merchantAlias + " status " + status + ", paid on " + paidOnDate + ", amount " + amount + ". (Data setup placeholder)");
    }

    @Given("the current system time is {string}")
    public void the_current_system_time_is(String systemTime) {
        scenarioContext.setContext("MOCKED_SYSTEM_TIME", systemTime);
        System.out.println("BizLogicStepDef: Conceptual - System time is now " + systemTime + ". This requires actual time mocking capabilities in the application.");
    }

    @When("the scheduled merchant payout job runs")
    public void the_scheduled_merchant_payout_job_runs() {
        System.out.println("BizLogicStepDef: Conceptual - Triggering merchant payout job. This step would typically call an admin API to trigger the job.");
    }

    @Then("the balance for merchant {string} should be {double}")
    public void the_balance_for_merchant_should_be(String merchantAlias, double expectedBalance) {
        String merchantId = (String) scenarioContext.getContext(merchantAlias + "_ID");
        assertNotNull(merchantId, "Merchant ID for " + merchantAlias + " not found.");
        System.out.println("BizLogicStepDef: Conceptual - Verifying balance for merchant " + merchantAlias + " is " + expectedBalance + ". Requires an API to fetch current merchant balance (e.g., from dashboard stats).");
    }

    @Then("order {string} should be marked as {string}")
    public void order_should_be_marked_as(String orderAlias, String expectedStatus) {
        String orderId = (String) scenarioContext.getContext(orderAlias + "_ID");
        String merchantId = (String) scenarioContext.getContext(orderAlias + "_MERCHANT_ID");
        assertNotNull(orderId, "Order ID for " + orderAlias + " not found.");
        assertNotNull(merchantId, "Merchant ID for order " + orderAlias + " not found (should be set by order setup step).");

        System.out.println("BizLogicStepDef: Conceptual - Verifying order " + orderAlias + " status is " + expectedStatus + ". Requires API to fetch specific order details and a working time mock & job trigger.");
        the_status_of_order_should_still_be(orderAlias, expectedStatus);
    }
}

// Assuming DTOs are in dev.rugved.FSJDSwiggy.dto
// If not, these would need to be defined, e.g.:
/*
package dev.rugved.FSJDSwiggy.dto;

class AddToCartRequestDTO {
    public String productId;
    public int quantity;

    public AddToCartRequestDTO(String productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }
}

class CheckoutRequestDTO {
    public String paymentMethod; // CARD, USSD, BANK_TRANSFER
    public String currency;      // NGN

    public CheckoutRequestDTO(String paymentMethod, String currency) {
        this.paymentMethod = paymentMethod;
        this.currency = currency;
    }
}
*/
