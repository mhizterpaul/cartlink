package dev.paul.cartlink.product.service;

import dev.paul.cartlink.product.model.ProductFormSchema;
import dev.paul.cartlink.product.repository.ProductFormSchemaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProductFormSchemaService {
    private final ProductFormSchemaRepository schemaRepository;

    @Autowired
    public ProductFormSchemaService(ProductFormSchemaRepository schemaRepository) {
        this.schemaRepository = schemaRepository;
    }

    public Optional<ProductFormSchema> getByProductId(String productId) {
        // Search for schema where schemaJson->>'productId' = :productId (Postgres JSONB
        // query)
        // This requires a custom query in the repository (not shown here)
        // For now, fallback to searching all and filtering in memory
        return schemaRepository.findAll().stream()
                .filter(schema -> {
                    try {
                        com.fasterxml.jackson.databind.JsonNode node = new com.fasterxml.jackson.databind.ObjectMapper()
                                .readTree(schema.getSchemaJson());
                        return node.has("productId") && productId.equals(node.get("productId").asText());
                    } catch (Exception e) {
                        return false;
                    }
                })
                .findFirst();
    }

    public ProductFormSchema saveOrUpdate(String productType, String schemaJson, String source, String status,
            String usageStatsJson) {
        Optional<ProductFormSchema> existing = schemaRepository.findByProductType(productType);
        ProductFormSchema schema = existing.orElseGet(ProductFormSchema::new);
        schema.setProductType(productType);
        schema.setSchemaJson(schemaJson);
        schema.setSource(source);
        schema.setStatus(status);
        schema.setUsageStatsJson(usageStatsJson);
        return schemaRepository.save(schema);
    }
}