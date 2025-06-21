package dev.paul.cartlink.product.model;

import dev.paul.cartlink.product.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class ProductSpecificationsTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void testProductSpecifications() {
        // Create a new Product instance
        Product product = new Product();
        product.setName("Test Product");
        product.setPrice(10.0);
        product.setUnitsInStock(100);
        product.setSpecifications(new HashMap<>());

        // Save this product
        Product savedProduct = productRepository.save(product);

        // Retrieve the product from the database
        Product retrievedProduct = productRepository.findById(savedProduct.getProductId()).orElse(null);
        assertNotNull(retrievedProduct);

        // Add a few key-value pairs to its specifications map
        Map<String, String> specifications = retrievedProduct.getSpecifications();
        if (specifications == null) {
            specifications = new HashMap<>(); // Ensure specifications is not null
        }
        specifications.put("color", "blue");
        specifications.put("material", "cotton");
        retrievedProduct.setSpecifications(specifications);


        // Save the updated product
        productRepository.save(retrievedProduct);

        // Retrieve the product again by its ID
        Product updatedProduct = productRepository.findById(retrievedProduct.getProductId()).orElse(null);
        assertNotNull(updatedProduct);

        // Assert that the specifications map in the retrieved product is not null
        assertNotNull(updatedProduct.getSpecifications());

        // Assert that the specifications map contains the keys "color" and "material"
        assertTrue(updatedProduct.getSpecifications().containsKey("color"));
        assertTrue(updatedProduct.getSpecifications().containsKey("material"));

        // Assert that the values for "color" and "material" are "blue" and "cotton" respectively
        assertEquals("blue", updatedProduct.getSpecifications().get("color"));
        assertEquals("cotton", updatedProduct.getSpecifications().get("material"));

        System.out.println("Product specifications test passed successfully!");
    }
}
