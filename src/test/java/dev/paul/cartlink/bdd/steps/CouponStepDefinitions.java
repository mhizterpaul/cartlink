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

    private String apiBaseUrl;
    private ResponseEntity<String> latestResponse;
    private Map<String, String> sharedData = new HashMap<>();

    @Before
    public void setUp() {
        couponRepository.deleteAll();
        // merchantProductRepository.deleteAll(); // If needed and not handled by other steps
        // productRepository.deleteAll();
        // merchantRepository.deleteAll();
        sharedData.clear();
        logger.info("CouponStepDefinitions: Cleared coupon repository and sharedData.");
    }

    @After
    public void tearDown() {}

    @Given("the API base URL is {string}")
    public void the_api_base_url_is(String baseUrl) {
        this.apiBaseUrl = baseUrl;
    }

    @Given("a merchant is logged in with email {string} and password {string}")
    public void a_merchant_is_logged_in_with_email_and_password(String email, String password) throws JsonProcessingException {
        Merchant merchant = merchantRepository.findByEmail(email).orElseGet(() -> {
            Merchant m = new Merchant();
            m.setEmail(email);
            m.setPassword(passwordEncoder.encode(password));
            m.setFirstName("CouponTest");
            m.setLastName("Merchant");
            return merchantRepository.save(m);
        });
        sharedData.put("merchantId", merchant.getMerchantId().toString()); // Store actual merchant ID

        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", email);
        loginRequest.put("password", password);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(loginRequest), headers);
        ResponseEntity<String> loginResponse = restTemplate.postForEntity(apiBaseUrl.replace("/v1","") + "/merchant/login", entity, String.class); // Corrected login path

        if(loginResponse.getStatusCodeValue() != 200) {
            logger.error("Merchant login failed for {}: {} - {}", email, loginResponse.getStatusCodeValue(), loginResponse.getBody());
             assertThat(loginResponse.getStatusCodeValue()).isEqualTo(200);
        }
        String responseBody = loginResponse.getBody();
        String token = com.jayway.jsonpath.JsonPath.read(responseBody, "$.token");
        // String loggedInMerchantId = Objects.toString(com.jayway.jsonpath.JsonPath.read(responseBody, "$.merchant.merchantId"));
        // sharedData.put("merchantId", loggedInMerchantId); // Already got from merchant object
        sharedData.put("merchantToken", token);
        logger.info("Merchant {} logged in. Token and merchantId {} stored.", email, sharedData.get("merchantId"));
    }

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
        sharedData.put(productKey, savedProduct.getProductId().toString()); // Store actual Product ID
        logger.info("Created Product '{}' with actual ID {}, stored as {}", productName, savedProduct.getProductId(), productKey);

        // Create MerchantProduct (link between merchant and product with specific stock/price)
        // This step was missing in some previous definitions, but crucial.
        // However, CouponController uses ProductID directly, not MerchantProductID for path.
        // The Coupon entity in service layer links to Product and Merchant.
        // So, we just need the Product's ID. Merchant is via @AuthenticationPrincipal.
    }

    @Given("the following coupon is created for product {string} by the merchant and its ID is stored as {string}:")
    public void coupon_is_created_for_product_and_id_stored(String productKey, String couponSharedKey, DataTable dataTable) throws JsonProcessingException {
        String productId = sharedData.get(productKey);
        assertThat(productId).isNotNull().withFailMessage("Product ID for key '" + productKey + "' not found in sharedData.");

        Map<String, String> row = dataTable.asMaps(String.class, String.class).get(0);
        String requestBody = objectMapper.writeValueAsString(row);

        String path = "/merchants/" + sharedData.get("merchantId") + "/products/" + productId + "/coupons";

        HttpHeaders headers = buildAuthenticatedHeaders();
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(apiBaseUrl + path, entity, String.class);

        assertThat(response.getStatusCodeValue()).isEqualTo(201);
        String couponId = Objects.toString(com.jayway.jsonpath.JsonPath.read(response.getBody(), "$.couponId"), null);
        assertThat(couponId).isNotNull();
        sharedData.put(couponSharedKey, couponId);
        logger.info("Created coupon for product ID {}, stored coupon ID {} as {}", productId, couponId, couponSharedKey);
    }


    private HttpHeaders buildAuthenticatedHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (sharedData.containsKey("merchantToken")) {
            headers.setBearerAuth(sharedData.get("merchantToken"));
        } else {
            logger.warn("No merchant token found for authenticated request!");
        }
        return headers;
    }

    private String resolvePathPlaceholders(String path) {
        String resolvedPath = path;
        for (Map.Entry<String, String> entry : sharedData.entrySet()) {
            if (resolvedPath.contains("{" + entry.getKey() + "}")) {
                resolvedPath = resolvedPath.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }
        if (resolvedPath.contains("{") && resolvedPath.contains("}")) {
             logger.warn("Path {} still contains unresolved placeholders: {}", path, resolvedPath);
        }
        return resolvedPath;
    }

    @When("a POST request is made to {string} with an authenticated merchant and the following body:")
    public void a_post_request_is_made_to_with_auth_merchant_body(String path, String requestBody) {
        HttpHeaders headers = buildAuthenticatedHeaders();
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers); // Assuming body doesn't need placeholder resolution here
        latestResponse = restTemplate.postForEntity(apiBaseUrl + resolvePathPlaceholders(path), entity, String.class);
        // Store couponId if it's a create response
        try {
            if (latestResponse.getStatusCodeValue() == 201 && latestResponse.getBody().contains("couponId")) {
                 String couponId = Objects.toString(com.jayway.jsonpath.JsonPath.read(latestResponse.getBody(), "$.couponId"), null);
                 if (couponId != null) sharedData.put("lastCouponId", couponId);
            }
        } catch (Exception e) { /* ignore if not relevant */ }
    }

    @When("a GET request is made to {string} with an authenticated merchant")
    public void a_get_request_is_made_to_with_auth_merchant(String path) {
        HttpHeaders headers = buildAuthenticatedHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        latestResponse = restTemplate.exchange(apiBaseUrl + resolvePathPlaceholders(path), HttpMethod.GET, entity, String.class);
    }

    @When("a DELETE request is made to {string} with an authenticated merchant")
    public void a_delete_request_is_made_to_with_auth_merchant(String path) {
        HttpHeaders headers = buildAuthenticatedHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        latestResponse = restTemplate.exchange(apiBaseUrl + resolvePathPlaceholders(path), HttpMethod.DELETE, entity, String.class);
    }

    @When("a POST request is made to {string} with the following body:") // Unauthenticated
    public void a_post_request_is_made_to_with_body(String path, String requestBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        latestResponse = restTemplate.postForEntity(apiBaseUrl + resolvePathPlaceholders(path), entity, String.class);
    }

    @When("a GET request is made to {string}") // Unauthenticated
    public void a_get_request_is_made_to(String path) {
        HttpEntity<Void> entity = new HttpEntity<>(new HttpHeaders());
        latestResponse = restTemplate.exchange(apiBaseUrl + resolvePathPlaceholders(path), HttpMethod.GET, entity, String.class);
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
        String resolvedExpectedValue = expectedValue;
        if (expectedValue.startsWith("{") && expectedValue.endsWith("}")) {
            resolvedExpectedValue = sharedData.getOrDefault(expectedValue.substring(1, expectedValue.length()-1), expectedValue);
        }
        String actualValue = Objects.toString(com.jayway.jsonpath.JsonPath.read(latestResponse.getBody(), "$." + jsonPath), "");
        assertThat(actualValue).isEqualTo(resolvedExpectedValue);
    }

    @Then("the response body should contain {string} with number value {string}")
    public void the_response_body_should_contain_with_number_value(String jsonPath, String expectedValue) {
        assertThat(latestResponse.getBody()).isNotNull();
        Object actualObject = com.jayway.jsonpath.JsonPath.read(latestResponse.getBody(), "$." + jsonPath);
        BigDecimal actualValue = new BigDecimal(actualObject.toString());
        BigDecimal expectedDecimalValue = new BigDecimal(expectedValue);
        assertThat(actualValue).isEqualByComparingTo(expectedDecimalValue);
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
        the_response_body_should_contain_a_key(fieldName);
    }
}
