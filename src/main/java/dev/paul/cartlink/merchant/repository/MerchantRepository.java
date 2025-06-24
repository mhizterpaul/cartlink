package dev.paul.cartlink.merchant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.paul.cartlink.merchant.model.Merchant;

import java.util.Optional;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, Long> {
    Optional<Merchant> findByEmail(String email);

    Optional<Merchant> findByVerificationToken(String verificationToken);

    Optional<Merchant> findByPasswordResetToken(String passwordResetToken);

    boolean existsByEmail(String email);
}