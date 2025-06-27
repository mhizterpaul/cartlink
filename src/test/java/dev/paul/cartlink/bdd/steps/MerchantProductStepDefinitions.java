package dev.paul.cartlink.bdd.steps;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.paul.cartlink.customer.repository.CustomerRepository; // Added import
import dev.paul.cartlink.merchant.model.Merchant;
import dev.paul.cartlink.merchant.repository.MerchantRepository;
import dev.paul.cartlink.product.repository.ProductRepository;
import dev.paul.cartlink.merchant.repository.MerchantProductRepository;
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

    @Autowired
    private CustomerRepository customerRepository; // Added to pass to CommonStepDefinitions

    @Autowired
    private ScenarioContext scenarioContext;
    private final CommonStepDefinitions commonSteps; // Added for DI

    // Shared state
    // private String apiBaseUrl; // Removed
    // private ResponseEntity<String> latestResponse; // Will use ScenarioContext
    private Map<String, String> sharedData = new HashMap<>(); // For auth tokens, merchantId, merchantProductIds

    @Autowired // Ensure Spring injects dependencies
    public MerchantProductStepDefinitions(
            TestRestTemplate restTemplate,
            ObjectMapper objectMapper,
            MerchantRepository merchantRepository,
            PasswordEncoder passwordEncoder,
            ProductRepository productRepository,
            MerchantProductRepository merchantProductRepository,
            CustomerRepository customerRepository,
            ScenarioContext scenarioContext,
            CommonStepDefinitions commonSteps // Added for DI
    ) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.merchantRepository = merchantRepository;
        this.passwordEncoder = passwordEncoder;
        this.productRepository = productRepository;
        this.merchantProductRepository = merchantProductRepository;
        this.customerRepository = customerRepository;
        this.scenarioContext = scenarioContext;
        this.commonSteps = commonSteps; // Added for DI
    }

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
        scenarioContext.clear(); // Clear scenario context as well
        logger.info("MerchantProductStepDefinitions: Cleared repositories, sharedData, and ScenarioContext.");
    }

    @After
    public void tearDown() {
    }

    @Given("a merchant {string} exists with password {string}")
    public void a_merchant_exists_with_password(String email, String password) {
        if (merchantRepository.findByEmail(email).isEmpty()) {
            Merchant merchant = new Merchant();
            merchant.setEmail(email);
            merchant.setPassword(passwordEncoder.encode(password));
            merchant.setFirstName(email.split("@")[0]); // Simple first name
            merchant.setLastName("User"); // Standardized last name
            merchantRepository.save(merchant);
            logger.info("Ensured merchant {} exists for test.", email);
        }
    }

    // This step was previously named a_merchant_is_logged_in_with_email_and_password
    // It's now generalized and calls the above existence check.
    @Given("a merchant {string} is logged in with password {string}")
    public void a_merchant_is_logged_in_with_password(String email, String password) throws JsonProcessingException {
        a_merchant_exists_with_password(email, password); // Ensure merchant exists

        String apiBaseUrl = scenarioContext.getString("apiBaseUrl");

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

    @Given("merchant {string} has a product {string} with price {double} and stock {int}, whose merchantProductId is stored as {string}")
    public void merchant_has_a_product_stored_as(String merchantEmail, String productName, double price, int stock, String storageKey) {
        Merchant merchant = merchantRepository.findByEmail(merchantEmail)
                .orElseThrow(() -> new AssertionError("Merchant " + merchantEmail + " not found for product setup."));

        // Create a generic base product if one doesn't exist by a similar name, or find existing.
        // This step focuses on the MerchantProduct, assuming base Product exists or can be generically created.
        List<dev.paul.cartlink.product.model.Product> existingProducts = productRepository.findByNameContainingIgnoreCase(productName);
        dev.paul.cartlink.product.model.Product baseProduct = existingProducts.isEmpty() ? null : existingProducts.get(0);

        if (baseProduct == null) {
            dev.paul.cartlink.product.model.Product p = new dev.paul.cartlink.product.model.Product();
            p.setName(productName);
            p.setBrand("TestBrand");
            p.setCategory("TestCategory");
            baseProduct = productRepository.save(p);
        }

        dev.paul.cartlink.merchant.model.MerchantProduct merchantProduct = new dev.paul.cartlink.merchant.model.MerchantProduct();
        merchantProduct.setMerchant(merchant);
        merchantProduct.setProduct(baseProduct);
        merchantProduct.setPrice(price);
        merchantProduct.setStock(stock);
        merchantProduct.setDescription("Product for " + merchantEmail);
        merchantProduct = merchantProductRepository.save(merchantProduct);

        sharedData.put(storageKey, merchantProduct.getId().toString());
        logger.info("Created merchant product for {} with ID {}, stored as {}.", merchantEmail, merchantProduct.getId(), storageKey);
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

    // This step is now handled by the generalized POST in CommonStepDefinitions.java
    // @When("a POST request is made to {string} with an authenticated merchant and the following body:")
    // public void a_post_request_is_made_to_with_auth_merchant_body(String path, String requestBody) {
    //     String apiBaseUrl = scenarioContext.getString("apiBaseUrl");
    //     HttpHeaders headers = buildAuthenticatedHeaders();
    //     HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
    //     ResponseEntity<String> response = restTemplate.postForEntity(apiBaseUrl + path, entity, String.class);
    //     scenarioContext.set("latestResponse", response);
    //     logger.info("Authenticated Merchant POST to {}: Status {}, Body {}", path, response.getStatusCodeValue(), response.getBody());
    //     // Store merchantProductId if present in response
    //     try {
    //         String mpId = Objects.toString(com.jayway.jsonpath.JsonPath.read(response.getBody(), "$.merchantProductId"), null);
    //         if (mpId != null) {
    //             sharedData.put("lastMerchantProductId", mpId);
    //             logger.info("Stored lastMerchantProductId: {}", mpId);
    //         }
    //     } catch (Exception e) { /* Path not found, or not an add product response */ }
    // }

    // @When("a GET request is made to {string} with an authenticated merchant")
    // public void a_get_request_is_made_to_with_auth_merchant(String path) {
    //     String apiBaseUrl = scenarioContext.getString("apiBaseUrl");
    //     HttpHeaders headers = buildAuthenticatedHeaders();
    //     HttpEntity<Void> entity = new HttpEntity<>(headers);
    //     ResponseEntity<String> response = restTemplate.exchange(apiBaseUrl + path, HttpMethod.GET, entity, String.class);
    //     scenarioContext.set("latestResponse", response);
    //     logger.info("Authenticated Merchant GET to {}: Status {}, Body {}", path, response.getStatusCodeValue(), response.getBody());
    // }

    @When("a PUT request is made to {string} with an authenticated merchant and the following body:")
    public void a_put_request_is_made_to_with_auth_merchant_body(String path, String requestBody) {
        String apiBaseUrl = scenarioContext.getString("apiBaseUrl");
        String resolvedPath = resolveMerchantProductPathVariables(path);
        HttpHeaders headers = buildAuthenticatedHeaders();
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(apiBaseUrl + resolvedPath, HttpMethod.PUT, entity, String.class);
        scenarioContext.set("latestResponse", response);
        logger.info("Authenticated Merchant PUT to {}: Status {}, Body {}", resolvedPath, response.getStatusCodeValue(), response.getBody());
    }

    // @When("a DELETE request is made to {string} with an authenticated merchant")
    // public void a_delete_request_is_made_to_with_auth_merchant(String path) {
    //     String apiBaseUrl = scenarioContext.getString("apiBaseUrl");
    //     String resolvedPath = resolveMerchantProductPathVariables(path);
    //     HttpHeaders headers = buildAuthenticatedHeaders();
    //     HttpEntity<Void> entity = new HttpEntity<>(headers);
    //     ResponseEntity<String> response = restTemplate.exchange(apiBaseUrl + resolvedPath, HttpMethod.DELETE, entity, String.class);
    //     scenarioContext.set("latestResponse", response);
    //     logger.info("Authenticated Merchant DELETE to {}: Status {}, Body {}", resolvedPath, response.getStatusCodeValue(), response.getBody());
    // }

    @Given("the following merchant product is added by the merchant, referencing an existing Product:")
    public void the_following_product_is_added_by_the_merchant(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        // CommonStepDefinitions commonSteps = new CommonStepDefinitions(scenarioContext, restTemplate, objectMapper, customerRepository, merchantRepository, passwordEncoder); // Manually create instance or autowire if Spring managed within this class
        // Use the injected commonSteps field instead
        for (Map<String, String> row : rows) {
            try {
                String requestBody = objectMapper.writeValueAsString(row);
                // Call the common POST step. Assumes merchant is already logged in and token is in scenarioContext.
                this.commonSteps.a_post_request_is_made_to_with_body("/merchants/products", requestBody);
                @SuppressWarnings("unchecked")
                ResponseEntity<String> latestResponse = scenarioContext.get("latestResponse", ResponseEntity.class);
                assertThat(latestResponse.getStatusCode().is2xxSuccessful()).isTrue(); // Ensure product was added
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to convert product row to JSON", e);
            }
        }
    }

    @Given("the following merchant product is added by the merchant and its merchantProductId is stored as {string}, referencing an existing Product:")
    public void the_following_product_is_added_and_id_stored(String sharedKey, DataTable dataTable) {
         List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
         // CommonStepDefinitions commonSteps = new CommonStepDefinitions(scenarioContext, restTemplate, objectMapper, customerRepository, merchantRepository, passwordEncoder); // Manually create instance
         // Use the injected commonSteps field instead
         // Assuming one row for simplicity for this step
         Map<String, String> row = rows.get(0);
         try {
            String requestBody = objectMapper.writeValueAsString(row);
            // Call the common POST step. Assumes merchant is already logged in and token is in scenarioContext.
            this.commonSteps.a_post_request_is_made_to_with_body("/merchants/products", requestBody);
            @SuppressWarnings("unchecked")
            ResponseEntity<String> latestResponse = scenarioContext.get("latestResponse", ResponseEntity.class);
            assertThat(latestResponse.getStatusCode().is2xxSuccessful()).isTrue();
            String mpId = Objects.toString(com.jayway.jsonpath.JsonPath.read(latestResponse.getBody(), "$.merchantProductId"), null);
            assertThat(mpId).isNotNull();
            sharedData.put(sharedKey, mpId); // Keep using local sharedData for this specific purpose if needed, or migrate to context
            logger.info("Stored merchantProductId {} as {}", mpId, sharedKey);
         } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert product row to JSON for storing ID", e);
        }
    }

    @When("merchant {string} attempts to update merchant product {string} with the following body:")
    public void merchant_attempts_to_update_merchant_product_with_body(String attackerEmail, String targetProductKey, String requestBody) {
        // This step assumes 'attackerEmail' is already logged in and their token is in sharedData.get("merchantToken")
        // It also assumes targetProductKey (e.g., "{product_of_merchant_b}") resolves to an ID via sharedData
        String apiBaseUrl = scenarioContext.getString("apiBaseUrl");
        String resolvedPath = resolveMerchantProductPathVariables("/merchants/products/" + targetProductKey); // Path uses the key directly

        HttpHeaders headers = buildAuthenticatedHeaders(); // Uses token of currently logged-in merchant (attacker)
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(apiBaseUrl + resolvedPath, HttpMethod.PUT, entity, String.class);
        scenarioContext.set("latestResponse", response);
        logger.info("Merchant {} attempting to update product at {}: Status {}, Body {}", attackerEmail, resolvedPath, response.getStatusCodeValue(), response.getBody());
    }

    @When("merchant {string} attempts to delete merchant product {string}")
    public void merchant_attempts_to_delete_merchant_product(String attackerEmail, String targetProductKey) {
        String apiBaseUrl = scenarioContext.getString("apiBaseUrl");
        String resolvedPath = resolveMerchantProductPathVariables("/merchants/products/" + targetProductKey);

        HttpHeaders headers = buildAuthenticatedHeaders(); // Uses token of currently logged-in merchant (attacker)
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(apiBaseUrl + resolvedPath, HttpMethod.DELETE, entity, String.class);
        scenarioContext.set("latestResponse", response);
        logger.info("Merchant {} attempting to delete product at {}: Status {}, Body {}", attackerEmail, resolvedPath, response.getStatusCodeValue(), response.getBody());
    }

    private String resolveMerchantProductPathVariables(String path) {
        String resolvedPath = path;
        // Example: /merchants/products/{mpid_to_update}
        // Resolve placeholders like {product_of_merchant_b} using sharedData
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\{([^}]+)\\}");
        java.util.regex.Matcher matcher = pattern.matcher(resolvedPath);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = sharedData.get(key);
            if (value != null) {
                matcher.appendReplacement(sb, value);
            } else {
                logger.warn("Placeholder {} not found in sharedData for path resolution", key);
                // Keep the placeholder as is, or throw an error, depending on desired strictness
                matcher.appendReplacement(sb, matcher.group(0)); // Keep original placeholder if not found
            }
        }
        matcher.appendTail(sb);
        resolvedPath = sb.toString();

        // Original simple replacement logic (can be removed or kept as fallback if needed)
        // for (Map.Entry<String, String> entry : sharedData.entrySet()) {
        //     if (path.contains("{" + entry.getKey() + "}")) {
        //         resolvedPath = resolvedPath.replace("{" + entry.getKey() + "}", entry.getValue());
        //     }
        // }

        if (resolvedPath.contains("{") && resolvedPath.contains("}")) { // Check after attempting resolution
             logger.warn("Path {} still contains unresolved placeholders after attempted resolution: {}", path, resolvedPath);
        }
        return resolvedPath;
    }

    // --- Common Then Steps (can be refactored) ---
    // Duplicate step definition "the response body should contain a {string}" removed. Will use CommonStepDefinitions.

    // @Then("the response body should contain {string} with value {string}")
    // public void the_response_body_should_contain_with_value(String jsonPath, String expectedValue) {
    //     @SuppressWarnings("unchecked")
    //     ResponseEntity<String> latestResponse = scenarioContext.get("latestResponse", ResponseEntity.class);
    //     assertThat(latestResponse.getBody()).isNotNull();
    //     String actualValue = Objects.toString(com.jayway.jsonpath.JsonPath.read(latestResponse.getBody(), "$." + jsonPath), "");
    //     assertThat(actualValue).isEqualTo(expectedValue);
    // }

    @Then("the response body should contain {string} with value {int}")
    public void the_response_body_should_contain_with_int_value(String jsonPath, Integer expectedValue) {
        @SuppressWarnings("unchecked")
        ResponseEntity<String> latestResponse = scenarioContext.get("latestResponse", ResponseEntity.class);
        assertThat(latestResponse.getBody()).isNotNull();
        Integer actualValue = com.jayway.jsonpath.JsonPath.read(latestResponse.getBody(), "$." + jsonPath);
        assertThat(actualValue).isEqualTo(expectedValue);
    }


    // @Then("the response body should be an empty list")
    // public void the_response_body_should_be_an_empty_list() {
    //     @SuppressWarnings("unchecked")
    //     ResponseEntity<String> latestResponse = scenarioContext.get("latestResponse", ResponseEntity.class);
    //     assertThat(latestResponse.getBody()).isNotNull();
    //     List<?> list = com.jayway.jsonpath.JsonPath.parse(latestResponse.getBody()).read("$");
    //     assertThat(list).isNotNull().isEmpty();
    // }

    // @Then("the response body should be a list with {int} item(s)")
    // public void the_response_body_should_be_a_list_with_items(int count) {
    //     @SuppressWarnings("unchecked")
    //     ResponseEntity<String> latestResponse = scenarioContext.get("latestResponse", ResponseEntity.class);
    //     assertThat(latestResponse.getBody()).isNotNull();
    //     List<?> list = com.jayway.jsonpath.JsonPath.parse(latestResponse.getBody()).read("$");
    //     assertThat(list).isNotNull().hasSize(count);
    // }

    // @Then("the response body should contain an {string} field")
    // public void the_response_body_should_contain_an_error_field(String fieldName) {
    //     @SuppressWarnings("unchecked")
    //     ResponseEntity<String> latestResponse = scenarioContext.get("latestResponse", ResponseEntity.class);
    //     assertThat(latestResponse.getBody()).isNotNull();
    //     com.jayway.jsonpath.JsonPath.read(latestResponse.getBody(), "$." + fieldName);
    // }

    @Then("the response body should contain {string} greater than {int}")
    public void the_response_body_should_contain_greater_than(String jsonPath, int value) {
        @SuppressWarnings("unchecked")
        ResponseEntity<String> latestResponse = scenarioContext.get("latestResponse", ResponseEntity.class);
        assertThat(latestResponse.getBody()).isNotNull();
        Integer actualValue = com.jayway.jsonpath.JsonPath.read(latestResponse.getBody(), "$." + jsonPath);
        assertThat(actualValue).isGreaterThan(value);
    }
}
