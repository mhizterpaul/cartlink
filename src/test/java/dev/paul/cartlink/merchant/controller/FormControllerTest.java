package dev.paul.cartlink.merchant.controller;

import dev.paul.cartlink.product.controller.FormController;
import dev.paul.cartlink.product.model.Product;
import dev.paul.cartlink.product.model.ProductType;
import dev.paul.cartlink.product.service.FormService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.InternalResourceViewResolver; // Or appropriate for JTE if needed, but for controller tests, less critical

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class FormControllerTest {

    @Mock
    private FormService formService;

    @InjectMocks
    private FormController formController;

    private MockMvc mockMvc;

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        sampleProduct = new Product();
        sampleProduct.setProductId(1L);
        sampleProduct.setName("Test JTE Product");
        sampleProduct.setType(ProductType.ELECTRONICS);
        sampleProduct.setSpecifications(new HashMap<>());

        // Standalone setup for controller tests is often simpler than full Spring
        // context
        mockMvc = MockMvcBuilders.standaloneSetup(formController)
                // Optional: Add view resolver if testing view rendering details, less common
                // for pure controller unit tests
                // .setViewResolvers(new InternalResourceViewResolver("/WEB-INF/jte/", ".jte"))
                // // Adjust for JTE if needed
                .build();
    }

    // Tests for showProductForm (GET)
    @Test
    void showProductForm_productFound_returnsFormViewWithData() throws Exception {
        Map<String, Object> formData = new HashMap<>();
        formData.put("product", sampleProduct);
        formData.put("missingFields", Arrays.asList("RAM", "Storage"));
        formData.put("suggestionBanner", "Please add missing fields.");

        when(formService.getProductDetailsForForm(1L)).thenReturn(formData);

        mockMvc.perform(get("/merchant/form/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("merchant/form/editProductForm.jte"))
                .andExpect(model().attribute("product", sampleProduct))
                .andExpect(model().attribute("missingFields", Arrays.asList("RAM", "Storage")))
                .andExpect(model().attribute("suggestionBanner", "Please add missing fields."));
    }

    @Test
    void showProductForm_productNotFound_redirects() throws Exception {
        when(formService.getProductDetailsForForm(2L)).thenReturn(new HashMap<>()); // Empty map indicates not found

        mockMvc.perform(get("/merchant/form/2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/merchant/products?error=productNotFound"));
    }

    @Test
    void showProductForm_productInMapIsNull_redirects() throws Exception {
        Map<String, Object> formData = new HashMap<>();
        formData.put("product", null); // Service indicates product not found this way
        when(formService.getProductDetailsForForm(3L)).thenReturn(formData);

        mockMvc.perform(get("/merchant/form/3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/merchant/products?error=productNotFound"));
    }

    // Tests for handleProductFormUpdate (POST)
    @Test
    void handleProductFormUpdate_success_redirectsAndAddsFlashAttribute() throws Exception {
        Map<String, String> expectedSpecifications = new HashMap<>();
        expectedSpecifications.put("Color", "Blue");
        expectedSpecifications.put("Size", "Large");

        doNothing().when(formService).updateProductWithAdditionalFields(eq(1L), anyMap());

        mockMvc.perform(post("/merchant/form/1")
                .param("specifications[Color]", "Blue")
                .param("specifications[Size]", "Large")
                .param("unrelatedParam", "shouldBeIgnored"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/merchant/product/1"))
                .andExpect(flash().attribute("successMessage", "Product updated successfully!"));

        verify(formService).updateProductWithAdditionalFields(eq(1L), eq(expectedSpecifications));
    }

    @Test
    void handleProductFormUpdate_serviceThrowsIllegalArgument_redirectsAndAddsErrorFlash() throws Exception {
        doThrow(new IllegalArgumentException("Product not found by service"))
                .when(formService).updateProductWithAdditionalFields(eq(1L), anyMap());

        mockMvc.perform(post("/merchant/form/1")
                .param("specifications[Color]", "Red"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/merchant/form/1"))
                .andExpect(flash().attribute("errorMessage", "Error updating product: Product not found by service"));
    }

    @Test
    void handleProductFormUpdate_serviceThrowsGenericException_redirectsAndAddsErrorFlash() throws Exception {
        doThrow(new RuntimeException("Unexpected service error"))
                .when(formService).updateProductWithAdditionalFields(eq(1L), anyMap());

        mockMvc.perform(post("/merchant/form/1")
                .param("specifications[Color]", "Green"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/merchant/form/1"))
                .andExpect(flash().attribute("errorMessage", "An unexpected error occurred."));
    }
}
