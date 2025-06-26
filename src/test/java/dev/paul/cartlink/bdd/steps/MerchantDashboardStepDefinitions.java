package dev.paul.cartlink.bdd.steps;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.paul.cartlink.merchant.model.Merchant;
import dev.paul.cartlink.merchant.repository.MerchantRepository;

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class MerchantDashboardStepDefinitions {

    private static final Logger logger = LoggerFactory.getLogger(MerchantDashboardStepDefinitions.class);

    @Autowired private TestRestTemplate restTemplate;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private MerchantRepository merchantRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private String apiBaseUrl;
    private ResponseEntity<String> latestResponse;
    private Map<String, String> sharedData = new HashMap<>(); // For auth token

    @Before
    public void setUp() {
        sharedData.clear();
        // No specific data cleanup here as dashboard reads existing data.
        // Merchant creation for login is idempotent.
        logger.info("MerchantDashboardStepDefinitions: Cleared sharedData.");
    }

    @After
    public void tearDown() {}

    @Given("the API base URL is {string}")
    public void the_api_base_url_is(String baseUrl) {
        this.apiBaseUrl = baseUrl;
    }

    @Given("a merchant is logged in with email {string} and password {string}")
    public void a_merchant_is_logged_in_with_email_and_password(String email, String password) throws JsonProcessingException {
        if (merchantRepository.findByEmail(email).isEmpty()) {
            Merchant merchant = new Merchant();
            merchant.setEmail(email);
            merchant.setPassword(passwordEncoder.encode(password));
            merchant.setFirstName("Dashboard");
            merchant.setLastName("User");
            merchantRepository.save(merchant);
        }

        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", email);
        loginRequest.put("password", password);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(loginRequest), headers);
        // Path from MerchantController for login
        ResponseEntity<String> loginResponse = restTemplate.postForEntity(apiBaseUrl + "/merchant/login", entity, String.class);

        if(loginResponse.getStatusCodeValue() != 200) {
            logger.error("Merchant login failed for {}: {} - {}", email, loginResponse.getStatusCodeValue(), loginResponse.getBody());
             assertThat(loginResponse.getStatusCodeValue()).isEqualTo(200);
        }
        String responseBody = loginResponse.getBody();
        String token = com.jayway.jsonpath.JsonPath.read(responseBody, "$.token");
        sharedData.put("merchantToken", token);
        logger.info("Merchant {} logged in for dashboard test. Token stored.", email);
    }

    private HttpHeaders buildAuthenticatedHeaders() {
        HttpHeaders headers = new HttpHeaders();
        if (sharedData.containsKey("merchantToken")) {
            headers.setBearerAuth(sharedData.get("merchantToken"));
        } else {
            logger.warn("No merchant token found in sharedData for authenticated request!");
        }
        return headers;
    }

    @When("a GET request is made to {string} with an authenticated merchant")
    public void a_get_request_is_made_to_with_auth_merchant(String path) {
        HttpHeaders headers = buildAuthenticatedHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        latestResponse = restTemplate.exchange(apiBaseUrl + path, HttpMethod.GET, entity, String.class);
        logger.info("Authenticated Merchant GET to {}: Status {}, Body {}", path, latestResponse.getStatusCodeValue(), latestResponse.getBody());
    }

    @When("a GET request is made to {string}") // Unauthenticated
    public void a_get_request_is_made_to(String path) {
        HttpEntity<Void> entity = new HttpEntity<>(new HttpHeaders());
        latestResponse = restTemplate.exchange(apiBaseUrl + path, HttpMethod.GET, entity, String.class);
        logger.info("Unauthenticated GET to {}: Status {}, Body {}", path, latestResponse.getStatusCodeValue(), latestResponse.getBody());
    }

    // --- Then Steps (Common) ---
    @Then("the response status code should be {int}")
    public void the_response_status_code_should_be(Integer statusCode) {
        assertThat(latestResponse.getStatusCodeValue()).isEqualTo(statusCode);
    }

    @Then("the response body should contain a {string}") // Check for key existence
    public void the_response_body_should_contain_a_key(String jsonPath) {
        assertThat(latestResponse.getBody()).isNotNull();
        com.jayway.jsonpath.JsonPath.read(latestResponse.getBody(), "$." + jsonPath);
    }

    @Then("the response body should be a list")
    public void the_response_body_should_be_a_list() {
        assertThat(latestResponse.getBody()).isNotNull();
        List<?> list = com.jayway.jsonpath.JsonPath.parse(latestResponse.getBody()).read("$");
        assertThat(list).isInstanceOf(List.class);
    }
}
