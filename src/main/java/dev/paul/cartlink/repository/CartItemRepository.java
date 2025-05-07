package dev.paul.cartlink.repository;

import dev.paul.cartlink.model.Cart;
import dev.paul.cartlink.model.CartItem;
import dev.paul.cartlink.model.ProductLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCart(Cart cart);
    Optional<CartItem> findByCartAndProductLink(Cart cart, ProductLink productLink);
} 