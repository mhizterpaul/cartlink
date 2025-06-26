package dev.paul.cartlink.bdd.steps;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.paul.cartlink.customer.model.Customer;
import dev.paul.cartlink.customer.repository.CustomerRepository;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class CustomerStepDefinitions {

    private static final Logger logger = LoggerFactory.getLogger(CustomerStepDefinitions.class);

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // Shared state between steps
    private String apiBaseUrl;
    private ResponseEntity<String> latestResponse;
    private Map<String, String> sharedData = new HashMap<>(); // For tokens, customer IDs etc.

    @Before
    public void setUp() {
        // Consider more targeted cleanup if tests interfere
        customerRepository.deleteAll();
        logger.info("Cleared customer repository before scenario.");
    }

    @After
    public void tearDown() {
        // Cleanup if necessary
    }

    // --- Generic Steps (can be refactored into a common class later) ---

    @Given("the API base URL is {string}")
    public void the_api_base_url_is(String baseUrl) {
        this.apiBaseUrl = baseUrl;
    }

    // This step can be moved to a common/generic step definitions file
    @When("a POST request is made to {string} with the following body:")
    public void a_post_request_is_made_to_with_body(String path, String requestBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        latestResponse = restTemplate.postForEntity(apiBaseUrl + path, entity, String.class);
        logger.info("POST request to {}{} with body: {}", apiBaseUrl, path, requestBody);
        logger.info("Response status: {}, body: {}", latestResponse.getStatusCode(), latestResponse.getBody());
    }

    @When("a PUT request is made to {string} with the following body:") // Unauthenticated PUT
    public void a_put_request_is_made_to_with_body(String path, String requestBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        latestResponse = restTemplate.exchange(apiBaseUrl + path, HttpMethod.PUT, entity, String.class);
        logger.info("PUT request to {}{} with body: {}", apiBaseUrl, path, requestBody);
        logger.info("Response status: {}, body: {}", latestResponse.getStatusCode(), latestResponse.getBody());
    }

    @When("a GET request is made to {string}") // Unauthenticated GET
    public void a_get_request_is_made_to(String path) {
        HttpEntity<Void> entity = new HttpEntity<>(new HttpHeaders());
        latestResponse = restTemplate.exchange(apiBaseUrl + path, HttpMethod.GET, entity, String.class);
        logger.info("GET request to {}{}", apiBaseUrl, path);
        logger.info("Response status: {}, body: {}", latestResponse.getStatusCode(), latestResponse.getBody());
    }

    // --- Customer Specific Authenticated Steps ---

    @When("a POST request is made to {string} with an authenticated customer and the following body:")
    public void a_post_request_is_made_to_with_an_authenticated_customer_and_the_following_body(String path, String requestBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(sharedData.get("customerToken")); // Assumes customerToken was stored
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        latestResponse = restTemplate.postForEntity(apiBaseUrl + path, entity, String.class);
        logger.info("Authenticated Customer POST request to {}{} with body: {}", apiBaseUrl, path, requestBody);
        logger.info("Response status: {}, body: {}", latestResponse.getStatusCode(), latestResponse.getBody());
    }

    @When("a PUT request is made to {string} with an authenticated customer and the following body:")
    public void a_put_request_is_made_to_with_an_authenticated_customer_and_the_following_body(String path, String requestBody) {
        String resolvedPath = resolveCustomerPathVariables(path);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (sharedData.containsKey("customerToken")) {
            headers.setBearerAuth(sharedData.get("customerToken"));
        }
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        latestResponse = restTemplate.exchange(apiBaseUrl + resolvedPath, HttpMethod.PUT, entity, String.class);
        logger.info("Authenticated Customer PUT request to {}{} with body: {}", apiBaseUrl, resolvedPath, requestBody);
        logger.info("Response status: {}, body: {}", latestResponse.getStatusCode(), latestResponse.getBody());
    }

    @When("a GET request is made to {string} with an authenticated customer")
    public void a_get_request_is_made_to_with_an_authenticated_customer(String path) {
        String resolvedPath = resolveCustomerPathVariables(path);
        HttpHeaders headers = new HttpHeaders();
        if (sharedData.containsKey("customerToken")) {
             headers.setBearerAuth(sharedData.get("customerToken"));
        }
        HttpEntity<String> entity = new HttpEntity<>(headers);
        latestResponse = restTemplate.exchange(apiBaseUrl + resolvedPath, HttpMethod.GET, entity, String.class);
        logger.info("Authenticated Customer GET request to {}{}", apiBaseUrl, resolvedPath);
        logger.info("Response status: {}, body: {}", latestResponse.getStatusCode(), latestResponse.getBody());
    }

    @When("a DELETE request is made to {string} with an authenticated customer")
    public void a_delete_request_is_made_to_with_an_authenticated_customer(String path) {
        String resolvedPath = resolveCustomerPathVariables(path);
        HttpHeaders headers = new HttpHeaders();
        if (sharedData.containsKey("customerToken")) {
            headers.setBearerAuth(sharedData.get("customerToken"));
        }
        HttpEntity<String> entity = new HttpEntity<>(headers);
        latestResponse = restTemplate.exchange(apiBaseUrl + resolvedPath, HttpMethod.DELETE, entity, String.class);
        logger.info("Authenticated Customer DELETE request to {}{}", apiBaseUrl, resolvedPath);
        logger.info("Response status: {}, body: {}", latestResponse.getStatusCode(), latestResponse.getBody());
    }


    // This step can be moved to a common/generic step definitions file
    @Then("the response status code should be {int}")
    public void the_response_status_code_should_be(Integer statusCode) {
        assertThat(latestResponse.getStatusCodeValue()).isEqualTo(statusCode);
    }

    // This step can be moved to a common/generic step definitions file
    @Then("the response body should contain a {string}")
    public void the_response_body_should_contain_a(String jsonPath) {
        String responseBody = latestResponse.getBody();
        assertThat(responseBody).isNotNull();
        try {
            com.jayway.jsonpath.JsonPath.read(responseBody, "$." + jsonPath);
        } catch (com.jayway.jsonpath.PathNotFoundException e) {
            throw new AssertionError("JSON path '" + jsonPath + "' not found in response body: " + responseBody, e);
        } catch (Exception e) {
            throw new AssertionError("Error reading JSON path '" + jsonPath + "' in response body: " + responseBody, e);
        }
    }

    // This step can be moved to a common/generic step definitions file
    @Then("the response body should contain {string} with value {string}")
    public void the_response_body_should_contain_with_value(String jsonPath, String expectedValue) {
        String responseBody = latestResponse.getBody();
        assertThat(responseBody).isNotNull();
        try {
            String actualValue = com.jayway.jsonpath.JsonPath.read(responseBody, "$." + jsonPath).toString();
            assertThat(actualValue).isEqualTo(expectedValue);
        } catch (com.jayway.jsonpath.PathNotFoundException e) {
            throw new AssertionError("JSON path '" + jsonPath + "' not found in response body: " + responseBody, e);
        } catch (Exception e) {
            throw new AssertionError("Error reading JSON path '" + jsonPath + "' in response body: " + responseBody, e);
        }
    }

    @Then("the response body should contain {string} with boolean value {string}")
    public void the_response_body_should_contain_with_boolean_value(String jsonPath, String expectedValueStr) {
        String responseBody = latestResponse.getBody();
        assertThat(responseBody).isNotNull();
        boolean expectedValue = Boolean.parseBoolean(expectedValueStr);
        try {
            boolean actualValue = com.jayway.jsonpath.JsonPath.read(responseBody, "$." + jsonPath);
            assertThat(actualValue).isEqualTo(expectedValue);
        } catch (com.jayway.jsonpath.PathNotFoundException e) {
            throw new AssertionError("JSON path '" + jsonPath + "' not found in response body: " + responseBody, e);
        } catch (Exception e) {
            throw new AssertionError("Error reading JSON path '" + jsonPath + "' in response body: " + responseBody, e);
        }
    }

    // This step can be moved to a common/generic step definitions file
    @Then("the response body should contain a message like {string}")
    public void the_response_body_should_contain_a_message_like(String messageSubstring) {
        String responseBody = latestResponse.getBody();
        assertThat(responseBody).isNotNull();
        boolean found = false;
        try {
            String message = com.jayway.jsonpath.JsonPath.read(responseBody, "$.message");
            if (message != null && message.contains(messageSubstring)) found = true;
        } catch (Exception e) { /* ignore */ }
        if (!found) {
            try {
                String error = com.jayway.jsonpath.JsonPath.read(responseBody, "$.error");
                if (error != null && error.contains(messageSubstring)) found = true;
            } catch (Exception e) { /* ignore */ }
        }
        if (!found) {
             assertThat(responseBody).containsIgnoringCase(messageSubstring);
        } else {
            // Assertion is implicitly true if found via JsonPath
        }
    }

    @Given("a customer already exists with email {string}")
    public void a_customer_already_exists_with_email(String email) {
        createCustomerIfNotExists(email, "Existing", "Customer", "+234000000000");
    }

    @Given("a customer already exists with email {string} and password {string} in the system")
    public void a_customer_already_exists_with_email_and_password_in_system(String email, String password) {
        // This step is primarily for conceptual clarity in the feature file.
        // The actual password is not stored on the Customer entity directly in this project.
        // CustomerService.authenticate(email, password) handles the logic.
        // We just ensure the customer record exists.
        createCustomerIfNotExists(email, "LoginTest", "User", "+234111222333");
        // Store password conceptually for login step if needed, though not used by this method directly.
        // sharedData.put(email + "_password", password);
    }

    @Given("a customer {string} exists with ID {string}")
    public void a_customer_exists_with_id(String email, String idString) {
        Long id = Long.parseLong(idString);
        if (customerRepository.findById(id).isEmpty()) {
            if (customerRepository.findByEmail(email).isPresent()) {
                // If email exists but ID doesn't match, this is a problematic test data setup.
                // For now, we'll assume this means we should create one with this email if ID is free.
                // Or, ideally, the test should ensure IDs are unique or handled by sequence.
                logger.warn("Customer with email {} already exists but not with ID {}. Test data might be inconsistent.", email, id);
                // To be safe, let's not create a duplicate email. This step might need refinement
                // if strict ID control for new entities is needed.
                // For now, we rely on the findByEmail check in createCustomer.
            }
            Customer customer = createCustomerIfNotExists(email, "SpecificId", "User", "+234555666777");
            // The ID is auto-generated. This step definition cannot guarantee a specific ID '123' on creation
            // unless we modify the entity or use a direct save with ID if allowed (usually not for auto-generated).
            // So, for this step to be robust, the test should likely CREATE a customer, get its ID, then use that ID.
            // Or, the test setup needs to ensure a customer with a known ID (e.g. from a test data script) exists.
            // For now, we'll store the created customer's actual ID if we just created them.
            sharedData.put("customerId_" + email, customer.getCustomerId().toString());
            logger.info("Ensured customer {} exists. Actual ID for tests (if newly created): {}. Requested test ID was {}",
                        email, customer.getCustomerId(), id);
            // This makes the scenario "Given a customer "get.cust@example.com" exists with ID "123""
            // slightly less direct if we can't force ID 123.
            // The test should then use the ID from sharedData.
        }
    }

    private Customer createCustomerIfNotExists(String email, String firstName, String lastName, String phoneNumber) {
        return customerRepository.findByEmail(email).orElseGet(() -> {
            Customer customer = new Customer();
            customer.setEmail(email);
            customer.setFirstName(firstName);
            customer.setLastName(lastName);
            customer.setPhoneNumber(phoneNumber);
            Customer saved = customerRepository.save(customer);
            logger.info("Created precondition customer with email: {}", email);
            return saved;
        });
    }

    @Given("a customer is logged in with email {string} and password {string}")
    public void a_customer_is_logged_in_with_email_and_password(String email, String password) throws JsonProcessingException {
        // Ensure customer exists for login. Customer entity does not have password.
        // The CustomerService.authenticate(email, password) and generateJwtForCustomer(customer)
        // will be called by the /api/v1/customers/login endpoint.
        // For this step, we directly call the login endpoint to get a token.

        if (customerRepository.findByEmail(email).isEmpty()) {
            // Create a basic customer if not present, actual password setting/checking is part of service logic
            Customer customer = new Customer();
            customer.setEmail(email);
            customer.setFirstName("Login");
            customer.setLastName("User");
            // No password on customer entity, CustomerService.authenticate must handle this
            customerRepository.save(customer);
            logger.info("Created customer for login test with email: {}", email);
        }

        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", email);
        loginRequest.put("password", password); // Password will be used by CustomerService.authenticate

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(loginRequest), headers);

        ResponseEntity<String> loginResponse = restTemplate.postForEntity(apiBaseUrl + "/customers/login", entity, String.class);

        String responseBody = loginResponse.getBody();
        logger.info("Customer login attempt for {} status: {}, body: {}", email, loginResponse.getStatusCodeValue(), responseBody);
        assertThat(loginResponse.getStatusCodeValue()).isEqualTo(200); // Assuming login endpoint is functional

        assertThat(responseBody).isNotNull();
        String token = com.jayway.jsonpath.JsonPath.read(responseBody, "$.token");
        Long customerIdLong = ((Number) com.jayway.jsonpath.JsonPath.read(responseBody, "$.customer.customerId")).longValue();

        assertThat(token).isNotBlank();
        sharedData.put("customerToken", token);
        sharedData.put("customerId", String.valueOf(customerIdLong));
        logger.info("Customer {} logged in successfully. Token and customerId stored.", email);
    }


    private String resolveCustomerPathVariables(String path) {
        String resolvedPath = path;
        if (path.contains("{customerId}")) {
            resolvedPath = path.replace("{customerId}", sharedData.getOrDefault("customerId", "UNKNOWN_CUSTOMER_ID"));
        }
        // Add more replacements if other path variables are used
        return resolvedPath;
    }
}
