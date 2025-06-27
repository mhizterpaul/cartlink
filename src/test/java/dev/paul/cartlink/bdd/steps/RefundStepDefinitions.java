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
    // private ResponseEntity<String> latestResponse; // Will be retrieved from ScenarioContext
    // private Map<String, String> sharedData = new HashMap<>(); // Will use ScenarioContext

    @Before
    public void setUp() {
        refundRequestRepository.deleteAll();
        // sharedData.clear(); // ScenarioContext is managed by its own lifecycle or cleared in CommonHooks
        logger.info("RefundStepDefinitions: Cleared refund request repository.");
    }

    @After
    public void tearDown() {}

    // Removed duplicate "a customer is logged in with email {string} and password {string}"
    // This will be handled by CommonStepDefinitions.java

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


        MerchantProduct merchantProduct = merchantProductRepository.findByMerchantAndProduct(finalMerchant, finalProduct).orElseGet(()-> {
            MerchantProduct mp = new MerchantProduct(); mp.setMerchant(finalMerchant); mp.setProduct(finalProduct);

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

        scenarioContext.set(sharedKey, savedOrder.getOrderId().toString()); // Use ScenarioContext
        logger.info("Created order with actual ID {} for customer {}, stored as {} in ScenarioContext. Symbolic test ID was {}",
                    savedOrder.getOrderId(), customerEmail, sharedKey, symbolicOrderId);
    }

    @Given("the customer {string} submitted a refund request for order {string} with reason {string}")
    public void customer_submitted_refund_request_for_order(String customerEmail, String orderSharedKey, String reason) {
        Customer customer = customerRepository.findByEmail(customerEmail)
            .orElseThrow(() -> new AssertionError("Customer " + customerEmail + " not found for refund setup."));
        String orderIdStr = scenarioContext.getString(orderSharedKey); // Use ScenarioContext
        assertThat(orderIdStr).isNotNull().withFailMessage("Order ID for key " + orderSharedKey + " not found in ScenarioContext.");
        Order order = orderRepository.findById(Long.parseLong(orderIdStr))
            .orElseThrow(() -> new AssertionError("Order with ID " + orderIdStr + " not found for refund setup."));

        RefundRequest refundRequest = new RefundRequest();
        refundRequest.setCustomer(customer);
        refundRequest.setOrder(order);
        refundRequest.setReason(reason);
        refundRequest.setAccountNumber("PRESET_ACC_NUM");
        refundRequest.setBankName("PRESET_BANK");
        refundRequest.setAccountName(customer.getFirstName() + " " + customer.getLastName());
        refundRequest.setStatus(RefundStatus.PENDING);
        refundRequest.setRequestedAt(LocalDateTime.now());
        refundRequestRepository.save(refundRequest);
        logger.info("Created pre-existing refund request for order ID {} with reason '{}'", orderIdStr, reason);
    }

    @Given("an order with ID {string} exists for customer {string} and has no refund requests, and its actual ID is stored as {string}")
    public void an_order_exists_for_customer_with_no_refund_requests_stored_as(String symbolicOrderId, String customerEmail, String sharedKey) {
        an_order_exists_for_customer_stored_as(symbolicOrderId, customerEmail, sharedKey); // This now uses ScenarioContext
        String actualOrderIdStr = scenarioContext.getString(sharedKey); // Use ScenarioContext
        Order order = orderRepository.findById(Long.parseLong(actualOrderIdStr))
            .orElseThrow(() -> new AssertionError("Order not found for refund verification: " + actualOrderIdStr));
        List<RefundRequest> refunds = refundRequestRepository.findByOrder(order);
        assertThat(refunds).isEmpty();
        logger.info("Ensured order ID {} (stored as {}) has no refund requests.", actualOrderIdStr, sharedKey);
    }

    private HttpHeaders buildAuthenticatedCustomerHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (scenarioContext.containsKey("customerToken")) { // Use ScenarioContext
            headers.setBearerAuth(scenarioContext.getString("customerToken"));
        } else {
             logger.warn("No customerToken found in ScenarioContext for authenticated request!");
        }
        return headers;
    }

    // Removed resolvePathPlaceholders and resolveBodyPlaceholders, will rely on CommonStepDefinitions or direct ScenarioContext usage

    // This step definition is now in CommonStepDefinitions.java
    // @When("a POST request is made to {string} with an authenticated customer and the following body:")
    // public void a_post_request_is_made_to_with_auth_customer_body(String path, String requestBody) {
    //     ...
    // }

    // Removed duplicate "a POST request is made to {string} with the following body:" as it will be handled by CommonStepDefinitions

    // This step is now in CommonStepDefinitions.java
    // @When("a GET request is made to {string} with an authenticated customer")
    // public void a_get_request_is_made_to_with_auth_customer(String path) {
    //     ...
    // }

    private String resolvePlaceholdersFromScenarioContext(String valueWithPlaceholders) {
        String resolvedValue = valueWithPlaceholders;
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\{([^}]+)\\}");
        java.util.regex.Matcher matcher = pattern.matcher(resolvedValue);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1);
            if (scenarioContext.containsKey(key)) {
                matcher.appendReplacement(sb, scenarioContext.getString(key));
            } else {
                logger.warn("Placeholder {{{}}} found in value but key not in ScenarioContext.", key);
                matcher.appendReplacement(sb, matcher.group(0)); // Keep original placeholder
            }
        }
        matcher.appendTail(sb);
        resolvedValue = sb.toString();

        if (resolvedValue.contains("{") && resolvedValue.contains("}")) {
             logger.warn("Value {} still contains unresolved placeholders: {}", valueWithPlaceholders, resolvedValue);
        }
        return resolvedValue;
    }

    // Removed duplicate method "the_response_body_should_contain_with_value"
    // as it's covered by CommonStepDefinitions.the_response_body_should_contain_with_value

    // Removed duplicate method "the_response_body_should_contain_an_error_field"
    // as it is defined in CommonStepDefinitions.java

    // Removed duplicate method "the_response_body_should_be_a_list_with_at_least_items"
    // as it's covered by CommonStepDefinitions

    // Removed duplicate method "the_response_body_should_be_an_empty_list"
    // as it's covered by CommonStepDefinitions.the_response_body_should_be_an_empty_list

    @Then("the response body should contain {string}") // General check for a key
    public void the_response_body_should_contain_key(String jsonPath) {
        @SuppressWarnings("unchecked")
        ResponseEntity<String> latestResponse = scenarioContext.get("latestResponse", ResponseEntity.class);
        assertThat(latestResponse).isNotNull();
        assertThat(latestResponse.getBody()).isNotNull();
        com.jayway.jsonpath.JsonPath.read(latestResponse.getBody(), "$." + jsonPath);
    }
}
