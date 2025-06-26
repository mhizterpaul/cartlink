Feature: Payment Processing
  # Tests for initiating payments and processing refunds.
  # Base path: /api/v1/payments

  Background:
    Given the API base URL is "/api/v1"
    # Assume an order exists for payment initiation/refund.
    # Customer and Merchant context might be implicit via the order.
    And a customer "payment.user@example.com" exists
    And a merchant "payment.merchant@example.com" exists
    And this merchant has a product "PaymentProduct" with original ProductID "pp1" price 50.00 and stock 5, whose MerchantProductID is "payment_mpid"
    And an order with ID "pay_order_1" exists for customer "payment.user@example.com" involving merchantProduct "payment_mpid" with total price 50.00 and its actual ID is stored as "paymentOrderId"

  Scenario: Initiate Payment Successfully
    # Payment initiation uses @RequestParam, not JSON body.
    When a POST form request is made to "/payments/initiate" with the following parameters:
      | orderId  | {paymentOrderId}    |
      | method   | CARD                |
      | amount   | 50.00               |
      | currency | NGN                 |
      | txRef    | TX-PAY-{paymentOrderId} |
    Then the response status code should be 200
    And the response body should contain a "paymentId"
    And the response body should contain "order.orderId" with value "{paymentOrderId}" # Check if value is string or number
    And the response body should contain "method" with value "CARD"
    And the response body should contain "status" with value "PENDING" # Initial status
    And the response body should contain "amount" with number value "50.00"
    And the response body should contain "currency" with value "NGN"
    And the response body should contain "txRef" with value "TX-PAY-{paymentOrderId}"

  Scenario: Initiate Payment for Non-existent Order
    When a POST form request is made to "/payments/initiate" with the following parameters:
      | orderId  | 99999             | # Non-existent order
      | method   | CARD              |
      | amount   | 20.00             |
      | currency | NGN               |
      | txRef    | TX-PAY-99999      |
    Then the response status code should be 400
    And the response body should be the string "Order not found" # Controller returns plain string

  Scenario: Initiate Payment with Missing Required Parameter (e.g., amount)
    When a POST form request is made to "/payments/initiate" with the following parameters:
      | orderId  | {paymentOrderId}    |
      | method   | CARD                |
      | currency | NGN                 |
      | txRef    | TX-MISSING-{paymentOrderId} |
      # "amount" is missing
    Then the response status code should be 400 # Spring usually handles missing @RequestParam

  Scenario: Trigger Refund for an Order Successfully
    # This endpoint in controller seems to trigger a general refund process, not specific to the orderId in path for refund logic itself.
    # The call paymentService.autoRefundStaleOrders() suggests it's a batch/demo trigger.
    # The response is hardcoded: "Refund process triggered (see logs for actual refund)"
    When a POST request is made to "/payments/refund/{paymentOrderId}" with no body
    Then the response status code should be 200
    And the response body should be the string "Refund process triggered (see logs for actual refund)"

  Scenario: Trigger Refund for Non-existent Order
    # Even if order doesn't exist, the current controller logic for refund might still return 200
    # because it calls a generic service method not strictly tied to the path variable for its main logic.
    # This test will verify the actual behavior.
    When a POST request is made to "/payments/refund/99998" with no body # Non-existent order
    Then the response status code should be 200 # Based on current controller impl.
    And the response body should be the string "Refund process triggered (see logs for actual refund)"

  # Authentication for these payment endpoints is not explicitly defined in the controller
  # (e.g., no @AuthenticationPrincipal). This implies they might be unsecured or secured at a
  # higher level (e.g., gateway, or expecting some form of service-to-service auth not visible here).
  # The tests will run them without explicit client-side auth tokens for now.
  # If they are secured, these tests will fail with 401/403, highlighting the need for auth. Tool output for `create_file_with_block`:
