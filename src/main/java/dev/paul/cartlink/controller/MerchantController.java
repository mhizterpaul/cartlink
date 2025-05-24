package dev.paul.cartlink.controller;

import dev.paul.cartlink.dto.SignUpRequest;
import dev.paul.cartlink.model.Merchant;
import dev.paul.cartlink.model.Review;
import dev.paul.cartlink.model.Complaint;
import dev.paul.cartlink.model.Order;
import dev.paul.cartlink.service.MerchantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/merchant")
public class MerchantController {
    private static final Logger logger = LoggerFactory.getLogger(MerchantController.class);
    @Autowired
    private MerchantService merchantService;

    @PostMapping("/signup")
    public ResponseEntity<?> registerMerchant(@RequestBody SignUpRequest request) {
        try {
            Merchant merchant = new Merchant();
            merchant.setEmail(request.getEmail());
            merchant.setPassword(request.getPassword());
            merchant.setFirstName(request.getFirstName());
            merchant.setLastName(request.getLastName());
            merchant.setMiddleName(request.getMiddleName());
            merchant.setPhoneNumber(request.getPhoneNumber());

            Merchant registeredMerchant = merchantService.registerMerchant(merchant);
            return ResponseEntity.ok(Map.of(
                    "merchantId", registeredMerchant.getMerchantId(),
                    "message", "Merchant registered successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        try {
            logger.info("Login attempt for email: {}", credentials.get("email"));

            if (credentials.get("email") == null || credentials.get("password") == null) {
                logger.error("Missing email or password in login request");
                return ResponseEntity.badRequest().body(Map.of("error", "Email and password are required"));
            }

            String token = merchantService.login(
                    credentials.get("email"),
                    credentials.get("password"));

            // Get merchant details
            Merchant merchant = merchantService.getMerchantByEmail(credentials.get("email"));
            logger.info("Login successful for merchant: {}", merchant.getEmail());

            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "merchant", Map.of(
                            "merchantId", merchant.getMerchantId(),
                            "email", merchant.getEmail(),
                            "firstName", merchant.getFirstName() != null ? merchant.getFirstName() : "",
                            "lastName", merchant.getLastName() != null ? merchant.getLastName() : "",
                            "middleName", merchant.getMiddleName() != null ? merchant.getMiddleName() : "",
                            "phoneNumber", merchant.getPhoneNumber() != null ? merchant.getPhoneNumber() : "")));
        } catch (IllegalArgumentException e) {
            logger.error("Login failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error during login: ", e);
            return ResponseEntity.badRequest().body(Map.of("error", "An unexpected error occurred"));
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            String newToken = merchantService.refreshToken(token);
            return ResponseEntity.ok(Map.of("token", newToken));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/password-reset-request")
    public ResponseEntity<?> requestPasswordReset(@RequestBody Map<String, String> request) {
        try {
            merchantService.requestPasswordReset(request.get("email"));
            return ResponseEntity.ok(Map.of("message", "Password reset link sent to email"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/password-reset")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        try {
            merchantService.resetPassword(
                    request.get("email"),
                    request.get("resetToken"),
                    request.get("newPassword"));
            return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            merchantService.logout(token);
            return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<Merchant> getMerchantProfile() {
        return ResponseEntity.ok(merchantService.getCurrentMerchant());
    }

    @PutMapping("/profile")
    public ResponseEntity<Merchant> updateMerchantProfile(@RequestBody Merchant merchant) {
        return ResponseEntity.ok(merchantService.updateMerchantProfile(merchant));
    }

    @GetMapping("/reviews")
    public ResponseEntity<List<Review>> getMerchantReviews() {
        return ResponseEntity.ok(merchantService.getMerchantReviews());
    }

    @GetMapping("/complaints")
    public ResponseEntity<List<Complaint>> getMerchantComplaints() {
        return ResponseEntity.ok(merchantService.getMerchantComplaints());
    }

    @GetMapping("/orders")
    public ResponseEntity<List<Order>> getMerchantOrders() {
        return ResponseEntity.ok(merchantService.getMerchantOrders());
    }

    @GetMapping("/dashboard/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        return ResponseEntity.ok(merchantService.getDashboardStats());
    }
}