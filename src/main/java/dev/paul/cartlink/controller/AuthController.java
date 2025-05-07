package dev.paul.cartlink.controller;

import dev.paul.cartlink.dto.AuthResponse;
import dev.paul.cartlink.dto.LoginRequest;
import dev.paul.cartlink.dto.SignUpRequest;
import dev.paul.cartlink.service.SecurityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final SecurityService securityService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signUp(@Valid @RequestBody SignUpRequest signUpRequest, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest()
                    .body(AuthResponse.builder()
                            .message("Validation failed: " + result.getFieldErrors().stream()
                                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                                    .reduce("", (a, b) -> a + "; " + b))
                            .build());
        }

        try {
            UserDetails userDetails = securityService.registerUser(signUpRequest);
            String token = securityService.generateToken(userDetails);

            return ResponseEntity.ok(AuthResponse.builder()
                    .token(token)
                    .userType(userDetails.getAuthorities().stream()
                            .findFirst()
                            .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                            .orElse("USER"))
                    .email(userDetails.getUsername())
                    .message("User registered successfully")
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(AuthResponse.builder()
                            .message("Registration failed: " + e.getMessage())
                            .build());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = securityService.generateToken(userDetails);

            return ResponseEntity.ok(AuthResponse.builder()
                    .token(token)
                    .userType(userDetails.getAuthorities().stream()
                            .findFirst()
                            .map(auth -> auth.getAuthority().replace("ROLE_", ""))
                            .orElse("USER"))
                    .email(userDetails.getUsername())
                    .message("Login successful")
                    .build());
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401)
                    .body(AuthResponse.builder()
                            .message("Invalid credentials")
                            .build());
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestHeader("Authorization") String token) {
        try {
            if (!securityService.validateToken(token)) {
                throw new IllegalArgumentException("Invalid token");
            }

            String email = securityService.getEmailFromToken(token);
            UserDetails userDetails = securityService.loadUserByUsername(email);
            String newToken = securityService.generateToken(userDetails);

            return ResponseEntity.ok(AuthResponse.builder()
                    .token(newToken)
                    .message("Token refreshed successfully")
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body(AuthResponse.builder()
                            .message("Invalid token")
                            .build());
        }
    }
}
