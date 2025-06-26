package dev.paul.cartlink.bdd.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.paul.cartlink.link.model.LinkAnalytics;
import dev.paul.cartlink.link.repository.LinkAnalyticsRepository;

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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class AnalyticsStepDefinitions {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsStepDefinitions.class);

    @Autowired private TestRestTemplate restTemplate;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private LinkAnalyticsRepository linkAnalyticsRepository;

    private String apiBaseUrl;
    private ResponseEntity<String> latestResponse;
    private Map<String, String> sharedData = new HashMap<>();

    @Before
    public void setUp() {
        linkAnalyticsRepository.deleteAll(); // Clear analytics before each scenario
        sharedData.clear();
        logger.info("AnalyticsStepDefinitions: Cleared link_analytics repository and sharedData.");
    }

    @After
    public void tearDown() {}

    @Given("the API base URL is {string}")
    public void the_api_base_url_is(String baseUrl) {
        this.apiBaseUrl = baseUrl;
    }

    @Given("a LinkAnalytics entity exists with ID {string} and its actual ID is stored as {string}")
    public void a_link_analytics_entity_exists_stored_as(String symbolicId, String sharedKey) {
        LinkAnalytics analytics = new LinkAnalytics();
        // Set some default values, actual values aren't critical for testing endpoint functionality
        analytics.setGeolocation("Initial Geolocation");
        analytics.setBounceRate(50.0);
        analytics.setAverageTimeSpent(60L); // 60 seconds
        analytics.setLastUpdated(LocalDateTime.now());
        // Other fields like totalUniqueClicks default to 0 as per entity.

        LinkAnalytics savedAnalytics = linkAnalyticsRepository.save(analytics);
        sharedData.put(sharedKey, savedAnalytics.getAnalyticsId().toString());
        logger.info("Created LinkAnalytics entity with actual ID {}, stored as {}. Symbolic test ID was {}",
                    savedAnalytics.getAnalyticsId(), sharedKey, symbolicId);
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

    @When("a GET request is made to {string}")
    public void a_get_request_is_made_to(String path) {
        HttpEntity<Void> entity = new HttpEntity<>(new HttpHeaders()); // No auth needed as per controller
        latestResponse = restTemplate.exchange(apiBaseUrl + resolvePlaceholders(path), HttpMethod.GET, entity, String.class);
        logger.info("GET to {}: Status {}, Body {}", resolvePlaceholders(path), latestResponse.getStatusCodeValue(), latestResponse.getBody());
    }

    @When("a POST request is made to {string} with the following body:")
    public void a_post_request_is_made_to_with_body(String path, String requestBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // No auth needed as per controller
        HttpEntity<String> entity = new HttpEntity<>(resolvePlaceholders(requestBody), headers); // Body might contain placeholders if needed
        latestResponse = restTemplate.postForEntity(apiBaseUrl + resolvePlaceholders(path), entity, String.class);
        logger.info("POST to {}: Status {}, Body {}", resolvePlaceholders(path), latestResponse.getStatusCodeValue(), latestResponse.getBody());
    }

    // --- Then Steps ---
    @Then("the response status code should be {int}")
    public void the_response_status_code_should_be(Integer statusCode) {
        assertThat(latestResponse.getStatusCodeValue()).isEqualTo(statusCode);
    }

    @Then("the response body should contain a {string} with number value {string}")
    public void the_response_body_should_contain_with_number_value(String jsonPath, String expectedValueKey) {
        assertThat(latestResponse.getBody()).isNotNull();
        // Resolve expected value if it's a placeholder (e.g. "{analyticsTestId}")
        String resolvedExpectedValueStr = resolvePlaceholders(expectedValueKey);

        Object actualObject = com.jayway.jsonpath.JsonPath.read(latestResponse.getBody(), "$." + jsonPath);

        // Handle comparison flexibly for numbers (Integer, Long, Double)
        BigDecimal actualValue = new BigDecimal(actualObject.toString());
        BigDecimal expectedDecimalValue = new BigDecimal(resolvedExpectedValueStr);

        assertThat(actualValue).isEqualByComparingTo(expectedDecimalValue);
    }

    @Then("the response body should contain a {string}") // General key check
    public void the_response_body_should_contain_a_key(String jsonPath) {
        assertThat(latestResponse.getBody()).isNotNull();
        com.jayway.jsonpath.JsonPath.read(latestResponse.getBody(), "$." + jsonPath);
    }
}
