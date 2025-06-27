package dev.paul.cartlink.bdd.steps;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.paul.cartlink.merchant.model.Coupon;
import dev.paul.cartlink.merchant.model.Merchant;
import dev.paul.cartlink.merchant.model.MerchantProduct; // Needed if we create MerchantProduct
import dev.paul.cartlink.merchant.repository.CouponRepository;
import dev.paul.cartlink.merchant.repository.MerchantProductRepository;
import dev.paul.cartlink.merchant.repository.MerchantRepository;
import dev.paul.cartlink.product.model.Product;
import dev.paul.cartlink.product.repository.ProductRepository;
import dev.paul.cartlink.bdd.context.ScenarioContext;

import io.cucumber.datatable.DataTable;
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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class CouponStepDefinitions {

    private static final Logger logger = LoggerFactory.getLogger(CouponStepDefinitions.class);

    @Autowired private TestRestTemplate restTemplate;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private MerchantRepository merchantRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private MerchantProductRepository merchantProductRepository; // To create MerchantProduct link

    @Autowired private CouponRepository couponRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Autowired
    private ScenarioContext scenarioContext;

    // private String apiBaseUrl; // Removed, using ScenarioContext
    // private ResponseEntity<String> latestResponse; // Will be set in scenarioContext by request steps
    // private Map<String, String> sharedData = new HashMap<>(); // Will use ScenarioContext

    @Before
    public void setUp() {
        couponRepository.deleteAll();
        // merchantProductRepository.deleteAll(); // If needed and not handled by other steps
        // productRepository.deleteAll();
        // merchantRepository.deleteAll();
        // sharedData.clear(); // Not using local sharedData anymore
        logger.info("CouponStepDefinitions: Cleared coupon repository.");
    }

    @After
    public void tearDown() {}

    // This step is now in CommonStepDefinitions.java
    // @Given("a merchant is logged in with email {string} and password {string}")
    // public void a_merchant_is_logged_in_with_email_and_password(String email, String password) throws JsonProcessingException {
    //     String apiBaseUrl = scenarioContext.getString("apiBaseUrl");
    //     Merchant merchant = merchantRepository.findByEmail(email).orElseGet(() -> {
    //         Merchant m = new Merchant();
    //         m.setEmail(email);
    //         m.setPassword(passwordEncoder.encode(password));
    //         m.setFirstName("CouponTest");
    //         m.setLastName("Merchant");
    //         return merchantRepository.save(m);
    //     });
    //     scenarioContext.set("merchantId", merchant.getMerchantId().toString()); // Store actual merchant ID in ScenarioContext
    //
    //     Map<String, String> loginRequest = new HashMap<>();
    //     loginRequest.put("email", email);
    //     loginRequest.put("password", password);
    //     HttpHeaders headers = new HttpHeaders();
    //     headers.setContentType(MediaType.APPLICATION_JSON);
    //     HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(loginRequest), headers);
    //     // Note: CommonStepDefinitions uses /merchants/login. This one used /merchant/login.
    //     // Sticking to the one in CommonStepDefinitions for consistency.
    //     ResponseEntity<String> loginResponse = restTemplate.postForEntity(apiBaseUrl + "/merchants/login", entity, String.class);
    //
    //     if(loginResponse.getStatusCodeValue() != 200) {
    //         logger.error("Merchant login failed for {}: {} - {}", email, loginResponse.getStatusCodeValue(), loginResponse.getBody());
    //          assertThat(loginResponse.getStatusCodeValue()).isEqualTo(200);
    //     }
    //     String responseBody = loginResponse.getBody();
    //     String token = com.jayway.jsonpath.JsonPath.read(responseBody, "$.token");
    //     scenarioContext.set("merchantToken", token); // Store in ScenarioContext
    //     logger.info("Merchant {} logged in. Token and merchantId {} stored in ScenarioContext.", email, scenarioContext.getString("merchantId"));
    // }

    @Given("merchant {string} has a product {string} with original ProductID {string} price {double} and stock {int}, whose actual ProductID is stored as {string} and MerchantProductID as {string}")
    public void merchant_has_product_details_stored(String merchantEmail, String productName, String originalProdId, double price, int stock, String productKey, String mpKey) {
        Merchant merchant = merchantRepository.findByEmail(merchantEmail)
            .orElseThrow(() -> new AssertionError("Merchant " + merchantEmail + " not found."));

        // Create Product
        Product product = new Product();
        product.setName(productName);
        product.setBrand("CouponBrand");
        product.setCategory("CouponCategory");
        Product savedProduct = productRepository.save(product);
        scenarioContext.set(productKey, savedProduct.getProductId().toString()); // Store actual Product ID in ScenarioContext
        logger.info("Created Product '{}' with actual ID {}, stored as {}", productName, savedProduct.getProductId(), productKey);

        // Create MerchantProduct (link between merchant and product with specific stock/price)
        // This step was missing in some previous definitions, but crucial.
        // However, CouponController uses ProductID directly, not MerchantProductID for path.
        // The Coupon entity in service layer links to Product and Merchant.
        // So, we just need the Product's ID. Merchant is via @AuthenticationPrincipal.
    }

    @Given("the following coupon is created for product {string} by the merchant and its ID is stored as {string}:")
    public void coupon_is_created_for_product_and_id_stored(String productKey, String couponSharedKey, DataTable dataTable) throws JsonProcessingException {
        String productId = scenarioContext.getString(productKey); // Get from ScenarioContext
        assertThat(productId).isNotNull().withFailMessage("Product ID for key '" + productKey + "' not found in ScenarioContext.");

        Map<String, String> row = dataTable.asMaps(String.class, String.class).get(0);
        String requestBody = objectMapper.writeValueAsString(row);

        String path = "/merchants/" + scenarioContext.getString("merchantId") + "/products/" + productId + "/coupons"; // Get merchantId from ScenarioContext

        HttpHeaders headers = buildAuthenticatedHeaders();
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(scenarioContext.getString("apiBaseUrl") + path, entity, String.class);
        scenarioContext.set("latestResponse", response); // Store response

        assertThat(response.getStatusCodeValue()).isEqualTo(201);
        String couponId = Objects.toString(com.jayway.jsonpath.JsonPath.read(response.getBody(), "$.couponId"), null);
        assertThat(couponId).isNotNull();
        scenarioContext.set(couponSharedKey, couponId); // Store in ScenarioContext
        logger.info("Created coupon for product ID {}, stored coupon ID {} as {}", productId, couponId, couponSharedKey);
    }


    private HttpHeaders buildAuthenticatedHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (scenarioContext.containsKey("merchantToken")) { // Check ScenarioContext
            headers.setBearerAuth(scenarioContext.getString("merchantToken")); // Get from ScenarioContext
        } else {
            logger.warn("No merchant token found for authenticated request in ScenarioContext!");
        }
        return headers;
    }

    private String resolvePathPlaceholders(String path) {
        String resolvedPath = path;
        String[] keysToResolve = {"merchantId", "productId", "couponId", "lastCouponId"}; // Add other common keys if needed

        for (String key : keysToResolve) {
            if (resolvedPath.contains("{" + key + "}")) {
                if (scenarioContext.containsKey(key)) {
                    resolvedPath = resolvedPath.replace("{" + key + "}", scenarioContext.getString(key));
                } else {
                    logger.warn("Placeholder {{{}}} found in path but key not in ScenarioContext.", key);
                }
            }
        }

        if (resolvedPath.contains("{") && resolvedPath.contains("}")) {
             logger.warn("Path {} still contains unresolved placeholders after specific key resolution: {}", path, resolvedPath);
        }
        return resolvedPath;
    }

    @When("a POST request is made to {string} with an authenticated merchant and the following body:")
    public void a_post_request_is_made_to_with_auth_merchant_body(String path, String requestBody) {
        String apiBaseUrl = scenarioContext.getString("apiBaseUrl");
        HttpHeaders headers = buildAuthenticatedHeaders();
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers); // Assuming body doesn't need placeholder resolution here
        ResponseEntity<String> response = restTemplate.postForEntity(apiBaseUrl + resolvePathPlaceholders(path), entity, String.class);
        scenarioContext.set("latestResponse", response);
        // Store couponId if it's a create response
        try {
            if (response.getStatusCodeValue() == 201 && response.getBody().contains("couponId")) {
                 String couponId = Objects.toString(com.jayway.jsonpath.JsonPath.read(response.getBody(), "$.couponId"), null);
                 if (couponId != null) scenarioContext.set("lastCouponId", couponId); // Store in ScenarioContext
            }
        } catch (Exception e) { /* ignore if not relevant */ }
    }

    @When("a GET request is made to {string} with an authenticated merchant")
    public void a_get_request_is_made_to_with_auth_merchant(String path) {
        String apiBaseUrl = scenarioContext.getString("apiBaseUrl");
        HttpHeaders headers = buildAuthenticatedHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(apiBaseUrl + resolvePathPlaceholders(path), HttpMethod.GET, entity, String.class);
        scenarioContext.set("latestResponse", response);
    }

    @When("a DELETE request is made to {string} with an authenticated merchant")
    public void a_delete_request_is_made_to_with_auth_merchant(String path) {
        String apiBaseUrl = scenarioContext.getString("apiBaseUrl");
        HttpHeaders headers = buildAuthenticatedHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(apiBaseUrl + resolvePathPlaceholders(path), HttpMethod.DELETE, entity, String.class);
        scenarioContext.set("latestResponse", response);
    }

    @When("a POST request is made to {string} with the following body:") // Unauthenticated
    public void a_post_request_is_made_to_with_body(String path, String requestBody) {
        String apiBaseUrl = scenarioContext.getString("apiBaseUrl");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(apiBaseUrl + resolvePathPlaceholders(path), entity, String.class);
        scenarioContext.set("latestResponse", response);
    }

    // --- Then Steps ---

    @Then("the response body should contain a {string}")
    public void the_response_body_should_contain_a_key(String jsonPath) {
        @SuppressWarnings("unchecked")
        ResponseEntity<String> localLatestResponse = scenarioContext.get("latestResponse", ResponseEntity.class); // Prefer context
        assertThat(localLatestResponse.getBody()).isNotNull();
        com.jayway.jsonpath.JsonPath.read(localLatestResponse.getBody(), "$." + jsonPath);
    }

    @Then("the response body should contain {string} with value {string}")
    public void the_response_body_should_contain_with_value(String jsonPath, String expectedValue) {
        @SuppressWarnings("unchecked")
        ResponseEntity<String> localLatestResponse = scenarioContext.get("latestResponse", ResponseEntity.class);
        assertThat(localLatestResponse.getBody()).isNotNull();
        String resolvedExpectedValue = expectedValue;
        if (expectedValue.startsWith("{") && expectedValue.endsWith("}")) {
            String placeholderKey = expectedValue.substring(1, expectedValue.length() - 1);
            if (scenarioContext.containsKey(placeholderKey)) {
                resolvedExpectedValue = scenarioContext.getString(placeholderKey);
            } else {
                 logger.warn("Placeholder value for key '{}' not found in ScenarioContext, using literal: {}", placeholderKey, expectedValue);
            }
        }
        String actualValue = Objects.toString(com.jayway.jsonpath.JsonPath.read(localLatestResponse.getBody(), "$." + jsonPath), "");
        assertThat(actualValue).isEqualTo(resolvedExpectedValue);
    }

    // This step is now in CommonStepDefinitions.java
    // @Then("the response body should contain {string} with number value {string}")
    // public void the_response_body_should_contain_with_number_value(String jsonPath, String expectedValue) {
    //     ResponseEntity<String> localLatestResponse = scenarioContext.get("latestResponse", ResponseEntity.class);
    //     assertThat(localLatestResponse.getBody()).isNotNull();
    //     Object actualObject = com.jayway.jsonpath.JsonPath.read(localLatestResponse.getBody(), "$." + jsonPath);
    //     BigDecimal actualValue = new BigDecimal(actualObject.toString());
    //     BigDecimal expectedDecimalValue = new BigDecimal(expectedValue);
    //     assertThat(actualValue).isEqualByComparingTo(expectedDecimalValue);
    // }

    // This step is now in CommonStepDefinitions.java
    // @Then("the response body should be an empty list")
    // public void the_response_body_should_be_an_empty_list() {
    //     @SuppressWarnings("unchecked")
    //     ResponseEntity<String> localLatestResponse = scenarioContext.get("latestResponse", ResponseEntity.class);
    //     assertThat(localLatestResponse.getBody()).isNotNull();
    //     List<?> list = com.jayway.jsonpath.JsonPath.parse(localLatestResponse.getBody()).read("$");
    //     assertThat(list).isNotNull().isEmpty();
    // }

    @Then("the response body should be a list with {int} item(s)")
    public void the_response_body_should_be_a_list_with_items(int count) {
        @SuppressWarnings("unchecked")
        ResponseEntity<String> localLatestResponse = scenarioContext.get("latestResponse", ResponseEntity.class);
        assertThat(localLatestResponse.getBody()).isNotNull();
        List<?> list = com.jayway.jsonpath.JsonPath.parse(localLatestResponse.getBody()).read("$");
        assertThat(list).isNotNull().hasSize(count);
    }

    @Then("the response body should contain an {string} field")
    public void the_response_body_should_contain_an_error_field(String fieldName) {
        the_response_body_should_contain_a_key(fieldName);
    }
}
