# 📦 CartLink API Requirements v1

Base URL: `/api/v1`
All endpoints return JSON unless stated otherwise.
Authentication is done via JWT in `Authorization: Bearer <token>` header.
Customers are tracked via **cookies**.

---

## 🔐 Authentication & User Management

### 🏍️ Merchant Endpoints

#### 1. Sign Up

* **POST** `/api/v1/merchants/signup`

```json
{
  "email": "string",
  "password": "string",
  "firstName": "string",
  "lastName": "string",
  "image": "string"
}
```

* **Responses**:

  * `201 Created`: `{ merchantId, token, merchantDetails }`
  * `400 Bad Request`
  * `500 Internal Server Error`

#### 2. Login

* **POST** `/api/v1/merchants/login`

```json
{
  "email": "string",
  "password": "string"
}
```

* `200 OK`: `{ merchantId, token, merchantDetails }`
* `401 Unauthorized`: Invalid credentials

#### 3. Password Reset

* **Request Reset**: `POST /api/v1/merchants/password-reset-request`

```json
{ "email": "string" }
```

* `200 OK`: `{ success, message }`

* `404 Not Found`

* **Reset Password**: `POST /api/v1/merchants/password-reset`

```json
{ "email": "string", "resetToken": "string", "newPassword": "string" }
```

* `200 OK`: `{ success, message }`
* `400 Bad Request`

#### 4. Refresh Token

* **POST** `/api/v1/merchants/refresh-token`

```json
{ "refreshToken": "string" }
```

* `200 OK`: `{ token }`

#### 5. Get Profile

* **GET** `/api/v1/merchants/{merchantId}`
* `200 OK`: `{ merchantProfile }`
* `401 Unauthorized`

#### 6. Update Profile

* **PUT** `/api/v1/merchants/{merchantId}`

```json
{ "firstName": "string", "lastName": "string", "phoneNumber": "+2348000000000" }
```

* `200 OK`: `{ success, message }`

*Placeholder:* Add audit logging fields to track last login, account creation, and profile edits.

---

### 👤 Customer Endpoints

#### 1. Sign Up

* **POST** `/api/v1/customers/signup`

```json
{
  "email": "string",
  "firstName": "string",
  "lastName": "string",
  "phoneNumber": "+2348000000000",
  "address": {
    "street": "string",
    "city": "string",
    "state": "string",
    "country": "string",
    "postalCode": "string"
  }
}
```

* `201 Created`: JWT token and customer details
* `400 Bad Request`

#### 2. Update Customer Profile

* **PUT** `/api/v1/customers/profile`

```json
{ "firstName": "string", "lastName": "string", "phoneNumber": "+2348000000000", "address": { ... } }
```

* `200 OK`: `{ success, message }`

#### 3. Get Order History

* **GET** `/api/v1/customers/orders/history?page=1&limit=20`
* `200 OK`: List of past orders

---

## 📦 Product Management

### 1. Add Product

* **POST** `/api/v1/merchants/{merchantId}/products`
* **Request Body**: dynamic backend-generated form
* `201 Created`: `{ productId, merchantProductId, productDetails }`
* `401 Unauthorized`

*Note:* Schema for new product validation is created at runtime and used to validate products with the same `typeId`. If a `typeId` derived from a `productType` is not found, the schema is initialized and reused.

### 2. Edit Product

* **PUT** `/api/v1/merchants/{merchantId}/products/{productId}`

```json
{
  "name": "string",
  "model": "string",
  "manufacturer": "string",
  "stock": number,
  "price": number,
  "coupon": number[],
  "productDetails": {},
  "merchantId": "string",
  "customers": []
}
```

* `200 OK`: `{ success, message }`
* `404 Not Found`

### 3. Delete Product

* **DELETE** `/api/v1/merchants/{merchantId}/products/{productId}`
* `200 OK`: `{ success, productId }`

### 4. List Products

* **GET** `/api/v1/merchants/{merchantId}/products?page=1&limit=20&sort=price&order=asc`
* `200 OK`: `[{ productId, name, model, stock, price, ... }]`

### 5. Search Products

* **GET** `/api/v1/merchants/{merchantId}/products/search?query=...`
* `200 OK`: `[{ ... }]`

### 6. Get In-stock Products

* **GET** `/api/v1/merchants/{merchantId}/products/in-stock`
* `200 OK`: `[{ ... }]`

### 7. Get Out-of-stock Products

* **GET** `/api/v1/merchants/{merchantId}/products/out-of-stock`
* `200 OK`: `[{ ... }]`

### 8. Batch Upload Products (CSV/Excel)

* **POST** `/api/v1/merchants/{merchantId}/products/upload`
* `201 Created`: `{ success, addedCount }`

### 9. Download Product Catalogue

* **GET** `/api/v1/merchants/{merchantId}/products/template`
* `200 OK`: File download (Content-Type: `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`)

---

## 🕜 Product Form Generator

### 1. Generate Product Form (HTML)

* **POST** `/api/v1/merchants/{merchantId}/products/form`

```json
{
  "category": "string",
  "productType": "string",
  "brand": "string",
  "name": "string",
  "description": "string"
}
```

* **Accepted Categories**:

  * 📱 Electronics
  * 🍗 Apparel
  * 💄 Cosmetics
  * ⚗️ Chemicals
  * 🥤 Beverages
  * 💪 Mechanical Machine
  * ⚙️ Electromechanical Machine
  * 🔌 Electrical Appliance
  * 💪 Tools
  * 🏠 Home Decor

* `200 OK`: HTML form (Content-Type: `text/html`)

* `400 Bad Request`

*Note:* Validation schema is stored and reused for each `typeId`.

---

## 🔗 Product Link Management

### 1. Generate Link

* **POST** `/api/v1/merchants/{merchantId}/products/{productId}/generate-link`
* `201 Created`: `{ linkId, url }`

### 2. Get All Links

* **GET** `/api/v1/merchants/{merchantId}/products/links?page=1&limit=20`
* `200 OK`: `[{ linkId, productId, url }]`

### 3. Get Link Analytics

* **GET** `/api/v1/merchants/{merchantId}/products/links/{linkId}/analytics?startDate=&endDate=`
* `200 OK`:

```json
{
  "averageDurationSeconds": 35.7,
  "mostClicked": [
    { "linkId": 1, "clicks": 150, "url": "string" }
  ],
  "sourcesByPercentage": [
    { "source": "Facebook", "percentage": 40 },
    { "source": "Twitter", "percentage": 20 }
  ],
  "bounceRate": 42.1,
  "geoDistribution": [
    { "country": "Nigeria", "percentage": 60 },
    { "country": "Kenya", "percentage": 30 }
  ],
  "deviceTypes": [
    { "device": "Mobile", "percentage": 70 },
    { "device": "Desktop", "percentage": 30 }
  ],
  "totalSources": 4,
  "totalClicks": 250
}
```

### 4. Get Link Traffic Sources

* **GET** `/api/v1/merchants/{merchantId}/products/links/{linkId}/traffic`
* `200 OK`: `[{ source: "Facebook", clicks: 154 }, { source: "Google", clicks: 231 }]`

---

## 📦 Order Management

### Merchant

#### 1. View Orders

* **GET** `/api/v1/merchants/{merchantId}/orders?status=&startDate=&endDate=&page=1&limit=20`
* `200 OK`: Order list

#### 2. Update Order Status

* **PUT** `/api/v1/merchants/{merchantId}/orders/{orderId}/status`

```json
{ "status": "string" }
```

* `200 OK`: `{ success, message }`

#### 3. Get Orders By Link

* **GET** `/api/v1/merchants/{merchantId}/orders/link/{linkId}`
* `200 OK`: `[{ ... }]`

### Customer

#### 1. View Cart

* **GET** `/api/v1/customers/cart`
* `200 OK`: Cart object

#### 2. Add to Cart

* **POST** `/api/v1/customers/cart/items`
* `201 Created`

#### 3. Remove from Cart

* **DELETE** `/api/v1/customers/cart/items/{itemId}`
* `200 OK`

#### 4. Update Quantity

* **PUT** `/api/v1/customers/cart/items/{itemId}`

```json
{ "quantity": number }
```

* `200 OK`

---

## 💬 Complaint Handling

### 1. Submit Complaint

* **POST** `/api/v1/customers/orders/{orderId}/complaint`

```json
{ "title": "string", "description": "string", "category": "string" }
```

* `201 Created`: Complaint details

### 2. Get Customer Complaints

* **GET** `/api/v1/customers/orders/complaints`
* `200 OK`: Complaint list

### 3. Get Order Complaints

* **GET** `/api/v1/customers/orders/{orderId}/complaints`
* `200 OK`: Complaint list

---

## 💸 Refund Management

### 1. Submit Refund Request

* **POST** `/api/v1/customers/orders/{orderId}/refund`

```json
{ "reason": "string" }
```

* `201 Created`: Refund request

### 2. Get Customer Refunds

* **GET** `/api/v1/customers/orders/refunds`
* `200 OK`

### 3. Get Order Refunds

* **GET** `/api/v1/customers/orders/{orderId}/refunds`
* `200 OK`

---

## 📊 Merchant Dashboard

### 1. Dashboard Stats

* **GET** `/api/v1/merchants/{merchantId}/dashboard/stats`
* `200 OK`:

```json
{
  "totalSales": 12000.50,
  "totalOrders": 340,
  "todaySales": 540.00,
  "totalCustomers": 78,
  "analytics": {
    "totalSalesChange": 12.5,
    "totalOrdersChange": -5.0,
    "todaySalesChange": 8.2,
    "totalCustomersChange": 4.3
  }
}
```

### 2. Sales Data

* **GET** `/api/v1/merchants/{merchantId}/dashboard/sales-data?period=week|month|quarter&startDate=date&endDate=date`
* `200 OK`: `[{ startDate: "2024-07-01", endDate: "2024-07-07", totalSales: 123.45 }]`

### 3. Traffic Data

* **GET** `/api/v1/merchants/{merchantId}/dashboard/traffic-data`
* `200 OK`: `[{ source: "Facebook", clicks: 154 }, { source: "Google", clicks: 231 }]`

---

## 💳 Coupon Management

### 1. Create Coupon

* **POST** `/api/v1/merchants/{merchantId}/products/{productId}/coupons`

```json
{
  "discount": number,
  "validFrom": "2024-07-01T00:00:00Z",
  "validUntil": "2024-07-10T00:00:00Z",
  "maxUsage": number,
  "maxUsers": number
}
```

* `201 Created`: `{ couponId }`

### 2. Get Coupons for Product

* **GET** `/api/v1/merchants/{merchantId}/products/{productId}/coupons`
* `200 OK`: `[{ couponId, discount, maxUsage, usedCount, validFrom, validUntil }]`

### 3. Delete Coupon

* **DELETE** `/api/v1/merchants/{merchantId}/products/coupons/{couponId}`
* `200 OK`: `{ success: true }`

---

---

## 🔍 Suggestions / Improvements

* Add pagination and filtering support to listing endpoints (e.g., products, orders).
* Placeholder: Add rate limiting and throttling guidance.
* Placeholder: Add endpoint versioning strategy and backward compatibility plan.
* Placeholder: Include consistent error response format.
* Placeholder: Add ISO 8601 date format and E.164 phone number enforcement in documentation or validation layer.
* Placeholder: Add audit trail for critical actions (e.g., price update, refund approval).
* Placeholder: Add batch product update endpoint.
* Placeholder: Add webhook integration for order events.
