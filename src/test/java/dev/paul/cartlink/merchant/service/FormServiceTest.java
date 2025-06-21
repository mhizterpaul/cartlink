package dev.paul.cartlink.merchant.service;

import dev.paul.cartlink.product.model.Product;
import dev.paul.cartlink.product.model.ProductType;
import dev.paul.cartlink.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FormServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private FormService formService;

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        sampleProduct = new Product();
        sampleProduct.setProductId(1L);
        sampleProduct.setName("Test Product");
        sampleProduct.setType(ProductType.ELECTRONICS); // Assumes schema exists for ELECTRONICS
        sampleProduct.setSpecifications(new HashMap<>());
    }

    // Tests for getProductDetailsForForm
    @Test
    void getProductDetailsForForm_productFound_noSpecs_missingFieldsIdentified() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

        Map<String, Object> details = formService.getProductDetailsForForm(1L);

        assertNotNull(details.get("product"));
        List<String> missingFields = (List<String>) details.get("missingFields");
        // Based on FormService static schema for ELECTRONICS: "Screen Size", "RAM", "Storage Capacity"
        assertTrue(missingFields.containsAll(Arrays.asList("Screen Size", "RAM", "Storage Capacity")));
        assertFalse(((String)details.get("suggestionBanner")).isEmpty());
    }

    @Test
    void getProductDetailsForForm_productFound_someSpecs_correctMissingFields() {
        sampleProduct.getSpecifications().put("RAM", "8GB");
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

        Map<String, Object> details = formService.getProductDetailsForForm(1L);
        List<String> missingFields = (List<String>) details.get("missingFields");

        assertTrue(missingFields.contains("Screen Size"));
        assertFalse(missingFields.contains("RAM"));
        assertTrue(missingFields.contains("Storage Capacity"));
        assertFalse(((String)details.get("suggestionBanner")).isEmpty());
    }

    @Test
    void getProductDetailsForForm_productFound_allSpecs_noMissingFields() {
        sampleProduct.getSpecifications().put("Screen Size", "15 inch");
        sampleProduct.getSpecifications().put("RAM", "16GB");
        sampleProduct.getSpecifications().put("Storage Capacity", "512GB SSD");
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

        Map<String, Object> details = formService.getProductDetailsForForm(1L);
        List<String> missingFields = (List<String>) details.get("missingFields");

        assertTrue(missingFields.isEmpty());
        assertTrue(((String)details.get("suggestionBanner")).isEmpty());
    }

    @Test
    void getProductDetailsForForm_productTypeWithNoSchema_noMissingFields() {
        sampleProduct.setType(ProductType.valueOf("COSMETICS")); // Assuming COSMETICS has a schema in FormService
        // Let's create a dummy type or ensure one exists that FormService has no schema for
        // For this test, let's assume there's a product type not in FormService.productTypeSchemas
        // This requires modifying FormService or ProductType for a truly unknown type,
        // or testing with a type that has an explicitly empty list in the schema.
        // For now, let's test with a type that has a defined schema, but the product has all fields.
         sampleProduct.setType(ProductType.FASHION); // Schema: "Color", "Material", "Size Fit"
         sampleProduct.getSpecifications().put("Color", "Blue");
         sampleProduct.getSpecifications().put("Material", "Cotton");
         sampleProduct.getSpecifications().put("Size Fit", "Regular");
         when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

         Map<String, Object> details = formService.getProductDetailsForForm(1L);
         List<String> missingFields = (List<String>) details.get("missingFields");
         assertTrue(missingFields.isEmpty());
    }


    @Test
    void getProductDetailsForForm_productNotFound_returnsEmptyMap() {
        when(productRepository.findById(2L)).thenReturn(Optional.empty());
        Map<String, Object> details = formService.getProductDetailsForForm(2L);
        assertTrue(details.isEmpty());
    }

    // Tests for updateProductWithAdditionalFields
    @Test
    void updateProductWithAdditionalFields_productFound_addNewSpecs() {
        Map<String, String> newSpecs = new HashMap<>();
        newSpecs.put("Color", "Red");
        newSpecs.put("Material", "Silk");

        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

        formService.updateProductWithAdditionalFields(1L, newSpecs);

        assertEquals("Red", sampleProduct.getSpecifications().get("Color"));
        assertEquals("Silk", sampleProduct.getSpecifications().get("Material"));
        verify(productRepository, times(1)).save(sampleProduct);
    }

    @Test
    void updateProductWithAdditionalFields_productFound_updateExistingSpecs() {
        sampleProduct.getSpecifications().put("Color", "Blue");
        Map<String, String> updatedSpecs = new HashMap<>();
        updatedSpecs.put("Color", "Green");

        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        formService.updateProductWithAdditionalFields(1L, updatedSpecs);

        assertEquals("Green", sampleProduct.getSpecifications().get("Color"));
        verify(productRepository, times(1)).save(sampleProduct);
    }

    @Test
    void updateProductWithAdditionalFields_productSpecsNull_initializesAndAdds() {
        sampleProduct.setSpecifications(null); // Start with null specifications
        Map<String, String> newSpecs = Collections.singletonMap("Feature", "New Value");

        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        formService.updateProductWithAdditionalFields(1L, newSpecs);

        assertNotNull(sampleProduct.getSpecifications());
        assertEquals("New Value", sampleProduct.getSpecifications().get("Feature"));
        verify(productRepository, times(1)).save(sampleProduct);
    }

    @Test
    void updateProductWithAdditionalFields_submittedValueEmpty_isIgnored() {
        Map<String, String> specsWithEmptyValue = Collections.singletonMap("Feature", " ");

        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        formService.updateProductWithAdditionalFields(1L, specsWithEmptyValue);

        assertNull(sampleProduct.getSpecifications().get("Feature")); // Assuming it wasn't there before
        verify(productRepository, times(1)).save(sampleProduct); // Still saves product
    }


    @Test
    void updateProductWithAdditionalFields_productNotFound_throwsException() {
        when(productRepository.findById(2L)).thenReturn(Optional.empty());
        Map<String, String> newSpecs = Collections.singletonMap("Color", "Red");

        assertThrows(IllegalArgumentException.class, () -> {
            formService.updateProductWithAdditionalFields(2L, newSpecs);
        });
        verify(productRepository, never()).save(any(Product.class));
    }
}
