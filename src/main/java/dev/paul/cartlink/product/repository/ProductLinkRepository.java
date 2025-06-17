package dev.paul.cartlink.product.repository;

import dev.paul.cartlink.merchant.dto.MerchantProduct;
import dev.paul.cartlink.product.model.ProductLink;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductLinkRepository extends JpaRepository<ProductLink, Long> {
    List<ProductLink> findByMerchantProduct(MerchantProduct merchantProduct);

    Optional<ProductLink> findByUrl(String url);

    List<ProductLink> findByMerchantProduct_Merchant_MerchantId(Long merchantId);
}