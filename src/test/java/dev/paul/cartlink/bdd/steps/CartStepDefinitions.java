package dev.paul.cartlink.bdd.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.paul.cartlink.product.model.Product;
import dev.paul.cartlink.product.repository.ProductRepository;
import dev.paul.cartlink.customer.model.Customer;
import dev.paul.cartlink.customer.repository.CustomerRepository;
import dev.paul.cartlink.merchant.model.Merchant; // Added
import dev.paul.cartlink.merchant.repository.MerchantRepository; // Added
import dev.paul.cartlink.merchant.model.MerchantProduct; // Added
import dev.paul.cartlink.merchant.repository.MerchantProductRepository; // Added
import org.springframework.security.crypto.password.PasswordEncoder; // Added

import io.cucumber.datatable.DataTable;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;


import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.math.BigDecimal;
import java.util.HashMap;


import static org.assertj.core.api.Assertions.assertThat;

public class CartStepDefinitions {

    private static final Logger logger = LoggerFactory.getLogger(CartStepDefinitions.class);

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MerchantRepository merchantRepository; // Added

    @Autowired
    private MerchantProductRepository merchantProductRepository; // Added

    @Autowired
    private PasswordEncoder passwordEncoder; // Added

    @Autowired
    private CustomerRepository customerRepository; // For logged-in user context if needed for cart

    // Shared state
    private String apiBaseUrl;
    private ResponseEntity<String> latestResponse;
    private String cartCookieId; // To store and reuse the cart_cookie_id
    private Map<String, String> sharedData = new HashMap<>(); // For tokens, customer IDs, item IDs from responses

    @Before
    public void setUp() {
        productRepository.deleteAll(); // Clear products for clean test environment
        // customerRepository.deleteAll(); // Handled by CustomerStepDefinitions normally
        cartCookieId = null; // Reset cookie for each scenario
        sharedData.clear();
        logger.info("CartStepDefinitions: Cleared products and cartCookieId.");
    }

    @After
    public void tearDown() {
        // Cleanup if necessary
    }

    @Given("the API base URL is {string}")
    public void the_api_base_url_is(String baseUrl) {
        this.apiBaseUrl = baseUrl;
    }

    @Given("a new cart session is started")
    public void a_new_cart_session_is_started() {
        // Make a simple request (like get empty cart) to ensure a cookie is set if not present
        // Or just initialize cartCookieId to null, first request will set it.
        // Forcing cookie generation by calling an endpoint that sets it:
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                apiBaseUrl + "/customers/cart", HttpMethod.GET, entity, String.class);
        extractCartCookie(response);
        assertThat(cartCookieId).isNotNull().isNotBlank();
        logger.info("New cart session started. Cookie ID: {}", cartCookieId);
    }

    private void extractCartCookie(ResponseEntity<?> response) {
        List<String> cookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        if (cookies != null) {
            for (String cookieStr : cookies) {
                if (cookieStr.startsWith("cart_cookie_id=")) {
                    cartCookieId = cookieStr.substring("cart_cookie_id=".length(), cookieStr.indexOf(';'));
                    logger.info("Extracted cart_cookie_id: {}", cartCookieId);
                    break;
                }
            }
        }
    }

    private HttpHeaders buildHeadersWithCartCookie() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (cartCookieId != null) {
            headers.add(HttpHeaders.COOKIE, "cart_cookie_id=" + cartCookieId);
        }
        // Add customer auth token if available in sharedData (for checkout)
        if (sharedData.containsKey("customerToken")) {
            headers.setBearerAuth(sharedData.get("customerToken"));
        }
        return headers;
    }

    @Given("a product with ID {long} exists with price {double} and stock {int}")
    public void a_product_exists_with_id_price_stock(Long testProductId, Double price, Integer stock) {
        // This step sets up a Product and a MerchantProduct for cart tests.
        // The "productId" used in cart requests refers to the base Product's ID.

        Merchant merchant = merchantRepository.findByEmail("cart.test.merchant@example.com").orElseGet(() -> {
            Merchant m = new Merchant();
            m.setEmail("cart.test.merchant@example.com");
            m.setFirstName("CartTest");
            m.setLastName("Merchant");
            m.setPassword(passwordEncoder.encode("password")); // Assuming PasswordEncoder is available
            return merchantRepository.save(m);
        });

        Product product = productRepository.findById(testProductId).orElseGet(() -> {
            Product p = new Product();
            // Product.productId is not auto-generated based on its definition (has setId).
            // This means we can set it if we want to match testProductId.
            // However, if it were auto-generated, we'd save then use p.getProductId().
            // For now, let's assume we can't/shouldn't set it directly if it's meant to be an existing product.
            // If this step implies CREATING a product if it doesn't exist with this ID, then:
            p.setProductId(testProductId); // Setting it to match the test's expectation for productId
            p.setName("Test Product " + testProductId);
            p.setBrand("CartBrand");
            p.setCategory("CartCategory");
            return productRepository.save(p);
        });

        // Ensure product ID used in sharedData is the actual one (especially if not set manually)
        sharedData.put("product_" + testProductId + "_actualId", product.getProductId().toString());

        // Create or update MerchantProduct
        // A real system might have a unique constraint on (merchant, product) for MerchantProduct.
        MerchantProduct merchantProduct = merchantProductRepository
            .findByMerchantAndProduct(merchant, product) // Need to add this method to repo if it doesn't exist
            .orElseGet(() -> {
                MerchantProduct mp = new MerchantProduct();
                mp.setMerchant(merchant);
                mp.setProduct(product);
                return mp;
            });

        merchantProduct.setPrice(price);
        merchantProduct.setStock(stock);
        merchantProduct.setDescription("Product for cart test " + product.getProductId());
        merchantProduct.setmerchantProductType("GENERAL"); // Assuming a default type

        merchantProductRepository.save(merchantProduct);
        logger.info("Ensured MerchantProduct for Product ID {} and Merchant {} exists with price {} and stock {}. MP_ID: {}",
            product.getProductId(), merchant.getEmail(), price, stock, merchantProduct.getId());
    }

    @Given("the following item is added to the cart:")
    public void the_following_item_is_added_to_the_cart(DataTable dataTable) {
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> row : rows) {
            long productId = Long.parseLong(row.get("productId"));
            int quantity = Integer.parseInt(row.get("quantity"));

            String actualProductIdStr = sharedData.getOrDefault("product_" + productId + "_actualId", String.valueOf(productId));

            String requestBody = String.format("{\"productId\": %s, \"quantity\": %d}", actualProductIdStr, quantity);

            HttpHeaders headers = buildHeadersWithCartCookie();
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(apiBaseUrl + "/customers/cart/items", entity, String.class);
            extractCartCookie(response); // Update cookie if it changed (e.g. first item creates cart)
            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();

            // Try to get item ID from response if needed for later steps
            // Assuming response is CartResponse and items have an "id" or "itemId"
            // e.g., "cart.items[0].id"
            try {
                List<Integer> itemIds = com.jayway.jsonpath.JsonPath.read(response.getBody(), "$.cart.items[?(@.productId=="+actualProductIdStr+")].id");
                if (!itemIds.isEmpty()) {
                    sharedData.put("cart_item_id_for_product_" + productId, itemIds.get(0).toString());
                     logger.info("Stored cart item ID {} for product {}", itemIds.get(0), productId);
                }
            } catch (Exception e) {
                logger.warn("Could not extract item ID from add to cart response: {}", e.getMessage());
            }
        }
    }


    @When("a {word} request is made to {string} using the cart session")
    public void a_request_is_made_to_using_cart_session(String method, String path) {
        HttpHeaders headers = buildHeadersWithCartCookie();
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        latestResponse = restTemplate.exchange(apiBaseUrl + resolveCartPathVariables(path), HttpMethod.valueOf(method.toUpperCase()), entity, String.class);
        extractCartCookie(latestResponse);
        logger.info("{} request to {} (cart session). Status: {}, Body: {}", method, path, latestResponse.getStatusCode(), latestResponse.getBody());
    }

    @When("a {word} request is made to {string} using the cart session with the following body:")
    public void a_request_is_made_to_using_cart_session_with_body(String method, String path, String requestBody) {
        HttpHeaders headers = buildHeadersWithCartCookie();
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        latestResponse = restTemplate.exchange(apiBaseUrl + resolveCartPathVariables(path), HttpMethod.valueOf(method.toUpperCase()), entity, String.class);
        extractCartCookie(latestResponse);
        logger.info("{} request to {} (cart session) with body. Status: {}, Body: {}", method, path, latestResponse.getStatusCode(), latestResponse.getBody());
    }

    @When("a POST request is made to {string} using the cart session and authenticated customer with the following body:")
    public void a_post_request_is_made_to_using_cart_session_and_authenticated_customer_with_body(String path, String requestBody) {
        // This step assumes customerToken is already in sharedData via a "customer is logged in" step
        HttpHeaders headers = buildHeadersWithCartCookie(); // Will include Bearer token if present
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        latestResponse = restTemplate.postForEntity(apiBaseUrl + resolveCartPathVariables(path), entity, String.class);
        extractCartCookie(latestResponse);
        logger.info("POST request to {} (cart session, auth customer) with body. Status: {}, Body: {}", path, latestResponse.getStatusCode(), latestResponse.getBody());
    }


    // Then steps (can be common)
    @Then("the response status code should be {int}")
    public void the_response_status_code_should_be(Integer statusCode) {
        assertThat(latestResponse.getStatusCodeValue()).isEqualTo(statusCode);
    }

    @Then("the response body should contain {string}")
    public void the_response_body_should_contain_a(String jsonPath) {
        String responseBody = latestResponse.getBody();
        assertThat(responseBody).isNotNull();
        com.jayway.jsonpath.JsonPath.read(responseBody, "$." + jsonPath);
    }

    @Then("the response body should contain {string} with value {string}")
    public void the_response_body_should_contain_with_value(String jsonPath, String expectedValue) {
        String responseBody = latestResponse.getBody();
        assertThat(responseBody).isNotNull();
        String actualValue = Objects.toString(com.jayway.jsonpath.JsonPath.read(responseBody, "$." + jsonPath), "");
        assertThat(actualValue).isEqualTo(expectedValue);
    }

    @Then("the response body should contain {string} with number value {string}")
    public void the_response_body_should_contain_with_number_value(String jsonPath, String expectedValue) {
        String responseBody = latestResponse.getBody();
        assertThat(responseBody).isNotNull();
        Object actualObject = com.jayway.jsonpath.JsonPath.read(responseBody, "$." + jsonPath);
        BigDecimal actualValue = new BigDecimal(actualObject.toString());
        BigDecimal expectedDecimalValue = new BigDecimal(expectedValue);
        assertThat(actualValue).isEqualByComparingTo(expectedDecimalValue);
    }

    @Then("the response body should contain {string} as an empty list")
    public void the_response_body_should_contain_as_an_empty_list(String jsonPath) {
        String responseBody = latestResponse.getBody();
        assertThat(responseBody).isNotNull();
        List<?> list = com.jayway.jsonpath.JsonPath.read(responseBody, "$." + jsonPath);
        assertThat(list).isNotNull().isEmpty();
    }

    @Then("the response body should contain {string} as a list with {int} item(s)")
    public void the_response_body_should_contain_as_a_list_with_items(String jsonPath, int count) {
        String responseBody = latestResponse.getBody();
        assertThat(responseBody).isNotNull();
        List<?> list = com.jayway.jsonpath.JsonPath.read(responseBody, "$." + jsonPath);
        assertThat(list).isNotNull().hasSize(count);
    }

    @Then("the response body should contain a message like {string}")
    public void the_response_body_should_contain_a_message_like(String messageSubstring) {
        String responseBody = latestResponse.getBody();
        assertThat(responseBody).isNotNull();
        // This is a simplified check. A more robust check might parse the JSON
        // and look for a specific "message" or "error" field.
        assertThat(responseBody).containsIgnoringCase(messageSubstring);
    }

    @Given("a customer is logged in with email {string} and password {string}")
    public void a_customer_is_logged_in_with_email_and_password(String email, String password) throws Exception {
        // Re-use login mechanism from CustomerStepDefinitions if possible, or duplicate simplified version
        // For now, direct call to login endpoint (assuming it's available and works)
        if (customerRepository.findByEmail(email).isEmpty()) {
            Customer customer = new Customer();
            customer.setEmail(email);
            customer.setFirstName("Checkout");
            customer.setLastName("User");
            customerRepository.save(customer);
        }

        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", email);
        loginRequest.put("password", password);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(loginRequest), headers);

        ResponseEntity<String> loginResponse = restTemplate.postForEntity(apiBaseUrl + "/customers/login", entity, String.class);
        assertThat(loginResponse.getStatusCodeValue()).isEqualTo(200);

        String responseBody = loginResponse.getBody();
        assertThat(responseBody).isNotNull();
        String token = com.jayway.jsonpath.JsonPath.read(responseBody, "$.token");
        sharedData.put("customerToken", token); // Store customer token for authenticated cart operations
        logger.info("Customer {} logged in for cart test. Token stored.", email);
    }

    private String resolveCartPathVariables(String path) {
        String resolvedPath = path;
        // Example: /customers/cart/items/{itemId}
        // We need to get itemId from sharedData, which should have been stored after adding an item.
        // This requires careful management of item IDs.
        // Let's assume itemId for product "X" is stored as "cart_item_id_for_product_X"

        // A simpler way for feature files might be to use a known/static item ID like "1"
        // if the first item added always gets ID 1 in a clean test.
        // Or, use a placeholder like {last_added_item_id}

        // For now, if path has {itemId}, we try to replace it with a value from sharedData.
        // This is a simplistic approach.
        if (path.contains("{itemId}")) {
            // This part is tricky: which item's ID to use?
            // The feature file says "/customers/cart/items/1" - this means we assume item ID 1.
            // This assumption must hold in the test context (e.g. first item added to a clean cart gets ID 1).
            // If not, this step definition or feature file needs to be smarter.
            // For the scenario "Update Item Quantity in Cart", it uses "/customers/cart/items/1".
            // The step "the following item is added to the cart" for that scenario adds one item.
            // We need to ensure that one item gets itemId 1.
            // The `sharedData.put("cart_item_id_for_product_" + productId, itemIds.get(0).toString());`
            // tries to store it. Let's assume for product with test ID 1, its cart item ID is stored.

            String itemIdToUse = sharedData.get("cart_item_id_for_product_1"); // Defaulting to product 1's item for now
            if (itemIdToUse == null && path.matches(".*/items/\\d+")) {
                 // If path is like /items/1, use that 1 directly.
                 // This is what the feature file currently does.
            } else if (itemIdToUse != null) {
                resolvedPath = path.replace("{itemId}", itemIdToUse);
            } else {
                 logger.warn("Path {} contains {{itemId}} but no item ID was found in sharedData. Using path as is or with hardcoded '1'.", path);
                 // Fallback or error if necessary. For now, if the path is literal like /items/1, it will pass through.
                 // If it's /items/{itemId} and no replacement, it will fail.
            }
        }
        return resolvedPath;
    }
}
