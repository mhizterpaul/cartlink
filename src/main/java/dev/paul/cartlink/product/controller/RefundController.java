package dev.paul.cartlink.product.controller;

import dev.paul.cartlink.customer.model.Customer;
import dev.paul.cartlink.customer.service.RefundService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/customers/orders")
public class RefundController {

    private final RefundService refundService;

    public RefundController(RefundService refundService) {
        this.refundService = refundService;
    }

    @PostMapping("/{orderId}/refund")
    public ResponseEntity<?> requestRefund(@AuthenticationPrincipal Customer customer,
            @PathVariable Long orderId,
            @RequestBody Map<String, String> request) {
        try {
            refundService.requestRefund(
                    customer,
                    orderId,
                    request.get("reason"),
                    request.get("accountNumber"),
                    request.get("bankName"),
                    request.get("accountName"));
            return ResponseEntity.ok(Map.of("message", "Refund request submitted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/refunds")
    public ResponseEntity<?> getCustomerRefunds(@AuthenticationPrincipal Customer customer) {
        return ResponseEntity.ok(refundService.getCustomerRefunds(customer));
    }

    @GetMapping("/{orderId}/refunds")
    public ResponseEntity<?> getOrderRefunds(@AuthenticationPrincipal Customer customer,
            @PathVariable Long orderId) {
        return ResponseEntity.ok(refundService.getOrderRefunds(orderId));
    }
}