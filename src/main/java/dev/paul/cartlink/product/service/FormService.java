package dev.paul.cartlink.product.service;

import dev.paul.cartlink.product.model.Product;
import dev.paul.cartlink.product.model.ProductType; // Assuming ProductType enum exists in this package
import dev.paul.cartlink.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FormService {

    @Autowired
    private ProductRepository productRepository;

    // Simulated schema/template for product types
    private static final Map<ProductType, List<String>> productTypeSchemas = new HashMap<>();

    static {
        // Example schema: Define some expected fields for different product types
        productTypeSchemas.put(ProductType.ELECTRONICS, List.of("Screen Size", "RAM", "Storage Capacity"));
        productTypeSchemas.put(ProductType.FASHION, List.of("Color", "Material", "Size Fit"));
        productTypeSchemas.put(ProductType.COSMETICS, List.of("Skin Concern", "Volume", "SPF"));
        // Add more types and their expected fields as needed
    }

    /**
     * Prepares details for rendering the product form, including identifying
     * missing fields.
     *
     * @param productId The ID of the product.
     * @return A map containing product details and a list of missing fields.
     *         Returns an empty map if the product is not found.
     */
    public Map<String, Object> getProductDetailsForForm(Long productId) {
        Optional<Product> productOptional = productRepository.findById(productId);
        if (!productOptional.isPresent()) {
            return Collections.emptyMap(); // Or throw an exception
        }

        Product product = productOptional.get();
        ProductType type = product.getType(); // Assuming Product has a getType() method returning ProductType

        Map<String, Object> details = new HashMap<>();
        details.put("product", product);

        List<String> expectedFields = productTypeSchemas.getOrDefault(type, Collections.emptyList());
        Map<String, String> currentSpecifications = product.getSpecifications() == null ? new HashMap<>()
                : product.getSpecifications();

        List<String> missingFields = expectedFields.stream()
                .filter(field -> !currentSpecifications.containsKey(field) || currentSpecifications.get(field) == null
                        || currentSpecifications.get(field).trim().isEmpty())
                .collect(Collectors.toList());

        details.put("missingFields", missingFields);
        details.put("suggestionBanner",
                !missingFields.isEmpty() ? "Add missing fields to help improve the product listing." : "");

        return details;
    }

    /**
     * Updates the product's additional fields (specifications).
     *
     * @param productId       The ID of the product to update.
     * @param submittedFields A map of fields to add or update in the product's
     *                        specifications.
     */
    @Transactional // Ensures the operation is atomic
    public void updateProductWithAdditionalFields(Long productId, Map<String, String> submittedFields) {
        Optional<Product> productOptional = productRepository.findById(productId);
        if (!productOptional.isPresent()) {
            // Handle product not found, perhaps throw an exception
            throw new IllegalArgumentException("Product not found with ID: " + productId);
        }

        Product product = productOptional.get();

        if (product.getSpecifications() == null) {
            product.setSpecifications(new HashMap<>());
        }

        // Merge submitted fields into the existing specifications
        // This will add new fields or update existing ones.
        for (Map.Entry<String, String> entry : submittedFields.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().trim().isEmpty()) {
                product.getSpecifications().put(entry.getKey(), entry.getValue());
            } else {
                // Optionally remove the key if the submitted value is empty/null
                // product.getSpecifications().remove(entry.getKey());
            }
        }

        productRepository.save(product);
    }
}
