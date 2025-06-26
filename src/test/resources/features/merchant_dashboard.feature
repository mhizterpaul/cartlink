Feature: Merchant Dashboard
  # Tests for merchant dashboard analytics and statistics.
  # Base path: /api/merchant/dashboard/... (Controller is at /api/merchant)

  Background:
    Given the API base URL is "/api" # Matching MerchantDashboardController
    And a merchant is logged in with email "dash.merchant@example.com" and password "DashPass123!"
    # Preconditions for dashboard data (orders, sales, traffic) would be complex to set up fully
    # via API in these BDD tests. Tests will primarily check for endpoint availability,
    # basic structure of response, and authentication.
    # Specific data values will be hard to assert without direct DB manipulation or extensive API setup.

  Scenario: Get Dashboard Stats Successfully
    When a GET request is made to "/merchant/dashboard/stats" with an authenticated merchant
    Then the response status code should be 200
    And the response body should contain a "totalSales"
    And the response body should contain a "totalOrders"
    And the response body should contain a "todaySales"
    And the response body should contain a "totalCustomers"
    And the response body should contain "analytics.totalSalesChange" # Example nested field

  Scenario: Get Dashboard Stats Without Authentication
    When a GET request is made to "/merchant/dashboard/stats" # No auth
    Then the response status code should be 401 # Or 403, based on security config

  Scenario: Get Sales Data Successfully (Weekly)
    When a GET request is made to "/merchant/dashboard/sales-data?period=week" with an authenticated merchant
    Then the response status code should be 200
    And the response body should be a list # List of sales data points

  Scenario: Get Sales Data Successfully (Monthly)
    When a GET request is made to "/merchant/dashboard/sales-data?period=month" with an authenticated merchant
    Then the response status code should be 200
    And the response body should be a list

  Scenario: Get Sales Data Successfully (Date Range)
    # Assuming date format YYYY-MM-DD from controller/service usage
    When a GET request is made to "/merchant/dashboard/sales-data?startDate=2023-01-01&endDate=2023-01-31" with an authenticated merchant
    Then the response status code should be 200
    And the response body should be a list

  Scenario: Get Sales Data Without Authentication
    When a GET request is made to "/merchant/dashboard/sales-data?period=week" # No auth
    Then the response status code should be 401 # Or 403

  Scenario: Get Traffic Data Successfully
    When a GET request is made to "/merchant/dashboard/traffic-data" with an authenticated merchant
    Then the response status code should be 200
    And the response body should be a list # List of traffic data points

  Scenario: Get Traffic Data Without Authentication
    When a GET request is made to "/merchant/dashboard/traffic-data" # No auth
    Then the response status code should be 401 # Or 403

  # Note: The actual content of sales/traffic data depends heavily on existing orders and analytics data
  # which is not being explicitly created in these BDD steps.
  # These tests primarily verify the endpoints are secured and return the expected structure (e.g., a list).
  # Asserting specific values would require a more controlled data setup environment. Tool output for `create_file_with_block`:
