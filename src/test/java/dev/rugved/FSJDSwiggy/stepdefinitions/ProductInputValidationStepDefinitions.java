package dev.rugved.FSJDSwiggy.stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

// Removed: import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

public class ProductInputValidationStepDefinitions {

    // Removed: response, request, baseUrl fields
    // Removed: initializeRequestIfNeeded method

    @Autowired
    private ScenarioContext scenarioContext;


    @When("I send a POST request to {string} with the token and product body:")
    public void i_send_a_post_request_to_with_the_token_and_product_body(String endpoint, String requestBody) {
        RequestSpecification rs = (RequestSpecification) scenarioContext.getContext("REQUEST_SPEC");
        assertNotNull(rs, "RequestSpecification not found in ScenarioContext.");
        // Token should be on rs from login in CommonStepDefinitions.

        Response response = rs
                    .contentType(ContentType.JSON)
                    .body(requestBody)
                    .when()
                    .post(endpoint);
        scenarioContext.setContext("LAST_RESPONSE", response);
    }

    // REMOVED: @Then("the response body should indicate {string}") - Moved to CommonStepDefinitions


    @Then("the created product with name {string} should have its name sanitized when retrieved")
    public void the_created_product_with_name_should_have_its_name_sanitized_when_retrieved(String originalName) {
        Response createResponse = (Response) scenarioContext.getContext("LAST_RESPONSE");
        assertNotNull(createResponse, "LAST_RESPONSE is null, product creation step might have failed or not run.");
        assertEquals(201, createResponse.getStatusCode(), "Product creation failed. Response: " + createResponse.asString());

        String productId = createResponse.jsonPath().getString("productId");
        String merchantId = createResponse.jsonPath().getString("merchantId");

        if (merchantId == null) {
            String loggedInUserAlias = (String) scenarioContext.getContext("LOGGED_IN_USER_ALIAS");
            if (loggedInUserAlias != null) { // Check if loggedInUserAlias is not null
                 merchantId = (String) scenarioContext.getContext(loggedInUserAlias + "_ID");
            }
             if (merchantId == null && loggedInUserAlias != null && loggedInUserAlias.equals("input_validator_merchant")) {
                merchantId = "validator_mid";
            }
        }
        assertNotNull(productId, "productId not found in creation response or context. Response: " + createResponse.asString());
        assertNotNull(merchantId, "merchantId not found in creation response or context (tried alias and default). Response: " + createResponse.asString());

        RequestSpecification rs = (RequestSpecification) scenarioContext.getContext("REQUEST_SPEC");
        assertNotNull(rs, "RequestSpecification not found in ScenarioContext for retrieving product.");

        String getEndpoint = "/merchants/" + merchantId + "/products/" + productId;
        Response getResponse = rs.when().get(getEndpoint);

        assertEquals(200, getResponse.getStatusCode(), "Failed to retrieve created product " + productId + ". Endpoint: " + getEndpoint + ". Response: " + getResponse.asString());
        String retrievedName = getResponse.jsonPath().getString("name");
        String retrievedDescription = getResponse.jsonPath().getString("productDetails.description");


        assertNotNull(retrievedName, "Retrieved product name is null.");
        assertFalse(retrievedName.contains("<script>"), "Retrieved product name appears to contain raw script tags: " + retrievedName);

        // Check if originalName implies XSS attempt and verify specific sanitization
        if (originalName.contains("<script>")) {
            assertTrue(retrievedName.contains("&lt;script&gt;"),
                       "Retrieved product name '" + retrievedName + "' was not sanitized as expected (should contain &lt;script&gt;). Original: '" + originalName + "'");
        } else {
            // If no script tags in original, it should match (or be handled by other forms of sanitization if any)
            // This part might need adjustment based on whether non-XSS names are also modified by sanitization
             assertEquals(originalName, retrievedName, "Retrieved product name '" + retrievedName + "' does not match original '" + originalName + "' when no XSS was present.");
        }

        String originalDescription = "Test <img src=x onerror=alert('XSS')>";
        if (retrievedDescription != null && originalDescription.contains("<img")) { // Only check if original had the img tag
            assertFalse(retrievedDescription.contains("<img src=x onerror=alert('XSS')>"), "Retrieved description appears to contain raw img tag with onerror: " + retrievedDescription);
            assertTrue(retrievedDescription.contains("&lt;img src=x onerror=alert('XSS')&gt;"),
                   "Retrieved product description '" + retrievedDescription + "' was not sanitized as expected for img tag.");
        }
    }

    @When("I send a POST request to {string} for form generation with the token and body:")
    public void i_send_a_post_request_to_for_form_generation_with_the_token_and_body(String endpoint, String requestBody) {
        RequestSpecification rs = (RequestSpecification) scenarioContext.getContext("REQUEST_SPEC");
        assertNotNull(rs, "RequestSpecification not found in ScenarioContext.");

        Response response = rs
                    .contentType(ContentType.JSON)
                    .body(requestBody)
                    .when()
                    .post(endpoint);
        scenarioContext.setContext("LAST_RESPONSE", response);
    }

    @Then("the response content type should be {string}")
    public void the_response_content_type_should_be(String expectedContentType) {
        Response lastResponse = (Response) scenarioContext.getContext("LAST_RESPONSE");
        assertNotNull(lastResponse, "Response was null.");
        assertEquals(expectedContentType, lastResponse.getContentType(), "Content-Type did not match.");
    }

    @Then("the response body should not contain the raw string {string}")
    public void the_response_body_should_not_contain_the_raw_string(String rawString) {
        Response lastResponse = (Response) scenarioContext.getContext("LAST_RESPONSE");
        assertNotNull(lastResponse, "Response was null.");
        String responseBody = lastResponse.getBody().asString();
        assertFalse(responseBody.contains(rawString), "Response body unexpectedly contained raw string: " + rawString);
    }

    @Then("the response body should contain a sanitized version like {string}")
    public void the_response_body_should_contain_a_sanitized_version_like(String sanitizedString) {
        Response lastResponse = (Response) scenarioContext.getContext("LAST_RESPONSE");
        assertNotNull(lastResponse, "Response was null.");
        String responseBody = lastResponse.getBody().asString();
        assertTrue(responseBody.contains(sanitizedString), "Response body did not contain sanitized string: '" + sanitizedString + "'. Body: " + responseBody);
    }

    @Given("I have a CSV file {string} exceeding {int}MB")
    public void i_have_a_csv_file_exceeding_mb(String fileName, Integer sizeMb) throws IOException {
        // Ensure temp directory exists or handle potential creation issues
        Path tempDir = Files.createTempDirectory("cucumber_temp_files");
        Path tempFile = Files.createTempFile(tempDir, fileName.split("\\.")[0], "." + fileName.split("\\.")[1]);
        long sizeBytes = sizeMb * 1024L * 1024L;
        Files.write(tempFile, new byte[(int) (sizeBytes + 1000)]);
        scenarioContext.setContext("FILE_TO_UPLOAD_PATH", tempFile.toAbsolutePath().toString());
        scenarioContext.setContext("FILE_TO_UPLOAD_NAME", fileName);
        // Register for cleanup if possible, or ensure cleanup in @After hook
        scenarioContext.setContext("TEMP_FILE_TO_CLEAN_" + fileName, tempFile.toAbsolutePath().toString());
    }

    @When("I send a POST multipart file request to {string} with the token and file {string}")
    public void i_send_a_post_multipart_file_request_to_with_the_token_and_file(String endpoint, String fileContextKeyOrName) {
        RequestSpecification rs = (RequestSpecification) scenarioContext.getContext("REQUEST_SPEC");
        assertNotNull(rs, "RequestSpecification not found in ScenarioContext.");

        String filePath = (String) scenarioContext.getContext("FILE_TO_UPLOAD_PATH");
        // String fileName = (String) scenarioContext.getContext("FILE_TO_UPLOAD_NAME");
        assertNotNull(filePath, "File path for '" + fileContextKeyOrName + "' not found in scenario context. Ensure the Given step for file creation ran.");

        File fileToUpload = new File(filePath);
        assertTrue(fileToUpload.exists(), "File to upload does not exist: " + filePath);

        Response response = rs
                    .multiPart("file", fileToUpload) // "file" is the typical param name for file uploads
                    .when()
                    .post(endpoint);
        scenarioContext.setContext("LAST_RESPONSE", response);

        // Temp file cleanup is important. Could be done in an @After hook in CommonStepDefinitions
        // by iterating over keys starting with "TEMP_FILE_TO_CLEAN_".
        // For now, direct cleanup here:
        try {
            Files.deleteIfExists(fileToUpload.toPath());
            Path parentDir = fileToUpload.toPath().getParent();
            if (parentDir.toString().contains("cucumber_temp_files") && Files.isDirectory(parentDir) && Files.list(parentDir).findAny().isEmpty()) {
                Files.deleteIfExists(parentDir);
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not delete temp file/dir: " + filePath + " - " + e.getMessage());
        }
    }

    @Given("I have a text file {string} with content {string}")
    public void i_have_a_text_file_with_content(String fileName, String content) throws IOException {
        Path tempDir = Files.createTempDirectory("cucumber_temp_files");
        Path tempFile = Files.createTempFile(tempDir, fileName.split("\\.")[0], "." + fileName.split("\\.")[1]);
        Files.writeString(tempFile, content);
        scenarioContext.setContext("FILE_TO_UPLOAD_PATH", tempFile.toAbsolutePath().toString());
        scenarioContext.setContext("FILE_TO_UPLOAD_NAME", fileName);
        scenarioContext.setContext("TEMP_FILE_TO_CLEAN_" + fileName, tempFile.toAbsolutePath().toString());
    }
}
