package dev.paul.cartlink.controller;

import dev.paul.cartlink.model.*;
import dev.paul.cartlink.service.ProductLinkService;
import dev.paul.cartlink.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/merchants/products")
public class ProductLinkController {

    private final ProductLinkService productLinkService;
    private final ProductService productService;

    public ProductLinkController(ProductLinkService productLinkService,
                               ProductService productService) {
        this.productLinkService = productLinkService;
        this.productService = productService;
    }

    @PostMapping("/{productId}/generate-link")
    public ResponseEntity<?> generateProductLink(@AuthenticationPrincipal Merchant merchant,
                                               @PathVariable Long productId) {
        try {
            MerchantProduct merchantProduct = productService.getMerchantProducts(merchant).stream()
                    .filter(mp -> mp.getProduct().getProductId().equals(productId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Product not found"));

            ProductLink productLink = productLinkService.generateProductLink(merchantProduct);
            return ResponseEntity.ok(Map.of(
                "linkId", productLink.getLinkId(),
                "url", productLink.getUrl(),
                "qrCode", productLink.getQrCode()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/links")
    public ResponseEntity<?> getProductLinks(@AuthenticationPrincipal Merchant merchant) {
        List<ProductLink> links = productLinkService.getMerchantProductLinks(merchant);
        return ResponseEntity.ok(links);
    }

    @GetMapping("/links/{linkId}/analytics")
    public ResponseEntity<?> getLinkAnalytics(@AuthenticationPrincipal Merchant merchant,
                                            @PathVariable Long linkId,
                                            @RequestParam(required = false) String startDate,
                                            @RequestParam(required = false) String endDate) {
        try {
            ProductLink productLink = productLinkService.getMerchantProductLinks(merchant).stream()
                    .filter(link -> link.getLinkId().equals(linkId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Link not found"));

            LocalDateTime start = startDate != null ? LocalDateTime.parse(startDate) : LocalDateTime.now().minusDays(30);
            LocalDateTime end = endDate != null ? LocalDateTime.parse(endDate) : LocalDateTime.now();

            List<LinkAnalytics> analytics = productLinkService.getLinkAnalytics(productLink, start, end);
            return ResponseEntity.ok(analytics);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/links/{linkId}/traffic")
    public ResponseEntity<?> getTrafficSources(@AuthenticationPrincipal Merchant merchant,
                                             @PathVariable Long linkId) {
        try {
            ProductLink productLink = productLinkService.getMerchantProductLinks(merchant).stream()
                    .filter(link -> link.getLinkId().equals(linkId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Link not found"));

            List<LinkAnalytics> trafficSources = productLinkService.getTrafficSources(productLink);
            return ResponseEntity.ok(trafficSources);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
} 