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

    private String apiBaseUrl;
    private ResponseEntity<String> latestResponse;
    private Map<String, String> sharedData = new HashMap<>();

    @Before
    public void setUp() {
        orderRepository.deleteAll();
        // merchantProductRepository.deleteAll(); // Might be needed if not cleaned by MerchantProductSteps
        // productRepository.deleteAll();         // Might be needed if not cleaned by MerchantProductSteps
        // customerRepository.deleteAll();      // Handled by CustomerSteps
        // merchantRepository.deleteAll();      // Handled by MerchantAuthSteps
        sharedData.clear();
        logger.info("OrderStepDefinitions: Cleared order repository and sharedData.");
    }

    @After
    public void tearDown() {}

    @Given("the API base URL is {string}")
    public void the_api_base_url_is(String baseUrl) {
        this.apiBaseUrl = baseUrl;
    }

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
        ResponseEntity<String> loginResponse = restTemplate.postForEntity(apiBaseUrl + "/merchant/login", entity, String.class); // Path from MerchantController

        if(loginResponse.getStatusCodeValue() != 200) {
            logger.error("Merchant login failed for {}: {} - {}", email, loginResponse.getStatusCodeValue(), loginResponse.getBody());
             assertThat(loginResponse.getStatusCodeValue()).isEqualTo(200); // Fail test if login fails
        }

        String responseBody = loginResponse.getBody();
        String token = com.jayway.jsonpath.JsonPath.read(responseBody, "$.token");
        String merchantId = Objects.toString(com.jayway.jsonpath.JsonPath.read(responseBody, "$.merchant.merchantId"));
        sharedData.put("merchantToken", token);
        sharedData.put("merchantId", merchantId); // Used in paths like /merchants/{merchantId}/orders
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
        ResponseEntity<String> loginResponse = restTemplate.postForEntity(apiBaseUrl + "/customers/login", entity, String.class); // Path from CustomerController

        if(loginResponse.getStatusCodeValue() != 200) {
            logger.error("Customer login failed for {}: {} - {}", email, loginResponse.getStatusCodeValue(), loginResponse.getBody());
             assertThat(loginResponse.getStatusCodeValue()).isEqualTo(200);
        }
        String responseBody = loginResponse.getBody();
        String token = com.jayway.jsonpath.JsonPath.read(responseBody, "$.token");
        sharedData.put("customerToken", token);
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
        sharedData.put(sharedMpidKey, mp.getId().toString());
        logger.info("Created MerchantProduct '{}' with ID {} for merchant {}, stored as {}", productName, mp.getId(), merchantEmail, sharedMpidKey);
    }

    @Given("an order with ID {string} exists for merchant {string} with initial status {string}")
    public void an_order_exists_for_merchant(String orderIdStr, String merchantEmail, String statusStr) {
        Long orderId = Long.parseLong(orderIdStr);
        if (orderRepository.findById(orderId).isEmpty()) {
            Merchant merchant = merchantRepository.findByEmail(merchantEmail)
                    .orElseThrow(() -> new AssertionError("Merchant " + merchantEmail + " not found for order setup."));

            // Need a customer and a merchant product to create an order
            Customer customer = customerRepository.findByEmail("order.customer@example.com") // From Background
                    .orElseThrow(() -> new AssertionError("Default customer not found for order setup."));

            String mpidStr = sharedData.get("orderable_mpid"); // From Background
            assertThat(mpidStr).isNotNull().withFailMessage("orderable_mpid not found in sharedData for order setup.");
            MerchantProduct mp = merchantProductRepository.findById(Long.parseLong(mpidStr))
                    .orElseThrow(() -> new AssertionError("Default merchant product not found for order setup."));

            Order order = new Order();
            // order.setOrderId(orderId); // ID is auto-generated, cannot set manually unless DB allows identity insert
            order.setCustomer(customer);
            order.setMerchantProduct(mp);
            order.setQuantity(1);
            order.setTotalPrice(mp.getPrice()); // Simplified
            order.setStatus(OrderStatus.valueOf(statusStr.toUpperCase()));
            order.setOrderDate(LocalDateTime.now());
            Order savedOrder = orderRepository.save(order);
            // This step implies we need to refer to order by ID '1' or '2' in feature file.
            // This is only possible if we can control ID generation or update the feature file dynamically.
            // For now, we create an order, but its ID might not be '1' or '2'.
            // This step needs adjustment: either create order via API, or use its actual ID.
            logger.warn("Created order with ACTUAL ID {}. Test step specified ID {}. Test might not target correct order if ID is hardcoded in path.", savedOrder.getOrderId(), orderId);
            // To make this work, we should store the actual ID and use it.
            sharedData.put("orderId_" + orderIdStr, savedOrder.getOrderId().toString());
        }
    }

    private HttpHeaders buildAuthHeaders(boolean forMerchant) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String tokenKey = forMerchant ? "merchantToken" : "customerToken";
        if (sharedData.containsKey(tokenKey)) {
            headers.setBearerAuth(sharedData.get(tokenKey));
        } else {
            // For guest customer placing order, no auth token is fine.
            if (forMerchant || (sharedData.containsKey("customerTokenRequired") && Boolean.parseBoolean(sharedData.get("customerTokenRequired")))) {
                 logger.warn("No {} found in sharedData for authenticated request!", tokenKey);
            }
        }
        return headers;
    }

    private String resolvePath(String path) {
        String tempPath = path;
        if (tempPath.contains("{merchantId}")) {
            tempPath = tempPath.replace("{merchantId}", sharedData.getOrDefault("merchantId", "UNKNOWN_MERCHANT_ID"));
        }
        // For order IDs, try to use the stored actual ID if available
        // e.g. path "/merchants/{merchantId}/orders/1/status" -> orderId_1
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("/orders/(\\d+)/").matcher(tempPath);
        if (matcher.find()) {
            String specifiedOrderId = matcher.group(1);
            String actualOrderId = sharedData.get("orderId_" + specifiedOrderId);
            if (actualOrderId != null) {
                tempPath = tempPath.replace("/orders/" + specifiedOrderId + "/", "/orders/" + actualOrderId + "/");
            }
        }
        return tempPath;
    }

    private String resolveBody(String body) {
        String tempBody = body;
        if (tempBody.contains("{direct_order_mpid}")) {
            tempBody = tempBody.replace("{direct_order_mpid}", sharedData.getOrDefault("direct_order_mpid", "UNKNOWN_MPID"));
        }
         if (tempBody.contains("{orderable_mpid}")) { // From merchant order feature
            tempBody = tempBody.replace("{orderable_mpid}", sharedData.getOrDefault("orderable_mpid", "UNKNOWN_MPID_ORDERABLE"));
        }
        // Resolve merchantProductId.id to its actual string value for JSON
        // e.g. "merchantProduct.id" with value "{direct_order_mpid}"
        // This is for response checking, not request body.
        // The current resolveBody is for request bodies.
        return tempBody;
    }


    @When("a GET request is made to {string} with an authenticated merchant")
    public void a_get_request_is_made_to_with_auth_merchant(String path) {
        HttpHeaders headers = buildAuthHeaders(true);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        latestResponse = restTemplate.exchange(apiBaseUrl + resolvePath(path), HttpMethod.GET, entity, String.class);
    }

    @When("a PUT request is made to {string} with an authenticated merchant and the following body:")
    public void a_put_request_is_made_to_with_auth_merchant_body(String path, String requestBody) {
        HttpHeaders headers = buildAuthHeaders(true);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        latestResponse = restTemplate.exchange(apiBaseUrl + resolvePath(path), HttpMethod.PUT, entity, String.class);
    }

    @When("a PATCH request is made to {string} with an authenticated merchant")
    public void a_patch_request_is_made_to_with_auth_merchant(String path) {
        HttpHeaders headers = buildAuthHeaders(true);
        HttpEntity<Void> entity = new HttpEntity<>(headers); // PATCH can have body, but this scenario doesn't
        latestResponse = restTemplate.exchange(apiBaseUrl + resolvePath(path), HttpMethod.PATCH, entity, String.class);
    }

    @When("a POST request is made to {string} with an authenticated customer and the following body:")
    public void a_post_request_is_made_to_with_auth_customer_body(String path, String requestBody) {
        sharedData.put("customerTokenRequired", "true"); // Mark that token is expected
        HttpHeaders headers = buildAuthHeaders(false); // Customer token
        HttpEntity<String> entity = new HttpEntity<>(resolveBody(requestBody), headers);
        latestResponse = restTemplate.postForEntity(apiBaseUrl + resolvePath(path), entity, String.class);
        sharedData.remove("customerTokenRequired");
    }

    @When("a POST request is made to {string} with the following body:") // For guest customer
    public void a_post_request_is_made_to_with_body(String path, String requestBody) {
        HttpHeaders headers = buildAuthHeaders(false); // No customer token expected / needed for guest
        HttpEntity<String> entity = new HttpEntity<>(resolveBody(requestBody), headers);
        latestResponse = restTemplate.postForEntity(apiBaseUrl + resolvePath(path), entity, String.class);
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
        // Resolve expectedValue if it's a placeholder like {direct_order_mpid}
        String resolvedExpectedValue = resolveBody(expectedValue);
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
