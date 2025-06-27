package dev.paul.cartlink.bdd.steps;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.paul.cartlink.customer.model.Customer;
import dev.paul.cartlink.customer.repository.CustomerRepository;
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

    @Autowired
    private ScenarioContext scenarioContext;

    // private ResponseEntity<String> latestResponse; // Removed: Now using ScenarioContext
    // No longer using local sharedData map

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

    // This step is now in CommonStepDefinitions.java
    // @When("a POST request is made to {string} with the following body:")
    // ...

    @When("a PUT request is made to {string} with the following body:") // Unauthenticated PUT
    public void a_put_request_is_made_to_with_body(String path, String requestBody) {
        String apiBaseUrl = scenarioContext.getString("apiBaseUrl");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(apiBaseUrl + path, HttpMethod.PUT, entity, String.class);
        scenarioContext.set("latestResponse", response); // Use ScenarioContext
        logger.info("PUT request to {}{} with body: {}", apiBaseUrl, path, requestBody);
        logger.info("Response status: {}, body: {}", response.getStatusCode(), response.getBody());
    }

    // --- Customer Specific Authenticated Steps ---

    // This step is now in CommonStepDefinitions.java
    // @When("a POST request is made to {string} with an authenticated customer and the following body:")
    // public void a_post_request_is_made_to_with_an_authenticated_customer_and_the_following_body(String path, String requestBody) {
    //    ...
    // }

    @When("a PUT request is made to {string} with an authenticated customer and the following body:")
    public void a_put_request_is_made_to_with_an_authenticated_customer_and_the_following_body(String path, String requestBody) {
        String apiBaseUrl = scenarioContext.getString("apiBaseUrl");
        String resolvedPath = resolveCustomerPathVariables(path);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String customerToken = scenarioContext.getString("customerToken");
        if (customerToken != null) {
            headers.setBearerAuth(customerToken);
        } else {
            logger.warn("No customerToken found in ScenarioContext for authenticated PUT request!");
        }
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(apiBaseUrl + resolvedPath, HttpMethod.PUT, entity, String.class);
        scenarioContext.set("latestResponse", response); // Use ScenarioContext
        logger.info("Authenticated Customer PUT request to {}{} with body: {}", apiBaseUrl, resolvedPath, requestBody);
        logger.info("Response status: {}, body: {}", response.getStatusCode(), response.getBody());
    }

    // This step is now in CommonStepDefinitions.java
    // @When("a GET request is made to {string} with an authenticated customer")
    // public void a_get_request_is_made_to_with_an_authenticated_customer(String path) {
    //    ...
    // }

    @When("a DELETE request is made to {string} with an authenticated customer")
    public void a_delete_request_is_made_to_with_an_authenticated_customer(String path) {
        String apiBaseUrl = scenarioContext.getString("apiBaseUrl");
        String resolvedPath = resolveCustomerPathVariables(path);
        HttpHeaders headers = new HttpHeaders();
        String customerToken = scenarioContext.getString("customerToken");
        if (customerToken != null) {
            headers.setBearerAuth(customerToken);
        } else {
            logger.warn("No customerToken found in ScenarioContext for authenticated DELETE request!");
        }
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(apiBaseUrl + resolvedPath, HttpMethod.DELETE, entity, String.class);
        scenarioContext.set("latestResponse", response); // Use ScenarioContext
        logger.info("Authenticated Customer DELETE request to {}{}", apiBaseUrl, resolvedPath);
        logger.info("Response status: {}, body: {}", response.getStatusCode(), response.getBody());
    }


    // This step can be moved to a common/generic step definitions file
    // Step moved to CommonStepDefinitions.java

    // This step can be moved to a common/generic step definitions file
    // @Then("the response body should contain {string} with value {string}")
    // ... (rest of commented out method)

    // @Then("the response body should contain {string} with boolean value {string}")
    // ... (rest of commented out method)

    // This step can be moved to a common/generic step definitions file
    // @Then("the response body should contain a message like {string}")
    // ... (rest of commented out method)

    @Given("a customer already exists with email {string}")
    public void a_customer_already_exists_with_email(String email) {
        createCustomerIfNotExists(email, "Existing", "Customer", "+234000000000");
    }

    @Given("a customer {string} exists with ID {string}")
    public void a_customer_exists_with_id(String email, String idString) {
        Long id = Long.parseLong(idString);
        if (customerRepository.findById(id).isEmpty()) {
            if (customerRepository.findByEmail(email).isPresent()) {
                logger.warn("Customer with email {} already exists but not with ID {}. Test data might be inconsistent.", email, id);
            }
            Customer customer = createCustomerIfNotExists(email, "SpecificId", "User", "+234555666777");
            // Storing the actual ID under a key that includes the email for potential retrieval if needed by that specific email.
            // However, if the test logic strictly depends on the symbolic 'idString' as the key, this needs careful handling.
            scenarioContext.set("customerId_" + email, customer.getCustomerId().toString()); // Use ScenarioContext
            logger.info("Ensured customer {} exists. Actual ID for tests (if newly created): {}. Requested test ID was {}",
                        email, customer.getCustomerId(), id);
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

    // This step is now in CommonStepDefinitions.java
    // @Given("a customer is logged in with email {string} and password {string}")
    // public void a_customer_is_logged_in_with_email_and_password(String email, String password) throws JsonProcessingException {
    //     // Ensure customer exists for login. Customer entity does not have password.
    //     // The CustomerService.authenticate(email, password) and generateJwtForCustomer(customer)
    //     // will be called by the /api/v1/customers/login endpoint.
    //     // For this step, we directly call the login endpoint to get a token.
    //
    //     if (customerRepository.findByEmail(email).isEmpty()) {
    //         // Create a basic customer if not present, actual password setting/checking is part of service logic
    //         Customer customer = new Customer();
    //         customer.setEmail(email);
    //         customer.setFirstName("Login");
    //         customer.setLastName("User");
    //         // No password on customer entity, CustomerService.authenticate must handle this
    //         customerRepository.save(customer);
    //         logger.info("Created customer for login test with email: {}", email);
    //     }
    //
    //     Map<String, String> loginRequest = new HashMap<>();
    //     loginRequest.put("email", email);
    //     loginRequest.put("password", password); // Password will be used by CustomerService.authenticate
    //
    //     HttpHeaders headers = new HttpHeaders();
    //     headers.setContentType(MediaType.APPLICATION_JSON);
    //     HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(loginRequest), headers);
    //
    //     ResponseEntity<String> loginResponse = restTemplate.postForEntity(scenarioContext.getString("apiBaseUrl") + "/customers/login", entity, String.class);
    //
    //     String responseBody = loginResponse.getBody();
    //     logger.info("Customer login attempt for {} status: {}, body: {}", email, loginResponse.getStatusCodeValue(), responseBody);
    //     assertThat(loginResponse.getStatusCodeValue()).isEqualTo(200); // Assuming login endpoint is functional
    //
    //     assertThat(responseBody).isNotNull();
    //     String token = com.jayway.jsonpath.JsonPath.read(responseBody, "$.token");
    //     Long customerIdLong = ((Number) com.jayway.jsonpath.JsonPath.read(responseBody, "$.customer.customerId")).longValue();
    //
    //     assertThat(token).isNotBlank();
    //     sharedData.put("customerToken", token); // This will need to use ScenarioContext
    //     sharedData.put("customerId", String.valueOf(customerIdLong)); // This will need to use ScenarioContext
    //     logger.info("Customer {} logged in successfully. Token and customerId stored.", email);
    // }


    private String resolveCustomerPathVariables(String path) {
        String resolvedPath = path;
        if (path.contains("{customerId}")) {
            String customerId = scenarioContext.getString("customerId");
            if (customerId == null) {
                logger.warn("Attempted to resolve {customerId} but it was not found in ScenarioContext. Using UNKNOWN_CUSTOMER_ID.");
                customerId = "UNKNOWN_CUSTOMER_ID"; // Fallback, though ideally the step setting customerId ensures it's there.
            }
            resolvedPath = path.replace("{customerId}", customerId);
        }
        // Add more replacements if other path variables are used
        return resolvedPath;
    }
}
