package dev.rugved.FSJDSwiggy.stepdefinitions;

import io.cucumber.java.en.When;
import io.cucumber.java.en.And;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.springframework.beans.factory.annotation.Autowired;

import static io.restassured.RestAssured.given; // Keep for direct use if needed
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GeneralApiStepDefinitions {

    // Removed: response, request, baseUrl, initializeRequestIfNeeded

    @Autowired
    private ScenarioContext scenarioContext;


    @When("I send a POST request to {string} with the token, body:")
    public void i_send_a_post_request_to_with_the_token_body(String endpoint, String requestBody) {
        RequestSpecification rs = (RequestSpecification) scenarioContext.getContext("REQUEST_SPEC");
        assertNotNull(rs, "RequestSpecification not found in ScenarioContext. Ensure CommonStepDefinitions @Before hook ran and user is logged in.");

        Response response = rs
                    .contentType(ContentType.JSON)
                    .body(requestBody)
                    .when()
                    .post(endpoint);
        scenarioContext.setContext("LAST_RESPONSE", response);
    }

    @And("without a {string} header")
    public void without_a_header(String headerName) {
        // This step will now store information in scenarioContext.
        // The @When step that follows will be responsible for using this information.
        scenarioContext.setContext("OMIT_HEADER_" + headerName.toUpperCase().replace("-", "_"), true);
        System.out.println("GeneralStepDef: Flag set to OMIT header: " + headerName);
    }

    @And("with header {string} as {string}")
    public void with_header_as(String headerName, String headerValue) {
        // This step will now store information in scenarioContext.
        // The @When step that follows will be responsible for using this information.
        scenarioContext.setContext("CUSTOM_HEADER_" + headerName.toUpperCase().replace("-", "_"), headerValue);
        System.out.println("GeneralStepDef: Flag set for CUSTOM header: " + headerName + "=" + headerValue);
    }

    @When("I send a POST request to {string} with the token and potentially modified headers and body:")
    public void i_send_a_post_request_to_with_token_modified_headers_body(String endpoint, String requestBody) {
        RequestSpecification rs = (RequestSpecification) scenarioContext.getContext("REQUEST_SPEC");
        assertNotNull(rs, "RequestSpecification not found in ScenarioContext.");

        // Create a mutable copy of the request spec from context to add/modify headers for this request only
        RequestSpecification tempRs = given().spec(rs); // Copies the spec

        // Check for Content-Type omission
        if (Boolean.TRUE.equals(scenarioContext.getContext("OMIT_HEADER_CONTENT_TYPE"))) {
            // RestAssured adds Content-Type automatically if there's a body.
            // True omission is hard. This might be more about *not explicitly setting* it.
            // For now, this means we don't call tempRs.contentType().
            System.out.println("GeneralStepDef: Attempting to send without explicit Content-Type.");
        } else {
            // Check for custom Content-Type
            String customContentType = (String) scenarioContext.getContext("CUSTOM_HEADER_CONTENT_TYPE");
            if (customContentType != null) {
                tempRs.contentType(customContentType);
                System.out.println("GeneralStepDef: Using custom Content-Type: " + customContentType);
            } else {
                tempRs.contentType(ContentType.JSON); // Default if not omitted or customized
                System.out.println("GeneralStepDef: Using default Content-Type: JSON.");
            }
        }

        // Example for other custom headers (this would need to be more generic for many headers)
        String customAccept = (String) scenarioContext.getContext("CUSTOM_HEADER_ACCEPT");
        if (customAccept != null) {
            tempRs.header("Accept", customAccept);
        }
        // ... (add logic for other specific custom headers if needed, or a loop for generic custom headers)

        Response response = tempRs.body(requestBody).when().post(endpoint);
        scenarioContext.setContext("LAST_RESPONSE", response);

        // Clean up context flags for headers for this scenario
        scenarioContext.removeContext("OMIT_HEADER_CONTENT_TYPE");
        scenarioContext.removeContext("CUSTOM_HEADER_CONTENT_TYPE");
        scenarioContext.removeContext("CUSTOM_HEADER_ACCEPT"); // Example cleanup
    }


    @When("I send a GET request to {string} with the token for general API tests")
    public void i_send_a_get_request_to_with_the_token_for_general_api_tests(String endpoint) {
        RequestSpecification rs = (RequestSpecification) scenarioContext.getContext("REQUEST_SPEC");
        assertNotNull(rs, "RequestSpecification not found in ScenarioContext. Ensure CommonStepDefinitions @Before hook ran and user is logged in.");

        Response response = rs.when().get(endpoint);
        scenarioContext.setContext("LAST_RESPONSE", response);
    }
}
