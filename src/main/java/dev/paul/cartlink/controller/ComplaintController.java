package dev.paul.cartlink.controller;

import dev.paul.cartlink.model.Customer;
import dev.paul.cartlink.service.ComplaintService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/customers/orders")
public class ComplaintController {

    private final ComplaintService complaintService;

    public ComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    @PostMapping("/{orderId}/complaint")
    public ResponseEntity<?> submitComplaint(@AuthenticationPrincipal Customer customer,
                                           @PathVariable Long orderId,
                                           @RequestBody Map<String, String> request) {
        try {
            complaintService.submitComplaint(
                customer,
                orderId,
                request.get("title"),
                request.get("description"),
                request.get("category")
            );
            return ResponseEntity.ok(Map.of("message", "Complaint submitted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/complaints")
    public ResponseEntity<?> getCustomerComplaints(@AuthenticationPrincipal Customer customer) {
        return ResponseEntity.ok(complaintService.getCustomerComplaints(customer));
    }

    @GetMapping("/{orderId}/complaints")
    public ResponseEntity<?> getOrderComplaints(@AuthenticationPrincipal Customer customer,
                                              @PathVariable Long orderId) {
        return ResponseEntity.ok(complaintService.getOrderComplaints(orderId));
    }
} 