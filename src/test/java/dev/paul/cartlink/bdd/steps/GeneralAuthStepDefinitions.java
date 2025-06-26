package dev.paul.cartlink.bdd.steps;

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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class GeneralAuthStepDefinitions {

    private static final Logger logger = LoggerFactory.getLogger(GeneralAuthStepDefinitions.class);

    @Autowired private TestRestTemplate restTemplate;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private MerchantRepository merchantRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private String apiBaseUrl;
    private ResponseEntity<String> latestResponse;
    // No sharedData needed specifically for tokens here as they are passed in requests

    @Before
    public void setUp() {
        // merchantRepository.deleteAll(); // Be cautious if other features create merchants
        logger.info("GeneralAuthStepDefinitions: Setup initiated.");
    }

    @After
    public void tearDown() {}

    @Given("the API base URL is {string}")
    public void the_api_base_url_is(String baseUrl) {
        this.apiBaseUrl = baseUrl;
    }

    @Given("a merchant {string} exists with password {string}")
    public void a_merchant_exists_with_password(String email, String password) {
        if (merchantRepository.findByEmail(email).isEmpty()) {
            Merchant merchant = new Merchant();
            merchant.setEmail(email);
            merchant.setPassword(passwordEncoder.encode(password));
            merchant.setFirstName(email.split("@")[0]);
            merchant.setLastName("User");
            // By default, email is not verified, no tokens set initially
            merchant.setEmailVerified(false);
            merchantRepository.save(merchant);
            logger.info("Ensured merchant {} exists with specified password.", email);
        }
    }

    @Given("merchant {string} has an unverified email with verification token {string}")
    public void merchant_has_unverified_email_with_token(String email, String token) {
        Merchant merchant = merchantRepository.findByEmail(email)
            .orElseThrow(() -> new AssertionError("Merchant " + email + " not found for token setup."));
        merchant.setEmailVerified(false);
        merchant.setVerificationToken(token);
        merchant.setVerificationTokenExpiry(LocalDateTime.now().plusHours(24)); // Standard expiry
        merchantRepository.save(merchant);
        logger.info("Set verification token {} for merchant {}", token, email);
    }

    @Given("merchant {string} has a password reset token {string}")
    public void merchant_has_password_reset_token(String email, String token) {
        Merchant merchant = merchantRepository.findByEmail(email)
            .orElseThrow(() -> new AssertionError("Merchant " + email + " not found for password reset token setup."));
        merchant.setPasswordResetToken(token);
        merchant.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(1)); // Standard expiry
        merchantRepository.save(merchant);
        logger.info("Set password reset token {} for merchant {}", token, email);
    }

    @When("a GET request is made to {string}")
    public void a_get_request_is_made_to(String path) {
        HttpEntity<Void> entity = new HttpEntity<>(new HttpHeaders());
        latestResponse = restTemplate.exchange(apiBaseUrl + path, HttpMethod.GET, entity, String.class);
        logger.info("GET to {}: Status {}, Body (partial): {}", path, latestResponse.getStatusCodeValue(), latestResponse.getBody() != null ? latestResponse.getBody().substring(0, Math.min(100, latestResponse.getBody().length())) : "null");
    }

    @When("a POST request is made to {string} with the following body:")
    public void a_post_request_is_made_to_with_body(String path, String requestBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        latestResponse = restTemplate.postForEntity(apiBaseUrl + path, entity, String.class);
        logger.info("POST to {}: Status {}, Body: {}", path, latestResponse.getStatusCodeValue(), latestResponse.getBody());
    }

    // --- Then Steps ---
    @Then("the response status code should be {int}")
    public void the_response_status_code_should_be(Integer statusCode) {
        assertThat(latestResponse.getStatusCodeValue()).isEqualTo(statusCode);
    }

    @Then("the response body should be the string {string}")
    public void the_response_body_should_be_the_string(String expectedBody) {
        assertThat(latestResponse.getBody()).isEqualTo(expectedBody);
    }

    @Then("the response content type should be {string}")
    public void the_response_content_type_should_be(String expectedContentType) {
        MediaType mediaType = latestResponse.getHeaders().getContentType();
        assertThat(mediaType).isNotNull();
        assertThat(mediaType.toString()).containsIgnoringCase(expectedContentType); // Use contains for text/html;charset=UTF-8
    }

    @Then("the response body should contain {string}")
    public void the_response_body_should_contain_string(String expectedSubstring) {
        assertThat(latestResponse.getBody()).isNotNull();
        assertThat(latestResponse.getBody()).contains(expectedSubstring);
    }
}
