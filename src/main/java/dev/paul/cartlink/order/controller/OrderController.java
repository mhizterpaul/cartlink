package dev.paul.cartlink.order.controller;

import dev.paul.cartlink.customer.model.Customer;
import dev.paul.cartlink.customer.service.CustomerService;
import dev.paul.cartlink.merchant.model.Merchant;
import dev.paul.cartlink.merchant.model.MerchantProduct;
import dev.paul.cartlink.merchant.repository.MerchantProductRepository;
import dev.paul.cartlink.order.model.Order;
import dev.paul.cartlink.order.model.OrderStatus;
import dev.paul.cartlink.order.service.OrderService;
import dev.paul.cartlink.payment.service.PaymentService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/merchants/{merchantId}/orders")
public class OrderController {

    private final OrderService orderService;
    private final CustomerService customerService;
    private final MerchantProductRepository merchantProductRepository;
    private final PaymentService paymentService;

    public OrderController(OrderService orderService, CustomerService customerService, MerchantProductRepository merchantProductRepository, PaymentService paymentService) {
        this.orderService = orderService;
        this.customerService = customerService;
        this.merchantProductRepository = merchantProductRepository;
        this.paymentService = paymentService;
    }

    @GetMapping
    public ResponseEntity<?> getOrders(@PathVariable Long merchantId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        try {
            Merchant merchant = orderService.getMerchantById(merchantId);
            List<Order> orders;
            if (status != null) {
                orders = orderService.getMerchantOrdersByStatus(merchant, OrderStatus.valueOf(status));
            } else if (startDate != null && endDate != null) {
                LocalDateTime start = LocalDateTime.parse(startDate);
                LocalDateTime end = LocalDateTime.parse(endDate);
                orders = orderService.getMerchantOrdersByDateRange(merchant, start, end);
            } else {
                orders = orderService.getMerchantOrders(merchant);
            }
            // TODO: Add pagination logic
            return ResponseEntity.ok(orders);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long merchantId,
            @PathVariable Long orderId,
            @RequestBody Map<String, String> request) {
        try {
            Order order = orderService.updateOrderStatus(orderId, OrderStatus.valueOf(request.get("status")));
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Order status updated successfully",
                    "orderId", order.getOrderId(),
                    "newStatus", order.getStatus()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    @GetMapping("api/v1/merchants/{merchantId}/orders/{linkId}")
    public ResponseEntity<?> getOrdersByLink(@PathVariable Long merchantId,
            @PathVariable Long linkId) {
        try {
            List<Order> orders = orderService.getOrdersByProductLink(linkId);
            return ResponseEntity.ok(orders);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Customer places an order (guest or logged in)
    @PostMapping("/api/v1/customers/orders")
    public ResponseEntity<?> placeOrder(
            @RequestBody Map<String, Object> request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Customer customer = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String jwt = authHeader.substring(7);
                customer = customerService.getCustomerFromJwt(jwt);
                if (customer == null) {
                    return ResponseEntity.status(401).body(Map.of("error", "Invalid or expired token"));
                }
            } else {
                Map<String, Object> customerMap = (Map<String, Object>) request.get("customer");
                if (customerMap == null || customerMap.get("email") == null) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Customer email is required for guest checkout"));
                }
                String email = (String) customerMap.get("email");
                customer = customerService.findByEmail(email);
                if (customer == null) {
                    // Create new customer (all details except password required)
                    customer = customerService.getOrCreateCustomer(customerMap);
                }
            }
            // Extract order details
            Long merchantProductId = Long.valueOf(request.get("merchantProductId").toString());
            MerchantProduct merchantProduct = merchantProductRepository.findById(merchantProductId)
                .orElseThrow(() -> new IllegalArgumentException("MerchantProduct not found"));
            Integer quantity = Integer.valueOf(request.get("quantity").toString());
            Long productLinkId = request.get("productLinkId") != null
                    ? Long.valueOf(request.get("productLinkId").toString())
                    : null;
            Order order = orderService.createOrder(merchantProduct, customer, quantity, productLinkId);
            return ResponseEntity.status(201).body(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{orderId}/delivered")
    public ResponseEntity<?> markOrderDelivered(@PathVariable Long merchantId, @PathVariable Long orderId) {
        try {
            Order order = orderService.updateOrderStatus(orderId, OrderStatus.DELIVERED);
            paymentService.payMerchant(orderId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Order marked as delivered and merchant paid",
                "orderId", order.getOrderId(),
                "newStatus", order.getStatus()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}