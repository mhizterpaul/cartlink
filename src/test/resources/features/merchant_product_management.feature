Feature: Merchant Product Management
  # Tests for merchants managing their products.
  # Base path: /api/merchants/products (Note: API_REQUIREMENTS.md has /api/v1/merchants/{merchantId}/products)
  # Merchant context is derived from @AuthenticationPrincipal

  Background:
    Given the API base URL is "/api" # Adjusted to match controller's lack of /v1
    And a merchant is logged in with email "product.merchant@example.com" and password "ProdPass123!"
    # The merchant's token and ID should be stored by the login step for authenticated requests.

  Scenario: Add a New Product Successfully
    When a POST request is made to "/merchants/products" with an authenticated merchant and the following body:
      """
      {
        "name": "Super Gadget",
        "brand": "GadgetBrand",
        "category": "Electronics",
        "description": "A very super gadget.",
        "stock": 100,
        "price": 99.99
      }
      """
    Then the response status code should be 200 # Controller returns OK, not 201
    And the response body should contain a "productId"
    And the response body should contain a "merchantProductId"
    And the response body should contain "message" with value "Merchant product added successfully"
    # Store merchantProductId for later use, e.g., sharedData.put("lastMerchantProductId", ...)

  Scenario: Add a Product with Missing Required Fields (e.g., name)
    When a POST request is made to "/merchants/products" with an authenticated merchant and the following body:
      """
      {
        "brand": "IncompleteBrand",
        "category": "Electronics",
        "description": "Missing name.",
        "stock": 50,
        "price": 49.99
      }
      """
    Then the response status code should be 400
    And the response body should contain an "error" field # e.g. "Name is required" - depends on service validation

  Scenario: List Merchant's Products (initially empty)
    When a GET request is made to "/merchants/products" with an authenticated merchant
    Then the response status code should be 200
    And the response body should be an empty list # Assuming no products yet for this merchant in this scenario

  Scenario: List Merchant's Products After Adding One
    Given the following product is added by the merchant:
      | name        | brand      | category    | description   | stock | price |
      | Cool Widget | WidgetCorp | Accessories | A cool widget | 20    | 19.99 |
    When a GET request is made to "/merchants/products" with an authenticated merchant
    Then the response status code should be 200
    And the response body should be a list with 1 item
    And the response body should contain "[0].product.name" with value "Cool Widget"
    And the response body should contain "[0].stock" with value "20" # Assuming stock is directly on MerchantProduct

  Scenario: Update an Existing Product Successfully
    Given the following product is added by the merchant and its merchantProductId is stored as "mpid_to_update":
      | name            | brand          | category    | description       | stock | price |
      | Old Product Name | OldBrand      | Electronics | Original product. | 10    | 50.00 |
    When a PUT request is made to "/merchants/products/{mpid_to_update}" with an authenticated merchant and the following body:
      """
      {
        "name": "Updated Product Name",
        "brand": "NewBrand",
        "category": "Electronics",
        "description": "Updated product description.",
        "stock": 15,
        "price": 55.75
      }
      """
    Then the response status code should be 200
    And the response body should contain "message" with value "Merchant product updated successfully"
    # And a subsequent GET for this product should show updated details

  Scenario: Attempt to Update a Non-existent Product
    When a PUT request is made to "/merchants/products/99999" with an authenticated merchant and the following body: # 99999 is non-existent MPID
      """
      {
        "name": "Ghost Product",
        "stock": 5,
        "price": 10.00
      }
      """
    Then the response status code should be 400 # Or 404, controller uses IllegalArgumentException -> 400
    And the response body should contain an "error" field # e.g. "Merchant product not found"

  Scenario: Delete an Existing Product Successfully
    Given the following product is added by the merchant and its merchantProductId is stored as "mpid_to_delete":
      | name            | brand          | category    | description       | stock | price |
      | ProductToDelete | BrandToDelete | Temporary   | Will be deleted.  | 5     | 25.00 |
    When a DELETE request is made to "/merchants/products/{mpid_to_delete}" with an authenticated merchant
    Then the response status code should be 200
    And the response body should contain "message" with value "Merchant product deleted successfully"
    # And a subsequent GET for this product should result in it not being listed or a 404 if fetching by ID

  Scenario: Attempt to Delete a Non-existent Product
    When a DELETE request is made to "/merchants/products/99998" with an authenticated merchant # 99998 is non-existent MPID
    Then the response status code should be 400 # Or 404
    And the response body should contain an "error" field # e.g. "Merchant product not found"

  Scenario: Search Merchant Products
    Given the following product is added by the merchant:
      | name        | brand      | category    | description           | stock | price |
      | Alpha Item  | SearchCorp | Gadgets     | An alpha item example | 30    | 12.99 |
    And the following product is added by the merchant:
      | name        | brand      | category    | description           | stock | price |
      | Beta Product | SearchCorp | Gadgets     | A beta product example| 15    | 22.50 |
    When a GET request is made to "/merchants/products/search?query=Alpha" with an authenticated merchant
    Then the response status code should be 200
    And the response body should be a list with 1 item
    And the response body should contain "[0].product.name" with value "Alpha Item"

  Scenario: Get In-Stock Merchant Products
    Given the following product is added by the merchant: # In stock
      | name           | brand     | category | description      | stock | price |
      | InStockProduct | MyBrand   | Tools    | Product in stock | 5     | 10.00 |
    And the following product is added by the merchant: # Out of stock
      | name              | brand     | category | description         | stock | price |
      | OutOfStockProduct | MyBrand   | Tools    | Product out of stock| 0     | 20.00 |
    When a GET request is made to "/merchants/products/in-stock" with an authenticated merchant
    Then the response status code should be 200
    And the response body should be a list with 1 item
    And the response body should contain "[0].product.name" with value "InStockProduct"
    And the response body should contain "[0].stock" greater than 0 # More robust check

  # Note: Batch upload tests would be more complex due to List<Map> request body.
  # Note: Form generation endpoint POST /api/merchants/products (identified by params like category, productType)
  # is not covered here due to its external Python dependency and potential overload with Add Product.
  # Testing it would require mocking the llm_form_generator.py script or ensuring it's available and configured.
  # This should be documented as a limitation if it cannot be tested.
