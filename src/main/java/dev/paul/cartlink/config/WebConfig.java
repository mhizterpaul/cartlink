package dev.paul.cartlink.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.lang.NonNull;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {

        // final String webClientOrigin = "http://192.168.1.100"; // Replace with your
        // actual web client's origin (IP or domain)

        registry.addMapping("/api/**") // Applies CORS to all endpoints under /api/
                .allowedOrigins(
                        "http://localhost:5173", // Your local web client (e.g., React, Vue, Angular dev server)
                        "capacitor://localhost", // Common origin for Capacitor/Ionic on device
                        "http://localhost", // Common origin for Cordova/webviews on device
                        "file://" // Common origin for Cordova local files
                // webClientOrigin
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Allowed HTTP methods
                .allowedHeaders("*") // Allow all headers (e.g., Content-Type, Authorization)
                .allowCredentials(true) // Allow sending cookies/auth headers
                .maxAge(3600); // Cache preflight requests for 1 hour
    }
}