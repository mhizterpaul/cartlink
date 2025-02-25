package dev.mhizterpaul.sociocart.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class Auth{

    @Autowired
    private AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<String> signUp(@RequestBody Auth auth) {
        authService.signUp(auth);
        return ResponseEntity.ok("User signed up successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<String> logIn(@RequestBody Auth auth) {
        boolean isAuthenticated = authService.logIn(auth);
        if (isAuthenticated) {
            return ResponseEntity.ok("User logged in successfully");
        } else {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }
}
