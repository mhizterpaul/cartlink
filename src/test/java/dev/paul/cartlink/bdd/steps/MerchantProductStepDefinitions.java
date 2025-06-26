package dev.paul.cartlink.bdd.steps;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.paul.cartlink.merchant.model.Merchant;
import dev.paul.cartlink.merchant.repository.MerchantRepository;
import dev.paul.cartlink.product.repository.ProductRepository;
import dev.paul.cartlink.merchant.repository.MerchantProductRepository;

import io.cucumber.datatable.DataTable;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class MerchantProductStepDefinitions {

    private static final Logger logger = LoggerFactory.getLogger(MerchantProductStepDefinitions.class);

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // For setting up merchant for login

    @Autowired
    private ProductRepository productRepository; // To clean up products

    @Autowired
    private MerchantProductRepository merchantProductRepository; // To clean up merchant_products

    // Shared state
    private String apiBaseUrl;
    private ResponseEntity<String> latestResponse;
    private Map<String, String> sharedData = new HashMap<>(); // For auth tokens, merchantId, merchantProductIds

    @Before
    public void setUp() {
        // Clear relevant repositories
        merchantProductRepository.deleteAll();
        productRepository.deleteAll();
        // merchantRepository.deleteAll(); // Usually handled by MerchantAuthStepDefinitions if run together,
                                        // but good to ensure clean state if run standalone.
                                        // Be cautious if scenarios depend on merchants from other features.
                                        // For isolated merchant product tests, this is fine.
        sharedData.clear();
        logger.info("MerchantProductStepDefinitions: Cleared repositories and sharedData.");
    }

    @After
    public void tearDown() {
    }

    @Given("the API base URL is {string}")
    public void the_api_base_url_is(String baseUrl) {
        this.apiBaseUrl = baseUrl;
    }

    @Given("a merchant is logged in with email {string} and password {string}")
    public void a_merchant_is_logged_in_with_email_and_password(String email, String password) throws JsonProcessingException {
        // Ensure merchant exists
        if (merchantRepository.findByEmail(email).isEmpty()) {
            Merchant merchant = new Merchant();
            merchant.setEmail(email);
            merchant.setPassword(passwordEncoder.encode(password));
            merchant.setFirstName("ProductTest");
            merchant.setLastName("Merchant");
            merchantRepository.save(merchant);
        }

        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", email);
        loginRequest.put("password", password);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(loginRequest), headers);

        // Assuming merchant login endpoint is /api/merchant/login from MerchantController
        ResponseEntity<String> loginResponse = restTemplate.postForEntity(apiBaseUrl + "/merchant/login", entity, String.class);
        assertThat(loginResponse.getStatusCodeValue()).isEqualTo(200);

        String responseBody = loginResponse.getBody();
        assertThat(responseBody).isNotNull();
        String token = com.jayway.jsonpath.JsonPath.read(responseBody, "$.token");
        String merchantId = Objects.toString(com.jayway.jsonpath.JsonPath.read(responseBody, "$.merchant.merchantId"), null);

        sharedData.put("merchantToken", token);
        sharedData.put("merchantId", merchantId);
        logger.info("Merchant {} logged in. Token and ID stored.", email);
    }

    private HttpHeaders buildAuthenticatedHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (sharedData.containsKey("merchantToken")) {
            headers.setBearerAuth(sharedData.get("merchantToken"));
        } else {
            logger.warn("No merchant token found in sharedData for authenticated request!");
            // This would likely lead to a 401/403 which might be the expected outcome for some tests.
        }
        return headers;
    }

    @When("a POST request is made to {string} with an authenticated merchant and the following body:")
    public void a_post_request_is_made_to_with_auth_merchant_body(String path, String requestBody) {
        HttpHeaders headers = buildAuthenticatedHeaders();
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        latestResponse = restTemplate.postForEntity(apiBaseUrl + path, entity, String.class);
        logger.info("Authenticated Merchant POST to {}: Status {}, Body {}", path, latestResponse.getStatusCodeValue(), latestResponse.getBody());
        // Store merchantProductId if present in response
        try {
            String mpId = Objects.toString(com.jayway.jsonpath.JsonPath.read(latestResponse.getBody(), "$.merchantProductId"), null);
            if (mpId != null) {
                sharedData.put("lastMerchantProductId", mpId);
                logger.info("Stored lastMerchantProductId: {}", mpId);
            }
        } catch (Exception e) { /* Path not found, or not an add product response */ }
    }

    @When("a GET request is made to {string} with an authenticated merchant")
    public void a_get_request_is_made_to_with_auth_merchant(String path) {
        HttpHeaders headers = buildAuthenticatedHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        latestResponse = restTemplate.exchange(apiBaseUrl + path, HttpMethod.GET, entity, String.class);
        logger.info("Authenticated Merchant GET to {}: Status {}, Body {}", path, latestResponse.getStatusCodeValue(), latestResponse.getBody());
    }

    @When("a PUT request is made to {string} with an authenticated merchant and the following body:")
    public void a_put_request_is_made_to_with_auth_merchant_body(String path, String requestBody) {
        String resolvedPath = resolveMerchantProductPathVariables(path);
        HttpHeaders headers = buildAuthenticatedHeaders();
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        latestResponse = restTemplate.exchange(apiBaseUrl + resolvedPath, HttpMethod.PUT, entity, String.class);
        logger.info("Authenticated Merchant PUT to {}: Status {}, Body {}", resolvedPath, latestResponse.getStatusCodeValue(), latestResponse.getBody());
    }

    @When("a DELETE request is made to {string} with an authenticated merchant")
    public void a_delete_request_is_made_to_with_auth_merchant(String path) {
        String resolvedPath = resolveMerchantProductPathVariables(path);
        HttpHeaders headers = buildAuthenticatedHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        latestResponse = restTemplate.exchange(apiBaseUrl + resolvedPath, HttpMethod.DELETE, entity, String.class);
        logger.info("Authenticated Merchant DELETE to {}: Status {}, Body {}", resolvedPath, latestResponse.getStatusCodeValue(), latestResponse.getBody());
    }

    @Given("the following product is added by the merchant:")
    public void the_following_product_is_added_by_the_merchant(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            try {
                String requestBody = objectMapper.writeValueAsString(row);
                a_post_request_is_made_to_with_auth_merchant_body("/merchants/products", requestBody);
                assertThat(latestResponse.getStatusCode().is2xxSuccessful()).isTrue(); // Ensure product was added
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to convert product row to JSON", e);
            }
        }
    }

    @Given("the following product is added by the merchant and its merchantProductId is stored as {string}:")
    public void the_following_product_is_added_and_id_stored(String sharedKey, DataTable dataTable) {
         List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
         // Assuming one row for simplicity for this step
         Map<String, String> row = rows.get(0);
         try {
            String requestBody = objectMapper.writeValueAsString(row);
            a_post_request_is_made_to_with_auth_merchant_body("/merchants/products", requestBody);
            assertThat(latestResponse.getStatusCode().is2xxSuccessful()).isTrue();
            String mpId = Objects.toString(com.jayway.jsonpath.JsonPath.read(latestResponse.getBody(), "$.merchantProductId"), null);
            assertThat(mpId).isNotNull();
            sharedData.put(sharedKey, mpId);
            logger.info("Stored merchantProductId {} as {}", mpId, sharedKey);
         } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert product row to JSON for storing ID", e);
        }
    }

    private String resolveMerchantProductPathVariables(String path) {
        String resolvedPath = path;
        // Example: /merchants/products/{mpid_to_update}
        for (Map.Entry<String, String> entry : sharedData.entrySet()) {
            if (path.contains("{" + entry.getKey() + "}")) {
                resolvedPath = resolvedPath.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }
        if (resolvedPath.contains("{") && resolvedPath.contains("}")) {
             logger.warn("Path {} still contains unresolved placeholders.", resolvedPath);
        }
        return resolvedPath;
    }

    // --- Common Then Steps (can be refactored) ---
    @Then("the response status code should be {int}")
    public void the_response_status_code_should_be(Integer statusCode) {
        assertThat(latestResponse.getStatusCodeValue()).isEqualTo(statusCode);
    }

    @Then("the response body should contain a {string}")
    public void the_response_body_should_contain_a(String jsonPath) {
        assertThat(latestResponse.getBody()).isNotNull();
        com.jayway.jsonpath.JsonPath.read(latestResponse.getBody(), "$." + jsonPath);
    }

    @Then("the response body should contain {string} with value {string}")
    public void the_response_body_should_contain_with_value(String jsonPath, String expectedValue) {
        assertThat(latestResponse.getBody()).isNotNull();
        String actualValue = Objects.toString(com.jayway.jsonpath.JsonPath.read(latestResponse.getBody(), "$." + jsonPath), "");
        assertThat(actualValue).isEqualTo(expectedValue);
    }

    @Then("the response body should contain {string} with value {int}")
    public void the_response_body_should_contain_with_int_value(String jsonPath, Integer expectedValue) {
        assertThat(latestResponse.getBody()).isNotNull();
        Integer actualValue = com.jayway.jsonpath.JsonPath.read(latestResponse.getBody(), "$." + jsonPath);
        assertThat(actualValue).isEqualTo(expectedValue);
    }


    @Then("the response body should be an empty list")
    public void the_response_body_should_be_an_empty_list() {
        assertThat(latestResponse.getBody()).isNotNull();
        List<?> list = com.jayway.jsonpath.JsonPath.parse(latestResponse.getBody()).read("$");
        assertThat(list).isNotNull().isEmpty();
    }

    @Then("the response body should be a list with {int} item(s)")
    public void the_response_body_should_be_a_list_with_items(int count) {
        assertThat(latestResponse.getBody()).isNotNull();
        List<?> list = com.jayway.jsonpath.JsonPath.parse(latestResponse.getBody()).read("$");
        assertThat(list).isNotNull().hasSize(count);
    }

    @Then("the response body should contain an {string} field")
    public void the_response_body_should_contain_an_error_field(String fieldName) {
        assertThat(latestResponse.getBody()).isNotNull();
        com.jayway.jsonpath.JsonPath.read(latestResponse.getBody(), "$." + fieldName);
    }

    @Then("the response body should contain {string} greater than {int}")
    public void the_response_body_should_contain_greater_than(String jsonPath, int value) {
        assertThat(latestResponse.getBody()).isNotNull();
        Integer actualValue = com.jayway.jsonpath.JsonPath.read(latestResponse.getBody(), "$." + jsonPath);
        assertThat(actualValue).isGreaterThan(value);
    }
}
