Feature: Customer Place Order
  # Tests for customers placing orders directly (not via cart checkout).
  # Endpoint: POST /api/v1/customers/orders

  Background:
    Given the API base URL is "/api/v1"
    # Assume a merchant, product, and merchantProduct exist for placing an order.
    And a merchant "order.merchant2@example.com" exists
    And this merchant has a product "DirectOrderProduct" with original ProductID "dop1" price 25.00 and stock 10, and its MerchantProductID is stored as "direct_order_mpid"

  Scenario: Logged-in Customer Places an Order Successfully
    Given a customer is logged in with email "direct.order.cust@example.com" and password "DirectPass123!"
    When a POST request is made to "/customers/orders" with an authenticated customer and the following body:
      """
      {
        "merchantProductId": "{direct_order_mpid}",
        "quantity": 2
      }
      """
      # productLinkId is optional
    Then the response status code should be 201
    And the response body should contain an "orderId"
    And the response body should contain "customer.email" with value "direct.order.cust@example.com"
    And the response body should contain "merchantProduct.id" with value "{direct_order_mpid}" # Check if ID is string or number
    And the response body should contain "quantity" with value "2"
    And the response body should contain "status" with value "PENDING" # Default initial status

  Scenario: Guest Customer Places an Order Successfully (New Guest)
    When a guest POST request is made to "/customers/orders" with the following body:
      """
      {
        "customer": {
          "email": "guest.new@example.com",
          "firstName": "Guest",
          "lastName": "UserNew"
          # Other customer details if required by getOrCreateCustomer
        },
        "merchantProductId": "{direct_order_mpid}",
        "quantity": 1
      }
      """
    Then the response status code should be 201
    And the response body should contain an "orderId"
    And the response body should contain "customer.email" with value "guest.new@example.com"
    And the response body should contain "quantity" with value "1"

  Scenario: Guest Customer Places an Order Successfully (Existing Guest Email)
    Given a customer "guest.existing@example.com" exists with first name "GuestExisting"
    When a guest POST request is made to "/customers/orders" with the following body:
      """
      {
        "customer": {
          "email": "guest.existing@example.com",
          "firstName": "GuestExisting", # May or may not match existing, service might update or ignore
          "lastName": "UserExisting"
        },
        "merchantProductId": "{direct_order_mpid}",
        "quantity": 3
      }
      """
    Then the response status code should be 201
    And the response body should contain "customer.email" with value "guest.existing@example.com"
    # And the response body should reflect that the existing customer was used (e.g., check customerId if returned)

  Scenario: Place Order for Non-existent MerchantProduct
    Given a customer is logged in with email "fail.order.cust@example.com" and password "FailPass123!"
    When a POST request is made to "/customers/orders" with an authenticated customer and the following body:
      """
      {
        "merchantProductId": "99999", # Non-existent
        "quantity": 1
      }
      """
    Then the response status code should be 400 # Or 404
    And the response body should contain an "error" field with a message like "MerchantProduct not found"

  Scenario: Place Order with Insufficient Stock
    # Assuming "direct_order_mpid" has stock 10 (from Background)
    Given a customer is logged in with email "stockout.cust@example.com" and password "StockPass123!"
    When a POST request is made to "/customers/orders" with an authenticated customer and the following body:
      """
      {
        "merchantProductId": "{direct_order_mpid}",
        "quantity": 11 # Exceeds stock
      }
      """
    Then the response status code should be 400
    And the response body should contain an "error" field with a message like "Insufficient stock" # Or similar

  Scenario: Place Order with Missing merchantProductId
    Given a customer is logged in with email "missing.field.cust@example.com" and password "MissingPass123!"
    When a POST request is made to "/customers/orders" with an authenticated customer and the following body:
      """
      {
        "quantity": 1
      }
      """
    Then the response status code should be 400
    And the response body should contain an "error" field # e.g. "merchantProductId is required"

  Scenario: Place Order with Missing Quantity
    Given a customer is logged in with email "missing.qty.cust@example.com" and password "MissingQtyPass!"
    When a POST request is made to "/customers/orders" with an authenticated customer and the following body:
      """
      {
        "merchantProductId": "{direct_order_mpid}"
      }
      """
    Then the response status code should be 400
    And the response body should contain an "error" field # e.g. "quantity is required"

  Scenario: Guest Customer Places Order with Missing Customer Email
    When a guest POST request is made to "/customers/orders" with the following body:
      """
      {
        "customer": {
          "firstName": "NoEmail",
          "lastName": "Guest"
        },
        "merchantProductId": "{direct_order_mpid}",
        "quantity": 1
      }
      """
    Then the response status code should be 400
    And the response body should contain an "error" field with a message like "Customer email is required for guest checkout"

  # Note: productLinkId is optional and not extensively tested here.
  # Further tests could involve different customer states (verified, not verified if applicable).
  # The controller logic for creating/finding guest customer (customerService.getOrCreateCustomer) behavior
  # with partial details could also be explored more.Tool output for `create_file_with_block`:
