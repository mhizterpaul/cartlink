Feature: General Authentication Actions
  # Tests for general auth actions like email verification and password reset (using /api/auth endpoints).

  Background:
    Given the API base URL is "/api" # AuthController is at /api/auth
    # Merchant entities are used by AuthService for these operations.
    And a merchant "auth.action@example.com" exists with password "AuthPass123!"

  Scenario: Verify Email with Valid Token
    Given merchant "auth.action@example.com" has an unverified email with verification token "valid-verify-token"
    When a GET request is made to "/auth/verify-email?token=valid-verify-token"
    Then the response status code should be 200
    And the response body should be the string "Email verified successfully"
    # And the merchant "auth.action@example.com" should now be email verified (DB check)

  Scenario: Verify Email with Invalid Token
    When a GET request is made to "/auth/verify-email?token=invalid-or-expired-token"
    Then the response status code should be 400
    And the response body should be the string "Invalid or expired token"

  Scenario: Request Password Reset for Existing User
    When a POST request is made to "/auth/request-password-reset" with the following body:
      """
      { "email": "auth.action@example.com" }
      """
    Then the response status code should be 200
    And the response body should be the string "If a user with that email exists, a password reset link has been sent."
    # And a password reset token should be generated for "auth.action@example.com" (DB check)

  Scenario: Request Password Reset for Non-Existing User
    When a POST request is made to "/auth/request-password-reset" with the following body:
      """
      { "email": "nosuchuser.auth@example.com" }
      """
    Then the response status code should be 200 # Controller always returns 200 to not reveal existence
    And the response body should be the string "If a user with that email exists, a password reset link has been sent."

  Scenario: Show Reset Password Form (HTML)
    When a GET request is made to "/auth/reset-password?token=some-html-token"
    Then the response status code should be 200
    And the response content type should be "text/html"
    And the response body should contain "<title>Reset Password</title>" # Or other unique HTML element from the form
    And the response body should contain "name=\"token\" value=\"some-html-token\"" # Check token is in form

  Scenario: Reset Password with Valid Token
    Given merchant "auth.action@example.com" has a password reset token "valid-reset-token-for-auth"
    When a POST request is made to "/auth/reset-password" with the following body:
      """
      {
        "token": "valid-reset-token-for-auth",
        "password": "NewPassword123!"
      }
      """
    Then the response status code should be 200
    And the response body should be the string "Password reset successfully"
    # And merchant "auth.action@example.com" should be able to log in with "NewPassword123!"

  Scenario: Reset Password with Invalid Token
    When a POST request is made to "/auth/reset-password" with the following body:
      """
      {
        "token": "invalid-reset-token-for-auth",
        "password": "AnotherNewPassword123!"
      }
      """
    Then the response status code should be 400
    And the response body should be the string "Invalid or expired token"

  # Note: These tests assume that the AuthService correctly handles token generation and validation logic,
  # including setting/clearing tokens on the Merchant entity and checking expiry.
  # The "merchant ... has ... token" steps will directly manipulate Merchant entity for test setup. Tool output for `create_file_with_block`:
