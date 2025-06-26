Feature: Complaint Management
  # Tests for customers managing complaints related to their orders.
  # Base path: /api/v1/customers/orders

  Background:
    Given the API base URL is "/api/v1"
    And a customer is logged in with email "complaint.user@example.com" and password "ComplaintPass123!"
    # Assume an order exists for this customer to file a complaint against.
    # This order needs to be created in a way that its ID is known.
    And an order with ID "c_order_1" exists for customer "complaint.user@example.com" and its actual ID is stored as "complaintOrderId"

  Scenario: Submit a New Complaint Successfully
    When a POST request is made to "/customers/orders/{complaintOrderId}/complaint" with an authenticated customer and the following body:
      """
      {
        "title": "Product Damaged",
        "description": "The product arrived with a crack in the casing.",
        "category": "PRODUCT_DAMAGE"
      }
      """
    Then the response status code should be 201
    And the response body should contain a "complaintId"
    And the response body should contain "title" with value "Product Damaged"
    And the response body should contain "order.orderId" with value "{complaintOrderId}" # Check if orderId is string or number
    And the response body should contain "customer.email" with value "complaint.user@example.com"

  Scenario: Submit Complaint for Non-existent Order
    When a POST request is made to "/customers/orders/99999/complaint" with an authenticated customer and the following body: # 99999 is non-existent order
      """
      {
        "title": "Fake Complaint",
        "description": "Order never existed.",
        "category": "OTHER"
      }
      """
    Then the response status code should be 400 # Or 404, depends on service logic (e.g. "Order not found")
    And the response body should contain an "error" field

  Scenario: Submit Complaint with Missing Title
    When a POST request is made to "/customers/orders/{complaintOrderId}/complaint" with an authenticated customer and the following body:
      """
      {
        "description": "Missing title for this complaint.",
        "category": "MISSING_INFORMATION"
      }
      """
    Then the response status code should be 400
    And the response body should contain an "error" field # e.g., "Title is required"

  Scenario: Get All Complaints for Authenticated Customer
    # Precondition: Customer "complaint.user@example.com" has submitted at least one complaint (e.g., from previous scenario)
    Given the customer "complaint.user@example.com" submitted a complaint for order "{complaintOrderId}" with title "Previous Complaint"
    When a GET request is made to "/customers/orders/complaints" with an authenticated customer
    Then the response status code should be 200
    And the response body should be a list with at least 1 item
    And the response body should contain "[0].title" # Check for presence of title in the first complaint

  Scenario: Get Complaints for a Specific Order
    Given the customer "complaint.user@example.com" submitted a complaint for order "{complaintOrderId}" with title "Specific Order Complaint"
    When a GET request is made to "/customers/orders/{complaintOrderId}/complaints" with an authenticated customer
    Then the response status code should be 200
    And the response body should be a list with at least 1 item
    And the response body should contain "[0].title" with value "Specific Order Complaint"
    And the response body should contain "[0].order.orderId" with value "{complaintOrderId}"

  Scenario: Get Complaints for an Order with No Complaints
    Given an order with ID "c_order_2" exists for customer "complaint.user@example.com" and has no complaints, and its actual ID is stored as "noComplaintOrderId"
    When a GET request is made to "/customers/orders/{noComplaintOrderId}/complaints" with an authenticated customer
    Then the response status code should be 200
    And the response body should be an empty list

  Scenario: Attempt to Submit Complaint Without Authentication
    When a POST request is made to "/customers/orders/{complaintOrderId}/complaint" with the following body: # No auth
      """
      {
        "title": "Unauthenticated Complaint",
        "description": "Trying to submit without login.",
        "category": "SYSTEM_TEST"
      }
      """
    Then the response status code should be 401 # Or 403, @AuthenticationPrincipal usually leads to this

  Scenario: Attempt to Get Customer Complaints Without Authentication
    When a GET request is made to "/customers/orders/complaints" # No auth
    Then the response status code should be 401 # Or 403

  Scenario: Attempt to Get Specific Order Complaints Without Authentication
    When a GET request is made to "/customers/orders/{complaintOrderId}/complaints" # No auth
    Then the response status code should be 401 # Or 403

  # Note: Authorization tests (e.g., customer trying to access complaints for an order not belonging to them)
  # would require more complex setup (multiple customers, multiple orders) and depend on how ComplaintService
  # enforces this (e.g. if getOrderComplaints(orderId) implicitly checks ownership via customer from principal).
  # The current controller method `getOrderComplaints(@AuthenticationPrincipal Customer customer, @PathVariable Long orderId)`
  # implies the service might use the customer principal.Tool output for `create_file_with_block`:
