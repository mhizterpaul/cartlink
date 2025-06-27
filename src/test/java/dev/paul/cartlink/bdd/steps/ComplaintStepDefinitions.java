package dev.paul.cartlink.bdd.steps;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.paul.cartlink.complaint.model.Complaint;
import dev.paul.cartlink.complaint.repository.ComplaintRepository;
import dev.paul.cartlink.customer.model.Customer;
import dev.paul.cartlink.customer.repository.CustomerRepository;
import dev.paul.cartlink.merchant.model.Merchant;
import dev.paul.cartlink.merchant.model.MerchantProduct;
import dev.paul.cartlink.merchant.repository.MerchantProductRepository;
import dev.paul.cartlink.merchant.repository.MerchantRepository;
import dev.paul.cartlink.order.model.Order;
import dev.paul.cartlink.order.model.OrderStatus;
import dev.paul.cartlink.complaint.model.ComplaintStatus; // Import ComplaintStatus
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

public class ComplaintStepDefinitions {

    private static final Logger logger = LoggerFactory.getLogger(ComplaintStepDefinitions.class);

    @Autowired private TestRestTemplate restTemplate;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private MerchantRepository merchantRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private MerchantProductRepository merchantProductRepository;
    @Autowired private OrderRepository orderRepository;

    @Autowired private ComplaintRepository complaintRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Autowired
    private ScenarioContext scenarioContext;

    // private ResponseEntity<String> latestResponse; // Removed: Now using ScenarioContext
    // private Map<String, String> sharedData = new HashMap<>(); // Removed: Now using ScenarioContext

    @Before
    public void setUp() {
        complaintRepository.deleteAll();
        // orderRepository.deleteAll();
        // sharedData.clear(); // Removed
        logger.info("ComplaintStepDefinitions: Cleared complaint repository.");
    }

    @After
    public void tearDown() {}

    // This step is now in CommonStepDefinitions.java
    // @Given("a customer is logged in with email {string} and password {string}")
    // ... (rest of commented out method)

    // Removed duplicate step: @Given("an order with ID {string} exists for customer {string} and its actual ID is stored as {string}")
    // ... (rest of comment)

    @Given("the customer {string} submitted a complaint for order {string} with title {string}")
    public void customer_submitted_complaint_for_order_with_title(String customerEmail, String orderSharedKey, String complaintTitle) {
        Customer customer = customerRepository.findByEmail(customerEmail)
            .orElseThrow(() -> new AssertionError("Customer " + customerEmail + " not found for complaint setup."));
        String orderIdStr = scenarioContext.getString(orderSharedKey); // Corrected: Use ScenarioContext
        assertThat(orderIdStr).isNotNull().withFailMessage("Order ID for key " + orderSharedKey + " not found in ScenarioContext.");
        Order order = orderRepository.findById(Long.parseLong(orderIdStr))
            .orElseThrow(() -> new AssertionError("Order with ID " + orderIdStr + " not found for complaint setup."));

        Complaint complaint = new Complaint();
        complaint.setCustomer(customer);
        complaint.setOrder(order);
        complaint.setTitle(complaintTitle);
        complaint.setDescription("A pre-existing complaint for testing GET APIs.");
        complaint.setCategory("GENERAL_INQUIRY");
        complaint.setStatus(ComplaintStatus.PENDING); // Use valid Enum: PENDING
        complaint.setCreatedAt(LocalDateTime.now());
        complaintRepository.save(complaint);
        logger.info("Created pre-existing complaint titled '{}' for order ID {}", complaintTitle, orderIdStr);
    }

    @Given("an order with ID {string} exists for customer {string} and has no complaints, and its actual ID is stored as {string}")
    public void an_order_exists_for_customer_with_no_complaints_stored_as(String symbolicOrderId, String customerEmail, String sharedKey) {
        // Create the order directly here, similar to how it was done before or how RefundStepDefinitions does it.
        // Ensure it uses complaint-specific details if necessary and stores ID in ScenarioContext.
        Customer customer = customerRepository.findByEmail(customerEmail)
            .orElseThrow(() -> new AssertionError("Customer " + customerEmail + " not found for order setup."));

        Merchant tempMerchant = merchantRepository.findByEmail("complaint.specific.merchant@example.com").orElseGet(() -> {
            Merchant m = new Merchant();
            m.setEmail("complaint.specific.merchant@example.com");
            m.setFirstName("ComplaintSetup");
            m.setLastName("Merchant");
            m.setPassword(passwordEncoder.encode("password"));
            return merchantRepository.save(m);
        });
        final Merchant finalMerchant = tempMerchant;

        List<Product> existingProducts = productRepository.findByNameContainingIgnoreCase("ComplaintSetupProduct");
        Product tempProduct = existingProducts.isEmpty() ? null : existingProducts.get(0);
        if (tempProduct == null) {
            Product p = new Product();
            p.setName("ComplaintSetupProduct");
            p.setBrand("BrandCS");
            p.setCategory("CategoryCS");
            tempProduct = productRepository.save(p);
        }
        final Product productForMerchantProduct = tempProduct;

        MerchantProduct merchantProduct = merchantProductRepository.findByMerchantAndProduct(finalMerchant, productForMerchantProduct).orElseGet(()-> {
            MerchantProduct mp = new MerchantProduct();
            mp.setMerchant(finalMerchant);
            mp.setProduct(productForMerchantProduct);
            mp.setPrice(15.0); // Different price for clarity if needed
            mp.setStock(150);  // Different stock
            mp.setDescription("Product for complaint setup and verification");
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
        logger.info("ComplaintStepDefinitions: Created order with actual ID {} for customer {}, stored as {} in ScenarioContext. Symbolic test ID was {}",
                    savedOrder.getOrderId(), customerEmail, sharedKey, symbolicOrderId);

        // Verify no complaints exist for this newly created order
        List<Complaint> complaints = complaintRepository.findByOrder(savedOrder);
        assertThat(complaints).isEmpty();
        logger.info("ComplaintStepDefinitions: Ensured order ID {} (stored as {}) has no complaints.", savedOrder.getOrderId(), sharedKey);
    }


    private HttpHeaders buildAuthenticatedCustomerHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String customerToken = scenarioContext.getString("customerToken"); // Retrieve from ScenarioContext
        if (customerToken != null) {
            headers.setBearerAuth(customerToken);
        } else {
             logger.warn("No customerToken found in ScenarioContext for authenticated request!");
        }
        return headers;
    }

    // Using ScenarioContext based placeholder resolution
    private String resolvePlaceholders(String valueWithPlaceholders) {
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


    @When("for a complaint, a POST request is made to {string} with an authenticated customer and the following body:")
    public void for_a_complaint_a_post_request_is_made_to_with_auth_customer_body(String path, String requestBody) {
        HttpHeaders headers = buildAuthenticatedCustomerHeaders();
        String resolvedBody = resolvePlaceholders(requestBody); // Use new resolvePlaceholders
        String resolvedPath = resolvePlaceholders(path);       // Use new resolvePlaceholders
        HttpEntity<String> entity = new HttpEntity<>(resolvedBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(scenarioContext.getString("apiBaseUrl") + resolvedPath, entity, String.class);
        scenarioContext.set("latestResponse", response); // Store in ScenarioContext
        logger.info("Authenticated Customer POST to {}: Status {}, Body {}", resolvedPath, response.getStatusCodeValue(), response.getBody());
    }

    // This step is now in CommonStepDefinitions.java
    // @When("a POST request is made to {string} with the following body:") // For unauthenticated
    // public void a_post_request_is_made_to_with_body(String path, String requestBody) {
    //     HttpHeaders headers = new HttpHeaders(); // No auth
    //     headers.setContentType(MediaType.APPLICATION_JSON);
    //     HttpEntity<String> entity = new HttpEntity<>(resolveBodyPlaceholders(requestBody), headers);
    //     latestResponse = restTemplate.postForEntity(scenarioContext.getString("apiBaseUrl") + resolvePathPlaceholders(path), entity, String.class);
    //      logger.info("Unauthenticated POST to {}: Status {}, Body {}", resolvePathPlaceholders(path), latestResponse.getStatusCodeValue(), latestResponse.getBody());
    // }


    @When("for a complaint, a GET request is made to {string} with an authenticated customer")
    public void for_a_complaint_a_get_request_is_made_to_with_auth_customer(String path) {
        HttpHeaders headers = buildAuthenticatedCustomerHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        String resolvedPath = resolvePlaceholders(path); // Corrected: Use resolvePlaceholders
        ResponseEntity<String> response = restTemplate.exchange(scenarioContext.getString("apiBaseUrl") + resolvedPath, HttpMethod.GET, entity, String.class);
        scenarioContext.set("latestResponse", response); // Corrected: Store in ScenarioContext
        logger.info("Authenticated Customer GET to {}: Status {}, Body {}", resolvedPath, response.getStatusCodeValue(), response.getBody());
    }

    // --- Then Steps (Common) ---
    // Step moved to CommonStepDefinitions.java

    // @Then("the response body should contain {string} with value {string}")
    // public void the_response_body_should_contain_with_value(String jsonPath, String expectedValue) {
    //     @SuppressWarnings("unchecked")
    //     ResponseEntity<String> response = scenarioContext.get("latestResponse", ResponseEntity.class);
    //     assertThat(response.getBody()).isNotNull();
    //     String resolvedExpectedValue = resolveBodyPlaceholders(expectedValue); // Resolve if expected value is like {complaintOrderId}
    //     String actualValue = Objects.toString(com.jayway.jsonpath.JsonPath.read(response.getBody(), "$." + jsonPath), "");
    //     assertThat(actualValue).isEqualTo(resolvedExpectedValue);
    // }

    // This step is now redundant as the generic one in CommonStepDefinitions can be used.
    // Feature files should be updated if they used "an {string} field" to use "a {string} field"
    // @Then("the response body should contain an {string} field")
    // public void the_response_body_should_contain_an_error_field(String fieldName) {
    //     // This would require calling the common step, which is not standard across step def classes
    //     // Or, feature files should just use the "a {string}" version directly.
    // }

    // This step is now in CommonStepDefinitions.java
    // @Then("the response body should be a list with at least {int} item(s)")
    // public void the_response_body_should_be_a_list_with_at_least_items(int minCount) {
    //     @SuppressWarnings("unchecked")
    //     ResponseEntity<String> response = scenarioContext.get("latestResponse", ResponseEntity.class);
    //     assertThat(response.getBody()).isNotNull();
    //     List<?> list = com.jayway.jsonpath.JsonPath.parse(response.getBody()).read("$");
    //     assertThat(list).isNotNull().hasSizeGreaterThanOrEqualTo(minCount);
    // }

    // This step is now in CommonStepDefinitions.java
    // @Then("the response body should be an empty list")
    // public void the_response_body_should_be_an_empty_list() {
    //     @SuppressWarnings("unchecked")
    //     ResponseEntity<String> response = scenarioContext.get("latestResponse", ResponseEntity.class);
    //     assertThat(response.getBody()).isNotNull();
    //     List<?> list = com.jayway.jsonpath.JsonPath.parse(response.getBody()).read("$");
    //     assertThat(list).isNotNull().isEmpty();
    // }
}
