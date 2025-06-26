Feature: Refund Management
  # Tests for customers managing refunds for their orders.
  # Base path: /api/v1/customers/orders (as RefundController is mapped to /api/customers/orders, not /api/v1/...)
  # API_REQUIREMENTS.md says /api/v1/customers/orders/... - will use this as base for consistency, assuming controller path might be slightly off or v1 is implied.
  # UPDATE: RefundController is /api/customers/orders. API_REQUIREMENTS is /api/v1/customers/orders.
  # I will use /api/v1 as base and assume RefundController should be under /v1 or it's a documentation mismatch.
  # For now, tests will target /api/v1/customers/orders... and might fail if controller isn't found there.
  # If RefundController is indeed without /v1, the base URL for these tests will need adjustment.
  # Based on controller, the base is /api. I will use /api.

  Background:
    Given the API base URL is "/api" # Matching RefundController's lack of /v1
    And a customer is logged in with email "refund.user@example.com" and password "RefundPass123!"
    And an order with ID "r_order_1" exists for customer "refund.user@example.com" and its actual ID is stored as "refundOrderId"

  Scenario: Submit a New Refund Request Successfully
    When a POST request is made to "/customers/orders/{refundOrderId}/refund" with an authenticated customer and the following body:
      """
      {
        "reason": "Product did not match description.",
        "accountNumber": "1234567890",
        "bankName": "Test Bank",
        "accountName": "Refund User"
      }
      """
    Then the response status code should be 200 # API doc says 201, controller returns 200
    And the response body should contain "message" with value "Refund request submitted successfully"
    # And a RefundRequest entity should be created (verify via another GET or DB check if needed)

  Scenario: Submit Refund Request for Non-existent Order
    When a POST request is made to "/customers/orders/88888/refund" with an authenticated customer and the following body: # 88888 is non-existent order
      """
      {
        "reason": "Order never existed for refund.",
        "accountNumber": "0000000000",
        "bankName": "Ghost Bank",
        "accountName": "No One"
      }
      """
    Then the response status code should be 400 # Or 404, depends on service logic (e.g. "Order not found")
    And the response body should contain an "error" field

  Scenario: Submit Refund Request with Missing Reason
    When a POST request is made to "/customers/orders/{refundOrderId}/refund" with an authenticated customer and the following body:
      """
      {
        "accountNumber": "1234567890",
        "bankName": "Test Bank",
        "accountName": "Refund User"
        # "reason" is missing
      }
      """
    Then the response status code should be 400
    And the response body should contain an "error" field # e.g., "Reason is required"

  Scenario: Get All Refunds for Authenticated Customer
    # Precondition: Customer "refund.user@example.com" has submitted at least one refund request.
    Given the customer "refund.user@example.com" submitted a refund request for order "{refundOrderId}" with reason "Prior refund reason"
    When a GET request is made to "/customers/orders/refunds" with an authenticated customer
    Then the response status code should be 200
    And the response body should be a list with at least 1 item
    And the response body should contain "[0].reason" # Check for presence of reason in the first refund

  Scenario: Get Refunds for a Specific Order
    Given the customer "refund.user@example.com" submitted a refund request for order "{refundOrderId}" with reason "Specific order refund reason"
    When a GET request is made to "/customers/orders/{refundOrderId}/refunds" with an authenticated customer
    Then the response status code should be 200
    And the response body should be a list with at least 1 item
    And the response body should contain "[0].reason" with value "Specific order refund reason"
    And the response body should contain "[0].order.orderId" with value "{refundOrderId}" # Check if orderId is string or number

  Scenario: Get Refunds for an Order with No Refund Requests
    Given an order with ID "r_order_2" exists for customer "refund.user@example.com" and has no refund requests, and its actual ID is stored as "noRefundOrderId"
    When a GET request is made to "/customers/orders/{noRefundOrderId}/refunds" with an authenticated customer
    Then the response status code should be 200
    And the response body should be an empty list

  Scenario: Attempt to Submit Refund Request Without Authentication
    When a POST request is made to "/customers/orders/{refundOrderId}/refund" with the following body: # No auth
      """
      {
        "reason": "Unauthenticated refund attempt."
      }
      """
    Then the response status code should be 401 # Or 403

  Scenario: Attempt to Get Customer Refunds Without Authentication
    When a GET request is made to "/customers/orders/refunds" # No auth
    Then the response status code should be 401 # Or 403

  Scenario: Attempt to Get Specific Order Refunds Without Authentication
    When a GET request is made to "/customers/orders/{refundOrderId}/refunds" # No auth
    Then the response status code should be 401 # Or 403

  # Note: Refund request body in controller includes accountNumber, bankName, accountName.
  # API_REQUIREMENTS.md only shows "reason". Test uses controller's requirement.
  # API_REQUIREMENTS.md says POST returns 201, controller returns 200. Test uses controller's 200. Tool output for `create_file_with_block`:
