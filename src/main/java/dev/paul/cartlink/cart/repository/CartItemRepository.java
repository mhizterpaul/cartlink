package dev.paul.cartlink.cart.repository;

import dev.paul.cartlink.cart.model.Cart;
import dev.paul.cartlink.cart.model.CartItem;
import dev.paul.cartlink.link.model.LinkAnalytics;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCart(Cart cart);

    @Query("SELECT ci FROM CartItem ci JOIN ci.product mp JOIN mp.linkAnalytics la WHERE ci.cart = :cart AND la = :analytics")
    Optional<CartItem> findByCartAndLinkAnalytics(Cart cart, LinkAnalytics analytics);
}