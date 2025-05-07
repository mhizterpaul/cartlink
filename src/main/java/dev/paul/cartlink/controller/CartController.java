package dev.paul.cartlink.controller;

import dev.paul.cartlink.model.Customer;
import dev.paul.cartlink.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/customers/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/items")
    public ResponseEntity<?> addToCart(@AuthenticationPrincipal Customer customer,
                                     @RequestBody Map<String, Object> request) {
        try {
            Long productLinkId = Long.valueOf(request.get("productLinkId").toString());
            Integer quantity = Integer.valueOf(request.get("quantity").toString());

            cartService.addToCart(customer, productLinkId, quantity);
            return ResponseEntity.ok(Map.of("message", "Item added to cart successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<?> removeFromCart(@AuthenticationPrincipal Customer customer,
                                          @PathVariable Long itemId) {
        try {
            cartService.removeFromCart(customer, itemId);
            return ResponseEntity.ok(Map.of("message", "Item removed from cart successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<?> updateCartItemQuantity(@AuthenticationPrincipal Customer customer,
                                                  @PathVariable Long itemId,
                                                  @RequestBody Map<String, Integer> request) {
        try {
            cartService.updateCartItemQuantity(customer, itemId, request.get("quantity"));
            return ResponseEntity.ok(Map.of("message", "Cart item quantity updated successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getCart(@AuthenticationPrincipal Customer customer) {
        return ResponseEntity.ok(cartService.getCart(customer));
    }
} 