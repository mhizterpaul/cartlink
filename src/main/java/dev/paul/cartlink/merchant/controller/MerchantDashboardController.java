package dev.paul.cartlink.merchant.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.paul.cartlink.merchant.service.MerchantService;

import org.springframework.http.ResponseEntity;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/merchant/dashboard")
public class MerchantDashboardController {

    private final MerchantService merchantService;

    public MerchantDashboardController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = merchantService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/sales-data")
    public ResponseEntity<List<Map<String, Object>>> getSalesData() {
        // Replace with actual logic to fetch sales data
        List<Map<String, Object>> salesData = merchantService.getSalesDataForChart(); // Assuming this method exists or
                                                                                      // will be created
        return ResponseEntity.ok(salesData);
    }

    @GetMapping("/traffic-data")
    public ResponseEntity<List<Map<String, Object>>> getTrafficData() {
        // Replace with actual logic to fetch traffic data
        List<Map<String, Object>> trafficData = merchantService.getTrafficDataForChart(); // Assuming this method exists
                                                                                          // or will be created
        return ResponseEntity.ok(trafficData);
    }
}