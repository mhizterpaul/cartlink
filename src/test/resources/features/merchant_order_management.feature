Feature: Merchant Order Management
  # Tests for merchants managing their orders.
  # Base path: /api/v1/merchants/{merchantId}/orders

  Background:
    Given the API base URL is "/api/v1"
    And a merchant is logged in with email "order.merchant@example.com" and password "OrderPass123!"
    # Merchant ID will be stored as "merchantId" in sharedData by the login step.
    # Assume a product and a customer exist for creating orders.
    And a customer "order.customer@example.com" exists
    And merchant "order.merchant@example.com" has a product "OrderableProduct" with price 10.00 and stock 50, and its merchantProductId is stored as "orderable_mpid"

  Scenario: List Merchant's Orders (initially empty or after setup)
    When a GET request is made to "/merchants/{merchantId}/orders" with an authenticated merchant
    Then the response status code should be 200
    And the response body should be a list # Potentially empty if no orders created yet for this merchant

  Scenario: Merchant Updates Order Status
    # Precondition: An order must exist for this merchant.
    # This requires creating an order through customer place order or direct setup.
    Given an order with ID "1" exists for merchant "order.merchant@example.com" with initial status "PENDING"
    When a PUT request is made to "/merchants/{merchantId}/orders/1/status" with an authenticated merchant and the following body:
      """
      {
        "status": "PROCESSING"
      }
      """
    Then the response status code should be 200
    And the response body should contain "success" with boolean value "true"
    And the response body should contain "newStatus" with value "PROCESSING"

  Scenario: Merchant Marks Order as Delivered
    Given an order with ID "2" exists for merchant "order.merchant@example.com" with initial status "SHIPPED" # Or PAID
    When a PATCH request is made to "/merchants/{merchantId}/orders/2/delivered" with an authenticated merchant
    Then the response status code should be 200
    And the response body should contain "success" with boolean value "true"
    And the response body should contain "newStatus" with value "DELIVERED"
    # And potentially a message about merchant payment

  Scenario: Attempt to Update Status of Non-existent Order
    When a PUT request is made to "/merchants/{merchantId}/orders/99999/status" with an authenticated merchant and the following body:
      """
      {
        "status": "SHIPPED"
      }
      """
    Then the response status code should be 400 # Or 404, controller uses IllegalArgumentException
    And the response body should contain an "error" field

  Scenario: Attempt to Mark Non-existent Order as Delivered
    When a PATCH request is made to "/merchants/{merchantId}/orders/99998/delivered" with an authenticated merchant
    Then the response status code should be 400 # Or 404
    And the response body should contain an "error" field

  # Scenario: Merchant Views Orders by Link
  #   Path: /api/v1/merchants/{merchantId}/orders/link/{linkId}
  #   This requires a product link to be generated and an order associated with it.
  #   Given product links are not implemented, this is hard to test.
  #   If I assume the path is GET /merchants/{merchantId}/orders/link/{linkId} (relative to controller base):
  #   Given a product link with ID "plink123" exists
  #   And an order is associated with product link "plink123"
  #   When a GET request is made to "/merchants/{merchantId}/orders/link/plink123" with an authenticated merchant
  #   Then the response status code should be 200
  #   And the response body should be a list of orders associated with that link.
  #   Due to the problematic path in controller and dependency on unimplemented link feature, this is marked as difficult.

  # Note: Pagination for List Orders is mentioned in controller (TODO) but not fully tested here.
  # Note: Filtering orders by status or date range is also possible but not exhaustively covered.Tool output for `create_file_with_block`:
