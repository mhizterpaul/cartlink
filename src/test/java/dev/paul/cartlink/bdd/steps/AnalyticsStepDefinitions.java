package dev.paul.cartlink.bdd.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.paul.cartlink.link.model.LinkAnalytics;
import dev.paul.cartlink.link.repository.LinkAnalyticsRepository;
import dev.paul.cartlink.bdd.context.ScenarioContext; // Keep this one

// import dev.paul.cartlink.bdd.ScenarioContext; // Remove this duplicate
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
    @Autowired private ScenarioContext scenarioContext;


    private ResponseEntity<String> latestResponse;
    // private Map<String, String> sharedData = new HashMap<>(); // Will use scenarioContext instead

    @Before
    public void setUp() {
        linkAnalyticsRepository.deleteAll(); // Clear analytics before each scenario
        // scenarioContext.clear(); // Shared context, clear relevant parts or manage carefully
        logger.info("AnalyticsStepDefinitions: Cleared link_analytics repository.");
    }

    @After
    public void tearDown() {}


    // Removed duplicate @Given("the API base URL is {string}")

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
        scenarioContext.set(sharedKey, savedAnalytics.getAnalyticsId().toString());
        logger.info("Created LinkAnalytics entity with actual ID {}, stored as {}. Symbolic test ID was {}",
                    savedAnalytics.getAnalyticsId(), sharedKey, symbolicId);
    }

    private String resolvePlaceholders(String valueWithPlaceholders) {
        String resolvedValue = valueWithPlaceholders;
        // Example for resolving: if valueWithPlaceholders is "/analytics/{analyticsTestId}"
        // and scenarioContext has "analyticsTestId" -> "123", it becomes "/analytics/123"
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\{([^}]+)\\}");
        java.util.regex.Matcher matcher = pattern.matcher(resolvedValue);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1);
            Object value = scenarioContext.get(key, Object.class); // Get as Object first
            if (value != null) {
                matcher.appendReplacement(sb, value.toString());
            } else {
                logger.warn("Placeholder {} not found in scenarioContext", key);
                // Decide how to handle missing placeholders: throw error, or leave as is
                // For now, leave as is, which might lead to request errors if path is malformed
            }
        }
        matcher.appendTail(sb);
        return sb.toString();
    }


    @When("a GET request is made to {string}")
    public void a_get_request_is_made_to(String path) {
        HttpEntity<Void> entity = new HttpEntity<>(new HttpHeaders()); // No auth needed as per controller
        String resolvedPath = resolvePlaceholders(path);
        String baseUrl = scenarioContext.getString("apiBaseUrl");
        latestResponse = restTemplate.exchange(baseUrl + resolvedPath, HttpMethod.GET, entity, String.class);
        logger.info("GET to {}: Status {}, Body {}", resolvedPath, latestResponse.getStatusCodeValue(), latestResponse.getBody());
    }

    @When("a POST request is made to {string} with the following body:")
    public void a_post_request_is_made_to_with_body(String path, String requestBody) {
        String apiBaseUrl = scenarioContext.getString("apiBaseUrl");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // No auth needed as per controller
        String resolvedPath = resolvePlaceholders(path);
        String resolvedBody = resolvePlaceholders(requestBody);
        String baseUrl = scenarioContext.getString("apiBaseUrl");
        HttpEntity<String> entity = new HttpEntity<>(resolvedBody, headers);
        latestResponse = restTemplate.postForEntity(baseUrl + resolvedPath, entity, String.class);
        logger.info("POST to {}: Status {}, Body {}", resolvedPath, latestResponse.getStatusCodeValue(), latestResponse.getBody());
    }

    // --- Then Steps ---
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
