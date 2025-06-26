Feature: Customer Management

  Background:
    Given the API base URL is "/api/v1" # This matches CustomerController

  Scenario: Successful Customer Signup
    When a POST request is made to "/customers/signup" with the following body:
      """
      {
        "email": "new.customer@example.com",
        "firstName": "New",
        "lastName": "Customer",
        "phoneNumber": "+2348012345678",
        "address": {
          "street": "123 Test St",
          "city": "Testville",
          "state": "TS",
          "country": "NG",
          "postalCode": "100001"
        }
      }
      """
    Then the response status code should be 201
    And the response body should contain "customerId"
    And the response body should contain "email" with value "new.customer@example.com"
    And the response body should contain "firstName" with value "New"
    And the response body should contain "street" with value "123 Test St" # Assuming address is flattened or nested in response

  Scenario: Customer Signup with Missing Email
    When a POST request is made to "/customers/signup" with the following body:
      """
      {
        "firstName": "NoEmail",
        "lastName": "Customer",
        "phoneNumber": "+2348012345679"
      }
      """
    Then the response status code should be 400
    # And the response body should contain a message like "Email is required" # Or similar based on actual error

  Scenario: Customer Signup with Already Existing Email
    Given a customer already exists with email "existing.customer@example.com"
    When a POST request is made to "/customers/signup" with the following body:
      """
      {
        "email": "existing.customer@example.com",
        "firstName": "Another",
        "lastName": "Customer",
        "phoneNumber": "+2348012345600"
      }
      """
    Then the response status code should be 400 # Or 409, check controller/service
    And the response body should contain a message like "Email already exists"

  Scenario: Customer Signup with Missing First Name
    When a POST request is made to "/customers/signup" with the following body:
      """
      {
        "email": "missing.name@example.com",
        "lastName": "Customer",
        "phoneNumber": "+2348012345601"
      }
      """
    Then the response status code should be 400
    # And the response body should contain a message like "First name is required"

  Scenario: Customer Signup with Invalid Phone Number Format (if validation exists)
    When a POST request is made to "/customers/signup" with the following body:
      """
      {
        "email": "invalid.phone@example.com",
        "firstName": "Invalid",
        "lastName": "Phone",
        "phoneNumber": "12345"
      }
      """
    Then the response status code should be 400
    # And the response body should contain a message like "Invalid phone number format"

# Placeholder for more scenarios:
# - Validations for other fields (lastName, address components)
# - Max length for fields
# - Other edge cases for address (e.g., missing parts of address)

  Scenario: Successful Customer Login
    Given a customer already exists with email "login.cust@example.com" and password "CustPass123!" in the system
    # Note: "password" here is conceptual for the test, CustomerService handles actual auth.
    When a POST request is made to "/customers/login" with the following body:
      """
      {
        "email": "login.cust@example.com",
        "password": "CustPass123!"
      }
      """
    Then the response status code should be 200
    And the response body should contain a "token"
    And the response body should contain "customer.email" with value "login.cust@example.com"

  Scenario: Customer Login with Non-existent Email
    When a POST request is made to "/customers/login" with the following body:
      """
      {
        "email": "nosuchcustomer@example.com",
        "password": "Password123!"
      }
      """
    Then the response status code should be 401
    And the response body should contain a message like "Invalid credentials"

  Scenario: Customer Login with Incorrect Password
    Given a customer already exists with email "login.fail.cust@example.com" and password "CorrectCustPass!" in the system
    When a POST request is made to "/customers/login" with the following body:
      """
      {
        "email": "login.fail.cust@example.com",
        "password": "WrongCustPassword"
      }
      """
    Then the response status code should be 401
    And the response body should contain a message like "Invalid credentials"

  Scenario: Update Customer Profile Successfully
    Given a customer is logged in with email "update.cust@example.com" and password "UpdatePass123!"
    When a PUT request is made to "/customers/profile" with an authenticated customer and the following body:
      """
      {
        "email": "update.cust@example.com", # Must match authenticated user's email
        "firstName": "UpdatedCustFirst",
        "lastName": "UpdatedCustLast",
        "phoneNumber": "+2349098765432",
        "address": {
          "street": "456 Update Ave",
          "city": "UpdateCity",
          "state": "US",
          "country": "NG",
          "postalCode": "100002"
        }
      }
      """
    Then the response status code should be 200
    And the response body should contain "success" with boolean value "true"
    And the response body should contain "message" with value "Profile updated successfully"

  Scenario: Attempt to Update Another Customer's Profile
    Given a customer is logged in with email "attacker.cust@example.com" and password "AttackerPass123!"
    When a PUT request is made to "/customers/profile" with an authenticated customer and the following body:
      """
      {
        "email": "victim.cust@example.com", # Attempting to update different email
        "firstName": "MaliciousUpdate"
      }
      """
    Then the response status code should be 403 # Forbidden
    And the response body should contain a message like "Forbidden"

  Scenario: Update Customer Profile Without Authentication
    When a PUT request is made to "/customers/profile" with the following body: # No auth
      """
      {
        "email": "unauth.update@example.com",
        "firstName": "UnauthUpdate"
      }
      """
    Then the response status code should be 401 # Or 403, based on how controller handles missing auth for this path

  Scenario: Get Customer Order History Successfully
    Given a customer is logged in with email "history.cust@example.com" and password "HistoryPass123!"
    # And customer "history.cust@example.com" has some orders (precondition, potentially complex to set up here)
    When a GET request is made to "/customers/orders/history?page=1&limit=10" with an authenticated customer
    Then the response status code should be 200
    # And the response body should be a list (e.g., check if it's an array "$.content")
    # And the response body should potentially contain pagination details ("$.totalPages", "$.totalElements")

  Scenario: Get Customer Order History Without Authentication
    When a GET request is made to "/customers/orders/history" # No auth
    Then the response status code should be 401 # Or 403, based on @AuthenticationPrincipal behavior without auth

  Scenario: Get Customer by ID Successfully (as an admin or for self - assuming for self if no specific auth role)
    Given a customer "get.cust@example.com" exists with ID "123" # Simulate ID or retrieve after creation
    # And an appropriate user (admin or self) is logged in
    When a GET request is made to "/customers/123" # Assuming no specific auth needed beyond valid ID for now
    Then the response status code should be 200
    And the response body should contain "customerId" with value "123"
    And the response body should contain "email" with value "get.cust@example.com"

  Scenario: Get Customer by Non-existent ID
    When a GET request is made to "/customers/99999"
    Then the response status code should be 400 # Or 404, based on controller
    And the response body should contain a message like "Customer not found" # Or similar

  Scenario: Delete Customer Successfully
    Given a customer is logged in with email "delete.cust@example.com" and password "DeletePass123!"
    # The customerId will be available in sharedData after login step
    When a DELETE request is made to "/customers/{customerId}" with an authenticated customer
    Then the response status code should be 200
    And the response body should contain "success" with boolean value "true"
    And the response body should contain "message" with value "Customer deleted successfully"
    # And a subsequent GET request for this customerId should fail (e.g., 404)

  Scenario: Attempt to Delete Another Customer
    Given a customer "victim.cust.del@example.com" exists with ID "789"
    And a customer is logged in with email "attacker.cust.del@example.com" and password "AttackerDelPass!" (who is not customer 789)
    When a DELETE request is made to "/customers/789" with an authenticated customer
    Then the response status code should be 401 # Or 403 (Unauthorized/Forbidden)
    And the response body should contain a message like "Unauthorized"
