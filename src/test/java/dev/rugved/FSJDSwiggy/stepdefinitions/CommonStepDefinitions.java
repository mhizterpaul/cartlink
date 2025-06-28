package dev.rugved.FSJDSwiggy.stepdefinitions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.rugved.FSJDSwiggy.dto.UserCredentials;
import dev.rugved.FSJDSwiggy.dto.MerchantSignupRequest;
import dev.rugved.FSJDSwiggy.dto.CustomerSignupRequest;
import dev.rugved.FSJDSwiggy.dto.AddressDTO;
import dev.rugved.FSJDSwiggy.dto.ProductRequestDTO;
import dev.rugved.FSJDSwiggy.dto.AddToCartRequestDTO;
import dev.rugved.FSJDSwiggy.dto.CheckoutRequestDTO;
import java.util.Map;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;


import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommonStepDefinitions {

    @Autowired
    protected ScenarioContext scenarioContext;

    private ObjectMapper objectMapper = new ObjectMapper();
    private String baseUrl = "http://localhost:8080";

    @Before
    public void setUp() {
        if (scenarioContext instanceof ResettableContext) {
            ((ResettableContext) scenarioContext).reset();
        }
        RequestSpecification initialRequestSpec = given().baseUri(this.baseUrl + "/api/v1");
        scenarioContext.setContext("REQUEST_SPEC", initialRequestSpec);
        scenarioContext.setContext("BASE_API_URL", this.baseUrl + "/api/v1");
    }

    @After
    public void tearDown() {
        Set<String> keys = scenarioContext.getAllKeys();
        if (keys != null) {
            for (String key : keys) {
                if (key.startsWith("TEMP_FILE_TO_CLEAN_")) {
                    String filePathString = (String) scenarioContext.getContext(key);
                    if (filePathString != null) {
                        Path filePath = Paths.get(filePathString);
                        try {
                            Files.deleteIfExists(filePath);
                            System.out.println("Cleaned up temp file: " + filePathString);
                            Path parentDir = filePath.getParent();
                            if (parentDir != null && parentDir.toString().contains("cucumber_temp_files") && Files.isDirectory(parentDir)) {
                                try (var filesInDir = Files.list(parentDir)) {
                                    if (filesInDir.findAny().isEmpty()) {
                                        Files.deleteIfExists(parentDir);
                                        System.out.println("Cleaned up empty temp directory: " + parentDir);
                                    }
                                }
                            }
                        } catch (IOException e) {
                            System.err.println("Warning: Could not delete temp file/dir during teardown: " + filePathString + " - " + e.getMessage());
                        }
                    }
                }
            }
        }
        if (scenarioContext instanceof ResettableContext) {
             ((ResettableContext) scenarioContext).reset();
        }
    }

    @Given("the base API URL is {string}")
    public void the_base_api_url_is(String url) {
        this.baseUrl = url;
        RequestSpecification initialRequestSpec = given().baseUri(this.baseUrl);
        scenarioContext.setContext("REQUEST_SPEC", initialRequestSpec);
        scenarioContext.setContext("BASE_API_URL", this.baseUrl);
        System.out.println("Base API URL set to: " + url);
    }


    @Then("the response status code should be {int}")
    public void the_response_status_code_should_be(Integer statusCode) {
        Response lastResponse = (Response) scenarioContext.getContext("LAST_RESPONSE");
        assertNotNull(lastResponse, "Response was null, ensure a request was made prior to this step.");
        assertEquals(statusCode.intValue(), lastResponse.getStatusCode());
    }

    @Then("the response body should contain a message like {string}")
    public void the_response_body_should_contain_a_message_like(String messageSubstring) {
        Response lastResponse = (Response) scenarioContext.getContext("LAST_RESPONSE");
        assertNotNull(lastResponse, "Response was null.");
        String responseBody = lastResponse.getBody().asString();
        assertTrue(responseBody.toLowerCase().contains(messageSubstring.toLowerCase()),
                "Response body did not contain substring '" + messageSubstring + "'. Body: " + responseBody);
    }

    @Given("I am logged in as a {string} with email {string} and password {string}")
    public void i_am_logged_in_as_a_with_email_and_password(String role, String email, String password) throws JsonProcessingException {
        String loginEndpointSuffix = "";
        if ("CUSTOMER".equalsIgnoreCase(role)) {
            loginEndpointSuffix = "/customers/login";
        } else if ("MERCHANT".equalsIgnoreCase(role)) {
            loginEndpointSuffix = "/merchants/login";
        } else {
            throw new IllegalArgumentException("Unsupported role for login: " + role);
        }

        UserCredentials credentials = new UserCredentials(email, password);
        String credentialsJson = objectMapper.writeValueAsString(credentials);
        String currentBaseUrl = (String) scenarioContext.getContext("BASE_API_URL");
        if(currentBaseUrl == null) {
            System.err.println("Warning: BASE_API_URL not found in context during login, using default. Ensure @Before hook ran.");
            currentBaseUrl = this.baseUrl + "/api/v1";
        }

        Response loginResponse = given().baseUri(currentBaseUrl)
                                    .contentType(ContentType.JSON)
                                    .body(credentialsJson)
                                    .when()
                                    .post(loginEndpointSuffix);

        assertEquals(200, loginResponse.getStatusCode(), "Login failed for " + role + " with email " + email + ". Response: " + loginResponse.getBody().asString());
        String token = loginResponse.jsonPath().getString("token");
        assertNotNull(token, "Token not found in login response for " + role + " " + email);

        scenarioContext.setContext("CURRENT_JWT_TOKEN", token);
        scenarioContext.setContext("LOGGED_IN_USER_ROLE", role);
        scenarioContext.setContext("LOGGED_IN_USER_EMAIL", email);

        if ("MERCHANT".equalsIgnoreCase(role)) {
            String merchantId = loginResponse.jsonPath().getString("merchantId");
             assertNotNull(merchantId, "merchantId not found in merchant login response.");
            scenarioContext.setContext(email + "_ID", merchantId);
        }
        if ("CUSTOMER".equalsIgnoreCase(role)) {
            String customerId = loginResponse.jsonPath().getString("customer.id");
            if (customerId == null) {
                customerId = loginResponse.jsonPath().getString("customer.customerId");
            }
            assertNotNull(customerId, "customerId not found in customer login response for user " + email + ". Response: " + loginResponse.asString());
            scenarioContext.setContext(email + "_CUSTOMER_ID", customerId);
            String loggedInUserAlias = (String) scenarioContext.getContext("LOGGED_IN_USER_ALIAS");
            if (loggedInUserAlias != null && userAliasMatchesEmail(loggedInUserAlias, email)) {
                scenarioContext.setContext(loggedInUserAlias + "_ID", customerId);
            }
        }

        RequestSpecification rs = ((RequestSpecification) scenarioContext.getContext("REQUEST_SPEC"))
                                    .header("Authorization", "Bearer " + token);
        scenarioContext.setContext("REQUEST_SPEC", rs);
        System.out.println("Logged in as " + role + " (" + email + "). Token stored. REQUEST_SPEC updated with auth header.");
    }

    private boolean userAliasMatchesEmail(String alias, String email) {
        String aliasEmail = (String) scenarioContext.getContext(alias + "_EMAIL");
        return email.equals(aliasEmail);
    }


    @Given("I am logged in as {string} identified by email {string} and password {string}")
    public void i_am_logged_in_as_user_alias_with_email_and_password(String userAlias, String email, String password) throws JsonProcessingException {
        String role = "MERCHANT";
        if (userAlias.toLowerCase().contains("customer")) {
            role = "CUSTOMER";
        }
        scenarioContext.setContext("LOGGED_IN_USER_ALIAS", userAlias);
        scenarioContext.setContext(userAlias + "_EMAIL", email);

        i_am_logged_in_as_a_with_email_and_password(role, email, password);
        scenarioContext.setContext("JWT_TOKEN_" + userAlias, scenarioContext.getContext("CURRENT_JWT_TOKEN"));
        System.out.println("User alias '" + userAlias + "' (role: " + role + ") logged in.");
    }

    @Given("I have an invalid JWT token {string}")
    public void i_have_an_invalid_jwt_token(String token) {
        scenarioContext.setContext("CURRENT_JWT_TOKEN", token);
        RequestSpecification rs = ((RequestSpecification) scenarioContext.getContext("REQUEST_SPEC"))
                                    .header("Authorization", "Bearer " + token);
        scenarioContext.setContext("REQUEST_SPEC", rs);
        System.out.println("Set invalid JWT token. REQUEST_SPEC updated.");
    }

    @Given("merchant {string} with ID {string} exists")
    public void merchant_with_id_exists(String merchantAlias, String expectedMerchantId) throws JsonProcessingException {
        String existingId = (String) scenarioContext.getContext(merchantAlias + "_ID");
        String existingToken = (String) scenarioContext.getContext(merchantAlias + "_JWT_TOKEN");

        if (existingId != null && existingToken != null) {
            scenarioContext.setContext("CURRENT_JWT_TOKEN", existingToken);
            RequestSpecification rs = ((RequestSpecification) scenarioContext.getContext("REQUEST_SPEC"))
                                 .header("Authorization", "Bearer " + existingToken);
            scenarioContext.setContext("REQUEST_SPEC", rs);
            scenarioContext.setContext("LOGGED_IN_USER_ALIAS", merchantAlias);
            scenarioContext.setContext("LOGGED_IN_USER_ROLE", "MERCHANT");
            scenarioContext.setContext("LOGGED_IN_USER_EMAIL", scenarioContext.getContext(merchantAlias + "_EMAIL"));
            System.out.println("CommonStepDef: Merchant " + merchantAlias + " (ID: " + existingId + ") already exists in context. Token re-applied.");
            return;
        }

        String uniqueEmail = merchantAlias.replaceAll("[^a-zA-Z0-9]", "") + "_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        String password = "password123";

        MerchantSignupRequest signupRequest = new MerchantSignupRequest(
                uniqueEmail, password, merchantAlias.split("_")[0] + "Name", "LastName", "http://example.com/image.png");
        String requestBody = objectMapper.writeValueAsString(signupRequest);
        String signupUrl = (String) scenarioContext.getContext("BASE_API_URL");
        if (signupUrl == null) signupUrl = this.baseUrl + "/api/v1";

        Response response = given().baseUri(signupUrl)
                                .contentType(ContentType.JSON).body(requestBody)
                                .when().post("/merchants/signup");

        assertEquals(201, response.getStatusCode(), "Merchant signup failed for " + merchantAlias + ". Response: " + response.getBody().asString());

        String actualMerchantId = response.jsonPath().getString("merchantId");
        String token = response.jsonPath().getString("token");
        assertNotNull(actualMerchantId, "merchantId not found in signup response.");
        assertNotNull(token, "token not found in signup response.");

        scenarioContext.setContext(merchantAlias + "_ID", actualMerchantId);
        scenarioContext.setContext(merchantAlias + "_EMAIL", uniqueEmail);
        scenarioContext.setContext(merchantAlias + "_PASSWORD", password);
        scenarioContext.setContext(merchantAlias + "_JWT_TOKEN", token);
        scenarioContext.setContext("CURRENT_JWT_TOKEN", token);
        scenarioContext.setContext("LOGGED_IN_USER_ROLE", "MERCHANT");
        scenarioContext.setContext("LOGGED_IN_USER_EMAIL", uniqueEmail);
        scenarioContext.setContext("LOGGED_IN_USER_ALIAS", merchantAlias);

        RequestSpecification rs = ((RequestSpecification) scenarioContext.getContext("REQUEST_SPEC")).header("Authorization", "Bearer " + token);
        scenarioContext.setContext("REQUEST_SPEC", rs);
        System.out.println("CommonStepDef: Merchant " + merchantAlias + " created with ACTUAL ID " + actualMerchantId + ". Now logged in.");
    }

    @Given("product {string} with ID {string} belonging to merchant {string} exists")
    public void product_with_id_belonging_to_merchant_exists(String productAlias, String expectedProductId, String merchantAlias) throws JsonProcessingException {
        String merchantId = (String) scenarioContext.getContext(merchantAlias + "_ID");
        String merchantToken = (String) scenarioContext.getContext(merchantAlias + "_JWT_TOKEN");

        if (merchantId == null || merchantToken == null) {
            System.out.println("Merchant " + merchantAlias + " not found or not logged in for product creation. Attempting to create/log in merchant first.");
            merchant_with_id_exists(merchantAlias, "temp_mid_for_" + merchantAlias);
            merchantId = (String) scenarioContext.getContext(merchantAlias + "_ID");
            merchantToken = (String) scenarioContext.getContext(merchantAlias + "_JWT_TOKEN");
        }
        assertNotNull(merchantId, "Failed to ensure merchant " + merchantAlias + " exists and has an ID for product creation.");
        assertNotNull(merchantToken, "Failed to ensure merchant " + merchantAlias + " is logged in (no token for product creation).");

        String existingActualProductId = (String) scenarioContext.getContext(productAlias + "_ACTUAL_ID");
        if (existingActualProductId != null) {
            scenarioContext.setContext(productAlias + "_ID", existingActualProductId);
            System.out.println("CommonStepDef: Product " + productAlias + " (Actual ID: " + existingActualProductId + ") already exists in context for merchant " + merchantAlias + " (ID: " + merchantId + ").");
            return;
        }

        String productName = productAlias + " Name " + UUID.randomUUID().toString().substring(0, 4);
        String model = productAlias + "_Model_" + UUID.randomUUID().toString().substring(0,4);

        Map<String, Object> productDetailsMap = Map.of("price", 10.99, "stock", 100);

        ProductRequestDTO productRequest = new ProductRequestDTO(
            productName, model, "TestBrand", "Electronics",
            "GenericDevice",
            "Description for " + productName,
            productDetailsMap
        );
        String requestBody = objectMapper.writeValueAsString(productRequest);

        String productsUrlPath = "/merchants/" + merchantId + "/products";

        RequestSpecification currentRequestSpec = (RequestSpecification) scenarioContext.getContext("REQUEST_SPEC");
        assertNotNull(currentRequestSpec, "REQUEST_SPEC not found in context for product creation.");

        Response response = currentRequestSpec
                                .contentType(ContentType.JSON)
                                .body(requestBody)
                                .when()
                                .post(productsUrlPath);

        assertEquals(201, response.getStatusCode(), "Product creation failed for " + productAlias + ". Merchant: " + merchantAlias + " (ID: " + merchantId + "). URL: " + scenarioContext.getContext("BASE_API_URL") + productsUrlPath + ". Body: " + requestBody + ". Response: " + response.getBody().asString());

        String actualProductId = response.jsonPath().getString("productId");
        String merchantProductId = response.jsonPath().getString("merchantProductId");
        assertNotNull(actualProductId, "productId not found in product creation response.");
        assertNotNull(merchantProductId, "merchantProductId not found in product creation response.");

        scenarioContext.setContext(productAlias + "_ID", actualProductId);
        scenarioContext.setContext(productAlias + "_ACTUAL_ID", actualProductId);
        scenarioContext.setContext(productAlias + "_EXPECTED_ID", expectedProductId);
        scenarioContext.setContext(productAlias + "_MERCHANT_PRODUCT_ID", merchantProductId);
        scenarioContext.setContext(productAlias + "_MERCHANT_ID", merchantId);

        System.out.println("CommonStepDef: Product " + productAlias + " created with ACTUAL ID " + actualProductId + " for merchant " + merchantAlias + " (ID: " + merchantId + "). Gherkin referenced ID: " + expectedProductId);
    }

    @Given("customer {string} with ID {string} exists")
    public void customer_with_id_exists(String customerAlias, String expectedCustomerId) throws JsonProcessingException {
        String existingActualId = (String) scenarioContext.getContext(customerAlias + "_ID");
        String customerEmail = (String) scenarioContext.getContext(customerAlias + "_EMAIL");

        if (existingActualId != null && customerEmail != null) {
            System.out.println("CommonStepDef: Customer " + customerAlias + " (ID: " + existingActualId + ") already exists in context. Stored Email: " + customerEmail);
            return;
        }

        String uniqueEmail = customerAlias.replaceAll("[^a-zA-Z0-9]", "") + "_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        String uniquePhoneSuffix = UUID.randomUUID().toString().replaceAll("-","").substring(0, 7);
        String phoneNumber = "+234809" + uniquePhoneSuffix;

        AddressDTO address = new AddressDTO("123 Test St", "TestCity", "TestState", "TestCountry", "12345");
        CustomerSignupRequest signupRequest = new CustomerSignupRequest(
            uniqueEmail,
            customerAlias.split("_")[0] + "Name",
            "LastName",
            phoneNumber,
            address
        );
        String requestBody = objectMapper.writeValueAsString(signupRequest);
        String signupUrl = (String) scenarioContext.getContext("BASE_API_URL");
        if (signupUrl == null) signupUrl = this.baseUrl + "/api/v1";

        Response response = given().baseUri(signupUrl)
                                .contentType(ContentType.JSON)
                                .body(requestBody)
                                .when()
                                .post("/customers/signup");

        assertEquals(201, response.getStatusCode(), "Customer signup failed for " + customerAlias + ". Response: " + response.getBody().asString());

        String actualCustomerId = response.jsonPath().getString("id");
        if (actualCustomerId == null) {
            actualCustomerId = response.jsonPath().getString("customerId");
        }
        assertNotNull(actualCustomerId, "Customer ID not found in signup response for " + customerAlias + ". Response: " + response.asString());

        scenarioContext.setContext(customerAlias + "_ID", actualCustomerId);
        scenarioContext.setContext(customerAlias + "_EMAIL", uniqueEmail);
        scenarioContext.setContext(customerAlias + "_PASSWORD", "password123");
        scenarioContext.setContext(customerAlias + "_PHONE", phoneNumber);
        scenarioContext.setContext(customerAlias + "_ADDRESS", address);


        System.out.println("CommonStepDef: Customer " + customerAlias + " created with ACTUAL ID " + actualCustomerId + " (Gherkin referenced ID: " + expectedCustomerId + "). Email: " + uniqueEmail);
    }

    @Given("customer {string} with ID {string} exists and is logged in")
    public void customer_with_id_exists_and_is_logged_in(String customerAlias, String customerId) throws JsonProcessingException {
        customer_with_id_exists(customerAlias, customerId);

        String customerEmail = (String) scenarioContext.getContext(customerAlias + "_EMAIL");
        String customerPassword = (String) scenarioContext.getContext(customerAlias + "_PASSWORD");

        assertNotNull(customerEmail, "Email for customer alias " + customerAlias + " not found in context after creation step.");
        assertNotNull(customerPassword, "Password for customer alias " + customerAlias + " not found in context.");

        i_am_logged_in_as_a_with_email_and_password("CUSTOMER", customerEmail, customerPassword);

        scenarioContext.setContext("LOGGED_IN_USER_ALIAS", customerAlias);
        System.out.println("CommonStepDef: Customer " + customerAlias + " (ID: " + scenarioContext.getContext(customerAlias+"_ID") + ") is now logged in.");
    }

}

interface ResettableContext {
    void reset();
    Set<String> getAllKeys();
}

class MerchantSignupRequest {
    public String email;
    public String password;
    public String firstName;
    public String lastName;
    public String image;

    public MerchantSignupRequest(String email, String password, String firstName, String lastName, String image) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.image = image;
    }
}

class ProductRequestDTO {
    public String name;
    public String model;
    public String brand;
    public String category;
    public String productType;
    public String description;
    public Map<String, Object> productDetails;

    public ProductRequestDTO(String name, String model, String brand, String category, String productType, String description, Map<String, Object> productDetails) {
        this.name = name;
        this.model = model;
        this.brand = brand;
        this.category = category;
        this.productType = productType;
        this.description = description;
        this.productDetails = productDetails;
    }
}

// Added DTOs for AddToCart and Checkout
class AddToCartRequestDTO {
    public String productId;
    public int quantity;

    public AddToCartRequestDTO(String productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }
}

class CheckoutRequestDTO {
    public String paymentMethod;
    public String currency;

    public CheckoutRequestDTO(String paymentMethod, String currency) {
        this.paymentMethod = paymentMethod;
        this.currency = currency;
    }
}
/*
// Assuming these DTOs are in dev.rugved.FSJDSwiggy.dto and accessible
package dev.rugved.FSJDSwiggy.dto;

class AddressDTO {
    public String street;
    public String city;
    public String state;
    public String country;
    public String postalCode;

    public AddressDTO(String street, String city, String state, String country, String postalCode) {
        this.street = street;
        this.city = city;
        this.state = state;
        this.country = country;
        this.postalCode = postalCode;
    }
}

class CustomerSignupRequest {
    public String email;
    public String firstName;
    public String lastName;
    public String phoneNumber;
    public AddressDTO address;

    public CustomerSignupRequest(String email, String firstName, String lastName, String phoneNumber, AddressDTO address) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }
}
*/
