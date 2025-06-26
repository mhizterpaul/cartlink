Feature: Review Management
  # Tests for creating and retrieving product/merchant reviews.
  # Base path: /api/reviews

  Background:
    Given the API base URL is "/api" # ReviewController is at /api/reviews
    # Preconditions: Customer, Merchant, Product, Order might be needed to create a valid review context.
    And a customer "review.customer@example.com" exists and their ID is stored as "reviewCustomerId"
    And a merchant "review.merchant@example.com" exists and their ID is stored as "reviewMerchantId"
    # Assume Product and Order details are part of the Review object sent by client.
    # Or ReviewService links them based on other IDs in Review object.

  Scenario: Create a New Review Successfully
    # Assuming Review object needs customerId, merchantId, orderId, productId, rating, comment
    When a POST request is made to "/reviews" with the following body:
      """
      {
        "customer": { "customerId": "{reviewCustomerId}" },
        "merchant": { "merchantId": "{reviewMerchantId}" },
        "orderId": 123,
        "productId": 456,
        "rating": 5,
        "comment": "Excellent product and service!"
      }
      """
      # Note: The structure of Review object (especially customer/merchant linkage) needs to match Review.java entity.
      # The controller takes @RequestBody Review review.
    Then the response status code should be 200 # Controller returns OK, API_REQUIREMENTS.md suggests 200 too.
    And the response body should contain a "reviewId"
    And the response body should contain "rating" with value "5"
    And the response body should contain "comment" with value "Excellent product and service!"
    And the response body should contain "customer.customerId" with value "{reviewCustomerId}"
    And the response body should contain "merchant.merchantId" with value "{reviewMerchantId}"

  Scenario: Create Review with Missing Rating
    When a POST request is made to "/reviews" with the following body:
      """
      {
        "customer": { "customerId": "{reviewCustomerId}" },
        "merchant": { "merchantId": "{reviewMerchantId}" },
        "comment": "No rating provided."
      }
      """
    Then the response status code should be 400 # Assuming rating is mandatory by service/validation
    And the response body should contain an "error" field # e.g. "Rating is required"

  Scenario: Get All Reviews (Initially Empty or After Setup)
    When a GET request is made to "/reviews"
    Then the response status code should be 200
    And the response body should be a list # Potentially empty

  Scenario: Get All Reviews After Adding One
    Given the following review is created:
      | customerId         | merchantId         | orderId | productId | rating | comment            |
      | {reviewCustomerId} | {reviewMerchantId} | 789     | 101       | 4      | Pretty good stuff. |
    When a GET request is made to "/reviews"
    Then the response status code should be 200
    And the response body should be a list with at least 1 item
    And the response body should contain "[0].comment" with value "Pretty good stuff."
    And the response body should contain "[0].rating" with value "4"

  Scenario: Get Reviews by Merchant ID Successfully
    Given the following review is created for merchant "{reviewMerchantId}":
      | customerId         | merchantId         | orderId | productId | rating | comment                     |
      | {reviewCustomerId} | {reviewMerchantId} | 321     | 102       | 5      | Specific merchant review.   |
    When a GET request is made to "/reviews/merchant/{reviewMerchantId}"
    Then the response status code should be 200
    And the response body should be a list with at least 1 item
    And the response body should contain "[0].comment" with value "Specific merchant review."
    And all items in the list should have "merchant.merchantId" with value "{reviewMerchantId}"

  Scenario: Get Reviews for Non-existent Merchant ID
    When a GET request is made to "/reviews/merchant/99999" # Non-existent merchant
    Then the response status code should be 400 # Controller returns Bad Request with "Merchant not found"
    And the response body should contain "error" with value "Merchant not found"

  Scenario: Get Reviews by Merchant Email Successfully (Extra controller endpoint)
    Given the following review is created for merchant "review.merchant@example.com" (ID: {reviewMerchantId}):
      | customerId         | merchantId         | orderId | productId | rating | comment                     |
      | {reviewCustomerId} | {reviewMerchantId} | 654     | 103       | 3      | Review via merchant email.  |
    When a GET request is made to "/reviews/merchant/email/review.merchant@example.com"
    Then the response status code should be 200
    And the response body should be a list with at least 1 item
    And the response body should contain "[0].comment" with value "Review via merchant email."
    And all items in the list should have "merchant.merchantId" with value "{reviewMerchantId}"

  # Authentication for POST /reviews is not explicit in controller (no @AuthenticationPrincipal for customer).
  # It relies on the client sending customerId within the Review object.
  # This means any client can potentially post a review for any customer if they know the ID,
  # unless service layer adds validation. This is a potential security/design point to note.
  # GET endpoints are public. Tool output for `create_file_with_block`:
