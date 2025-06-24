package dev.paul.cartlink.merchant.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dev.paul.cartlink.merchant.service.MerchantService;

import org.springframework.http.ResponseEntity;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/merchant")
public class MerchantDashboardController {

    private final MerchantService merchantService;

    public MerchantDashboardController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    @GetMapping("/dashboard/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = merchantService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/dashboard/sales-data")
    public ResponseEntity<List<Map<String, Object>>> getSalesData(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        List<Map<String, Object>> salesData = merchantService.getSalesDataForChart(period, startDate, endDate);
        return ResponseEntity.ok(salesData);
    }

    @GetMapping("/dashboard/traffic-data")
    public ResponseEntity<List<Map<String, Object>>> getTrafficData() {
        List<Map<String, Object>> trafficData = merchantService.getTrafficDataForChart();
        return ResponseEntity.ok(trafficData);
    }
}