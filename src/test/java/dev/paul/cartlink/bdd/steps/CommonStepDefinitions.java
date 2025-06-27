package dev.paul.cartlink.bdd.steps;


import dev.paul.cartlink.bdd.context.ScenarioContext; // Updated import
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.paul.cartlink.customer.model.Customer;
import dev.paul.cartlink.customer.repository.CustomerRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate; // Added for RestTemplate
import java.util.List; // Added for List
import java.util.Objects; // Added for Objects.toString
import java.math.BigDecimal; // Added for BigDecimal
import static org.assertj.core.api.Assertions.assertThat; // Added for assertThat
// import org.springframework.stereotype.Component; // Removed import

// @Component // Make this a Spring-managed bean - REMOVED as per Cucumber best practice
public class CommonStepDefinitions {

    private static final Logger logger = LoggerFactory.getLogger(CommonStepDefinitions.class);

    private final ScenarioContext scenarioContext;
    private final org.springframework.boot.test.web.client.TestRestTemplate restTemplate; // Changed type
    private final ObjectMapper objectMapper;
    private final CustomerRepository customerRepository;
    private final dev.paul.cartlink.merchant.repository.MerchantRepository merchantRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Autowired
    public CommonStepDefinitions(
            ScenarioContext scenarioContext,
            org.springframework.boot.test.web.client.TestRestTemplate restTemplate, // Changed type
            ObjectMapper objectMapper,
            CustomerRepository customerRepository,
            dev.paul.cartlink.merchant.repository.MerchantRepository merchantRepository,
            org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this.scenarioContext = scenarioContext;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.customerRepository = customerRepository;
        this.merchantRepository = merchantRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Given("the API base URL is {string}")
    public void the_api_base_url_is(String baseUrl) {
        scenarioContext.set("apiBaseUrl", baseUrl);
        logger.info("CommonStepDefinitions: API base URL set to: {}", baseUrl);
    }

    @When("a POST request is made to {string} with the following body:")
    public void a_post_request_is_made_to_with_body(String path, String requestBody) {
        String apiBaseUrl = scenarioContext.getString("apiBaseUrl");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Check for authentication tokens in ScenarioContext
        if (scenarioContext.containsKey("merchantToken")) {
            headers.setBearerAuth(scenarioContext.getString("merchantToken"));
            logger.info("COMMON POST: Using merchantToken for authentication.");
        } else if (scenarioContext.containsKey("customerToken")) {
            headers.setBearerAuth(scenarioContext.getString("customerToken"));
            logger.info("COMMON POST: Using customerToken for authentication.");
        }

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        String resolvedPath = resolvePathPlaceholdersCommon(path); // Resolve path placeholders

        ResponseEntity<String> response = restTemplate.postForEntity(apiBaseUrl + resolvedPath, entity, String.class);
        scenarioContext.set("latestResponse", response); // Store response in ScenarioContext

        logger.info("COMMON POST request to {}{} with body: {}", apiBaseUrl, resolvedPath, requestBody);
        logger.info("COMMON Response status: {}, body: {}", response.getStatusCode(), response.getBody());
    }

    // Helper method to resolve path placeholders, similar to what was in MerchantProductStepDefinitions
    private String resolvePathPlaceholdersCommon(String path) {
        String resolvedPath = path;
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\{([^}]+)\\}");
        java.util.regex.Matcher matcher = pattern.matcher(resolvedPath);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1);
            if (scenarioContext.containsKey(key)) {
                matcher.appendReplacement(sb, scenarioContext.getString(key));
            } else {
                logger.warn("COMMON RESOLVE: Placeholder {{{}}} found in path but key not in ScenarioContext.", key);
                matcher.appendReplacement(sb, matcher.group(0)); // Keep original placeholder if not found
            }
        }
        matcher.appendTail(sb);
        resolvedPath = sb.toString();

        if (resolvedPath.contains("{") && resolvedPath.contains("}")) {
             logger.warn("COMMON RESOLVE: Path {} still contains unresolved placeholders: {}", path, resolvedPath);
        }
        return resolvedPath;
    }


    @Then("the response status code should be {int}")
    public void the_response_status_code_should_be(Integer statusCode) {
        @SuppressWarnings("unchecked")
        ResponseEntity<String> latestResponse = scenarioContext.get("latestResponse", ResponseEntity.class);

        if (latestResponse == null) {
            throw new IllegalStateException("No 'latestResponse' found in ScenarioContext. This step should run after a request has been made.");
        }
        assertThat(latestResponse.getStatusCodeValue()).isEqualTo(statusCode);
    }

    @When("a GET request is made to {string}")
    public void a_get_request_is_made_to(String path) {
        String apiBaseUrl = scenarioContext.getString("apiBaseUrl");
        HttpHeaders headers = new HttpHeaders();
        // Check for authentication tokens in ScenarioContext
        if (scenarioContext.containsKey("merchantToken")) {
            headers.setBearerAuth(scenarioContext.getString("merchantToken"));
            logger.info("COMMON GET: Using merchantToken for authentication.");
        } else if (scenarioContext.containsKey("customerToken")) {
            headers.setBearerAuth(scenarioContext.getString("customerToken"));
            logger.info("COMMON GET: Using customerToken for authentication.");
        }

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        String resolvedPath = resolvePathPlaceholdersCommon(path); // Resolve path placeholders

        ResponseEntity<String> response = restTemplate.exchange(apiBaseUrl + resolvedPath, HttpMethod.GET, entity, String.class);
        scenarioContext.set("latestResponse", response);

        logger.info("COMMON GET request to {}{}", apiBaseUrl, resolvedPath);
        logger.info("COMMON Response status: {}, body: {}", response.getStatusCode(), response.getBody());
    }

    @Then("the response body should contain {string} with value {string}")
    public void the_response_body_should_contain_with_value(String jsonPath, String expectedValue) {
        @SuppressWarnings("unchecked")
        ResponseEntity<String> latestResponse = scenarioContext.get("latestResponse", ResponseEntity.class);
        if (latestResponse == null) {
            throw new IllegalStateException("No 'latestResponse' found in ScenarioContext.");
        }
        String responseBody = latestResponse.getBody();
        assertThat(responseBody).isNotNull();

        // Resolve placeholders in expectedValue from scenarioContext (if any were stored by name)
        String resolvedExpectedValue = expectedValue;
        if (expectedValue.startsWith("{") && expectedValue.endsWith("}")) {
            String placeholderKey = expectedValue.substring(1, expectedValue.length() - 1);
            if (scenarioContext.containsKey(placeholderKey)) {
                resolvedExpectedValue = scenarioContext.getString(placeholderKey);
            } else {
                // It might be a literal value that happens to be wrapped in braces, or a key from a different map.
                // For now, we'll assume if it's not in ScenarioContext, it's literal or handled by other means.
                logger.warn("Expected value placeholder '{}' not found in ScenarioContext, using literal value.", placeholderKey);
            }
        }

        try {
            String actualValue = Objects.toString(com.jayway.jsonpath.JsonPath.read(responseBody, "$." + jsonPath), "");
            assertThat(actualValue).isEqualTo(resolvedExpectedValue);
        } catch (com.jayway.jsonpath.PathNotFoundException e) {
            throw new AssertionError("JSON path '" + jsonPath + "' not found in response body: " + responseBody, e);
        } catch (Exception e) {
            throw new AssertionError("Error reading JSON path '" + jsonPath + "' or asserting value in response body: " + responseBody, e);
        }
    }

    @Then("the response body should contain a {string}")
    public void the_response_body_should_contain_a(String jsonPath) {
        @SuppressWarnings("unchecked")
        ResponseEntity<String> latestResponse = scenarioContext.get("latestResponse", ResponseEntity.class);
        if (latestResponse == null) {
            throw new IllegalStateException("No 'latestResponse' found in ScenarioContext.");
        }
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

    @Given("a customer is logged in with email {string} and password {string}")
    public void a_customer_is_logged_in_with_email_and_password(String email, String password) throws com.fasterxml.jackson.core.JsonProcessingException {
        // Ensure customer exists for login.
        if (customerRepository.findByEmail(email).isEmpty()) {
            Customer customer = new Customer();
            customer.setEmail(email);
            customer.setFirstName("DefaultLogin");
            customer.setLastName("User");
            // Assuming Customer entity doesn't store password directly,
            // and actual password check is done by the service during login.
            customerRepository.save(customer);
            logger.info("CommonStepDefinitions: Created customer for login: {}", email);
        }

        String apiBaseUrl = scenarioContext.getString("apiBaseUrl");
        java.util.Map<String, String> loginRequest = new java.util.HashMap<>();
        loginRequest.put("email", email);
        loginRequest.put("password", password);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(loginRequest), headers);

        ResponseEntity<String> loginResponse = restTemplate.postForEntity(apiBaseUrl + "/customers/login", entity, String.class);
        scenarioContext.set("latestResponse", loginResponse); // Store response

        String responseBody = loginResponse.getBody();
        logger.info("CommonStepDefinitions: Customer login attempt for {} status: {}, body: {}", email, loginResponse.getStatusCodeValue(), responseBody);
        assertThat(loginResponse.getStatusCodeValue()).isEqualTo(200);
        assertThat(responseBody).isNotNull();

        String token = com.jayway.jsonpath.JsonPath.read(responseBody, "$.token");
        Long customerIdLong = ((Number) com.jayway.jsonpath.JsonPath.read(responseBody, "$.customer.customerId")).longValue();

        assertThat(token).isNotBlank();
        scenarioContext.set("customerToken", token);
        scenarioContext.set("customerId", String.valueOf(customerIdLong));
        scenarioContext.set("authenticatedUserEmail", email); // Useful for subsequent steps
        logger.info("CommonStepDefinitions: Customer {} logged in. Token and customerId stored in ScenarioContext.", email);
    }

    @Then("the response body should contain {string} with boolean value {string}")
    public void the_response_body_should_contain_with_boolean_value(String jsonPath, String expectedValueStr) {
        @SuppressWarnings("unchecked")
        ResponseEntity<String> latestResponse = scenarioContext.get("latestResponse", ResponseEntity.class);
        if (latestResponse == null) {
            throw new IllegalStateException("No 'latestResponse' found in ScenarioContext.");
        }
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

    @Then("the response body should contain a message like {string}")
    public void the_response_body_should_contain_a_message_like(String messageSubstring) {
        @SuppressWarnings("unchecked")
        ResponseEntity<String> latestResponse = scenarioContext.get("latestResponse", ResponseEntity.class);
        if (latestResponse == null) {
            throw new IllegalStateException("No 'latestResponse' found in ScenarioContext.");
        }
        String responseBody = latestResponse.getBody();
        assertThat(responseBody).isNotNull();

        boolean found = false;
        try {
            String message = com.jayway.jsonpath.JsonPath.read(responseBody, "$.message");
            if (message != null && message.contains(messageSubstring)) {
                found = true;
            }
        } catch (com.jayway.jsonpath.PathNotFoundException e) {
            // $.message path not found, try $.error
        } catch (Exception e) {
            logger.warn("Exception while trying to read '$.message': {}", e.getMessage());
        }

        if (!found) {
            try {
                String error = com.jayway.jsonpath.JsonPath.read(responseBody, "$.error");
                if (error != null && error.contains(messageSubstring)) {
                    found = true;
                }
            } catch (com.jayway.jsonpath.PathNotFoundException e) {
                // $.error path not found either
            } catch (Exception e) {
                logger.warn("Exception while trying to read '$.error': {}", e.getMessage());
            }
        }

        if (!found) {
            // Fallback to raw string search if specific fields are not found or parsing fails
            assertThat(responseBody).containsIgnoringCase(messageSubstring);
        } else {
            // If found via JsonPath, the assertion is implicitly true.
            // We can add an explicit assertion here for clarity if needed, but it's covered.
            assertThat(found).isTrue();
        }
    }

    @Then("the response body should include the text {string}")
    public void the_response_body_should_include_the_text(String expectedSubstring) {
        @SuppressWarnings("unchecked")
        ResponseEntity<String> latestResponse = scenarioContext.get("latestResponse", ResponseEntity.class);
        if (latestResponse == null) {
            throw new IllegalStateException("No 'latestResponse' found in ScenarioContext.");
        }
        String responseBody = latestResponse.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody).containsIgnoringCase(expectedSubstring);
    }

    @Then("the response body should be a list with at least {int} item(s)")
    public void the_response_body_should_be_a_list_with_at_least_items(int minCount) {
        @SuppressWarnings("unchecked")
        ResponseEntity<String> latestResponse = scenarioContext.get("latestResponse", ResponseEntity.class);
        if (latestResponse == null) {
            throw new IllegalStateException("No 'latestResponse' found in ScenarioContext.");
        }
        String responseBody = latestResponse.getBody();
        assertThat(responseBody).isNotNull();
        try {
            List<?> list = com.jayway.jsonpath.JsonPath.parse(responseBody).read("$");
            assertThat(list).isNotNull().hasSizeGreaterThanOrEqualTo(minCount);
        } catch (Exception e) {
            throw new AssertionError("Error parsing response body as a list or asserting size: " + responseBody, e);
        }
    }

    @Given("a merchant is logged in with email {string} and password {string}")
    public void a_merchant_is_logged_in_with_email_and_password(String email, String password) throws com.fasterxml.jackson.core.JsonProcessingException {
        // Ensure merchant exists, or create if necessary for the test
        if (merchantRepository.findByEmail(email).isEmpty()) {
            dev.paul.cartlink.merchant.model.Merchant merchant = new dev.paul.cartlink.merchant.model.Merchant();
            merchant.setEmail(email);
            merchant.setPassword(passwordEncoder.encode(password)); // Ensure passwordEncoder is available and autowired
            merchant.setFirstName("CommonLoginMerchantFirst");
            merchant.setLastName("CommonLoginMerchantLast");
            // Set any other required fields for Merchant entity
            merchantRepository.save(merchant);
            logger.info("CommonStepDefinitions: Created merchant for login: {}", email);
        }

        String apiBaseUrl = scenarioContext.getString("apiBaseUrl");
        java.util.Map<String, String> loginRequest = new java.util.HashMap<>();
        loginRequest.put("email", email);
        loginRequest.put("password", password);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(loginRequest), headers);

        // Using /merchants/login as it's more common in REST APIs for plural resource
        ResponseEntity<String> loginResponse = restTemplate.postForEntity(apiBaseUrl + "/merchants/login", entity, String.class);
        scenarioContext.set("latestResponse", loginResponse); // Store response

        String responseBody = loginResponse.getBody();
        logger.info("CommonStepDefinitions: Merchant login attempt for {} status: {}, body: {}", email, loginResponse.getStatusCodeValue(), responseBody);
        assertThat(loginResponse.getStatusCodeValue()).isEqualTo(200);
        assertThat(responseBody).isNotNull();

        String token = com.jayway.jsonpath.JsonPath.read(responseBody, "$.token");
        // Assuming the response for merchant login also contains merchantId directly or nested
        // Adjust JsonPath based on actual response structure
        String merchantId = Objects.toString(com.jayway.jsonpath.JsonPath.read(responseBody, "$.merchantId"), null);
         if (merchantId == null) { // Fallback if not directly under root
            merchantId = Objects.toString(com.jayway.jsonpath.JsonPath.read(responseBody, "$.merchant.merchantId"), null);
        }

        assertThat(token).isNotBlank();
        assertThat(merchantId).isNotBlank();
        scenarioContext.set("merchantToken", token);
        scenarioContext.set("merchantId", merchantId);
        scenarioContext.set("authenticatedMerchantEmail", email);
        logger.info("CommonStepDefinitions: Merchant {} logged in. Token and merchantId {} stored in ScenarioContext.", email, merchantId);
    }

    @Then("the response body should contain {string} with number value {string}")
    public void the_response_body_should_contain_with_number_value(String jsonPath, String expectedValueStr) {
        @SuppressWarnings("unchecked")
        ResponseEntity<String> latestResponse = scenarioContext.get("latestResponse", ResponseEntity.class);
        if (latestResponse == null) {
            throw new IllegalStateException("No 'latestResponse' found in ScenarioContext.");
        }
        String responseBody = latestResponse.getBody();
        assertThat(responseBody).isNotNull();

        try {
            Object actualObject = com.jayway.jsonpath.JsonPath.read(responseBody, "$." + jsonPath);
            BigDecimal actualValue = new BigDecimal(actualObject.toString());
            BigDecimal expectedDecimalValue = new BigDecimal(expectedValueStr);
            assertThat(actualValue).isEqualByComparingTo(expectedDecimalValue);
        } catch (com.jayway.jsonpath.PathNotFoundException e) {
            throw new AssertionError("JSON path '" + jsonPath + "' not found in response body: " + responseBody, e);
        } catch (NumberFormatException e) {
            throw new AssertionError("Could not parse expected or actual value as BigDecimal for JSON path '" + jsonPath + "'. Expected: " + expectedValueStr + ", Actual: " + com.jayway.jsonpath.JsonPath.read(responseBody, "$." + jsonPath), e);
        } catch (Exception e) {
            throw new AssertionError("Error reading JSON path '" + jsonPath + "' or comparing number values in response body: " + responseBody, e);
        }
    }

    @Then("the response body should be an empty list")
    public void the_response_body_should_be_an_empty_list() {
        @SuppressWarnings("unchecked")
        ResponseEntity<String> latestResponse = scenarioContext.get("latestResponse", ResponseEntity.class);
        if (latestResponse == null) {
            throw new IllegalStateException("No 'latestResponse' found in ScenarioContext.");
        }
        String responseBody = latestResponse.getBody();
        assertThat(responseBody).isNotNull();
        try {
            List<?> list = com.jayway.jsonpath.JsonPath.parse(responseBody).read("$");
            assertThat(list).isNotNull().isEmpty();
        } catch (Exception e) {
            // Consider if the response might not be a JSON array, e.g. an error object.
            // For now, assume it's expected to be a list if this step is used.
            throw new AssertionError("Error parsing response body as a list or list was not empty: " + responseBody, e);
        }
    }

    @When("a GET request is made to {string} with an authenticated merchant")
    public void a_get_request_is_made_to_with_auth_merchant(String path) {
        String apiBaseUrl = scenarioContext.getString("apiBaseUrl");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON); // Though not strictly needed for GET, good practice
        if (scenarioContext.containsKey("merchantToken")) {
            headers.setBearerAuth(scenarioContext.getString("merchantToken"));
            logger.info("COMMON AUTH MERCHANT GET: Using merchantToken for authentication.");
        } else {
            logger.warn("COMMON AUTH MERCHANT GET: merchantToken not found in ScenarioContext for supposedly authenticated request!");
            // Depending on strictness, could throw an error here or proceed (expecting a 401/403)
        }
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        String resolvedPath = resolvePathPlaceholdersCommon(path); // Use existing helper
        ResponseEntity<String> response = restTemplate.exchange(apiBaseUrl + resolvedPath, HttpMethod.GET, entity, String.class);
        scenarioContext.set("latestResponse", response);
        logger.info("COMMON AUTH MERCHANT GET request to {}{} - Status: {}, Body: {}", apiBaseUrl, resolvedPath, response.getStatusCodeValue(), response.getBody());
    }

    @When("a DELETE request is made to {string} with an authenticated merchant")
    public void a_delete_request_is_made_to_with_auth_merchant(String path) {
        String apiBaseUrl = scenarioContext.getString("apiBaseUrl");
        HttpHeaders headers = new HttpHeaders();
        // Content-Type might not be strictly necessary for DELETE but doesn't hurt
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (scenarioContext.containsKey("merchantToken")) {
            headers.setBearerAuth(scenarioContext.getString("merchantToken"));
            logger.info("COMMON AUTH MERCHANT DELETE: Using merchantToken for authentication.");
        } else {
            logger.warn("COMMON AUTH MERCHANT DELETE: merchantToken not found in ScenarioContext for supposedly authenticated request!");
            // Consider throwing error or proceeding to expect 401/403
        }
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        String resolvedPath = resolvePathPlaceholdersCommon(path); // Use existing helper
        ResponseEntity<String> response = restTemplate.exchange(apiBaseUrl + resolvedPath, HttpMethod.DELETE, entity, String.class);
        scenarioContext.set("latestResponse", response);
        logger.info("COMMON AUTH MERCHANT DELETE request to {}{} - Status: {}, Body: {}", apiBaseUrl, resolvedPath, response.getStatusCodeValue(), response.getBody());
    }

    @Then("the response body should be a list with {int} item(s)")
    public void the_response_body_should_be_a_list_with_items(int count) {
        @SuppressWarnings("unchecked")
        ResponseEntity<String> latestResponse = scenarioContext.get("latestResponse", ResponseEntity.class);
        if (latestResponse == null) {
            throw new IllegalStateException("No 'latestResponse' found in ScenarioContext.");
        }
        String responseBody = latestResponse.getBody();
        assertThat(responseBody).isNotNull();
        try {
            List<?> list = com.jayway.jsonpath.JsonPath.parse(responseBody).read("$");
            assertThat(list).isNotNull().hasSize(count);
        } catch (Exception e) {
            throw new AssertionError("Error parsing response body as a list or asserting size: " + responseBody, e);
        }
    }

    @Then("the response body should contain an {string} field")
    public void the_response_body_should_contain_an_error_field(String fieldName) {
        // This implementation assumes that if a field is an "error field",
        // it's sufficient to just check for its existence using the common key check.
        the_response_body_should_contain_a(fieldName);
        logger.info("COMMON: Checked for error field '{}' in response body.", fieldName);
    }

    @When("a POST request is made to {string} with an authenticated customer and the following body:")
    public void a_post_request_is_made_to_with_authenticated_customer_and_body(String path, String requestBody) {
        String apiBaseUrl = scenarioContext.getString("apiBaseUrl");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (scenarioContext.containsKey("customerToken")) {
            headers.setBearerAuth(scenarioContext.getString("customerToken"));
            logger.info("COMMON AUTH CUSTOMER POST: Using customerToken for authentication.");
        } else {
            logger.warn("COMMON AUTH CUSTOMER POST: customerToken not found in ScenarioContext for authenticated request!");
            // Depending on strictness, could throw an error here or proceed (expecting a 401/403)
        }

        String resolvedPath = resolvePathPlaceholdersCommon(path);
        String resolvedBody = resolvePathPlaceholdersCommon(requestBody); // Assuming generic placeholder resolver works for body too

        HttpEntity<String> entity = new HttpEntity<>(resolvedBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(apiBaseUrl + resolvedPath, entity, String.class);
        scenarioContext.set("latestResponse", response);
        logger.info("COMMON AUTH CUSTOMER POST request to {}{} with body: {} - Status: {}, Body: {}",
                    apiBaseUrl, resolvedPath, resolvedBody, response.getStatusCodeValue(), response.getBody());
    }

    @When("a GET request is made to {string} with an authenticated customer")
    public void a_get_request_is_made_to_with_authenticated_customer(String path) {
        String apiBaseUrl = scenarioContext.getString("apiBaseUrl");
        HttpHeaders headers = new HttpHeaders();
        // Specifically for authenticated customer
        if (scenarioContext.containsKey("customerToken")) {
            headers.setBearerAuth(scenarioContext.getString("customerToken"));
            logger.info("COMMON AUTH CUSTOMER GET: Using customerToken for authentication.");
        } else {
            logger.warn("COMMON AUTH CUSTOMER GET: customerToken not found in ScenarioContext for authenticated request!");
            // Depending on strictness, could throw an error here or proceed (expecting a 401/403)
        }

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        String resolvedPath = resolvePathPlaceholdersCommon(path);

        ResponseEntity<String> response = restTemplate.exchange(apiBaseUrl + resolvedPath, HttpMethod.GET, entity, String.class);
        scenarioContext.set("latestResponse", response);

        logger.info("COMMON AUTH CUSTOMER GET request to {}{}", apiBaseUrl, resolvedPath);
        logger.info("COMMON Response status: {}, body: {}", response.getStatusCode(), response.getBody());
    }
}
