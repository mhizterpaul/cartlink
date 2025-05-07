package dev.paul.cartlink.repository;

import dev.paul.cartlink.model.Merchant;
import dev.paul.cartlink.model.MerchantProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MerchantProductRepository extends JpaRepository<MerchantProduct, Long> {
    List<MerchantProduct> findByMerchant(Merchant merchant);
    List<MerchantProduct> findByMerchantAndProductNameContainingIgnoreCase(Merchant merchant, String productName);
    List<MerchantProduct> findByMerchantAndStockGreaterThan(Merchant merchant, Integer stock);
} 