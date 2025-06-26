Feature: Coupon Management
  # Tests for merchants managing coupons for their products.
  # Base path: /api/v1/merchants/{merchantId}/products/{productId}/coupons

  Background:
    Given the API base URL is "/api/v1"
    And a merchant is logged in with email "coupon.merchant@example.com" and password "CouponPass123!"
    # Merchant ID will be stored as "merchantId".
    # A product needs to exist for this merchant to associate coupons with.
    And merchant "coupon.merchant@example.com" has a product "CouponProduct" with original ProductID "cp1" price 100.00 and stock 10, whose actual ProductID is stored as "couponProductId" and MerchantProductID as "couponMerchantProductId"

  Scenario: Create a New Coupon Successfully
    When a POST request is made to "/merchants/{merchantId}/products/{couponProductId}/coupons" with an authenticated merchant and the following body:
      """
      {
        "discount": 10.50,
        "validFrom": "2024-01-01T00:00:00Z",
        "validUntil": "2024-12-31T23:59:59Z",
        "maxUsage": 100,
        "maxUsers": 50
      }
      """
    Then the response status code should be 201
    And the response body should contain a "couponId"
    # Store couponId for later use, e.g., sharedData.put("lastCouponId", ...)

  Scenario: Create Coupon with Invalid Date Format
    When a POST request is made to "/merchants/{merchantId}/products/{couponProductId}/coupons" with an authenticated merchant and the following body:
      """
      {
        "discount": 5.00,
        "validFrom": "invalid-date",
        "validUntil": "2024-12-31T23:59:59Z",
        "maxUsage": 10,
        "maxUsers": 5
      }
      """
    Then the response status code should be 400 # Assuming Instant.parse will fail
    # And the response body should contain an "error" field indicating date format issue

  Scenario: Get Coupons for a Product (Initially Empty)
    When a GET request is made to "/merchants/{merchantId}/products/{couponProductId}/coupons" with an authenticated merchant
    Then the response status code should be 200
    And the response body should be an empty list

  Scenario: Get Coupons for a Product After Adding One
    Given the following coupon is created for product "{couponProductId}" by the merchant and its ID is stored as "c_id_1":
      | discount | validFrom            | validUntil           | maxUsage | maxUsers |
      | 15.00    | 2024-02-01T00:00:00Z | 2024-11-30T23:59:59Z | 200      | 100      |
    When a GET request is made to "/merchants/{merchantId}/products/{couponProductId}/coupons" with an authenticated merchant
    Then the response status code should be 200
    And the response body should be a list with 1 item
    And the response body should contain "[0].couponId" with value "{c_id_1}"
    And the response body should contain "[0].discount" with number value "15.00"

  Scenario: Delete an Existing Coupon Successfully
    Given the following coupon is created for product "{couponProductId}" by the merchant and its ID is stored as "c_id_to_delete":
      | discount | validFrom            | validUntil           | maxUsage | maxUsers |
      | 5.00     | 2024-03-01T00:00:00Z | 2024-03-31T23:59:59Z | 10       | 10       |
    When a DELETE request is made to "/merchants/{merchantId}/products/{couponProductId}/coupons/{c_id_to_delete}" with an authenticated merchant
    Then the response status code should be 200
    # And the response body might be empty or contain a success message/couponId (controller returns empty CouponIdResponse)
    # And a subsequent GET for coupons for this product should not list the deleted coupon

  Scenario: Attempt to Delete a Non-existent Coupon
    When a DELETE request is made to "/merchants/{merchantId}/products/{couponProductId}/coupons/99999" with an authenticated merchant # 99999 is non-existent coupon ID
    Then the response status code should be 400 # Or 404, depends on CouponService behavior (e.g., if it throws EntityNotFound)
    # And the response body should contain an "error" field

  Scenario: Attempt to Create Coupon Without Authentication
    When a POST request is made to "/merchants/{merchantId}/products/{couponProductId}/coupons" with the following body: # No auth
      """
      {
        "discount": 10.00,
        "validFrom": "2025-01-01T00:00:00Z",
        "validUntil": "2025-12-31T23:59:59Z"
      }
      """
    Then the response status code should be 401 # Or 403

  Scenario: Attempt to Get Coupons Without Authentication
    When a GET request is made to "/merchants/{merchantId}/products/{couponProductId}/coupons" # No auth
    Then the response status code should be 401 # Or 403

  # Note: {merchantId} and {productId} in paths are resolved from sharedData set during Background/Given steps.
  # The `couponProductId` refers to the actual ID of the Product entity. Tool output for `create_file_with_block`:
