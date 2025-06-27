Feature: Merchant Product Management
  # Tests for merchants managing their products.
  # Base path: /api/merchants/products (Note: API_REQUIREMENTS.md has /api/v1/merchants/{merchantId}/products)
  # Merchant context is derived from @AuthenticationPrincipal

  Background:
    Given the API base URL is "/api" # Adjusted to match controller's lack of /v1
    And a merchant is logged in with email "product.merchant@example.com" and password "ProdPass123!"
    # The merchant's token and ID should be stored by the login step for authenticated requests.
    # Assume a generic Product with productId=1L, name="Generic Product Alpha", brand="AlphaBrand", category="AlphaCategory" exists.
    # Assume a generic Product with productId=2L, name="Generic Product Beta", brand="BetaBrand", category="BetaCategory" exists.

  Scenario: Add a New MerchantProduct Successfully
    When a POST request is made to "/merchants/products" with an authenticated merchant and the following body:
      """
      {
        "productId": 1, # Assumes Product with ID 1 exists
        "description": "A very super gadget, merchant version.",
        "stock": 100,
        "price": 99.99
      }
      """
    Then the response status code should be 200 # Controller returns OK, not 201
    And the response body should contain a "productId" # This should be 1
    And the response body should contain "productId" with value 1
    And the response body should contain a "merchantProductId"
    And the response body should contain "message" with value "Merchant product added successfully"

  Scenario: Add a MerchantProduct with Missing Required Fields (e.g., productId)
    When a POST request is made to "/merchants/products" with an authenticated merchant and the following body:
      """
      {
        "description": "Missing productId.",
        "stock": 50,
        "price": 49.99
      }
      """
    Then the response status code should be 400
    And the response body should contain an "error" field # e.g. "productId is required" - depends on service validation

  Scenario: List Merchant's Products (initially empty)
    When a GET request is made to "/merchants/products" with an authenticated merchant
    Then the response status code should be 200
    And the response body should be an empty list # Assuming no products yet for this merchant in this scenario

  Scenario: List Merchant's Products After Adding One
    Given the following merchant product is added by the merchant, referencing an existing Product:
      | productId | description   | stock | price |
      | 1         | A cool widget | 20    | 19.99 | # Assumes Product ID 1 (name="Generic Product Alpha") exists
    When a GET request is made to "/merchants/products" with an authenticated merchant
    Then the response status code should be 200
    And the response body should be a list with 1 item
    And the response body should contain "[0].product.productId" with value 1
    And the response body should contain "[0].product.name" with value "Generic Product Alpha" # Assuming Product 1 has this name
    And the response body should contain "[0].stock" with value "20"

  Scenario: Update an Existing MerchantProduct Successfully
    Given the following merchant product is added by the merchant and its merchantProductId is stored as "mpid_to_update", referencing an existing Product:
      | productId | description       | stock | price |
      | 1         | Original product. | 10    | 50.00 | # Assumes Product ID 1 exists
    When a PUT request is made to "/merchants/products/{mpid_to_update}" with an authenticated merchant and the following body:
      """
      {
        "description": "Updated merchant product description.",
        "stock": 15,
        "price": 55.75
      }
      """
    Then the response status code should be 200
    And the response body should contain "message" with value "Merchant product updated successfully"
    # A subsequent GET for this product should show updated merchant-specific details (description, stock, price)
    # and the original product details (productId, name, brand, category from Product ID 1).

  Scenario: Attempt to Update a Non-existent MerchantProduct
    When a PUT request is made to "/merchants/products/99999" with an authenticated merchant and the following body: # 99999 is non-existent MPID
      """
      {
        "description": "Ghost Product",
        "stock": 5,
        "price": 10.00
      }
      """
    Then the response status code should be 400 # Or 404, controller uses IllegalArgumentException -> 400
    And the response body should contain an "error" field # e.g. "Merchant product not found"

  Scenario: Delete an Existing MerchantProduct Successfully
    Given the following merchant product is added by the merchant and its merchantProductId is stored as "mpid_to_delete", referencing an existing Product:
      | productId | description       | stock | price |
      | 1         | Will be deleted.  | 5     | 25.00 | # Assumes Product ID 1 exists
    When a DELETE request is made to "/merchants/products/{mpid_to_delete}" with an authenticated merchant
    Then the response status code should be 200
    And the response body should contain "message" with value "Merchant product deleted successfully"

  Scenario: Attempt to Delete a Non-existent MerchantProduct
    When a DELETE request is made to "/merchants/products/99998" with an authenticated merchant # 99998 is non-existent MPID
    Then the response status code should be 400 # Or 404
    And the response body should contain an "error" field

  Scenario: Search Merchant Products (by merchant's description, not base product name)
    # This search is on MerchantProduct fields. Searching by base Product fields would be different.
    Given the following merchant product is added by the merchant, referencing an existing Product:
      | productId | description           | stock | price |
      | 1         | Alpha item example    | 30    | 12.99 | # Product 1 (name="Generic Product Alpha")
    And the following merchant product is added by the merchant, referencing an existing Product:
      | productId | description           | stock | price |
      | 2         | Beta product example  | 15    | 22.50 | # Product 2 (name="Generic Product Beta")
    When a GET request is made to "/merchants/products/search?query=Alpha item" with an authenticated merchant
    Then the response status code should be 200
    And the response body should be a list with 1 item
    And the response body should contain "[0].product.productId" with value 1
    And the response body should contain "[0].description" with value "Alpha item example"
    And the response body should contain "[0].product.name" with value "Generic Product Alpha" # Check base product name too

  Scenario: Get In-Stock Merchant Products
    Given the following merchant product is added by the merchant, referencing an existing Product: # In stock
      | productId | description      | stock | price |
      | 1         | Product in stock | 5     | 10.00 | # Product 1
    And the following merchant product is added by the merchant, referencing an existing Product: # Out of stock
      | productId | description         | stock | price |
      | 2         | Product out of stock| 0     | 20.00 | # Product 2
    When a GET request is made to "/merchants/products/in-stock" with an authenticated merchant
    Then the response status code should be 200
    And the response body should be a list with 1 item
    And the response body should contain "[0].product.productId" with value 1
    And the response body should contain "[0].description" with value "Product in stock"
    And the response body should contain "[0].stock" greater than 0

  # Note: Batch upload tests would be more complex due to List<Map> request body.
  # Note: Form generation endpoint POST /api/merchants/products (identified by params like category, productType)
  # is not covered here due to its external Python dependency and potential overload with Add Product.
  # Testing it would require mocking the llm_form_generator.py script or ensuring it's available and configured.
  # This should be documented as a limitation if it cannot be tested.

  Scenario: Merchant A attempts to update Merchant B's product
    Given a merchant "merchantA@example.com" is logged in with password "PassA123!"
    And a merchant "merchantB@example.com" exists with password "PassB456!"
    And merchant "merchantB@example.com" has a product "Product B" with price 10.00 and stock 5, whose merchantProductId is stored as "product_of_merchant_b"
    When merchant "merchantA@example.com" attempts to update merchant product "{product_of_merchant_b}" with the following body:
      """
      {
        "description": "Attempted update by Merchant A",
        "stock": 1,
        "price": 1.00
      }
      """
    Then the response status code should be 403 # Or 404 if hidden

  Scenario: Merchant A attempts to delete Merchant B's product
    Given a merchant "merchantA@example.com" is logged in with password "PassA123!"
    And a merchant "merchantB@example.com" exists with password "PassB456!"
    And merchant "merchantB@example.com" has a product "Product B To Delete" with price 10.00 and stock 5, whose merchantProductId is stored as "product_to_delete_by_a"
    When merchant "merchantA@example.com" attempts to delete merchant product "{product_to_delete_by_a}"
    Then the response status code should be 403 # Or 404 if hidden
