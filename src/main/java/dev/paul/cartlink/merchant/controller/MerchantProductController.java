package dev.paul.cartlink.merchant.controller;

import dev.paul.cartlink.product.model.Product;
import dev.paul.cartlink.merchant.service.MerchantProductService;
import dev.paul.cartlink.merchant.model.Merchant;
import dev.paul.cartlink.merchant.model.MerchantProduct;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import dev.paul.cartlink.product.service.ProductFormSchemaService;
import dev.paul.cartlink.product.model.ProductFormSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

@RestController
@RequestMapping("/api/merchants/products")
public class MerchantProductController {

    private final MerchantProductService merchantProductService;
    private final ProductFormSchemaService schemaService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MerchantProductController(
            MerchantProductService merchantProductService, ProductFormSchemaService schemaService) {
        this.merchantProductService = merchantProductService;
        this.schemaService = schemaService;
    }

    @PostMapping(consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<?> generateProductForm(
            @PathVariable Long merchantId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String productType,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestBody(required = false) Map<String, Object> jsonBody) {
        // Accept both JSON and form-data
        if (jsonBody != null) {
            category = (String) jsonBody.getOrDefault("category", category);
            productType = (String) jsonBody.getOrDefault("productType", productType);
            brand = (String) jsonBody.getOrDefault("brand", brand);
            name = (String) jsonBody.getOrDefault("name", name);
            description = (String) jsonBody.getOrDefault("description", description);
        }
        if (category == null || productType == null || brand == null || name == null || description == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing required fields"));
        }

        // Call Python LLM service via subprocess (Mod_Python/Apache integration)
        String schemaJson;
        String productId = null;
        String representativeWord = null;
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "python3", "llm_form_generator.py",
                    "--category", category,
                    "--productType", productType,
                    "--brand", brand,
                    "--name", name,
                    "--description", description);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                return ResponseEntity.status(500).body(Map.of("error", "LLM service error"));
            }
            schemaJson = output.toString();
            // Parse productId and representativeWord from output
            JsonNode root = objectMapper.readTree(schemaJson);
            productId = root.has("productId") ? root.get("productId").asText() : null;
            representativeWord = root.has("representativeWord") ? root.get("representativeWord").asText() : null;
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Failed to call LLM service", "details", e.getMessage()));
        }

        // Save schema to DB
        ProductFormSchema schema = schemaService.saveOrUpdate(
                productType,
                schemaJson,
                "LLM",
                "active",
                null);
        Map<String, Object> response = new HashMap<>();
        response.put("productType", productType);
        response.put("productId", productId);
        response.put("representativeWord", representativeWord);
        response.put("schema", schemaJson);
        response.put("status", "saved");
        return ResponseEntity.ok(response);
    }

    // Render HTML form using JTE
    @Controller
    @RequestMapping("/form-html")
    public static class ProductFormHtmlController {
        @Autowired
        private ProductFormSchemaService schemaService;

        @GetMapping("/{productId}")
        public ModelAndView renderProductFormHtml(@PathVariable String productId) {
            // Find schema by productId
            ProductFormSchema schema = schemaService.getByProductId(productId).orElse(null);
            if (schema == null) {
                return new ModelAndView("error").addObject("message", "Form schema not found");
            }
            // Parse fields from schemaJson, excluding 'specifications'
            java.util.List fields = new java.util.ArrayList<>();
            try {
                com.fasterxml.jackson.databind.JsonNode root = new com.fasterxml.jackson.databind.ObjectMapper()
                        .readTree(schema.getSchemaJson());
                if (root.has("fields")) {
                    for (com.fasterxml.jackson.databind.JsonNode field : root.get("fields")) {
                        if (!field.has("name") || !"specifications".equals(field.get("name").asText())) {
                            fields.add(new com.fasterxml.jackson.databind.ObjectMapper().convertValue(field,
                                    java.util.Map.class));
                        }
                    }
                }
            } catch (Exception e) {
                return new ModelAndView("error").addObject("message", "Invalid schema format");
            }
            ModelAndView mv = new ModelAndView("merchant/form/productForm.jte");
            mv.addObject("productId", productId);
            mv.addObject("fields", fields);
            mv.addObject("productType", schema.getProductType());
            return mv;
        }
    }

    @PostMapping
    public ResponseEntity<?> addMerchantProduct(@AuthenticationPrincipal Merchant merchant,
            @RequestBody Map<String, Object> request) {
        try {
            MerchantProduct merchantProduct = new MerchantProduct();
            merchantProduct.setDescription((String) request.get("description"));
            merchantProduct.setStock((Integer) request.get("stock"));
            merchantProduct.setPrice((Double) request.get("price"));

            Product product = new Product();
            product.setName((String) request.get("name"));
            product.setBrand((String) request.get("brand"));
            product.setCategory((String) request.get("category"));

            MerchantProduct saved = merchantProductService.addMerchantProduct(
                    merchant,
                    product,
                    merchantProduct.getStock(),
                    merchantProduct.getPrice(),
                    merchantProduct.getDescription());

            return ResponseEntity.ok(Map.of(
                    "productId", saved.getProduct().getProductId(),
                    "merchantProductId", saved.getId(),
                    "message", "Merchant product added successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{merchantProductId}")
    public ResponseEntity<?> updateMerchantProduct(@AuthenticationPrincipal Merchant merchant,
            @PathVariable Long merchantProductId,
            @RequestBody Map<String, Object> request) {
        try {
            MerchantProduct merchantProduct = new MerchantProduct();
            merchantProduct.setDescription((String) request.get("description"));
            merchantProduct.setStock((Integer) request.get("stock"));
            merchantProduct.setPrice((Double) request.get("price"));

            Product product = new Product();
            product.setName((String) request.get("name"));
            product.setBrand((String) request.get("brand"));
            product.setCategory((String) request.get("category"));

            merchantProductService.updateMerchantProduct(merchant, merchantProductId, product,
                    merchantProduct.getStock(), merchantProduct.getPrice(), merchantProduct.getDescription());

            return ResponseEntity.ok(Map.of(
                    "message", "Merchant product updated successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{merchantProductId}")
    public ResponseEntity<?> deleteMerchantProduct(@AuthenticationPrincipal Merchant merchant,
            @PathVariable Long merchantProductId) {
        try {
            merchantProductService.deleteMerchantProduct(merchant, merchantProductId);
            return ResponseEntity.ok(Map.of("message", "Merchant product deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getMerchantProducts(@AuthenticationPrincipal Merchant merchant) {
        List<MerchantProduct> products = merchantProductService.getMerchantProducts(merchant);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchMerchantProducts(@AuthenticationPrincipal Merchant merchant,
            @RequestParam String query) {
        List<MerchantProduct> products = merchantProductService.searchMerchantProducts(merchant, query);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/in-stock")
    public ResponseEntity<?> getInStockMerchantProducts(@AuthenticationPrincipal Merchant merchant) {
        List<MerchantProduct> products = merchantProductService.getInStockMerchantProducts(merchant);
        return ResponseEntity.ok(products);
    }

    // Batch upload merchant products
    @PostMapping("/batch-upload")
    public ResponseEntity<?> batchUploadMerchantProducts(@AuthenticationPrincipal Merchant merchant,
            @RequestBody List<Map<String, Object>> products) {
        List<Map<String, Object>> failed = merchantProductService.batchUploadMerchantProducts(merchant, products);
        boolean success = failed.isEmpty();
        return ResponseEntity.ok(Map.of(
                "success", success,
                "message",
                success ? "All merchant products uploaded successfully" : "Some merchant products failed to upload",
                "failedItems", failed));
    }
}