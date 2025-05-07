package dev.paul.cartlink.controller;

import dev.paul.cartlink.model.*;
import dev.paul.cartlink.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/merchants/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<?> getOrders(@AuthenticationPrincipal Merchant merchant,
                                     @RequestParam(required = false) String status,
                                     @RequestParam(required = false) String startDate,
                                     @RequestParam(required = false) String endDate) {
        try {
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
            return ResponseEntity.ok(orders);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(@AuthenticationPrincipal Merchant merchant,
                                             @PathVariable Long orderId,
                                             @RequestBody Map<String, String> request) {
        try {
            Order order = orderService.updateOrderStatus(orderId, OrderStatus.valueOf(request.get("status")));
            return ResponseEntity.ok(Map.of(
                "message", "Order status updated successfully",
                "orderId", order.getOrderId(),
                "newStatus", order.getStatus()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{orderId}/tracking")
    public ResponseEntity<?> updateOrderTracking(@AuthenticationPrincipal Merchant merchant,
                                               @PathVariable Long orderId,
                                               @RequestBody Map<String, String> request) {
        try {
            orderService.updateOrderTracking(orderId, request.get("trackingId"));
            return ResponseEntity.ok(Map.of(
                "message", "Tracking information updated successfully",
                "orderId", orderId,
                "trackingId", request.get("trackingId")
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/link/{linkId}")
    public ResponseEntity<?> getOrdersByLink(@AuthenticationPrincipal Merchant merchant,
                                           @PathVariable Long linkId) {
        try {
            List<Order> orders = orderService.getOrdersByProductLink(linkId);
            return ResponseEntity.ok(orders);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
} 