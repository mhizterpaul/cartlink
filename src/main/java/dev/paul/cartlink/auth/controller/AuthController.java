package dev.paul.cartlink.auth.controller;

import dev.paul.cartlink.auth.service.AuthService;
import gg.jte.TemplateEngine;
import gg.jte.output.StringOutput;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final TemplateEngine templateEngine;

    public AuthController(AuthService authService, TemplateEngine templateEngine) {
        this.authService = authService;
        this.templateEngine = templateEngine;
    }

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam String token, HttpServletRequest request) {
        boolean success = authService.verifyEmail(token, request.getRemoteAddr());
        if (success) {
            return ResponseEntity.ok("Email verified successfully");
        } else {
            return ResponseEntity.badRequest().body("Invalid or expired token");
        }
    }

    @PostMapping("/request-password-reset")
    public ResponseEntity<String> requestPasswordReset(@RequestBody Map<String, String> body) {
        authService.sendPasswordResetEmail(body.get("email"));
        return ResponseEntity.ok("If a user with that email exists, a password reset link has been sent.");
    }

    @GetMapping(path = "/reset-password", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String showResetPasswordForm(@RequestParam String token) {
        StringOutput output = new StringOutput();
        templateEngine.render("auth/password-reset-form.jte", Collections.singletonMap("token", token), output);
        return output.toString();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> body) {
        boolean success = authService.resetPassword(body.get("token"), body.get("password"));
        if (success) {
            return ResponseEntity.ok("Password reset successfully");
        } else {
            return ResponseEntity.badRequest().body("Invalid or expired token");
        }
    }
} 