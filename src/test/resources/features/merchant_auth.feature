Feature: Merchant Authentication

  Background:
    Given the API base URL is "/api" # Adjusted base path

  Scenario: Successful Merchant Signup
    When a POST request is made to "/merchant/signup" with the following body:
      """
      {
        "email": "test.merchant@example.com",
        "password": "Password123!",
        "firstName": "Test",
        "lastName": "Merchant",
        "image": "http://example.com/image.png"
      }
      """
    Then the response status code should be 201
    And the response body should contain a "merchantId"
    And the response body should contain a "token"
    And the response body should contain "merchantDetails.email" with value "test.merchant@example.com"
    And the response body should contain "merchantDetails.firstName" with value "Test"

  Scenario: Merchant Signup with Missing Email
    When a POST request is made to "/merchant/signup" with the following body:
      """
      {
        "password": "Password123!",
        "firstName": "Test",
        "lastName": "Merchant"
      }
      """
    Then the response status code should be 400

  Scenario: Merchant Signup with Invalid Email Format
    When a POST request is made to "/merchant/signup" with the following body:
      """
      {
        "email": "invalid-email",
        "password": "Password123!",
        "firstName": "Test",
        "lastName": "Merchant"
      }
      """
    Then the response status code should be 400

  Scenario: Merchant Signup with Missing Password
    When a POST request is made to "/merchant/signup" with the following body:
      """
      {
        "email": "test.merchant2@example.com",
        "firstName": "Test",
        "lastName": "Merchant"
      }
      """
    Then the response status code should be 400

  Scenario: Merchant Signup with Missing First Name
    When a POST request is made to "/merchant/signup" with the following body:
      """
      {
        "email": "test.merchant3@example.com",
        "password": "Password123!"
      }
      """
    Then the response status code should be 400 # Assuming first name is required by validation

  Scenario: Merchant Signup with Already Existing Email
    Given a merchant already exists with email "existing.merchant@example.com"
    When a POST request is made to "/merchant/signup" with the following body:
      """
      {
        "email": "existing.merchant@example.com",
        "password": "Password123!",
        "firstName": "Another",
        "lastName": "Merchant"
      }
      """
    Then the response status code should be 400 # Or 409 Conflict, check controller
    And the response body should contain a message like "Email already exists"

  Scenario: Successful Merchant Login
    Given a merchant already exists with email "login.merchant@example.com" and password "LoginPass123!"
    When a POST request is made to "/merchant/login" with the following body:
      """
      {
        "email": "login.merchant@example.com",
        "password": "LoginPass123!"
      }
      """
    Then the response status code should be 200
    And the response body should contain a "merchantId"
    And the response body should contain a "token"
    And the response body should contain "merchantDetails.email" with value "login.merchant@example.com"

  Scenario: Merchant Login with Non-existent Email
    When a POST request is made to "/merchant/login" with the following body:
      """
      {
        "email": "nonexistent@example.com",
        "password": "Password123!"
      }
      """
    Then the response status code should be 401
    And the response body should contain a message like "Invalid credentials" # Or specific message

  Scenario: Merchant Login with Incorrect Password
    Given a merchant already exists with email "login.fail@example.com" and password "CorrectPassword123!"
    When a POST request is made to "/merchant/login" with the following body:
      """
      {
        "email": "login.fail@example.com",
        "password": "WrongPassword"
      }
      """
    Then the response status code should be 401
    And the response body should contain a message like "Invalid credentials" # Or specific message

  Scenario: Request Password Reset for Existing Merchant
    Given a merchant already exists with email "reset.request@example.com"
    When a POST request is made to "/merchant/password-reset-request" with the following body:
      """
      { "email": "reset.request@example.com" }
      """
    Then the response status code should be 200
    And the response body should contain "success" with boolean value "true"
    And the response body should contain "message" # Check for presence of a message

  Scenario: Request Password Reset for Non-existent Merchant
    When a POST request is made to "/merchant/password-reset-request" with the following body:
      """
      { "email": "nonexistent.reset@example.com" }
      """
    Then the response status code should be 404 # As per API_REQUIREMENTS.md
    And the response body should contain a message like "not found" # Or specific message

  Scenario: Successfully Reset Password with Valid Token
    # This scenario is more complex as it requires obtaining a valid resetToken first.
    # For a true BDD/integration test, we might need to:
    # 1. Request a password reset.
    # 2. Retrieve the token (e.g., from DB if not returned, or mock email service).
    # 3. Use the token to reset the password.
    # This might be simplified for now or require more intricate step definitions.
    # ---
    # Simplified version assuming we can simulate token generation or have a known one for testing:
    Given a merchant "reset.user@example.com" has requested a password reset and received a token "valid-reset-token"
    When a POST request is made to "/merchant/password-reset" with the following body:
      """
      {
        "email": "reset.user@example.com",
        "resetToken": "valid-reset-token",
        "newPassword": "NewSecurePassword123!"
      }
      """
    Then the response status code should be 200
    And the response body should contain "success" with boolean value "true"
    # And a subsequent login with "reset.user@example.com" and "NewSecurePassword123!" should be successful (optional step)

  Scenario: Attempt Password Reset with Invalid Token
    When a POST request is made to "/merchant/password-reset" with the following body:
      """
      {
        "email": "reset.user.invalidtoken@example.com",
        "resetToken": "invalid-or-expired-token",
        "newPassword": "NewSecurePassword123!"
      }
      """
    Then the response status code should be 400 # As per API_REQUIREMENTS.md
    And the response body should contain a message like "Invalid token" # Or specific message

  Scenario: Attempt Password Reset for Non-matching Email and Token
    Given a merchant "another.user@example.com" has requested a password reset and received a token "another-valid-token"
    When a POST request is made to "/merchant/password-reset" with the following body:
      """
      {
        "email": "attacker.email@example.com", # Different email
        "resetToken": "another-valid-token",   # Valid token but for another.user@example.com
        "newPassword": "NewSecurePassword123!"
      }
      """
    Then the response status code should be 400 # Or other appropriate error
    And the response body should contain a message like "Invalid token" # Or specific error for mismatch

  Scenario: Successfully Refresh Token
    Given a merchant is logged in with email "refresh.user@example.com" and password "RefreshPass123!"
    When a POST request is made to "/merchant/refresh-token" with an authenticated user and the following body:
      """
      {}
      """
      # Note: API_REQUIREMENTS.md says POST /api/v1/merchants/refresh-token with { "refreshToken": "string" }
      # However, the controller has /api/merchant/refresh-token and seems to expect the current token in Auth header.
      # This test will follow the controller's apparent behavior.
      # If it expects a refresh token in the body, this test will fail and highlight the discrepancy.
    Then the response status code should be 200
    And the response body should contain a "token"

  Scenario: Get Merchant Profile Successfully
    Given a merchant is logged in with email "profile.user@example.com" and password "ProfilePass123!"
    When a GET request is made to "/merchant/profile" with an authenticated user
    Then the response status code should be 200
    And the response body should contain "email" with value "profile.user@example.com"
    And the response body should contain a "merchantId"
    # Add more assertions for profile fields if necessary

  Scenario: Get Merchant Profile Without Authentication
    When a GET request is made to "/merchant/profile" without authentication
    Then the response status code should be 401 # Or 403, depending on security config

  Scenario: Update Merchant Profile Successfully
    Given a merchant is logged in with email "update.profile@example.com" and password "UpdatePass123!"
    When a PUT request is made to "/merchant/profile" with an authenticated user and the following body:
      """
      {
        "firstName": "UpdatedFirst",
        "lastName": "UpdatedLast",
        "phoneNumber": "+1234567890"
      }
      """
    Then the response status code should be 200
    And the response body should contain "firstName" with value "UpdatedFirst"
    And the response body should contain "lastName" with value "UpdatedLast"
    # And the response body should contain "message" with value "Profile updated successfully" (if applicable)

  Scenario: Update Merchant Profile Without Authentication
    When a PUT request is made to "/merchant/profile" without authentication and the following body:
      """
      {
        "firstName": "AttemptedUpdate"
      }
      """
    Then the response status code should be 401 # Or 403

  Scenario: Successfully Logout
    Given a merchant is logged in with email "logout.user@example.com" and password "LogoutPass123!"
    When a POST request is made to "/merchant/logout" with an authenticated user and the following body:
      """
      {}
      """
    Then the response status code should be 200
    And the response body should contain "message" with value "Logged out successfully"
    # Optional: And a subsequent request to a protected endpoint with the old token should fail

# Placeholder for more scenarios:
# - Password complexity rules (if any)
# - Max length for fields
# - etc.
