package dev.paul.cartlink.product.controller;

import dev.paul.cartlink.product.model.Product;
import dev.paul.cartlink.product.service.ProductService;
import dev.paul.cartlink.merchant.model.Merchant;
import dev.paul.cartlink.merchant.dto.MerchantProduct;

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
            product.setModel((String) request.get("model"));
            product.setManufacturer((String) request.get("manufacturer"));
            product.setDescription((String) request.get("description"));
            product.setSpecifications((Map<String, String>) request.get("specifications"));

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
            product.setModel((String) request.get("model"));
            product.setManufacturer((String) request.get("manufacturer"));
            product.setDescription((String) request.get("description"));
            product.setSpecifications((Map<String, String>) request.get("specifications"));

            MerchantProduct merchantProduct = productService.updateProduct(
                    productId,
                    product,
                    (Integer) request.get("stock"),
                    (Double) request.get("price"),
                    (Double) request.get("discount"),
                    (String) request.get("logisticsProvider"));

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
}