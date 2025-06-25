
## 🔁 Payment Flow Overview

1. **Customer places an order** → Order is created in DB (status: `PENDING_PAYMENT`)
2. **Customer pays via Flutterwave** → Supported methods: `card`, `ussd`, `banktransfer`
3. **Webhook or manual verification confirms success/failure**
4. **Update `order.status`**:

   * `PAID` → if successful
   * `FAILED` → if unsuccessful
5. **Hold funds in app wallet** until:

   * `order.status` becomes `DELIVERED` → then forward to merchant
   * If after 2 weeks `order.status` is not `SHIPPED` and no complaint exists → auto refund

---

## 🧾 Entity Summary

### ✅ `Order` Entity

```json
{
  "id": "ORDER-001",
  "customerId": "USER-123",
  "merchantId": "MERCHANT-456",
  "amount": 1500,
  "currency": "NGN",
  "status": "PENDING_PAYMENT",  // [PENDING_PAYMENT, PAID, FAILED, DELIVERED, REFUNDED, SHIPPED]
  "txRef": "TX-ORDER-001",
  "flwRef": null,
  "createdAt": "2025-06-25T12:00:00Z",
  "paidAt": null,
  "shippedAt": null,
  "deliveredAt": null,
  "refundStatus": null
}
```

---

## 🔐 Authentication

All Flutterwave API requests use:

```
Authorization: Bearer <FLW_SECRET_KEY>
```

---

## 🔧 API Spec

### 1. Initiate Payment

**POST** `/v1/payments/initiate`

Creates a payment session with Flutterwave.

**Request:**

```json
{
  "orderId": "ORDER-001",
  "redirectUrl": "https://yourapp.com/payment-redirect"
}
```

**Response:**

```json
{
  "paymentUrl": "https://checkout.flutterwave.com/v3/xxxxxxx"
}
```

Backend uses Flutterwave’s `/v3/payments` to create this.

---

### 2. Flutterwave Webhook

**POST** `/v1/webhooks/flutterwave`

Handles status updates from Flutterwave.

**Steps:**

* Verify signature using `X-FLWR-SIGNATURE`
* Match `tx_ref` to order
* If success: set `order.status = PAID`, save `flw_ref`
* If failure: set `order.status = FAILED`

---

### 3. Mark Order Delivered

**PATCH** `/v1/orders/:orderId/delivered`

Once merchant delivers order.

* Set `order.status = DELIVERED`
* Initiate **transfer** from app wallet to merchant using:

**POST** `/v3/transfers`

```json
{
  "account_bank": "044",  // e.g. Access Bank
  "account_number": "0123456789",
  "amount": 1500,
  "currency": "NGN",
  "reference": "ORDER-001-MERCHANT-PAYOUT",
  "narration": "Payout for delivered order ORDER-001"
}
```

---

### 4. Automatic Refund Logic (Scheduled Task)

Run daily (cron job or job queue):

```pseudo
For each order where:
  status == "PAID"
  AND shippedAt == null
  AND createdAt + 14 days < now
  AND no complaints linked
Then:
  - call POST /v3/refunds with order.transaction_id
  - update order.status = REFUNDED
```

**Refund API call:**

```json
{
  "transaction_id": 1234567,
  "amount": 1500
}
```

---

### 5. Complaint Check (optional)

Assume you have:

* `Complaints` entity linked to `orderId`
* `orders/:id/complaints` returns empty list if no complaints exist

---

## 🧠 Summary of Logic

| Event                        | Action                                  |
| ---------------------------- | --------------------------------------- |
| Customer pays                | Initiate `/v3/payments`                 |
| Flutterwave confirms success | Update order to `PAID`, store `flw_ref` |
| Flutterwave confirms failure | Update order to `FAILED`                |
| Merchant delivers order      | Update to `DELIVERED`, transfer funds   |
| No delivery after 14 days    | Check complaints, auto-refund if none   |
| Refund done                  | Update order to `REFUNDED`              |

---

## 🔄 Optional Enhancements

* **Notification system**: Email/SMS on status change
* **Admin dashboard**: Track pending deliveries and refunds

## 🔧 Step-by-Step Setup: Flutterwave + Spring Boot

---

### ✅ 1. Add Flutterwave SDK to Your `pom.xml`

```xml
<dependency>
    <groupId>com.flutterwave</groupId>
    <artifactId>flutterwave-java</artifactId>
    <version>1.1.5</version> <!-- latest as of now -->
</dependency>
```

---

### ✅ 2. Create Configuration Class

Create a config class to inject your Flutterwave credentials.

```java
@Configuration
public class FlutterwaveConfig {
    @Value("${flutterwave.secret-key}")
    private String secretKey;

    @Bean
    public Flutterwave flutterwaveClient() {
        return new Flutterwave(secretKey, Flutterwave.Environment.LIVE); // or .STAGING
    }
}
```

Add to `application.yml` or `application.properties`:

```yaml
flutterwave:
  secret-key: FLWSECK_TEST-xxxxxxxxxxxxxxxxxx
```

---

### ✅ 3. Service to Handle Payment Initialization

```java
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final Flutterwave flutterwave;

    public String initiatePayment(Order order) throws FlutterwaveException {
        PaymentRequest request = PaymentRequest.builder()
            .tx_ref(order.getTxRef())
            .amount(order.getAmount().toString())
            .currency("NGN")
            .redirect_url("https://yourapp.com/payment/redirect")
            .payment_options("card,ussd,banktransfer")
            .customer(new Customer(order.getCustomerEmail(), order.getCustomerName(), order.getCustomerPhone()))
            .customizations(new Customizations("CartLink Purchase", "Payment for Order #" + order.getId(), "https://yourapp.com/logo.png"))
            .build();

        PaymentResponse response = flutterwave.payments().initiatePayment(request);
        return response.getData().getLink(); // redirect customer here
    }
}
```

---

### ✅ 4. Webhook Controller to Handle Events

```java
@RestController
@RequestMapping("/webhooks/flutterwave")
public class FlutterwaveWebhookController {

    @Autowired
    private OrderService orderService;

    @Value("${flutterwave.secret-key}")
    private String secretKey;

    @PostMapping
    public ResponseEntity<?> handleWebhook(@RequestBody String payload, @RequestHeader("X-FLWR-SIGNATURE") String signature) throws Exception {
        String expectedSignature = HmacUtils.hmacSha256Hex(secretKey, payload);

        if (!expectedSignature.equals(signature)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid signature");
        }

        JSONObject json = new JSONObject(payload);
        JSONObject data = json.getJSONObject("data");
        String txRef = data.getString("tx_ref");
        String status = data.getString("status");

        if ("successful".equals(status)) {
            orderService.markOrderAsPaid(txRef, data.getString("flw_ref"), data.getInt("id"));
        } else {
            orderService.markOrderAsFailed(txRef);
        }

        return ResponseEntity.ok("Webhook processed");
    }
}
```

---

### ✅ 5. Verify Payment Manually (optional or for redirect flow)

```java
public void verifyTransaction(int flutterwaveTransactionId) throws FlutterwaveException {
    Transaction transaction = flutterwave.transactions().verifyTransaction(flutterwaveTransactionId);
    if ("successful".equals(transaction.getStatus())) {
        // Update order
    }
}
```

---

### ✅ 6. Transfer Funds to Merchant

```java
public void payMerchant(Order order, Merchant merchant) throws FlutterwaveException {
    TransferRequest transfer = TransferRequest.builder()
        .account_bank(merchant.getBankCode()) // e.g. "044" for Access Bank
        .account_number(merchant.getAccountNumber())
        .amount(order.getAmount().doubleValue())
        .currency("NGN")
        .narration("Payout for Order #" + order.getId())
        .reference("TX-ORDER-" + order.getId() + "-PAYOUT")
        .build();

    TransferResponse response = flutterwave.transfers().initiateTransfer(transfer);
    // handle response, update payout status
}
```

---

### ✅ 7. Scheduled Task to Auto-Refund

```java
@Scheduled(cron = "0 0 3 * * *") // 3 AM daily
public void autoRefundStaleOrders() {
    List<Order> staleOrders = orderRepository.findStaleUnshippedPaidOrdersOlderThan(14);
    for (Order order : staleOrders) {
        if (!complaintRepository.existsByOrderId(order.getId())) {
            flutterwave.refunds().initiateRefund(order.getFlutterwaveTransactionId(), order.getAmount());
            order.setStatus(OrderStatus.REFUNDED);
            orderRepository.save(order);
        }
    }
}
```

---

## 🧾 Application YAML Example

```yaml
flutterwave:
  secret-key: FLWSECK_TEST-xxxxxxxxxxxx
spring:
  main:
    allow-bean-definition-overriding: true
```

---

## 📦 Summary of SDK Features Used

| Action               | SDK Method                                       |
| -------------------- | ------------------------------------------------ |
| Initiate Payment     | `flutterwave.payments().initiatePayment()`       |
| Verify Transaction   | `flutterwave.transactions().verifyTransaction()` |
| Handle Webhook       | Manually parse + validate `X-FLWR-SIGNATURE`     |
| Transfer to Merchant | `flutterwave.transfers().initiateTransfer()`     |
| Refund Transaction   | `flutterwave.refunds().initiateRefund()`         |

