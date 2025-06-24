package dev.paul.cartlink.cart.service;

import dev.paul.cartlink.cart.dto.CartItemRequest;
import dev.paul.cartlink.cart.dto.CartItemUpdateRequest;
import dev.paul.cartlink.cart.model.Cart;
import dev.paul.cartlink.cart.model.CartItem;
import dev.paul.cartlink.cart.repository.CartItemRepository;
import dev.paul.cartlink.cart.repository.CartRepository;
import dev.paul.cartlink.merchant.model.MerchantProduct;
import dev.paul.cartlink.merchant.repository.MerchantProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MerchantProductRepository merchantProductRepository;

    @Transactional
    public Cart getOrCreateCart(String cookieId) {
        return cartRepository.findByCookieId(cookieId)
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setCookieId(cookieId);
                    return cartRepository.save(cart);
                });
    }

    @Transactional
    public Cart addItemToCart(String cookieId, CartItemRequest cartItemRequest) {
        Cart cart = getOrCreateCart(cookieId);
        MerchantProduct merchantProduct = merchantProductRepository.findById(cartItemRequest.getMerchantProductId())
                .orElseThrow(() -> new IllegalArgumentException("MerchantProduct not found"));

        Optional<CartItem> existingCartItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(merchantProduct.getId()))
                .findFirst();

        if (existingCartItem.isPresent()) {
            CartItem cartItem = existingCartItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + cartItemRequest.getQuantity());
            cartItemRepository.save(cartItem);
        } else {
            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(merchantProduct);
            cartItem.setQuantity(cartItemRequest.getQuantity());
            cart.getItems().add(cartItem);
            cartItemRepository.save(cartItem);
        }

        return cartRepository.save(cart);
    }

    @Transactional
    public Cart removeItemFromCart(String cookieId, Long itemId) {
        Cart cart = getOrCreateCart(cookieId);
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new IllegalArgumentException("Cart item does not belong to the current user's cart");
        }

        cart.getItems().remove(cartItem);
        cartItemRepository.delete(cartItem);
        return cartRepository.save(cart);
    }

    @Transactional
    public Cart updateItemQuantity(String cookieId, Long itemId, CartItemUpdateRequest updateRequest) {
        Cart cart = getOrCreateCart(cookieId);
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new IllegalArgumentException("Cart item does not belong to the current user's cart");
        }

        if (updateRequest.getQuantity() <= 0) {
            cart.getItems().remove(cartItem);
            cartItemRepository.delete(cartItem);
        } else {
            cartItem.setQuantity(updateRequest.getQuantity());
            cartItemRepository.save(cartItem);
        }
        return cartRepository.save(cart);
    }

    public Cart getCart(String cookieId) {
        return getOrCreateCart(cookieId);
    }
}