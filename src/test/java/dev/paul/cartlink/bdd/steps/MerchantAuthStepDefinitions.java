package dev.paul.cartlink.bdd.steps;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.paul.cartlink.merchant.model.Merchant; // Corrected path
import dev.paul.cartlink.merchant.repository.MerchantRepository; // Corrected path
import dev.paul.cartlink.merchant.dto.SignUpRequest; // Corrected path and class name
import dev.paul.cartlink.bdd.context.ScenarioContext;
import io.cucumber.java.After; // Correct hook import
import io.cucumber.java.Before; // Correct hook import
import io.cucumber.java.en.Given; // Correct Gherkin keyword import
import io.cucumber.java.en.Then; // Correct Gherkin keyword import
import io.cucumber.java.en.When; // Correct Gherkin keyword import
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class MerchantAuthStepDefinitions {

    private static final Logger logger = LoggerFactory.getLogger(MerchantAuthStepDefinitions.class);

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MerchantRepository merchantRepository; // For setting up preconditions

    @Autowired
    private PasswordEncoder passwordEncoder; // For creating users for preconditions

    @Autowired
    private ObjectMapper objectMapper; // For converting objects to JSON strings

    @Autowired
    private ScenarioContext scenarioContext;

    private ResponseEntity<String> latestResponse;
    // private Map<String, String> sharedData = new HashMap<>(); // To share data between steps, e.g. tokens. Will use ScenarioContext

    @Before
    public void setUp() {
        // Clean up database before each scenario if needed, or use @DirtiesContext
        // For now, specific cleanup will be in @After or managed by @Transactional if applicable
        merchantRepository.deleteAll(); // Simple cleanup for now
    }

    @After
    public void tearDown() {
        // Clean up any state after scenarios, e.g., created users for preconditions.
    }

    @When("a POST request is made to {string} with an authenticated user and the following body:")
    public void a_post_request_is_made_to_with_an_authenticated_user_and_the_following_body(String path, String requestBody) {
        String apiBaseUrl = scenarioContext.getString("apiBaseUrl");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(scenarioContext.getString("merchantToken")); // Use merchantToken from ScenarioContext
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        latestResponse = restTemplate.postForEntity(apiBaseUrl + path, entity, String.class);
        scenarioContext.set("latestResponse", latestResponse); // Store in context
        logger.info("Authenticated POST request to {}{} with body: {}", apiBaseUrl, path, requestBody);
        logger.info("Response status: {}, body: {}", latestResponse.getStatusCode(), latestResponse.getBody());
    }


    @When("a PUT request is made to {string} with an authenticated user and the following body:")
    public void a_put_request_is_made_to_with_an_authenticated_user_and_the_following_body(String path, String requestBody) {
        String apiBaseUrl = scenarioContext.getString("apiBaseUrl");
        String resolvedPath = resolvePathVariables(path);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (scenarioContext.containsKey("merchantToken")) { // Use merchantToken from ScenarioContext
            headers.setBearerAuth(scenarioContext.getString("merchantToken"));
        }
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        latestResponse = restTemplate.exchange(apiBaseUrl + resolvedPath, HttpMethod.PUT, entity, String.class);
        scenarioContext.set("latestResponse", latestResponse); // Store in context
        logger.info("Authenticated PUT request to {}{} with body: {}", apiBaseUrl, resolvedPath, requestBody);
        logger.info("Response status: {}, body: {}", latestResponse.getStatusCode(), latestResponse.getBody());
    }


    @When("a GET request is made to {string} with an authenticated user")
    public void a_get_request_is_made_to_with_an_authenticated_user(String path) {
        String apiBaseUrl = scenarioContext.getString("apiBaseUrl");
        String resolvedPath = resolvePathVariables(path);
        HttpHeaders headers = new HttpHeaders();
        if (scenarioContext.containsKey("merchantToken")) { // Use merchantToken from ScenarioContext
             headers.setBearerAuth(scenarioContext.getString("merchantToken"));
        }
        HttpEntity<String> entity = new HttpEntity<>(headers);
        latestResponse = restTemplate.exchange(apiBaseUrl + resolvedPath, HttpMethod.GET, entity, String.class);
        scenarioContext.set("latestResponse", latestResponse); // Store in context
        logger.info("Authenticated GET request to {}{}", apiBaseUrl, resolvedPath);
        logger.info("Response status: {}, body: {}", latestResponse.getStatusCode(), latestResponse.getBody());
    }

    @When("a PUT request is made to {string} without authentication and the following body:")
    public void a_put_request_is_made_to_without_authentication_and_the_following_body(String path, String requestBody) {
        String apiBaseUrl = scenarioContext.getString("apiBaseUrl");
        String resolvedPath = resolvePathVariables(path);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        latestResponse = restTemplate.exchange(apiBaseUrl + resolvedPath, HttpMethod.PUT, entity, String.class);
        scenarioContext.set("latestResponse", latestResponse); // Store in context
        logger.info("Unauthenticated PUT request to {}{} with body: {}", apiBaseUrl, resolvedPath, requestBody);
        logger.info("Response status: {}, body: {}", latestResponse.getStatusCode(), latestResponse.getBody());
    }

    @When("a GET request is made to {string} without authentication")
    public void a_get_request_is_made_to_without_authentication(String path) {
        String apiBaseUrl = scenarioContext.getString("apiBaseUrl");
        String resolvedPath = resolvePathVariables(path);
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        latestResponse = restTemplate.exchange(apiBaseUrl + resolvedPath, HttpMethod.GET, entity, String.class);
        scenarioContext.set("latestResponse", latestResponse); // Store in context
        logger.info("Unauthenticated GET request to {}{}", apiBaseUrl, resolvedPath);
        logger.info("Response status: {}, body: {}", latestResponse.getStatusCode(), latestResponse.getBody());
    }


    // This step is now in CommonStepDefinitions.java
    // @Then("the response body should contain a {string}")
    // public void the_response_body_should_contain_a(String jsonPath) {
    //     String responseBody = latestResponse.getBody();
    //     assertThat(responseBody).isNotNull();
    //     try {
    //         // Using Jayway JsonPath to check for existence and non-null/non-empty value
    //         com.jayway.jsonpath.JsonPath.read(responseBody, "$." + jsonPath);
    //     } catch (com.jayway.jsonpath.PathNotFoundException e) {
    //         throw new AssertionError("JSON path '" + jsonPath + "' not found in response body: " + responseBody, e);
    //     } catch (Exception e) {
    //         throw new AssertionError("Error reading JSON path '" + jsonPath + "' in response body: " + responseBody, e);
    //     }
    // }

    // @Then("the response body should contain {string} with boolean value {string}")
    // public void the_response_body_should_contain_with_boolean_value(String jsonPath, String expectedValueStr) {
    //     String responseBody = latestResponse.getBody();
    //     assertThat(responseBody).isNotNull();
    //     boolean expectedValue = Boolean.parseBoolean(expectedValueStr);
    //     try {
    //         boolean actualValue = com.jayway.jsonpath.JsonPath.read(responseBody, "$." + jsonPath);
    //         assertThat(actualValue).isEqualTo(expectedValue);
    //     } catch (com.jayway.jsonpath.PathNotFoundException e) {
    //         throw new AssertionError("JSON path '" + jsonPath + "' not found in response body: " + responseBody, e);
    //     } catch (Exception e) {
    //         throw new AssertionError("Error reading JSON path '" + jsonPath + "' in response body: " + responseBody, e);
    //     }
    // }


    // @Then("the response body should contain a message like {string}")
    // public void the_response_body_should_contain_a_message_like(String messageSubstring) {
    //     String responseBody = latestResponse.getBody();
    //     assertThat(responseBody).isNotNull();
    //     // This is a simple check. More robust would be to parse JSON and check a specific message field.
    //     // For now, checking if the substring exists.
    //     // Example: {"message": "Email already exists"} or {"error": "Validation failed", "details": ["Email already exists"]}
    //     // This might need adjustment based on actual error response structure.
    //
    //     // Attempt to parse as JSON and find a "message" or "error" field containing the substring
    //     boolean found = false;
    //     try {
    //         // Try to find a top-level "message"
    //         String message = com.jayway.jsonpath.JsonPath.read(responseBody, "$.message");
    //         if (message != null && message.contains(messageSubstring)) {
    //             found = true;
    //         }
    //     } catch (Exception e) { /* Path not found, ignore */ }
    //
    //     if (!found) {
    //         try {
    //              // Try to find a top-level "error"
    //             String error = com.jayway.jsonpath.JsonPath.read(responseBody, "$.error");
    //              if (error != null && error.contains(messageSubstring)) {
    //                 found = true;
    //             }
    //         } catch (Exception e) { /* Path not found, ignore */ }
    //     }
    //
    //     if (!found) {
    //          // Fallback to raw string search if specific fields are not found or parsing fails
    //         assertThat(responseBody).containsIgnoringCase(messageSubstring);
    //     } else {
    //         // If found via JsonPath, this assertion is implicitly true.
    //         // Could add specific assertion here if needed, e.g. assertThat(message).contains(messageSubstring)
    //     }
    // }

    @Given("a merchant already exists with email {string}")
    public void a_merchant_already_exists_with_email(String email) {
        if (merchantRepository.findByEmail(email).isEmpty()) {
            Merchant merchant = new Merchant();
            merchant.setEmail(email);
            merchant.setPassword(passwordEncoder.encode("Password123!")); // Dummy password
            merchant.setFirstName("Existing");
            merchant.setLastName("User");
            merchant.setImage("http://example.com/image.png"); // Corrected setter
            merchantRepository.save(merchant);
            logger.info("Created precondition merchant with email: {}", email);
        }
    }

    @Given("a merchant already exists with email {string} and password {string}")
    public void a_merchant_already_exists_with_email_and_password(String email, String password) {
        if (merchantRepository.findByEmail(email).isEmpty()) {
            Merchant merchant = new Merchant();
            merchant.setEmail(email);
            merchant.setPassword(passwordEncoder.encode(password)); // Encode the password
            merchant.setFirstName("Existing");
            merchant.setLastName("User with Password");
            merchant.setImage("http://example.com/image.png"); // Corrected setter
            merchantRepository.save(merchant);
            logger.info("Created precondition merchant with email: {} and specific password.", email);
        }
    }

    @Given("a merchant {string} has requested a password reset and received a token {string}")
    public void a_merchant_has_requested_password_reset(String email, String token) {
        Merchant merchant = merchantRepository.findByEmail(email).orElseGet(() -> {
            Merchant newMerchant = new Merchant();
            newMerchant.setEmail(email);
            newMerchant.setPassword(passwordEncoder.encode("SomePassword123!")); // Dummy password
            newMerchant.setFirstName("Reset");
            newMerchant.setLastName("User");
            return merchantRepository.save(newMerchant);
        });

        merchant.setPasswordResetToken(token);
        merchant.setPasswordResetTokenExpiry(java.time.LocalDateTime.now().plusHours(1)); // Valid for 1 hour
        merchantRepository.save(merchant);
        logger.info("Set up merchant {} with password reset token {}", email, token);
    }

    // This step is now in CommonStepDefinitions.java
    // @Given("a merchant is logged in with email {string} and password {string}")
    // public void a_merchant_is_logged_in_with_email_and_password(String email, String password) throws JsonProcessingException {
    //     // Ensure user exists, or create if necessary for the test
    //     if (merchantRepository.findByEmail(email).isEmpty()) {
    //         Merchant merchant = new Merchant();
    //         merchant.setEmail(email);
    //         merchant.setPassword(passwordEncoder.encode(password));
    //         merchant.setFirstName("Login");
    //         merchant.setLastName("User");
    //         merchant.setImage("http://example.com/login.png"); // Corrected setter
    //         merchantRepository.save(merchant);
    //         logger.info("Created merchant for login test with email: {}", email);
    //     }
    //
    //     Map<String, String> loginRequest = new HashMap<>();
    //     loginRequest.put("email", email);
    //     loginRequest.put("password", password);
    //
    //     HttpHeaders headers = new HttpHeaders();
    //     headers.setContentType(MediaType.APPLICATION_JSON);
    //     HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(loginRequest), headers);
    //
    //     ResponseEntity<String> loginResponse = restTemplate.postForEntity(scenarioContext.getString("apiBaseUrl") + "/merchants/login", entity, String.class);
    //     assertThat(loginResponse.getStatusCodeValue()).isEqualTo(200);
    //
    //     String responseBody = loginResponse.getBody();
    //     assertThat(responseBody).isNotNull();
    //     String token = com.jayway.jsonpath.JsonPath.read(responseBody, "$.token");
    //     String merchantId = com.jayway.jsonpath.JsonPath.read(responseBody, "$.merchantId").toString();
    //     assertThat(token).isNotBlank();
    //     scenarioContext.set("merchantToken", token); // Use ScenarioContext
    //     scenarioContext.set("merchantId", merchantId); // Use ScenarioContext
    //     logger.info("Merchant {} logged in successfully. Token and merchantId stored.", email);
    // }

    private String resolvePathVariables(String path) {
        String resolvedPath = path;
        if (path.contains("{merchantId}")) {
            String merchantId = scenarioContext.containsKey("merchantId") ? scenarioContext.getString("merchantId") : "UNKNOWN_MERCHANT_ID";
            resolvedPath = path.replace("{merchantId}", merchantId);
        }
        // Add more replacements if other path variables are used
        return resolvedPath;
    }
}
