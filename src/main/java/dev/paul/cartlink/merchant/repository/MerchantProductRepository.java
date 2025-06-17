package dev.paul.cartlink.merchant.repository;

import dev.paul.cartlink.merchant.dto.MerchantProduct;
import dev.paul.cartlink.merchant.model.Merchant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MerchantProductRepository extends JpaRepository<MerchantProduct, Long> {
    List<MerchantProduct> findByMerchant(Merchant merchant);

    List<MerchantProduct> findByMerchantAndProductNameContainingIgnoreCase(Merchant merchant, String productName);

    List<MerchantProduct> findByMerchantAndStockGreaterThan(Merchant merchant, Integer stock);
}