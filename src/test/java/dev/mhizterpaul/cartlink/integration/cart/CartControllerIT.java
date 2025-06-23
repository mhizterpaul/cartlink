package dev.mhizterpaul.cartlink.integration.cart;

import com.fasterxml.jackson.databind.ObjectMapper;
// Assuming DTOs are in dev.paul.cartlink.cart.dto or common
import dev.paul.cartlink.cart.dto.CartItemRequest; // Placeholder
import dev.paul.cartlink.cart.dto.CartItemUpdateRequest; // Placeholder
import dev.paul.cartlink.cart.dto.CartResponse; // Placeholder

// For test data setup (customer, product)
import dev.paul.cartlink.customer.model.Customer;
import dev.paul.cartlink.customer.repository.CustomerRepository;
import dev.paul.cartlink.product.model.Product;
import dev.paul.cartlink.product.repository.ProductRepository;
import dev.paul.cartlink.merchant.model.Merchant;
import dev.paul.cartlink.merchant.repository.MerchantRepository;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockCookie;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Customer Cart API Integration Tests")
public class CartControllerIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private MerchantRepository merchantRepository;

    private String testCustomerId;
    private String testProductId;
    private MockCookie customerSessionCookie;

    @BeforeEach
    void setUpTestDataAndSession() throws Exception {
        // Create Merchant
        Merchant merchant = new Merchant();
        merchant.setEmail("cartmerchant" + System.currentTimeMillis() + "@example.com");
        merchant.setPassword("password");
        merchant = merchantRepository.save(merchant);

        // Create Customer (and simulate login to get a session cookie)
        Customer customer = new Customer();
        customer.setEmail("cartcustomer" + System.currentTimeMillis() + "@example.com");
        // Set other required customer fields, e.g. password if login is performed
        // For simplicity, we'll assume the customer ID is used directly or a session is established.
        // In a real scenario, you might need to call a login endpoint if your app uses session cookies from login.
        // Here, we'll just save the customer and use its ID as a representation.
        Customer savedCustomer = customerRepository.save(customer);
        testCustomerId = savedCustomer.getId();

        // Create a product
        Product product = new Product();
        product.setName("Cart Test Product");
        product.setPrice(BigDecimal.valueOf(19.99));
        product.setStock(10);
        product.setMerchant(merchant);
        Product savedProduct = productRepository.save(product);
        testProductId = savedProduct.getId();

        // Simulate a customer session cookie.
        // In a real application, this cookie would be set by the server upon login.
        // The value might be a session ID managed by Spring Session or a JWT.
        // For @SpringBootTest, if Spring Security is involved, you might need to use
        // @WithMockUser or programmatically create an authentication token in SecurityContextHolder.
        // Using a simple MockCookie for now, assuming the CartController can resolve customer from it.
        customerSessionCookie = new MockCookie("JSESSIONID", "session-for-" + testCustomerId);
    }


    @Nested
    @DisplayName("Cart Operations BDT")
    class CartBDT {

        @Test
        @DisplayName("Customer should be able to add item, view cart, update quantity, remove item")
        void fullCartLifecycle() throws Exception {
            // 1. Add item to cart
            CartItemRequest addItemRequest = new CartItemRequest(testProductId, 2);
            MvcResult addResult = mockMvc.perform(post("/api/v1/customers/cart/items")
                    .cookie(customerSessionCookie)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(addItemRequest)))
                    .andExpect(status().isCreated()) // As per API spec
                    .andReturn();

            // Assuming add to cart might return the updated cart or item ID
            // For this BDT, we'll proceed to view cart

            // 2. View Cart
            MvcResult viewResult = mockMvc.perform(get("/api/v1/customers/cart")
                    .cookie(customerSessionCookie))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items").isArray())
                    .andExpect(jsonPath("$.items[0].productId").value(testProductId)) // Assuming DTO has productId
                    .andExpect(jsonPath("$.items[0].quantity").value(2))    // Assuming DTO has quantity
                    .andReturn();

            // Extract item ID if needed for update/delete (assuming CartResponse.Item has an id)
            // This part is highly dependent on the actual CartResponse DTO structure
            // For now, we'll use a placeholder itemId or assume it's the productID for cart item identification
            String cartItemId = testProductId; // Placeholder: In reality, this would be a unique cart_item_id

            // 3. Update Quantity
            CartItemUpdateRequest updateRequest = new CartItemUpdateRequest(5);
            mockMvc.perform(put("/api/v1/customers/cart/items/{itemId}", cartItemId)
                    .cookie(customerSessionCookie)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk());

            // Verify update by viewing cart again
             mockMvc.perform(get("/api/v1/customers/cart")
                    .cookie(customerSessionCookie))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items[0].quantity").value(5));

            // 4. Remove from Cart
            mockMvc.perform(delete("/api/v1/customers/cart/items/{itemId}", cartItemId)
                    .cookie(customerSessionCookie))
                    .andExpect(status().isOk());

            // Verify removal by viewing cart again
            mockMvc.perform(get("/api/v1/customers/cart")
                    .cookie(customerSessionCookie))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.items.length()").value(0)); // Cart should be empty
        }
    }

    // --- Notes on CartControllerIT ---
    // - Targets dev.paul.cartlink.cart.controller.CartController.
    // - Placeholder DTOs used (CartItemRequest, CartResponse, etc.).
    // - Simulates customer session using MockCookie. Actual session mechanism might vary.
    // - BDT test demonstrates a flow: add, view, update, remove.
    // - Assumes cart item identification for update/delete might be by product ID if no separate cart_item_id is exposed or used.
}
