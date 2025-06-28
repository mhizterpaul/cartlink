package dev.rugved.FSJDSwiggy.stepdefinitions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
// import dev.rugved.FSJDSwiggy.dto.UserCredentials; // Assuming this is moved or accessible
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static io.restassured.RestAssured.given; // Keep this for direct use if any
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class AuthenticationAuthorizationStepDefinitions {

    // Fields like response, request, jwtToken, baseUrl are removed as they should be managed
    // via ScenarioContext and the RequestSpecification from CommonStepDefinitions

    @Autowired
    private ScenarioContext scenarioContext;

    // ObjectMapper might still be needed for specific request body constructions here
    private ObjectMapper objectMapper = new ObjectMapper();


    // REMOVED: @Given("the base API URL is {string}") - Moved to CommonStepDefinitions
    // REMOVED: @Then("the response status code should be {int}") - Moved to CommonStepDefinitions
    // REMOVED: @Given("I have an invalid JWT token {string}") - Moved to CommonStepDefinitions
    // REMOVED: @Given("I am logged in as a {string} with email {string} and password {string}") - Moved to CommonStepDefinitions
    // REMOVED: @Given("merchant {string} with ID {string} exists") - Moved to CommonStepDefinitions
    // REMOVED: @Given("product {string} with ID {string} belonging to merchant {string} exists") - Moved to CommonStepDefinitions
    // REMOVED: @Given("customer {string} with ID {string} exists") - Moved to CommonStepDefinitions
    // REMOVED: @Then("the response body should contain a message like {string}") - Moved to CommonStepDefinitions
    // REMOVED: @Given("I am logged in as {string} with email {string} and password {string}") // (userAlias version) - Moved to CommonStepDefinitions


    @When("I send a GET request to {string} without authentication")
    public void i_send_a_get_request_to_without_authentication(String endpoint) {
        RequestSpecification rs = (RequestSpecification) scenarioContext.getContext("REQUEST_SPEC");
        assertNotNull(rs, "RequestSpecification not found in ScenarioContext. Ensure CommonStepDefinitions @Before hook ran.");
        // Ensure no lingering auth from a previous scenario if REQUEST_SPEC is somehow reused across scenarios (it shouldn't be with @Before)
        // For unauthenticated, we might want a pristine RequestSpecification
        String baseUrlFromContext = (String) scenarioContext.getContext("BASE_API_URL");
        assertNotNull(baseUrlFromContext, "BASE_API_URL not found in context.");

        Response response = given().baseUri(baseUrlFromContext) // Start fresh for unauthenticated
                                .when()
                                .get(endpoint);
        scenarioContext.setContext("LAST_RESPONSE", response);
    }

    @When("I send a GET request to {string} with the token")
    public void i_send_a_get_request_to_with_the_token(String endpoint) {
        RequestSpecification rs = (RequestSpecification) scenarioContext.getContext("REQUEST_SPEC");
        assertNotNull(rs, "RequestSpecification not found in ScenarioContext.");
        // Token should already be part of 'rs' if login steps in CommonStepDefinitions were called.
        // Or if "I have an invalid JWT token" was called.

        Response response = rs.when().get(endpoint);
        scenarioContext.setContext("LAST_RESPONSE", response);
    }

    @Given("I have a valid JWT token for the logged-in customer")
    public void i_have_a_valid_jwt_token_for_the_logged_in_customer() {
        String token = (String) scenarioContext.getContext("CURRENT_JWT_TOKEN");
        String role = (String) scenarioContext.getContext("LOGGED_IN_USER_ROLE");
        assertNotNull(token, "CURRENT_JWT_TOKEN not found. Ensure customer is logged in via CommonStepDefinitions.");
        assertEquals("CUSTOMER", role, "Logged in user is not a CUSTOMER.");
        // Verification step; actual token setting on REQUEST_SPEC is handled by CommonStepDefinitions login.
    }

    @Given("I have a valid JWT token for the logged-in merchant")
    public void i_have_a_valid_jwt_token_for_the_logged_in_merchant() {
        String token = (String) scenarioContext.getContext("CURRENT_JWT_TOKEN");
        String role = (String) scenarioContext.getContext("LOGGED_IN_USER_ROLE");
        assertNotNull(token, "CURRENT_JWT_TOKEN not found. Ensure merchant is logged in via CommonStepDefinitions.");
        assertEquals("MERCHANT", role, "Logged in user is not a MERCHANT.");
    }

    @When("I send a POST request to {string} with the token and body:")
    public void i_send_a_post_request_to_with_the_token_and_body(String endpoint, String requestBody) {
        RequestSpecification rs = (RequestSpecification) scenarioContext.getContext("REQUEST_SPEC");
        assertNotNull(rs, "RequestSpecification not found in ScenarioContext.");
        // Token should be on rs from login.

        Response response = rs
                    .contentType(ContentType.JSON)
                    .body(requestBody)
                    .when()
                    .post(endpoint);
        scenarioContext.setContext("LAST_RESPONSE", response);
    }

    @Given("I have a valid JWT token for {string}") // User Alias version
    public void i_have_a_valid_jwt_token_for(String userAlias) {
        String token = (String) scenarioContext.getContext("JWT_TOKEN_" + userAlias);
        String currentToken = (String) scenarioContext.getContext("CURRENT_JWT_TOKEN");
        assertNotNull(token, "JWT token for user alias '" + userAlias + "' not found. Ensure user is logged in.");
        assertEquals(token, currentToken, "Token for alias " + userAlias + " is not the CURRENT_JWT_TOKEN.");
        // This step now primarily serves as a validation/checkpoint.
        // The REQUEST_SPEC in context should already be configured by the aliased login step in CommonStepDefs.
    }

    @When("I send a PUT request to {string} with the token and body:")
    public void i_send_a_put_request_to_with_the_token_and_body(String endpoint, String requestBody) {
        RequestSpecification rs = (RequestSpecification) scenarioContext.getContext("REQUEST_SPEC");
        assertNotNull(rs, "RequestSpecification not found in ScenarioContext.");

        Response response = rs
                    .contentType(ContentType.JSON)
                    .body(requestBody)
                    .when()
                    .put(endpoint);
        scenarioContext.setContext("LAST_RESPONSE", response);
    }

    @Given("product {string} with ID {string} belonging to merchant {string} exists with details:")
    public void product_with_id_belonging_to_merchant_exists_with_details(String productAlias, String productId, String merchantIdOrAlias, String productDetailsJson) {
        String merchantId = (String) scenarioContext.getContext(merchantIdOrAlias + "_ID");
         if (merchantId == null) merchantId = merchantIdOrAlias; // Assume raw ID if alias not found

        scenarioContext.setContext(productAlias + "_ID", productId);
        scenarioContext.setContext(productAlias + "_MERCHANT_ID", merchantId);
        scenarioContext.setContext(productAlias + "_DETAILS", productDetailsJson);
        System.out.println("AuthStepDef: Ensuring product " + productAlias + " (ID: " + productId + ") for merchant ID: " + merchantId + " exists with details.");
        // Actual existence check/creation would be part of a more robust test data setup strategy
    }

    @Then("product {string} for merchant {string} should still have original details")
    public void product_for_merchant_should_still_have_original_details(String productAlias, String merchantIdToQuery) {
        String productId = (String) scenarioContext.getContext(productAlias + "_ID");
        String originalDetailsJson = (String) scenarioContext.getContext(productAlias + "_DETAILS");

        RequestSpecification rs = (RequestSpecification) scenarioContext.getContext("REQUEST_SPEC");
        assertNotNull(rs, "RequestSpecification not found in ScenarioContext.");
        assertNotNull(productId, "Product ID for alias " + productAlias + " not found in context.");
        assertNotNull(originalDetailsJson, "Original details for alias " + productAlias + " not found in context.");

        // The token on 'rs' is the one of the user performing the check (e.g., Merchant A)
        Response getResponse = rs.when().get("/merchants/" + merchantIdToQuery + "/products/" + productId);

        assertEquals(200, getResponse.getStatusCode(), "Failed to fetch product " + productId + " for merchant " + merchantIdToQuery + ". Response: " + getResponse.asString());

        try {
            Map<String, Object> originalDetailsMap = objectMapper.readValue(originalDetailsJson, Map.class);
            Map<String, Object> currentDetailsMap = getResponse.jsonPath().getMap("");

            // Compare relevant fields based on what 'originalDetailsJson' contained
            // This needs to be specific to the fields you expect to remain unchanged.
            // Example: if originalDetailsJson was the PUT body for an attempted update.
            if (originalDetailsMap.containsKey("name")) {
                 assertEquals(originalDetailsMap.get("name"), currentDetailsMap.get("name"), "Product name mismatch after failed update attempt.");
            }
            if (originalDetailsMap.containsKey("model")) {
                 assertEquals(originalDetailsMap.get("model"), currentDetailsMap.get("model"), "Product model mismatch.");
            }
             // Add more field comparisons as necessary
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing product details JSON for comparison", e);
        }
        System.out.println("Product " + productId + " still has original details as expected.");
    }

    @When("I send a DELETE request to {string} with the token")
    public void i_send_a_delete_request_to_with_the_token(String endpoint) {
        RequestSpecification rs = (RequestSpecification) scenarioContext.getContext("REQUEST_SPEC");
        assertNotNull(rs, "RequestSpecification not found in ScenarioContext.");

        Response response = rs.when().delete(endpoint);
        scenarioContext.setContext("LAST_RESPONSE", response);
    }

    @Then("product {string} for merchant {string} should still exist")
    public void product_for_merchant_should_still_exist(String productAlias, String merchantIdToQuery) {
        String productId = (String) scenarioContext.getContext(productAlias + "_ID");
        RequestSpecification rs = (RequestSpecification) scenarioContext.getContext("REQUEST_SPEC");

        assertNotNull(rs, "RequestSpecification not found in ScenarioContext.");
        assertNotNull(productId, "Product ID for alias " + productAlias + " not found.");

        Response getResponse = rs.when().get("/merchants/" + merchantIdToQuery + "/products/" + productId);

        assertEquals(200, getResponse.getStatusCode(), "Product " + productId + " for merchant " + merchantIdToQuery + " does not seem to exist (expected 200), or is not accessible. Response: " + getResponse.asString());
    }

    @Given("order {string} with ID {string} belonging to customer {string} exists")
    public void order_with_id_belonging_to_customer_exists(String orderAlias, String orderId, String customerAlias) {
        String customerId = (String) scenarioContext.getContext(customerAlias + "_ID");
        assertNotNull(customerId, "Customer ID for alias " + customerAlias + " not found in CommonStepDefinitions context.");
        scenarioContext.setContext(orderAlias + "_ID", orderId);
        scenarioContext.setContext(orderAlias + "_CUSTOMER_ID", customerId);
        System.out.println("AuthStepDef: Assuming order " + orderAlias + " (ID: " + orderId + ") for customer " + customerAlias + " (ID: " + customerId + ") exists.");
    }

    @Then("the response should not contain order ID {string}")
    public void the_response_should_not_contain_order_id(String forbiddenOrderId) {
        Response lastResponse = (Response) scenarioContext.getContext("LAST_RESPONSE");
        assertNotNull(lastResponse, "Response was null.");
        assertEquals(200, lastResponse.getStatusCode(), "Expected a 200 OK response for order history.");
        String responseBody = lastResponse.getBody().asString();

        // More robust check: parse to list of maps and check 'id' or 'orderId' field
        assertFalse(responseBody.contains("\"orderId\":\"" + forbiddenOrderId + "\""), "Response body contains forbidden order ID " + forbiddenOrderId + " (checking key \"orderId\")");
        assertFalse(responseBody.contains("\"id\":\"" + forbiddenOrderId + "\""), "Response body contains forbidden order ID " + forbiddenOrderId + " (checking key \"id\")");
        // General check as fallback, though less precise
        if (!responseBody.contains("\"orderId\":\"" + forbiddenOrderId + "\"") && !responseBody.contains("\"id\":\"" + forbiddenOrderId + "\"")) {
            assertFalse(responseBody.contains(forbiddenOrderId), "Response body contains forbidden order ID " + forbiddenOrderId + " (broad check)");
        }
    }

    @Given("merchant {string} exists with email {string}")
    public void merchant_exists_with_email(String merchantAlias, String email) {
        scenarioContext.setContext(merchantAlias + "_EMAIL", email);
        System.out.println("AuthStepDef: Assuming merchant " + merchantAlias + " with email " + email + " exists (data setup step).");
    }

    @When("I send a POST request to {string} with body:") // This is for unauthenticated POSTs
    public void i_send_a_post_request_to_with_body(String endpoint, String requestBody) {
        String baseUrlFromContext = (String) scenarioContext.getContext("BASE_API_URL");
        assertNotNull(baseUrlFromContext, "BASE_API_URL not found in context.");

        Response response = given().baseUri(baseUrlFromContext) // Start fresh for unauthenticated
                    .contentType(ContentType.JSON)
                    .body(requestBody)
                    .when()
                    .post(endpoint);
        scenarioContext.setContext("LAST_RESPONSE", response);
    }
}

// Removed UserCredentials class as it should be in CommonStepDefinitions or a DTO package
// Removed commented out ScenarioContext placeholder
