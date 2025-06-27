package dev.paul.cartlink.bdd.steps;


import dev.paul.cartlink.bdd.ScenarioContext;
import io.cucumber.java.en.Given;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class CommonStepDefinitions {

    private static final Logger logger = LoggerFactory.getLogger(CommonStepDefinitions.class);


    @Autowired
    private ScenarioContext scenarioContext;


    @Given("the API base URL is {string}")
    public void the_api_base_url_is(String baseUrl) {
        scenarioContext.set("apiBaseUrl", baseUrl);
        logger.info("CommonStepDefinitions: API base URL set to: {}", baseUrl);


    @When("a POST request is made to {string} with the following body:")
    public void a_post_request_is_made_to_with_body(String path, String requestBody) {
        String apiBaseUrl = scenarioContext.getString("apiBaseUrl");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(apiBaseUrl + path, entity, String.class);
        scenarioContext.set("latestResponse", response); // Store response in ScenarioContext

        logger.info("COMMON POST request to {}{} with body: {}", apiBaseUrl, path, requestBody);
        logger.info("COMMON Response status: {}, body: {}", response.getStatusCode(), response.getBody());
    }

    @Then("the response status code should be {int}")
    public void the_response_status_code_should_be(Integer statusCode) {
        // Retrieve the ResponseEntity from ScenarioContext.
        // The type parameter for get() should match how it was stored.
        // Assuming it's stored as ResponseEntity<String>.
        @SuppressWarnings("unchecked") // ScenarioContext.get returns Object if class not specified, or T if specified.
        ResponseEntity<String> latestResponse = scenarioContext.get("latestResponse", ResponseEntity.class);

        if (latestResponse == null) {
            throw new IllegalStateException("No 'latestResponse' found in ScenarioContext. This step should run after a request has been made.");
        }
        assertThat(latestResponse.getStatusCodeValue()).isEqualTo(statusCode);
    }

    @When("a GET request is made to {string}")
    public void a_get_request_is_made_to(String path) {
        String apiBaseUrl = scenarioContext.getString("apiBaseUrl");
        // This is for unauthenticated GET requests.
        // If a path needs placeholder resolution, that should be handled by the calling step or a more specific common step.
        // For a truly generic GET, we assume 'path' is final or resolved before calling this.
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(apiBaseUrl + path, HttpMethod.GET, entity, String.class);
        scenarioContext.set("latestResponse", response);

        logger.info("COMMON GET request to {}{}", apiBaseUrl, path);
        logger.info("COMMON Response status: {}, body: {}", response.getStatusCode(), response.getBody());
    }

    @Then("the response body should contain {string} with value {string}")
    public void the_response_body_should_contain_with_value(String jsonPath, String expectedValue) {
        @SuppressWarnings("unchecked")
        ResponseEntity<String> latestResponse = (ResponseEntity<String>) scenarioContext.get("latestResponse");
        if (latestResponse == null) {
            throw new IllegalStateException("No 'latestResponse' found in ScenarioContext.");
        }
        String responseBody = latestResponse.getBody();
        assertThat(responseBody).isNotNull();
        // Resolve expectedValue if it's a placeholder like {someIdFromSharedData}
        // This basic version doesn't resolve placeholders in expectedValue from sharedData.
        // If that's needed, this step would require access to sharedData or a resolver.
        // For now, assuming expectedValue is literal or resolved by the caller's context if necessary.
        String actualValue = Objects.toString(com.jayway.jsonpath.JsonPath.read(responseBody, "$." + jsonPath), "");
        assertThat(actualValue).isEqualTo(expectedValue);

    }
}
