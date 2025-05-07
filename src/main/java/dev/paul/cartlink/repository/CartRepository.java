package dev.paul.cartlink.repository;

import dev.paul.cartlink.model.Cart;
import dev.paul.cartlink.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByCustomer(Customer customer);
} 