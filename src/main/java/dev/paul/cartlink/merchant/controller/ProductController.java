package dev.paul.cartlink.merchant.controller;

import dev.paul.cartlink.product.model.Product;
import dev.paul.cartlink.product.service.ProductService;
import dev.paul.cartlink.merchant.model.Merchant;
import dev.paul.cartlink.merchant.model.MerchantProduct;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/merchants/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<?> addProduct(@AuthenticationPrincipal Merchant merchant,
            @RequestBody Map<String, Object> request) {
        try {
            Product product = new Product();
            product.setName((String) request.get("name"));
            product.setDescription((String) request.get("description"));

            MerchantProduct merchantProduct = productService.addProduct(
                    merchant,
                    product,
                    (Integer) request.get("stock"),
                    (Double) request.get("price"),
                    (Double) request.get("discount"),
                    (String) request.get("logisticsProvider"));

            return ResponseEntity.ok(Map.of(
                    "productId", merchantProduct.getProduct().getProductId(),
                    "merchantProductId", merchantProduct.getId(),
                    "message", "Product added successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{productId}")
    public ResponseEntity<?> updateProduct(@AuthenticationPrincipal Merchant merchant,
            @PathVariable Long productId,
            @RequestBody Map<String, Object> request) {
        try {
            Product product = new Product();
            product.setName((String) request.get("name"));
            product.setDescription((String) request.get("description"));

            return ResponseEntity.ok(Map.of(
                    "message", "Product updated successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<?> deleteProduct(@AuthenticationPrincipal Merchant merchant,
            @PathVariable Long productId) {
        try {
            productService.deleteProduct(productId);
            return ResponseEntity.ok(Map.of("message", "Product deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getProducts(@AuthenticationPrincipal Merchant merchant) {
        List<MerchantProduct> products = productService.getMerchantProducts(merchant);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchProducts(@AuthenticationPrincipal Merchant merchant,
            @RequestParam String query) {
        List<MerchantProduct> products = productService.searchMerchantProducts(merchant, query);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/in-stock")
    public ResponseEntity<?> getInStockProducts(@AuthenticationPrincipal Merchant merchant) {
        List<MerchantProduct> products = productService.getInStockProducts(merchant);
        return ResponseEntity.ok(products);
    }

    // Get product details by productId
    @GetMapping("/{productId}")
    public ResponseEntity<?> getProductDetails(@PathVariable Long productId) {
        Product product = productService.getProductById(productId);
        if (product == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Product not found"));
        }
        return ResponseEntity.ok(product);
    }

    // Batch upload products
    @PostMapping("/batch-upload")
    public ResponseEntity<?> batchUploadProducts(@AuthenticationPrincipal Merchant merchant,
            @RequestBody List<Map<String, Object>> products) {
        List<Map<String, Object>> failed = productService.batchUploadProducts(merchant, products);
        boolean success = failed.isEmpty();
        return ResponseEntity.ok(Map.of(
                "success", success,
                "message", success ? "All products uploaded successfully" : "Some products failed to upload",
                "failedItems", failed));
    }
}