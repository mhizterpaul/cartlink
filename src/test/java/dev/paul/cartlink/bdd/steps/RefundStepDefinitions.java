package dev.paul.cartlink.bdd.steps;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.paul.cartlink.customer.model.Customer;
import dev.paul.cartlink.customer.model.RefundRequest;
import dev.paul.cartlink.customer.model.RefundStatus; // Import RefundStatus
import dev.paul.cartlink.customer.repository.CustomerRepository;
import dev.paul.cartlink.customer.repository.RefundRequestRepository;
import dev.paul.cartlink.merchant.model.Merchant;
import dev.paul.cartlink.merchant.model.MerchantProduct;
import dev.paul.cartlink.merchant.repository.MerchantProductRepository;
import dev.paul.cartlink.merchant.repository.MerchantRepository;
import dev.paul.cartlink.order.model.Order;
import dev.paul.cartlink.order.model.OrderStatus;
import dev.paul.cartlink.order.repository.OrderRepository;
import dev.paul.cartlink.product.model.Product;
import dev.paul.cartlink.product.repository.ProductRepository;
import dev.paul.cartlink.bdd.context.ScenarioContext;

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

public class RefundStepDefinitions {

    private static final Logger logger = LoggerFactory.getLogger(RefundStepDefinitions.class);

    @Autowired private TestRestTemplate restTemplate;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private MerchantRepository merchantRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private MerchantProductRepository merchantProductRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private RefundRequestRepository refundRequestRepository;

    @Autowired private PasswordEncoder passwordEncoder;

    @Autowired
    private ScenarioContext scenarioContext;

    private ResponseEntity<String> latestResponse;
    private Map<String, String> sharedData = new HashMap<>();

    @Before
    public void setUp() {
        refundRequestRepository.deleteAll();
        sharedData.clear();
        logger.info("RefundStepDefinitions: Cleared refund request repository and sharedData.");
    }

    @After
    public void tearDown() {}

    @Given("a customer is logged in with email {string} and password {string}")
    public void a_customer_is_logged_in_with_email_and_password(String email, String password) throws JsonProcessingException {
        String apiBaseUrl = scenarioContext.getString("apiBaseUrl");
        if (customerRepository.findByEmail(email).isEmpty()) {
            Customer customer = new Customer();
            customer.setEmail(email);
            customer.setFirstName("RefundTest");
            customer.setLastName("User");
            customerRepository.save(customer);
        }

        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", email);
        loginRequest.put("password", password);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(loginRequest), headers);
        // Assuming customer login path is /api/v1/customers/login as per CustomerController
        // BUT RefundController is /api/customers/orders. If base URL is /api, then /customers/login
        ResponseEntity<String> loginResponse = restTemplate.postForEntity(apiBaseUrl + "/customers/login", entity, String.class);

        if(loginResponse.getStatusCodeValue() != 200) {
            logger.error("Customer login failed for {}: {} - {}", email, loginResponse.getStatusCodeValue(), loginResponse.getBody());
             assertThat(loginResponse.getStatusCodeValue()).isEqualTo(200);
        }
        String responseBody = loginResponse.getBody();
        String token = com.jayway.jsonpath.JsonPath.read(responseBody, "$.token");
        sharedData.put("customerToken", token);
        logger.info("Customer {} logged in for refund test. Token stored.", email);
    }

    @Given("an order with ID {string} exists for customer {string} and its actual ID is stored as {string}")
    public void an_order_exists_for_customer_stored_as(String symbolicOrderId, String customerEmail, String sharedKey) {
        Customer customer = customerRepository.findByEmail(customerEmail)
            .orElseThrow(() -> new AssertionError("Customer " + customerEmail + " not found for order setup."));

        Merchant merchant = merchantRepository.findByEmail("refund.merchant@example.com").orElseGet(() -> {
            Merchant m = new Merchant();
            m.setEmail("refund.merchant@example.com");
            m.setFirstName("Refund"); m.setLastName("Merchant"); m.setPassword(passwordEncoder.encode("password"));
            return merchantRepository.save(m);
        });
        final Merchant finalMerchant = merchant;

        List<Product> existingProducts = productRepository.findByNameContainingIgnoreCase("RefundProduct");
        Product tempProduct = existingProducts.isEmpty() ? null : existingProducts.get(0);
        if (tempProduct == null) {
            Product p = new Product(); p.setName("RefundProduct"); p.setBrand("Brand"); p.setCategory("Category");
            tempProduct = productRepository.save(p);
        }
        final Product finalProduct = tempProduct;

        final Product finalProduct = tempProduct; // final or effectively final

        // Use the findByMerchantAndProduct method added to MerchantProductRepository

        MerchantProduct merchantProduct = merchantProductRepository.findByMerchantAndProduct(merchant, finalProduct).orElseGet(()-> {
            MerchantProduct mp = new MerchantProduct(); mp.setMerchant(merchant); mp.setProduct(finalProduct); // use finalProduct

            mp.setPrice(20.0); mp.setStock(50); mp.setDescription("Product for refund testing");
            return merchantProductRepository.save(mp);
        });

        Order order = new Order();
        order.setCustomer(customer);
        order.setMerchantProduct(merchantProduct);
        order.setQuantity(1);
        order.setTotalPrice(merchantProduct.getPrice());
        order.setStatus(OrderStatus.DELIVERED);
        order.setOrderDate(LocalDateTime.now());
        Order savedOrder = orderRepository.save(order);

        sharedData.put(sharedKey, savedOrder.getOrderId().toString());
        logger.info("Created order with actual ID {} for customer {}, stored as {}. Symbolic test ID was {}",
                    savedOrder.getOrderId(), customerEmail, sharedKey, symbolicOrderId);
    }

    @Given("the customer {string} submitted a refund request for order {string} with reason {string}")
    public void customer_submitted_refund_request_for_order(String customerEmail, String orderSharedKey, String reason) {
        Customer customer = customerRepository.findByEmail(customerEmail)
            .orElseThrow(() -> new AssertionError("Customer " + customerEmail + " not found for refund setup."));
        String orderIdStr = sharedData.get(orderSharedKey);
        assertThat(orderIdStr).isNotNull().withFailMessage("Order ID for key " + orderSharedKey + " not found in sharedData.");
        Order order = orderRepository.findById(Long.parseLong(orderIdStr))
            .orElseThrow(() -> new AssertionError("Order with ID " + orderIdStr + " not found for refund setup."));

        RefundRequest refundRequest = new RefundRequest();
        refundRequest.setCustomer(customer);
        refundRequest.setOrder(order);
        refundRequest.setReason(reason);
        refundRequest.setAccountNumber("PRESET_ACC_NUM");
        refundRequest.setBankName("PRESET_BANK");
        refundRequest.setAccountName(customer.getFirstName() + " " + customer.getLastName());
        refundRequest.setStatus(RefundStatus.PENDING); // Use Enum
        refundRequest.setRequestedAt(LocalDateTime.now());
        refundRequestRepository.save(refundRequest);
        logger.info("Created pre-existing refund request for order ID {} with reason '{}'", orderIdStr, reason);
    }

    @Given("an order with ID {string} exists for customer {string} and has no refund requests, and its actual ID is stored as {string}")
    public void an_order_exists_for_customer_with_no_refund_requests_stored_as(String symbolicOrderId, String customerEmail, String sharedKey) {
        an_order_exists_for_customer_stored_as(symbolicOrderId, customerEmail, sharedKey);
        String actualOrderIdStr = sharedData.get(sharedKey);
        Order order = orderRepository.findById(Long.parseLong(actualOrderIdStr))
            .orElseThrow(() -> new AssertionError("Order not found for refund verification: " + actualOrderIdStr));
        List<RefundRequest> refunds = refundRequestRepository.findByOrder(order);
        assertThat(refunds).isEmpty();
        logger.info("Ensured order ID {} (stored as {}) has no refund requests.", actualOrderIdStr, sharedKey);
    }

    private HttpHeaders buildAuthenticatedCustomerHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (sharedData.containsKey("customerToken")) {
            headers.setBearerAuth(sharedData.get("customerToken"));
        } else {
             logger.warn("No customerToken found in sharedData for authenticated request!");
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

    private String resolveBodyPlaceholders(String body) {
        String resolvedBody = body;
         for (Map.Entry<String, String> entry : sharedData.entrySet()) {
            if (resolvedBody.contains("{" + entry.getKey() + "}")) {
                resolvedBody = resolvedBody.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }
        return resolvedBody;
    }

    @When("a POST request is made to {string} with an authenticated customer and the following body:")
    public void a_post_request_is_made_to_with_auth_customer_body(String path, String requestBody) {
        String apiBaseUrl = scenarioContext.getString("apiBaseUrl");
        HttpHeaders headers = buildAuthenticatedCustomerHeaders();
        HttpEntity<String> entity = new HttpEntity<>(resolveBodyPlaceholders(requestBody), headers);
        latestResponse = restTemplate.postForEntity(apiBaseUrl + resolvePathPlaceholders(path), entity, String.class);
    }

    @When("a POST request is made to {string} with the following body:")
    public void a_post_request_is_made_to_with_body(String path, String requestBody) {
        String apiBaseUrl = scenarioContext.getString("apiBaseUrl");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(resolveBodyPlaceholders(requestBody), headers);
        latestResponse = restTemplate.postForEntity(apiBaseUrl + resolvePathPlaceholders(path), entity, String.class);
    }

    @When("a GET request is made to {string} with an authenticated customer")
    public void a_get_request_is_made_to_with_auth_customer(String path) {
        String apiBaseUrl = scenarioContext.getString("apiBaseUrl");
        HttpHeaders headers = buildAuthenticatedCustomerHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        latestResponse = restTemplate.exchange(apiBaseUrl + resolvePathPlaceholders(path), HttpMethod.GET, entity, String.class);
    }

    @Then("the response body should contain {string} with value {string}")
    public void the_response_body_should_contain_with_value(String jsonPath, String expectedValue) {
        assertThat(latestResponse.getBody()).isNotNull();
        String resolvedExpectedValue = resolveBodyPlaceholders(expectedValue);
        String actualValue = Objects.toString(com.jayway.jsonpath.JsonPath.read(latestResponse.getBody(), "$." + jsonPath), "");
        assertThat(actualValue).isEqualTo(resolvedExpectedValue);
    }

    @Then("the response body should contain an {string} field")
    public void the_response_body_should_contain_an_error_field(String fieldName) {
        assertThat(latestResponse.getBody()).isNotNull();
        com.jayway.jsonpath.JsonPath.read(latestResponse.getBody(), "$." + fieldName);
    }

    @Then("the response body should be a list with at least {int} item(s)")
    public void the_response_body_should_be_a_list_with_at_least_items(int minCount) {
        assertThat(latestResponse.getBody()).isNotNull();
        List<?> list = com.jayway.jsonpath.JsonPath.parse(latestResponse.getBody()).read("$");
        assertThat(list).isNotNull().hasSizeGreaterThanOrEqualTo(minCount);
    }

    @Then("the response body should be an empty list")
    public void the_response_body_should_be_an_empty_list() {
        assertThat(latestResponse.getBody()).isNotNull();
        List<?> list = com.jayway.jsonpath.JsonPath.parse(latestResponse.getBody()).read("$");
        assertThat(list).isNotNull().isEmpty();
    }

    @Then("the response body should contain {string}") // General check for a key
    public void the_response_body_should_contain_key(String jsonPath) {
        assertThat(latestResponse.getBody()).isNotNull();
        com.jayway.jsonpath.JsonPath.read(latestResponse.getBody(), "$." + jsonPath);
    }
}
