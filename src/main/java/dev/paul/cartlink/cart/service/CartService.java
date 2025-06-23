package dev.paul.cartlink.cart.service;

import dev.paul.cartlink.cart.model.Cart;
import dev.paul.cartlink.cart.model.CartItem;
import dev.paul.cartlink.cart.repository.CartRepository;
import dev.paul.cartlink.cart.repository.CartItemRepository;
import dev.paul.cartlink.product.model.ProductLink;
import dev.paul.cartlink.product.repository.ProductLinkRepository;
import dev.paul.cartlink.customer.model.Customer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductLinkRepository productLinkRepository;

    public CartService(CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductLinkRepository productLinkRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productLinkRepository = productLinkRepository;
    }

    public CartItem addToCart(Customer customer, Long productLinkId, Integer quantity) {
        Cart cart = getOrCreateCart(customer);
        ProductLink productLink = productLinkRepository.findById(productLinkId)
                .orElseThrow(() -> new IllegalArgumentException("Product link not found"));

        CartItem existingItem = cartItemRepository.findByCartAndProductLink(cart, productLink)
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            return cartItemRepository.save(existingItem);
        }

        CartItem newItem = new CartItem();
        newItem.setCart(cart);
        newItem.setProductLink(productLink);
        newItem.setQuantity(quantity);
        newItem.setPrice(productLink.getMerchantProduct().getPrice());
        newItem.setDiscount(productLink.getMerchantProduct().getDiscount());

        return cartItemRepository.save(newItem);
    }

    public void removeFromCart(Customer customer, Long itemId) {
        Cart cart = getOrCreateCart(customer);
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));

        if (!item.getCart().equals(cart)) {
            throw new IllegalArgumentException("Cart item does not belong to customer");
        }

        cartItemRepository.delete(item);
    }

    public CartItem updateCartItemQuantity(Customer customer, Long itemId, Integer quantity) {
        Cart cart = getOrCreateCart(customer);
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));

        if (!item.getCart().equals(cart)) {
            throw new IllegalArgumentException("Cart item does not belong to customer");
        }

        item.setQuantity(quantity);
        return cartItemRepository.save(item);
    }

    public Cart getCart(Customer customer) {
        return getOrCreateCart(customer);
    }

    private Cart getOrCreateCart(Customer customer) {
        return cartRepository.findByCustomer(customer)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setCustomer(customer);
                    newCart.setTotalAmount(0.0);
                    return cartRepository.save(newCart);
                });
    }

    public void clearCart(Customer customer) {
        Cart cart = getOrCreateCart(customer);
        List<CartItem> items = cartItemRepository.findByCart(cart);
        cartItemRepository.deleteAll(items);
    }
}