package dev.paul.cartlink.complaint.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import dev.paul.cartlink.complaint.service.ComplaintService;
import dev.paul.cartlink.customer.model.Customer;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/customers/orders")
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
            var complaint = complaintService.submitComplaint(
                    customer,
                    orderId,
                    request.get("title"),
                    request.get("description"),
                    request.get("category"));
            // API requires 201 and complaint details
            return ResponseEntity.status(HttpStatus.CREATED).body(complaint);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/complaints")
    public ResponseEntity<?> getCustomerComplaints(@AuthenticationPrincipal Customer customer) {
        var complaints = complaintService.getCustomerComplaints(customer);
        return ResponseEntity.ok(complaints); // 200, complaint list
    }

    @GetMapping("/{orderId}/complaints")
    public ResponseEntity<?> getOrderComplaints(@AuthenticationPrincipal Customer customer,
            @PathVariable Long orderId) {
        var complaints = complaintService.getOrderComplaints(orderId);
        return ResponseEntity.ok(complaints); // 200, complaint list
    }
}