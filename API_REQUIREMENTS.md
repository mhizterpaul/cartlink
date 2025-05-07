# CartLink API Requirements - Merchant Functionality

## Authentication & User Management
1. Merchant Signup
   - Endpoint: POST /api/merchants/signup
   - Request Body: { email, password, firstName, lastName, middleName, image }
   - Response: { merchantId, token }

2. Merchant Login
   - Endpoint: POST /api/merchants/login
   - Request Body: { email, password }
   - Response: { merchantId, token }

3. Password Reset
   - Request Reset: POST /api/merchants/password-reset-request
   - Reset Password: POST /api/merchants/password-reset
   - Request Body: { email }
   - Response: { success, message }

## Product Management
1. Add New Product
   - Endpoint: POST /api/merchants/products
   - Request Body: { name, model, manufacturer, stock, price, discount, logisticsProvider }
   - Response: { productId, merchantProductId }

2. Edit Product
   - Endpoint: PUT /api/merchants/products/{productId}
   - Request Body: { name, model, manufacturer, stock, price, discount, logisticsProvider }
   - Response: { success, message }

3. Delete Product
   - Endpoint: DELETE /api/merchants/products/{productId}
   - Response: { success, message }

4. List Products
   - Endpoint: GET /api/merchants/products
   - Response: [{ productId, name, model, manufacturer, stock, price, discount, logisticsProvider }]

## Product Link Generation
1. Generate Product Link
   - Endpoint: POST /api/merchants/products/{productId}/generate-link
   - Response: { linkId, url, qrCode }

2. Get Product Links
   - Endpoint: GET /api/merchants/products/links
   - Response: [{ linkId, productId, url, qrCode, clicks, conversions }]

## Order Management
1. View Orders
   - Endpoint: GET /api/merchants/orders
   - Query Parameters: status, startDate, endDate
   - Response: [{ orderId, productDetails, orderSize, status, customerInfo, trackingInfo }]

2. Update Order Status
   - Endpoint: PUT /api/merchants/orders/{orderId}/status
   - Request Body: { status }
   - Response: { success, message }

## Analytics
1. Link Performance
   - Endpoint: GET /api/merchants/analytics/links
   - Query Parameters: startDate, endDate
   - Response: [{ linkId, clicks, conversions, conversionRate, revenue }]

2. Traffic Sources
   - Endpoint: GET /api/merchants/analytics/traffic
   - Query Parameters: startDate, endDate
   - Response: [{ source, visits, conversions, conversionRate }]

## Data Models

### Merchant
```java
public class Merchant {
    private Long merchantId;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String middleName;
    private String image;
    private Wallet wallet;
    private Double rating;
    private Integer ratingCount;
    private List<Review> reviews;
}
```

### Product
```java
public class Product {
    private Long productId;
    private String name;
    private String model;
    private String manufacturer;
    private String description;
    private Map<String, String> specifications;
}
```

### MerchantProduct
```java
public class MerchantProduct {
    private Long id;
    private Merchant merchant;
    private Product product;
    private Integer stock;
    private Double price;
    private Double discount;
    private String logisticsProvider;
}
```

### ProductLink
```java
public class ProductLink {
    private Long linkId;
    private MerchantProduct merchantProduct;
    private String url;
    private String qrCode;
    private Integer clicks;
    private Integer conversions;
    private List<LinkAnalytics> analytics;
}
```

### Order
```java
public class Order {
    private Long orderId;
    private MerchantProduct merchantProduct;
    private Integer orderSize;
    private OrderStatus status;
    private Boolean paid;
    private Customer customer;
    private String trackingId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### Analytics
```java
public class LinkAnalytics {
    private Long analyticsId;
    private ProductLink productLink;
    private String source;
    private String device;
    private String location;
    private LocalDateTime timestamp;
}

# CartLink API Requirements - Customer Functionality

## Authentication & User Management

### Customer Authentication
- **Customer Registration**
  - Endpoint: `POST /api/customers/signup`
  - Request Body:
    ```json
    {
      "email": "string",
      "password": "string",
      "firstName": "string",
      "lastName": "string",
      "phoneNumber": "string",
      "address": {
        "street": "string",
        "city": "string",
        "state": "string",
        "country": "string",
        "postalCode": "string"
      }
    }
    ```
  - Response: JWT token and customer details

- **Customer Login**
  - Endpoint: `POST /api/customers/login`
  - Request Body:
    ```json
    {
      "email": "string",
      "password": "string"
    }
    ```
  - Response: JWT token

- **Password Reset Request**
  - Endpoint: `POST /api/customers/password-reset-request`
  - Request Body:
    ```json
    {
      "email": "string"
    }
    ```
  - Response: Success message

- **Password Reset**
  - Endpoint: `POST /api/customers/password-reset`
  - Request Body:
    ```json
    {
      "email": "string",
      "resetToken": "string",
      "newPassword": "string"
    }
    ```
  - Response: Success message

## Product Management

## Cart Management

### Cart Operations
- **Add to Cart**
  - Endpoint: `POST /api/customers/cart/items`
  - Request Body:
    ```json
    {
      "productLinkId": "string",
      "quantity": "integer"
    }
    ```
  - Response: Updated cart details

- **Remove from Cart**
  - Endpoint: `DELETE /api/customers/cart/items/{itemId}`
  - Response: Updated cart details

- **Update Cart Item Quantity**
  - Endpoint: `PUT /api/customers/cart/items/{itemId}`
  - Request Body:
    ```json
    {
      "quantity": "integer"
    }
    ```
  - Response: Updated cart details

- **Get Cart**
  - Endpoint: `GET /api/customers/cart`
  - Response: Cart details with items

### Checkout
- **Initiate Checkout**
  - Endpoint: `POST /api/customers/checkout`
  - Request Body:
    ```json
    {
      "cartId": "string",
      "shippingAddress": {
        "street": "string",
        "city": "string",
        "state": "string",
        "country": "string",
        "postalCode": "string"
      },
      "paymentMethod": "string",
      "accountDetails": {
        "accountNumber": "string",
        "bankName": "string",
        "accountName": "string"
      }
    }
    ```
  - Response: Order details

## Order Management

### Customer Order Operations
- **Get Customer Orders**
  - Endpoint: `GET /api/customers/orders`
  - Response: List of customer's orders

- **Get Order Details**
  - Endpoint: `GET /api/customers/orders/{orderId}`
  - Response: Order details

- **Submit Complaint**
  - Endpoint: `POST /api/customers/orders/{orderId}/complaint`
  - Request Body:
    ```json
    {
      "title": "string",
      "description": "string",
      "category": "string"
    }
    ```
  - Response: Complaint details

- **Request Refund**
  - Endpoint: `POST /api/customers/orders/{orderId}/refund`
  - Request Body:
    ```json
    {
      "reason": "string"
    }
    ```
  - Response: Refund request details

## Analytics

## Data Models

### Customer
```java
public class Customer {
    private Long customerId;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private Address address;
    private List<Order> orders;
    private List<Complaint> complaints;
    private List<RefundRequest> refundRequests;
}
```

### Cart
```java
public class Cart {
    private Long cartId;
    private Customer customer;
    private List<CartItem> items;
    private Double totalAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### CartItem
```java
public class CartItem {
    private Long itemId;
    private Cart cart;
    private ProductLink productLink;
    private Integer quantity;
    private Double price;
    private Double discount;
}
```

### Complaint
```java
public class Complaint {
    private Long complaintId;
    private Order order;
    private Customer customer;
    private String title;
    private String description;
    private String category;
    private ComplaintStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
}
```

### RefundRequest
```java
public class RefundRequest {
    private Long refundId;
    private Order order;
    private Customer customer;
    private String reason;
    private Double amount;
    private RefundStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;
    private String accountNumber;
    private String bankName;
    private String accountName;
}
```

### Address
```java
public class Address {
    private Long addressId;
    private String street;
    private String city;
    private String state;
    private String country;
    private String postalCode;
}
```

### Enums
```java
public enum ComplaintStatus {
    PENDING, IN_PROGRESS, RESOLVED, REJECTED
}

public enum RefundStatus {
    PENDING, APPROVED, REJECTED, PROCESSED
} 