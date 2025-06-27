package dev.paul.cartlink.bdd.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.paul.cartlink.merchant.model.Merchant;
import dev.paul.cartlink.merchant.repository.MerchantRepository;
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

    @Autowired
    private ScenarioContext scenarioContext;

    private ResponseEntity<String> latestResponse;
    // No sharedData needed specifically for tokens here as they are passed in requests

    @Before
    public void setUp() {
        // merchantRepository.deleteAll(); // Be cautious if other features create merchants
        logger.info("GeneralAuthStepDefinitions: Setup initiated.");
    }

    @After
    public void tearDown() {}

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

    // --- Then Steps ---
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

    // This step is now in CommonStepDefinitions.java as "the response body should include the text {string}"
    // or "the response body should contain a {string}" for JSON path.
    // Feature files using this step need to be updated.
    // @Then("the response body should contain {string}")
    // public void the_response_body_should_contain_string(String expectedSubstring) {
    //     assertThat(latestResponse.getBody()).isNotNull();
    //     assertThat(latestResponse.getBody()).contains(expectedSubstring);
    // }
}
