Feature: Analytics Endpoints
  # Tests for generic analytics endpoints.
  # Base path: /api/analytics (matches LinkAnalyticsController)

  Background:
    Given the API base URL is "/api"
    # Precondition: An analytics entity must exist to be updated or fetched.
    # This requires creating a LinkAnalytics entity directly or through another API if possible.
    # For now, assume an entity can be created with a known ID for testing.
    And a LinkAnalytics entity exists with ID "la_1" and its actual ID is stored as "analyticsTestId"

  Scenario: Get Analytics Successfully
    When a GET request is made to "/analytics/{analyticsTestId}"
    Then the response status code should be 200
    And the response body should contain a "id" with number value "{analyticsTestId}" # Assuming id is returned and is numeric
    # And the response body should contain other expected analytics fields like "geolocation", "bounceRate" etc.

  Scenario: Get Analytics for Non-existent ID
    When a GET request is made to "/analytics/99999" # Non-existent ID
    Then the response status code should be 404 # Controller returns NotFound

  Scenario: Update Analytics Successfully
    When a POST request is made to "/analytics/{analyticsTestId}" with the following body:
      """
      {
        "geolocation": "New York, USA",
        "bounceRate": 60.5,
        "averageTimeSpent": 120
      }
      """
      # Note: Controller uses POST for update. API_REQUIREMENTS.md also says POST.
    Then the response status code should be 200 # Controller returns OK
    # And a subsequent GET for "/analytics/{analyticsTestId}" should reflect these updated values.
    # For simplicity here, just checking the update call was successful.

  Scenario: Update Analytics for Non-existent ID
    When a POST request is made to "/analytics/99998" with the following body: # Non-existent ID
      """
      {
        "bounceRate": 50.0
      }
      """
    Then the response status code should be 404

  Scenario: Update Analytics with Partial Data
    # Precondition: Ensure analyticsTestId has some initial values for other fields.
    When a POST request is made to "/analytics/{analyticsTestId}" with the following body:
      """
      {
        "bounceRate": 75.2
      }
      """
      # Only bounceRate is updated. Geolocation and averageTimeSpent should remain as they were.
    Then the response status code should be 200
    # And a subsequent GET should show bounceRate as 75.2 and other fields unchanged.

  # Authentication for these analytics endpoints is not explicitly defined in LinkAnalyticsController
  # (no @AuthenticationPrincipal or security annotations).
  # Tests run without client-side auth tokens. If secured, they'll fail, highlighting the need for auth. Tool output for `create_file_with_block`:
