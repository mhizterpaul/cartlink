package dev.paul.cartlink.bdd.steps;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.paul.cartlink.customer.model.Customer;
import dev.paul.cartlink.customer.model.Review;
import dev.paul.cartlink.customer.repository.CustomerRepository;
import dev.paul.cartlink.customer.repository.ReviewRepository;
import dev.paul.cartlink.merchant.model.Merchant;
import dev.paul.cartlink.merchant.repository.MerchantRepository;

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
import org.springframework.security.crypto.password.PasswordEncoder; // If any auth was needed

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class ReviewStepDefinitions {

    private static final Logger logger = LoggerFactory.getLogger(ReviewStepDefinitions.class);

    @Autowired private TestRestTemplate restTemplate;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private MerchantRepository merchantRepository;
    @Autowired private ReviewRepository reviewRepository;
    // No PasswordEncoder needed as Review endpoints don't involve direct login steps in this file.

    private String apiBaseUrl;
    private ResponseEntity<String> latestResponse;
    private Map<String, String> sharedData = new HashMap<>();

    @Before
    public void setUp() {
        reviewRepository.deleteAll(); // Clear reviews before each scenario
        // customerRepository.deleteAll(); // Should be handled by respective step defs or test data setup
        // merchantRepository.deleteAll();
        sharedData.clear();
        logger.info("ReviewStepDefinitions: Cleared review repository and sharedData.");
    }

    @After
    public void tearDown() {}

    @Given("the API base URL is {string}")
    public void the_api_base_url_is(String baseUrl) {
        this.apiBaseUrl = baseUrl;
    }

    @Given("a customer {string} exists and their ID is stored as {string}")
    public void a_customer_exists_stored_as(String email, String customerKey) {
        Customer customer = customerRepository.findByEmail(email).orElseGet(() -> {
            Customer c = new Customer();
            c.setEmail(email);
            c.setFirstName(email.split("@")[0]); // "review.customer"
            c.setLastName("User");
            return customerRepository.save(c);
        });
        sharedData.put(customerKey, customer.getCustomerId().toString());
        logger.info("Ensured customer {} exists with ID {}, stored as {}", email, customer.getCustomerId(), customerKey);
    }

    @Given("a merchant {string} exists and their ID is stored as {string}")
    public void a_merchant_exists_stored_as(String email, String merchantKey) {
        Merchant merchant = merchantRepository.findByEmail(email).orElseGet(() -> {
            Merchant m = new Merchant();
            m.setEmail(email);
            m.setFirstName(email.split("@")[0]); // "review.merchant"
            m.setLastName("Store");
            // m.setPassword("dummyPassword"); // If password was not nullable
            return merchantRepository.save(m);
        });
        sharedData.put(merchantKey, merchant.getMerchantId().toString());
        logger.info("Ensured merchant {} exists with ID {}, stored as {}", email, merchant.getMerchantId(), merchantKey);
    }

    @Given("the following review is created:")
    public void the_following_review_is_created(DataTable dataTable) throws JsonProcessingException {
        Map<String, String> row = dataTable.asMaps(String.class, String.class).get(0);

        String customerIdStr = resolvePlaceholders(row.get("customerId"));
        String merchantIdStr = resolvePlaceholders(row.get("merchantId"));

        Customer customer = customerRepository.findById(Long.parseLong(customerIdStr))
            .orElseThrow(() -> new AssertionError("Customer not found for review precondition: " + customerIdStr));
        Merchant merchant = merchantRepository.findById(Long.parseLong(merchantIdStr))
            .orElseThrow(() -> new AssertionError("Merchant not found for review precondition: " + merchantIdStr));

        Map<String, Object> reviewMap = new HashMap<>();
        reviewMap.put("customer", Map.of("customerId", customer.getCustomerId()));
        reviewMap.put("merchant", Map.of("merchantId", merchant.getMerchantId()));
        reviewMap.put("rating", Integer.parseInt(row.get("rating")));
        reviewMap.put("comment", row.get("comment"));
        // orderId and productId are not part of Review entity as per Review.java

        String requestBody = objectMapper.writeValueAsString(reviewMap);
        makePostRequest("/reviews", requestBody); // Using common method
        assertThat(latestResponse.getStatusCode().is2xxSuccessful()).isTrue();
        logger.info("Created precondition review: {}", requestBody);
    }

    @Given("the following review is created for merchant {string}:")
    public void the_following_review_is_created_for_merchant(String merchantKey, DataTable dataTable) throws JsonProcessingException {
        // This is largely the same as the above, just ensures the merchantId in data matches the one stored by merchantKey
        Map<String, String> row = dataTable.asMaps(String.class, String.class).get(0);

        String customerIdStr = resolvePlaceholders(row.get("customerId"));
        String actualMerchantId = sharedData.get(merchantKey);
        assertThat(actualMerchantId).isNotNull();
        // Ensure the merchantId in the DataTable matches the one from the merchantKey placeholder
        assertThat(resolvePlaceholders(row.get("merchantId"))).isEqualTo(actualMerchantId);


        Customer customer = customerRepository.findById(Long.parseLong(customerIdStr))
            .orElseThrow(() -> new AssertionError("Customer not found for review precondition: " + customerIdStr));
        Merchant merchant = merchantRepository.findById(Long.parseLong(actualMerchantId))
            .orElseThrow(() -> new AssertionError("Merchant not found for review precondition: " + actualMerchantId));

        Map<String, Object> reviewMap = new HashMap<>();
        reviewMap.put("customer", Map.of("customerId", customer.getCustomerId()));
        reviewMap.put("merchant", Map.of("merchantId", merchant.getMerchantId()));
        reviewMap.put("rating", Integer.parseInt(row.get("rating")));
        reviewMap.put("comment", row.get("comment"));

        String requestBody = objectMapper.writeValueAsString(reviewMap);
        makePostRequest("/reviews", requestBody);
        assertThat(latestResponse.getStatusCode().is2xxSuccessful()).isTrue();
        logger.info("Created precondition review for merchant {}: {}", actualMerchantId, requestBody);
    }

    private String resolvePlaceholders(String valueWithPlaceholders) {
        String resolvedValue = valueWithPlaceholders;
        for (Map.Entry<String, String> entry : sharedData.entrySet()) {
            if (resolvedValue.contains("{" + entry.getKey() + "}")) {
                resolvedValue = resolvedValue.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }
        return resolvedValue;
    }

    private void makePostRequest(String path, String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // No authentication on ReviewController POST, so no token needed.
        HttpEntity<String> entity = new HttpEntity<>(resolvePlaceholders(body), headers); // Resolve placeholders in body
        latestResponse = restTemplate.postForEntity(apiBaseUrl + resolvePlaceholders(path), entity, String.class);
        logger.info("POST to {}: Status {}, Body {}", resolvePlaceholders(path), latestResponse.getStatusCodeValue(), latestResponse.getBody());
    }

    @When("a POST request is made to {string} with the following body:")
    public void a_post_request_is_made_to_with_body(String path, String requestBody) {
        makePostRequest(path, requestBody);
    }

    @When("a GET request is made to {string}")
    public void a_get_request_is_made_to(String path) {
        HttpEntity<Void> entity = new HttpEntity<>(new HttpHeaders());
        latestResponse = restTemplate.exchange(apiBaseUrl + resolvePlaceholders(path), HttpMethod.GET, entity, String.class);
        logger.info("GET to {}: Status {}, Body {}", resolvePlaceholders(path), latestResponse.getStatusCodeValue(), latestResponse.getBody());
    }

    // --- Then Steps ---
    @Then("the response status code should be {int}")
    public void the_response_status_code_should_be(Integer statusCode) {
        assertThat(latestResponse.getStatusCodeValue()).isEqualTo(statusCode);
    }

    @Then("the response body should contain a {string}")
    public void the_response_body_should_contain_a_key(String jsonPath) {
        assertThat(latestResponse.getBody()).isNotNull();
        com.jayway.jsonpath.JsonPath.read(latestResponse.getBody(), "$." + jsonPath);
    }

    @Then("the response body should contain {string} with value {string}")
    public void the_response_body_should_contain_with_value(String jsonPath, String expectedValue) {
        assertThat(latestResponse.getBody()).isNotNull();
        String resolvedExpectedValue = resolvePlaceholders(expectedValue);
        String actualValue = Objects.toString(com.jayway.jsonpath.JsonPath.read(latestResponse.getBody(), "$." + jsonPath), "");
        assertThat(actualValue).isEqualTo(resolvedExpectedValue);
    }

    @Then("the response body should contain {string} with value {int}")
    public void the_response_body_should_contain_with_value_int(String jsonPath, Integer expectedValue) {
        assertThat(latestResponse.getBody()).isNotNull();
        Integer actualValue = com.jayway.jsonpath.JsonPath.read(latestResponse.getBody(), "$." + jsonPath);
        assertThat(actualValue).isEqualTo(expectedValue);
    }

    @Then("the response body should contain an {string} field")
    public void the_response_body_should_contain_an_error_field(String fieldName) {
        the_response_body_should_contain_a_key(fieldName);
    }

    @Then("the response body should be a list")
    public void the_response_body_should_be_a_list() {
        assertThat(latestResponse.getBody()).isNotNull();
        List<?> list = com.jayway.jsonpath.JsonPath.parse(latestResponse.getBody()).read("$");
        assertThat(list).isInstanceOf(List.class);
    }

    @Then("the response body should be a list with at least {int} item(s)")
    public void the_response_body_should_be_a_list_with_at_least_items(int minCount) {
        assertThat(latestResponse.getBody()).isNotNull();
        List<?> list = com.jayway.jsonpath.JsonPath.parse(latestResponse.getBody()).read("$");
        assertThat(list).isNotNull().hasSizeGreaterThanOrEqualTo(minCount);
    }

    @Then("all items in the list should have {string} with value {string}")
    public void all_items_in_list_should_have_value(String jsonPathToList, String expectedValue) {
        assertThat(latestResponse.getBody()).isNotNull();
        List<Map<String, Object>> list = com.jayway.jsonpath.JsonPath.parse(latestResponse.getBody()).read("$"); // Assumes list is root
        String resolvedExpectedValue = resolvePlaceholders(expectedValue);
        for (Map<String, Object> item : list) {
            // This assumes jsonPathToList is relative to each item, e.g., "merchant.merchantId"
            // For JsonPath to work on item map, it needs to be converted back to JSON string or use specific map access.
            // Simpler: Access via map keys if structure is known.
            // For "merchant.merchantId", it would be item.get("merchant").get("merchantId")
            String[] keys = jsonPathToList.split("\\.");
            Object currentValue = item;
            for (String key : keys) {
                assertThat(currentValue).isInstanceOf(Map.class);
                currentValue = ((Map<?,?>)currentValue).get(key);
                assertThat(currentValue).isNotNull();
            }
            assertThat(Objects.toString(currentValue)).isEqualTo(resolvedExpectedValue);
        }
    }
}
