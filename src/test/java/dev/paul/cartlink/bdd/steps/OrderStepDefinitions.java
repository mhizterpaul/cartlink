package dev.paul.cartlink.bdd.steps;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.paul.cartlink.customer.model.Customer;
import dev.paul.cartlink.customer.repository.CustomerRepository;
import dev.paul.cartlink.merchant.model.Merchant;
import dev.paul.cartlink.merchant.model.MerchantProduct;
import dev.paul.cartlink.merchant.repository.MerchantProductRepository;
import dev.paul.cartlink.merchant.repository.MerchantRepository;
import dev.paul.cartlink.order.model.Order;
import dev.paul.cartlink.order.model.OrderStatus;
import dev.paul.cartlink.order.repository.OrderRepository;
import dev.paul.cartlink.product.model.Product;
import dev.paul.cartlink.product.repository.ProductRepository;

import dev.paul.cartlink.bdd.ScenarioContext;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given; // Added back
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
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class OrderStepDefinitions {

    private static final Logger logger = LoggerFactory.getLogger(OrderStepDefinitions.class);

    @Autowired private TestRestTemplate restTemplate;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private MerchantRepository merchantRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private MerchantProductRepository merchantProductRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private ScenarioContext scenarioContext;

    private ResponseEntity<String> latestResponse;
    // private Map<String, String> sharedData = new HashMap<>(); // Replaced by scenarioContext

    @Before
    public void setUp() {
        // Clear only order-specific data or rely on other step definitions to clear their respective data.
        // ScenarioContext should be cleared at the beginning or end of a scenario.
        // If CommonStepDefinitions or another @Before runs first and clears it, that's fine.
        // For now, let's assume ScenarioContext is fresh or managed appropriately by another hook.
        // If this step definition is the first to use sharedData for sensitive ops, ensure it's clean.
        // Let's clear keys this class specifically uses or populates heavily.
        // However, the original sharedData.clear() was broad.
        // For now, let's assume ScenarioContext is managed by a higher-order hook or CucumberSpringConfiguration.
        // If issues arise, we can add scenarioContext.clear() here or in an @After hook in CommonStepDefinitions.
        orderRepository.deleteAll();
        logger.info("OrderStepDefinitions: Cleared order repository.");
    }

    @After
    public void tearDown() {}

    // Removed duplicate @Given("the API base URL is {string}")

    // --- Merchant Login (from MerchantAuthStepDefinitions, simplified) ---
    @Given("a merchant is logged in with email {string} and password {string}")
    public void a_merchant_is_logged_in_with_email_and_password(String email, String password) throws JsonProcessingException {
        if (merchantRepository.findByEmail(email).isEmpty()) {
            Merchant merchant = new Merchant();
            merchant.setEmail(email);
            merchant.setPassword(passwordEncoder.encode(password));
            merchant.setFirstName("OrderTest");
            merchant.setLastName("Merchant");
            merchant = merchantRepository.save(merchant);
        }

        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", email);
        loginRequest.put("password", password);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(loginRequest), headers);
        ResponseEntity<String> loginResponse = restTemplate.postForEntity(scenarioContext.getString("apiBaseUrl") + "/merchant/login", entity, String.class);

        if(loginResponse.getStatusCodeValue() != 200) {
            logger.error("Merchant login failed for {}: {} - {}", email, loginResponse.getStatusCodeValue(), loginResponse.getBody());
             assertThat(loginResponse.getStatusCodeValue()).isEqualTo(200); // Fail test if login fails
        }

        String responseBody = loginResponse.getBody();
        String token = com.jayway.jsonpath.JsonPath.read(responseBody, "$.token");
        String merchantId = Objects.toString(com.jayway.jsonpath.JsonPath.read(responseBody, "$.merchant.merchantId"));
        scenarioContext.set("merchantToken", token);
        scenarioContext.set("merchantId", merchantId);
        logger.info("Merchant {} logged in. Token: {}, ID: {}", email, token, merchantId);
    }

    // --- Customer Login (from CustomerStepDefinitions, simplified) ---
    @Given("a customer is logged in with email {string} and password {string}")
    public void a_customer_is_logged_in_with_email_and_password(String email, String password) throws JsonProcessingException {
        if (customerRepository.findByEmail(email).isEmpty()) {
            Customer customer = new Customer();
            customer.setEmail(email);
            customer.setFirstName("OrderPlacing");
            customer.setLastName("Customer");
            // No password on entity, CustomerService handles auth
            customerRepository.save(customer);
        }

        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", email);
        loginRequest.put("password", password);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(loginRequest), headers);
        ResponseEntity<String> loginResponse = restTemplate.postForEntity(scenarioContext.getString("apiBaseUrl") + "/customers/login", entity, String.class);

        if(loginResponse.getStatusCodeValue() != 200) {
            logger.error("Customer login failed for {}: {} - {}", email, loginResponse.getStatusCodeValue(), loginResponse.getBody());
             assertThat(loginResponse.getStatusCodeValue()).isEqualTo(200);
        }
        String responseBody = loginResponse.getBody();
        String token = com.jayway.jsonpath.JsonPath.read(responseBody, "$.token");
        scenarioContext.set("customerToken", token);
        logger.info("Customer {} logged in. Token stored.", email);
    }


    @Given("a customer {string} exists")
    public void a_customer_exists(String email) {
        if (customerRepository.findByEmail(email).isEmpty()) {
            Customer customer = new Customer();
            customer.setEmail(email);
            customer.setFirstName(email.split("\\.")[0]); // "order" from "order.customer@example.com"
            customer.setLastName("Customer");
            customerRepository.save(customer);
        }
    }

    @Given("a customer {string} exists with first name {string}")
    public void a_customer_exists_with_first_name(String email, String firstName) {
        if (customerRepository.findByEmail(email).isEmpty()) {
            Customer customer = new Customer();
            customer.setEmail(email);
            customer.setFirstName(firstName);
            customer.setLastName("User"); // Default last name
            customerRepository.save(customer);
            logger.info("Created customer {} with first name {}", email, firstName);
        } else {
            // Optionally update if details differ, or assume current state is fine
            logger.info("Customer {} already exists.", email);
        }
    }


    @Given("merchant {string} has a product {string} with price {double} and stock {int}, and its MerchantProductID is stored as {string}")
    public void merchant_has_product_stored_as(String merchantEmail, String productName, double price, int stock, String sharedMpidKey) {
        Merchant merchant = merchantRepository.findByEmail(merchantEmail)
                .orElseThrow(() -> new AssertionError("Merchant " + merchantEmail + " not found for product setup. Ensure merchant is created first."));

        Product product = new Product();
        product.setName(productName);
        product.setBrand("TestBrand");
        product.setCategory("TestCategory");
        // product.setMerchant(merchant); // If Product entity has direct merchant link
        product = productRepository.save(product);

        MerchantProduct mp = new MerchantProduct();
        mp.setProduct(product);
        mp.setMerchant(merchant);
        mp.setPrice(price);
        mp.setStock(stock);
        mp.setDescription("Test description for " + productName);
        mp = merchantProductRepository.save(mp);
        scenarioContext.set(sharedMpidKey, mp.getId().toString());
        logger.info("Created MerchantProduct '{}' with ID {} for merchant {}, stored as {}", productName, mp.getId(), merchantEmail, sharedMpidKey);
    }

    @Given("an order with ID {string} exists for merchant {string} with initial status {string}")
    public void an_order_exists_for_merchant(String orderIdStr, String merchantEmail, String statusStr) {
        Long orderId = Long.parseLong(orderIdStr);
        // This logic might need adjustment if IDs are not auto-incrementing from 1 or if tests run in parallel.
        // For simplicity, we're trying to create an order that might match the hardcoded ID if DB is clean.
        // A more robust way is to capture the created order's ID and use it.
        if (orderRepository.findById(orderId).isEmpty()) { // Check if an order with this specific ID already exists
            Merchant merchant = merchantRepository.findByEmail(merchantEmail)
                    .orElseThrow(() -> new AssertionError("Merchant " + merchantEmail + " not found for order setup."));

            Customer customer = customerRepository.findByEmail("order.customer@example.com")
                    .orElseGet(() -> {
                        Customer c = new Customer();
                        c.setEmail("order.customer@example.com");
                        c.setFirstName("OrderDefault");
                        c.setLastName("Customer");
                        return customerRepository.save(c);
                    });

            String mpidStr = scenarioContext.getString("orderable_mpid");
            assertThat(mpidStr).isNotNull().withFailMessage("orderable_mpid not found in scenarioContext for order setup.");
            MerchantProduct mp = merchantProductRepository.findById(Long.parseLong(mpidStr))
                    .orElseThrow(() -> new AssertionError("Merchant product for orderable_mpid not found."));

            Order order = new Order();
            order.setCustomer(customer);
            order.setMerchantProduct(mp);
            order.setQuantity(1);
            order.setTotalPrice(mp.getPrice());
            order.setStatus(OrderStatus.valueOf(statusStr.toUpperCase()));
            order.setOrderDate(LocalDateTime.now());
            Order savedOrder = orderRepository.save(order);
            // Store the actual ID using the symbolic ID from the feature file as key
            scenarioContext.set("orderId_" + orderIdStr, savedOrder.getOrderId().toString());
            logger.info("Created order with ACTUAL ID {}, as specified by symbolic ID {} in feature.", savedOrder.getOrderId(), orderIdStr);
        } else {
            logger.info("Order with specified ID {} already exists or was created by another step.", orderId);
            scenarioContext.set("orderId_" + orderIdStr, orderId.toString()); // Assume it's the correct one
        }
    }

    private HttpHeaders buildAuthHeaders(boolean forMerchant) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String tokenKey = forMerchant ? "merchantToken" : "customerToken";
        String token = scenarioContext.getString(tokenKey);
        if (token != null) {
            headers.setBearerAuth(token);
        } else {
            if (forMerchant || (scenarioContext.get("customerTokenRequired", Boolean.class) != null && scenarioContext.get("customerTokenRequired", Boolean.class))) {
                 logger.warn("No {} found in scenarioContext for authenticated request!", tokenKey);
            }
        }
        return headers;
    }

    private String resolvePath(String path) {
        String tempPath = path;
        if (tempPath.contains("{merchantId}")) {
            tempPath = tempPath.replace("{merchantId}", scenarioContext.getString("merchantId") != null ? scenarioContext.getString("merchantId") : "UNKNOWN_MERCHANT_ID");
        }
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("/orders/(\\d+)").matcher(tempPath);
        if (matcher.find()) {
            String specifiedOrderIdKey = "orderId_" + matcher.group(1);
            String actualOrderId = scenarioContext.getString(specifiedOrderIdKey);
            if (actualOrderId != null) {
                tempPath = matcher.replaceFirst("/orders/" + actualOrderId);
            } else {
                logger.warn("Actual order ID for symbolic key {} not found in scenarioContext. Using original path part: {}", specifiedOrderIdKey, matcher.group(1));
            }
        }
        return tempPath;
    }

    private String resolveBody(String body) {
        String tempBody = body;
        if (tempBody.contains("{direct_order_mpid}")) {
            tempBody = tempBody.replace("{direct_order_mpid}", scenarioContext.getString("direct_order_mpid") != null ? scenarioContext.getString("direct_order_mpid") : "UNKNOWN_MPID");
        }
         if (tempBody.contains("{orderable_mpid}")) {
            tempBody = tempBody.replace("{orderable_mpid}",  scenarioContext.getString("orderable_mpid") != null ? scenarioContext.getString("orderable_mpid") : "UNKNOWN_MPID_ORDERABLE");
        }
        return tempBody;
    }


    @When("a GET request is made to {string} with an authenticated merchant")
    public void a_get_request_is_made_to_with_auth_merchant(String path) {
        HttpHeaders headers = buildAuthHeaders(true);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        latestResponse = restTemplate.exchange(scenarioContext.getString("apiBaseUrl") + resolvePath(path), HttpMethod.GET, entity, String.class);
    }

    @When("a PUT request is made to {string} with an authenticated merchant and the following body:")
    public void a_put_request_is_made_to_with_auth_merchant_body(String path, String requestBody) {
        HttpHeaders headers = buildAuthHeaders(true);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        latestResponse = restTemplate.exchange(scenarioContext.getString("apiBaseUrl") + resolvePath(path), HttpMethod.PUT, entity, String.class);
    }

    @When("a PATCH request is made to {string} with an authenticated merchant")
    public void a_patch_request_is_made_to_with_auth_merchant(String path) {
        HttpHeaders headers = buildAuthHeaders(true);
        HttpEntity<Void> entity = new HttpEntity<>(headers); // PATCH can have body, but this scenario doesn't
        latestResponse = restTemplate.exchange(scenarioContext.getString("apiBaseUrl") + resolvePath(path), HttpMethod.PATCH, entity, String.class);
    }

    @When("a POST request is made to {string} with an authenticated customer and the following body:")
    public void a_post_request_is_made_to_with_auth_customer_body(String path, String requestBody) {
        scenarioContext.set("customerTokenRequired", true);
        HttpHeaders headers = buildAuthHeaders(false); // Customer token
        HttpEntity<String> entity = new HttpEntity<>(resolveBody(requestBody), headers);
        latestResponse = restTemplate.postForEntity(scenarioContext.getString("apiBaseUrl") + resolvePath(path), entity, String.class);
        scenarioContext.set("customerTokenRequired", null); // Reset flag
    }

    @When("a guest POST request is made to {string} with the following body:") // For guest customer
    public void a_guest_post_request_is_made_to_with_body(String path, String requestBody) {
        HttpHeaders headers = buildAuthHeaders(false); // No customer token expected / needed for guest
        HttpEntity<String> entity = new HttpEntity<>(resolveBody(requestBody), headers);
        latestResponse = restTemplate.postForEntity(scenarioContext.getString("apiBaseUrl") + resolvePath(path), entity, String.class);
    }

    // --- Then Steps (Common) ---
    @Then("the response status code should be {int}")
    public void the_response_status_code_should_be(Integer statusCode) {
        assertThat(latestResponse.getStatusCodeValue()).isEqualTo(statusCode);
    }

    @Then("the response body should contain an {string}")
    public void the_response_body_should_contain_an(String jsonPath) {
        assertThat(latestResponse.getBody()).isNotNull();
        com.jayway.jsonpath.JsonPath.read(latestResponse.getBody(), "$." + jsonPath);
    }

    @Then("the response body should contain an {string} field") // Alias for error field check
    public void the_response_body_should_contain_an_error_field(String fieldName) {
        the_response_body_should_contain_an(fieldName);
    }

    @Then("the response body should contain {string} with value {string}")
    public void the_response_body_should_contain_with_value(String jsonPath, String expectedValue) {
        assertThat(latestResponse.getBody()).isNotNull();
        String resolvedExpectedValue = expectedValue;
        if (expectedValue.startsWith("{") && expectedValue.endsWith("}")) {
            String placeholderKey = expectedValue.substring(1, expectedValue.length() - 1);
            resolvedExpectedValue = scenarioContext.getString(placeholderKey);
            assertThat(resolvedExpectedValue).withFailMessage("Placeholder " + placeholderKey + " not found in scenario context for assertion.").isNotNull();
        }
        String actualValue = Objects.toString(com.jayway.jsonpath.JsonPath.read(latestResponse.getBody(), "$." + jsonPath), "");
        assertThat(actualValue).isEqualTo(resolvedExpectedValue);
    }

    @Then("the response body should contain {string} with value {int}")
    public void the_response_body_should_contain_with_value_int(String jsonPath, Integer expectedValue) {
        assertThat(latestResponse.getBody()).isNotNull();
        Integer actualValue = com.jayway.jsonpath.JsonPath.read(latestResponse.getBody(), "$." + jsonPath);
        assertThat(actualValue).isEqualTo(expectedValue);
    }


    @Then("the response body should contain {string} with boolean value {string}")
    public void the_response_body_should_contain_with_boolean_value(String jsonPath, String expectedValueStr) {
        assertThat(latestResponse.getBody()).isNotNull();
        boolean expectedValue = Boolean.parseBoolean(expectedValueStr);
        boolean actualValue = com.jayway.jsonpath.JsonPath.read(latestResponse.getBody(), "$." + jsonPath);
        assertThat(actualValue).isEqualTo(expectedValue);
    }

    @Then("the response body should be a list")
    public void the_response_body_should_be_a_list() {
        assertThat(latestResponse.getBody()).isNotNull();
        List<?> list = com.jayway.jsonpath.JsonPath.parse(latestResponse.getBody()).read("$");
        assertThat(list).isInstanceOf(List.class);
    }
}
