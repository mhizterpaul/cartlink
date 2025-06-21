package dev.paul.cartlink.merchant.controller;

import dev.paul.cartlink.merchant.service.FormService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // For flash messages

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/merchant/form")
public class FormController {

    @Autowired
    private FormService formService;

    @GetMapping("/{productId}")
    public ModelAndView showProductForm(@PathVariable Long productId, Model model) {
        Map<String, Object> formData = formService.getProductDetailsForForm(productId);

        if (formData.isEmpty() || formData.get("product") == null) {
            // Product not found or error in service
            return new ModelAndView("redirect:/merchant/products?error=productNotFound"); // Example redirect
        }

        model.addAllAttributes(formData); // Add all data from service (product, missingFields, banner) to model

        return new ModelAndView("merchant/form/edit-product-form", model.asMap());
    }

    @PostMapping("/{productId}")
    public String handleProductFormUpdate(@PathVariable Long productId,
                                          @RequestParam Map<String, String> allRequestParams,
                                          RedirectAttributes redirectAttributes) {

        // Extract specifications from the request parameters.
        // JSP submits them as specifications[keyName]=value
        Map<String, String> specificationsToUpdate = new HashMap<>();
        for (Map.Entry<String, String> param : allRequestParams.entrySet()) {
            if (param.getKey().startsWith("specifications[")) {
                String key = param.getKey().substring("specifications[".length(), param.getKey().length() - 1);
                if (param.getValue() != null && !param.getValue().trim().isEmpty()) {
                    specificationsToUpdate.put(key, param.getValue());
                }
            }
        }

        try {
            formService.updateProductWithAdditionalFields(productId, specificationsToUpdate);
            redirectAttributes.addFlashAttribute("successMessage", "Product updated successfully!");
            return "redirect:/merchant/product/" + productId; // Adjust to your product view page
        } catch (IllegalArgumentException e) {
            // Log error e.g., e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating product: " + e.getMessage());
            return "redirect:/merchant/form/" + productId; // Show form again with error
        } catch (Exception e) {
            // Log error e.g., e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred.");
            return "redirect:/merchant/form/" + productId; // Show form again
        }
    }
}
