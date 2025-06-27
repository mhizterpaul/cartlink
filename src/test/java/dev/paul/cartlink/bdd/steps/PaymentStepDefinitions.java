package dev.paul.cartlink.bdd.steps;

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
import dev.paul.cartlink.bdd.context.ScenarioContext;

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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class PaymentStepDefinitions {

    private static final Logger logger = LoggerFactory.getLogger(PaymentStepDefinitions.class);

    @Autowired private TestRestTemplate restTemplate;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private MerchantRepository merchantRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private MerchantProductRepository merchantProductRepository;
    @Autowired private OrderRepository orderRepository;

    @Autowired private PasswordEncoder passwordEncoder; // For creating dummy merchants if needed for product setup

    @Autowired
    private ScenarioContext scenarioContext;
    // private String apiBaseUrl; // Removed
    private ResponseEntity<String> latestResponse;
    private Map<String, String> sharedData = new HashMap<>();

    @Before
    public void setUp() {
        // orderRepository.deleteAll(); // Orders are preconditions, specific cleanup if needed by scenario.
        sharedData.clear();
        logger.info("PaymentStepDefinitions: Cleared sharedData.");
    }

    @After
    public void tearDown() {}

    // Precondition steps to set up data
    @Given("a customer {string} exists")
    public void a_customer_exists(String email) {
        if (customerRepository.findByEmail(email).isEmpty()) {
            Customer customer = new Customer();
            customer.setEmail(email);
            customer.setFirstName(email.split("\\.")[0]);
            customer.setLastName("User");
            customerRepository.save(customer);
        }
    }

    @Given("a merchant {string} exists")
    public void a_merchant_exists(String email) {
        if (merchantRepository.findByEmail(email).isEmpty()) {
            Merchant merchant = new Merchant();
            merchant.setEmail(email);
            merchant.setFirstName(email.split("\\.")[0]);
            merchant.setLastName("Merchant");
            merchant.setPassword(passwordEncoder.encode("dummyPass")); // Required if not nullable
            merchantRepository.save(merchant);
        }
    }

    @Given("this merchant has a product {string} with original ProductID {string} price {double} and stock {int}, whose MerchantProductID is {string}")
    public void merchant_has_product_with_mpid(String productName, String originalProdId, double price, int stock, String mpSharedKey) {
        // Assuming merchant "payment.merchant@example.com" was created by previous step
        Merchant merchant = merchantRepository.findByEmail("payment.merchant@example.com")
            .orElseThrow(() -> new AssertionError("Merchant for product setup not found."));

        List<Product> existingProducts = productRepository.findByNameContainingIgnoreCase(productName);
        Product product = existingProducts.isEmpty() ? null : existingProducts.get(0);
        if (product == null) {
            Product p = new Product();
            p.setName(productName);
            p.setBrand("PayBrand");
            p.setCategory("PayCategory");
            product = productRepository.save(p);
        }

        MerchantProduct mp = new MerchantProduct();
        mp.setProduct(product);
        mp.setMerchant(merchant);
        mp.setPrice(price);
        mp.setStock(stock);
        mp.setDescription("Product for payment test");
        MerchantProduct savedMp = merchantProductRepository.save(mp);
        sharedData.put(mpSharedKey, savedMp.getId().toString()); // Store actual MerchantProduct ID
        logger.info("Created MerchantProduct ID {} for product '{}', stored as {}", savedMp.getId(), productName, mpSharedKey);
    }

    @Given("an order with ID {string} exists for customer {string} involving merchantProduct {string} with total price {double} and its actual ID is stored as {string}")
    public void an_order_exists_stored_as(String symbolicOrderId, String customerEmail, String mpSharedKey, double totalPrice, String orderSharedKey) {
        Customer customer = customerRepository.findByEmail(customerEmail)
            .orElseThrow(() -> new AssertionError("Customer " + customerEmail + " not found."));
        String mpIdStr = sharedData.get(mpSharedKey);
        assertThat(mpIdStr).isNotNull().withFailMessage("MerchantProductID for key " + mpSharedKey + " not found.");
        MerchantProduct mp = merchantProductRepository.findById(Long.parseLong(mpIdStr))
            .orElseThrow(() -> new AssertionError("MerchantProduct with ID " + mpIdStr + " not found."));

        Order order = new Order();
        order.setCustomer(customer);
        order.setMerchantProduct(mp);
        order.setQuantity(1); // Assuming quantity 1 for simplicity, totalPrice should match mp.price
        order.setTotalPrice(totalPrice); // Or BigDecimal.valueOf(totalPrice) if type matches
        order.setStatus(OrderStatus.PENDING);
        order.setOrderDate(LocalDateTime.now());
        Order savedOrder = orderRepository.save(order);

        sharedData.put(orderSharedKey, savedOrder.getOrderId().toString());
        logger.info("Created order with actual ID {} for customer {}, stored as {}. Symbolic test ID was {}",
                    savedOrder.getOrderId(), customerEmail, orderSharedKey, symbolicOrderId);
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

    @When("a POST form request is made to {string} with the following parameters:")
    public void a_post_form_request_is_made_to_with_parameters(String path, DataTable dataTable) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        Map<String, String> params = dataTable.asMaps(String.class, String.class).get(0);
        for (Map.Entry<String, String> entry : params.entrySet()) {
            map.add(entry.getKey(), resolvePlaceholders(entry.getValue()));
        }

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);
        latestResponse = restTemplate.postForEntity(scenarioContext.getString("apiBaseUrl") + resolvePlaceholders(path), entity, String.class);
        logger.info("POST Form to {}: Status {}, Body {}", resolvePlaceholders(path), latestResponse.getStatusCodeValue(), latestResponse.getBody());
    }

    @When("a POST request is made to {string} with no body")
    public void a_post_request_is_made_to_with_no_body(String path) {
        String apiBaseUrl = scenarioContext.getString("apiBaseUrl");
        HttpHeaders headers = new HttpHeaders(); // No specific Content-Type needed for no-body POST
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        latestResponse = restTemplate.postForEntity(apiBaseUrl + resolvePlaceholders(path), entity, String.class);
        logger.info("POST (no body) to {}: Status {}, Body {}", resolvePlaceholders(path), latestResponse.getStatusCodeValue(), latestResponse.getBody());
    }


    // --- Then Steps ---
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

    @Then("the response body should contain {string} with number value {string}")
    public void the_response_body_should_contain_with_number_value(String jsonPath, String expectedValue) {
        assertThat(latestResponse.getBody()).isNotNull();
        Object actualObject = com.jayway.jsonpath.JsonPath.read(latestResponse.getBody(), "$." + jsonPath);
        BigDecimal actualValue = new BigDecimal(actualObject.toString());
        BigDecimal expectedDecimalValue = new BigDecimal(expectedValue);
        assertThat(actualValue).isEqualByComparingTo(expectedDecimalValue);
    }

    @Then("the response body should be the string {string}")
    public void the_response_body_should_be_the_string(String expectedBody) {
        assertThat(latestResponse.getBody()).isEqualTo(expectedBody);
    }
}
