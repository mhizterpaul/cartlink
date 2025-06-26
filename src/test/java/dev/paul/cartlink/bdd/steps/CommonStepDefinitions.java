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
    }
}
