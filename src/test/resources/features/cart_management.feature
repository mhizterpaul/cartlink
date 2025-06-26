Feature: Cart Management
  # Tests for cart operations. These endpoints rely on a "cart_cookie_id" cookie.

  Background:
    Given the API base URL is "/api/v1"
    And a new cart session is started # This step will ensure a cart_cookie_id is ready

  Scenario: View Empty Cart
    When a GET request is made to "/customers/cart" using the cart session
    Then the response status code should be 200
    And the response body should contain "cart.items" as an empty list
    And the response body should contain "cart.totalAmount" with value "0.0" # Or 0 depending on type

  Scenario: Add Item to Cart
    # Precondition: A product must exist. For now, assume product with ID 1 exists.
    # This will require more setup if products are not seeded or easily creatable.
    # Let's assume CartItemRequest takes productId and quantity.
    Given a product with ID 1 exists with price 10.00 and stock 5
    When a POST request is made to "/customers/cart/items" using the cart session with the following body:
      """
      {
        "productId": 1,
        "quantity": 2
      }
      """
    Then the response status code should be 201
    And the response body should contain "cart.items" as a list with 1 item
    And the response body should contain "cart.items[0].productId" with value "1"
    And the response body should contain "cart.items[0].quantity" with value "2"
    And the response body should contain "cart.totalAmount" with value "20.0" # Assuming price 10.00 * 2

  Scenario: Add Another Item to Cart
    Given a product with ID 1 exists with price 10.00 and stock 5
    And a product with ID 2 exists with price 5.50 and stock 10
    And the following item is added to the cart:
      | productId | quantity |
      | 1         | 1        |
    When a POST request is made to "/customers/cart/items" using the cart session with the following body:
      """
      {
        "productId": 2,
        "quantity": 3
      }
      """
    Then the response status code should be 201
    And the response body should contain "cart.items" as a list with 2 items
    And the response body should contain "cart.totalAmount" with value "26.5" # 10.00 + (5.50 * 3) = 10.00 + 16.50

  Scenario: Update Item Quantity in Cart
    Given a product with ID 1 exists with price 10.00 and stock 5
    And the following item is added to the cart:
      | productId | quantity |
      | 1         | 1        |
    # Assuming the item added gets an itemId, e.g. 1 (this needs to be fetched from previous response)
    When a PUT request is made to "/customers/cart/items/1" using the cart session with the following body:
      """
      {
        "quantity": 3
      }
      """
    Then the response status code should be 200
    And the response body should contain "cart.items[0].quantity" with value "3"
    And the response body should contain "cart.totalAmount" with value "30.0"

  Scenario: Remove Item from Cart
    Given a product with ID 1 exists with price 10.00 and stock 5
    And the following item is added to the cart:
      | productId | quantity |
      | 1         | 2        |
    # Assuming the item added gets an itemId, e.g. 1
    When a DELETE request is made to "/customers/cart/items/1" using the cart session
    Then the response status code should be 200
    And the response body should contain "cart.items" as an empty list
    And the response body should contain "cart.totalAmount" with value "0.0"

  Scenario: Checkout Cart Successfully
    Given a customer is logged in with email "checkout.cust@example.com" and password "CheckoutPass123!"
    And a product with ID 1 exists with price 10.00 and stock 5
    And the following item is added to the cart:
      | productId | quantity |
      | 1         | 2        |
    When a POST request is made to "/customers/cart/checkout" using the cart session and authenticated customer with the following body:
      """
      {
        "paymentMethod": "CARD",
        "currency": "NGN"
      }
      """
    Then the response status code should be 200
    And the response body should contain an "orderId"
    And the response body should contain "paymentStatus" with value "PENDING" # Or as per actual logic
    And the response body should contain "message" with value "Checkout initiated"

  Scenario: Checkout Empty Cart
    Given a customer is logged in with email "empty.checkout@example.com" and password "EmptyPass!"
    When a POST request is made to "/customers/cart/checkout" using the cart session and authenticated customer with the following body:
      """
      {
        "paymentMethod": "CARD",
        "currency": "NGN"
      }
      """
    Then the response status code should be 400 # Or other error for empty cart
    And the response body should contain a message like "Cart is empty" # Or similar

  # Edge Cases:
  Scenario: Add Item to Cart Exceeding Stock
    Given a product with ID 3 exists with price 25.00 and stock 1
    When a POST request is made to "/customers/cart/items" using the cart session with the following body:
      """
      {
        "productId": 3,
        "quantity": 2
      }
      """
    Then the response status code should be 400 # Assuming server-side stock check
    And the response body should contain a message like "Insufficient stock" # Or similar

  Scenario: Update Item Quantity to Zero (should effectively remove or error)
    Given a product with ID 1 exists with price 10.00 and stock 5
    And the following item is added to the cart:
      | productId | quantity |
      | 1         | 2        |
    When a PUT request is made to "/customers/cart/items/1" using the cart session with the following body: # Assuming item ID 1
      """
      {
        "quantity": 0
      }
      """
    Then the response status code should be 200 # Or 400 if quantity 0 is invalid for update
    # If 200, then cart should be empty or item removed.
    # And the response body should contain "cart.items" as an empty list (if item removed)
    # Or the response body should contain "cart.items[0].quantity" with value "0" (if allowed) and total updated.

  Scenario: Attempt to Add Non-existent Product to Cart
    When a POST request is made to "/customers/cart/items" using the cart session with the following body:
      """
      {
        "productId": 99999, # Non-existent product
        "quantity": 1
      }
      """
    Then the response status code should be 400 # Or 404
    And the response body should contain a message like "Product not found"

  Scenario: Remove Non-existent Item from Cart
    When a DELETE request is made to "/customers/cart/items/99999" using the cart session # Non-existent item ID
    Then the response status code should be 404 # Or 400 / 200 with no change
    # And the response body might indicate item not found or cart unchanged.

  Scenario: Update Non-existent Item in Cart
    When a PUT request is made to "/customers/cart/items/99999" using the cart session with the following body: # Non-existent item ID
      """
      {
        "quantity": 5
      }
      """
    Then the response status code should be 404 # Or 400
    # And the response body might indicate item not found.

  # Note: Testing checkout with different payment methods or currencies could be additional scenarios
  # if the backend logic significantly differs for them beyond what's passed to a payment gateway.
  # Authentication for checkout: The CartController itself doesn't show @AuthenticationPrincipal.
  # API_REQUIREMENTS implies customer is tracked by cookies for cart, but checkout might need auth.
  # The CustomerController's checkoutCart has no auth param, but CartService.checkoutCart might require it.
  # The scenario "Checkout Cart Successfully" includes a "Given a customer is logged in..." step.
  # This implies the test step for "POST ... using cart session and authenticated customer" needs to handle both.
  # The current CartController does not seem to use Spring Security's Principal for cart operations, only cookies.
  # Checkout may implicitly use Customer details if CartService links cart_cookie_id to a customer upon login.
  # This needs to be verified by checking CartService logic.
  # For now, the step "using the cart session and authenticated customer" will pass customer auth token if available.
  # If CartService doesn't use it for checkout, it will be ignored. If it does, it will be used.
  # The `CheckoutRequest` in `API_REQUIREMENTS.md` does not contain customerId, so it's likely implicit.
