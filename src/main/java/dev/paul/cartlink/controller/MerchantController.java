package dev.paul.cartlink.controller;

import dev.paul.cartlink.model.Merchant;
import dev.paul.cartlink.service.MerchantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/merchants")
public class MerchantController {

    private final MerchantService merchantService;

    public MerchantController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerMerchant(@RequestBody Merchant merchant) {
        try {
            Merchant registeredMerchant = merchantService.registerMerchant(merchant);
            return ResponseEntity.ok(Map.of(
                "merchantId", registeredMerchant.getMerchantId(),
                "message", "Merchant registered successfully"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        try {
            String token = merchantService.login(
                credentials.get("email"),
                credentials.get("password")
            );
            return ResponseEntity.ok(Map.of("token", token));
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
                request.get("newPassword")
            );
            return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
} 