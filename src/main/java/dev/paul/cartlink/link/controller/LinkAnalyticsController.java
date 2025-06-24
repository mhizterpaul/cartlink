package dev.paul.cartlink.link.controller;

import dev.paul.cartlink.link.model.LinkAnalytics;
import dev.paul.cartlink.link.repository.LinkAnalyticsRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@RestController
@RequestMapping("/api/analytics")
public class LinkAnalyticsController {
    private final LinkAnalyticsRepository linkAnalyticsRepository;

    @Autowired
    public LinkAnalyticsController(LinkAnalyticsRepository linkAnalyticsRepository) {
        this.linkAnalyticsRepository = linkAnalyticsRepository;
    }

    // DTO for update request
    public static class AnalyticsUpdateRequest {
        public String geolocation;
        public Double bounceRate;
        public Long averageTimeSpent;
    }

    @PostMapping("/{analyticsId}")
    public ResponseEntity<?> updateAnalytics(@PathVariable Long analyticsId, @RequestBody AnalyticsUpdateRequest req, HttpServletRequest request) {
        Optional<LinkAnalytics> analyticsOpt = linkAnalyticsRepository.findById(analyticsId);
        if (analyticsOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        LinkAnalytics analytics = analyticsOpt.get();
        if (req.geolocation != null) analytics.setGeolocation(req.geolocation);
        if (req.bounceRate != null) analytics.setBounceRate(req.bounceRate);
        if (req.averageTimeSpent != null) analytics.setAverageTimeSpent(req.averageTimeSpent);
        analytics.setLastUpdated(java.time.LocalDateTime.now());
        linkAnalyticsRepository.save(analytics);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{analyticsId}")
    public ResponseEntity<?> getAnalytics(@PathVariable Long analyticsId) {
        Optional<LinkAnalytics> analyticsOpt = linkAnalyticsRepository.findById(analyticsId);
        if (analyticsOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(analyticsOpt.get());
    }
}