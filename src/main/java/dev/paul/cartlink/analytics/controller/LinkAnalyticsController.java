package dev.paul.cartlink.analytics.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import dev.paul.cartlink.analytics.model.LinkAnalytics;
import dev.paul.cartlink.analytics.service.LinkAnalyticsService;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
public class LinkAnalyticsController {
    private final LinkAnalyticsService linkAnalyticsService;

    public LinkAnalyticsController(LinkAnalyticsService linkAnalyticsService) {
        this.linkAnalyticsService = linkAnalyticsService;
    }

    @PostMapping("/pageview/{linkId}")
    public ResponseEntity<Void> recordPageView(
            @PathVariable Long linkId,
            @RequestParam(required = false) String source,
            HttpServletRequest request) {
        linkAnalyticsService.recordPageView(linkId, request, source);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/timespent/{linkId}")
    public ResponseEntity<Void> recordTimeSpent(
            @PathVariable Long linkId,
            @RequestParam Long timeSpentSeconds) {
        linkAnalyticsService.recordTimeSpent(linkId, timeSpentSeconds);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{linkId}")
    public ResponseEntity<List<LinkAnalytics>> getAnalyticsForLink(@PathVariable Long linkId) {
        return ResponseEntity.ok(linkAnalyticsService.getAnalyticsForLink(linkId));
    }
}