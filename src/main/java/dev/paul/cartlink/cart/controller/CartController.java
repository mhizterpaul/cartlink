package dev.paul.cartlink.cart.controller;

import dev.paul.cartlink.cart.dto.CartItemRequest;
import dev.paul.cartlink.cart.dto.CartItemUpdateRequest;
import dev.paul.cartlink.cart.dto.CartResponse;
import dev.paul.cartlink.cart.dto.CheckoutRequest;
import dev.paul.cartlink.cart.dto.CheckoutResponse;
import dev.paul.cartlink.cart.model.Cart;
import dev.paul.cartlink.cart.service.CartService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    private String getOrSetCookieId(@CookieValue(name = "cart_cookie_id", required = false) String cookieId, HttpServletResponse response) {
        if (cookieId == null) {
            cookieId = UUID.randomUUID().toString();
            Cookie cookie = new Cookie("cart_cookie_id", cookieId);
            cookie.setPath("/");
            cookie.setMaxAge(60 * 60 * 24 * 365); // 1 year
            response.addCookie(cookie);
        }
        return cookieId;
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart(@CookieValue(name = "cart_cookie_id", required = false) String cookieId, HttpServletResponse response) {
        String cartCookieId = getOrSetCookieId(cookieId, response);
        Cart cart = cartService.getCart(cartCookieId);
        return ResponseEntity.ok(new CartResponse(cart));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItemToCart(@CookieValue(name = "cart_cookie_id", required = false) String cookieId,
                                                      @RequestBody CartItemRequest cartItemRequest,
                                                      HttpServletResponse response) {
        String cartCookieId = getOrSetCookieId(cookieId, response);
        Cart cart = cartService.addItemToCart(cartCookieId, cartItemRequest);
        return new ResponseEntity<>(new CartResponse(cart), HttpStatus.CREATED);
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> removeItemFromCart(@CookieValue(name = "cart_cookie_id", required = false) String cookieId,
                                                            @PathVariable Long itemId,
                                                            HttpServletResponse response) {
        String cartCookieId = getOrSetCookieId(cookieId, response);
        Cart cart = cartService.removeItemFromCart(cartCookieId, itemId);
        return ResponseEntity.ok(new CartResponse(cart));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> updateItemQuantity(@CookieValue(name = "cart_cookie_id", required = false) String cookieId,
                                                           @PathVariable Long itemId,
                                                           @RequestBody CartItemUpdateRequest updateRequest,
                                                           HttpServletResponse response) {
        String cartCookieId = getOrSetCookieId(cookieId, response);
        Cart cart = cartService.updateItemQuantity(cartCookieId, itemId, updateRequest);
        return ResponseEntity.ok(new CartResponse(cart));
    }

    @PostMapping("/checkout")
    public ResponseEntity<CheckoutResponse> checkoutCart(@CookieValue(name = "cart_cookie_id", required = false) String cookieId,
                                                        @RequestBody CheckoutRequest request,
                                                        HttpServletResponse response) {
        String cartCookieId = getOrSetCookieId(cookieId, response);
        CheckoutResponse checkoutResponse = cartService.checkoutCart(cartCookieId, request);
        return ResponseEntity.ok(checkoutResponse);
    }
}