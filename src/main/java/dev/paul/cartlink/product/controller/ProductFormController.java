package dev.paul.cartlink.product.controller;

import dev.paul.cartlink.product.service.ProductFormSchemaService;
import dev.paul.cartlink.product.model.ProductFormSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

@RestController
@RequestMapping("/api/v1/merchants/{merchantId}/products/form")
public class ProductFormController {
    private final ProductFormSchemaService schemaService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public ProductFormController(ProductFormSchemaService schemaService) {
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
            @RequestBody(required = false) Map<String, Object> jsonBody
    ) {
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
        String typeId = null;
        String representativeWord = null;
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "python3", "llm_form_generator.py",
                "--category", category,
                "--productType", productType,
                "--brand", brand,
                "--name", name,
                "--description", description
            );
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
            // Parse typeId and representativeWord from output
            JsonNode root = objectMapper.readTree(schemaJson);
            typeId = root.has("typeId") ? root.get("typeId").asText() : null;
            representativeWord = root.has("representativeWord") ? root.get("representativeWord").asText() : null;
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to call LLM service", "details", e.getMessage()));
        }

        // Save schema to DB
        ProductFormSchema schema = schemaService.saveOrUpdate(
            productType,
            schemaJson,
            "LLM",
            "active",
            null
        );
        Map<String, Object> response = new HashMap<>();
        response.put("productType", productType);
        response.put("typeId", typeId);
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
        @GetMapping("/{typeId}")
        public ModelAndView renderProductFormHtml(@PathVariable String typeId) {
            // Find schema by typeId
            ProductFormSchema schema = schemaService.getByTypeId(typeId).orElse(null);
            if (schema == null) {
                return new ModelAndView("error").addObject("message", "Form schema not found");
            }
            // Parse fields from schemaJson, excluding 'specifications'
            java.util.List fields = new java.util.ArrayList<>();
            try {
                com.fasterxml.jackson.databind.JsonNode root = new com.fasterxml.jackson.databind.ObjectMapper().readTree(schema.getSchemaJson());
                if (root.has("fields")) {
                    for (com.fasterxml.jackson.databind.JsonNode field : root.get("fields")) {
                        if (!field.has("name") || !"specifications".equals(field.get("name").asText())) {
                            fields.add(new com.fasterxml.jackson.databind.ObjectMapper().convertValue(field, java.util.Map.class));
                        }
                    }
                }
            } catch (Exception e) {
                return new ModelAndView("error").addObject("message", "Invalid schema format");
            }
            ModelAndView mv = new ModelAndView("merchant/form/productForm.jte");
            mv.addObject("typeId", typeId);
            mv.addObject("fields", fields);
            mv.addObject("productType", schema.getProductType());
            return mv;
        }
    }
}

