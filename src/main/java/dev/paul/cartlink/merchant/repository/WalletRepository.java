package dev.paul.cartlink.merchant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.paul.cartlink.merchant.model.Wallet;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
}