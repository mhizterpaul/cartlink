package dev.paul.cartlink.merchant.service;

import dev.paul.cartlink.product.model.Product;
import dev.paul.cartlink.product.repository.ProductRepository;
import dev.paul.cartlink.merchant.model.Merchant;
import dev.paul.cartlink.merchant.model.MerchantProduct;
import dev.paul.cartlink.merchant.repository.MerchantProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class MerchantProductService {

    private final ProductRepository productRepository;
    private final MerchantProductRepository merchantProductRepository;

    public MerchantProductService(ProductRepository productRepository,
            MerchantProductRepository merchantProductRepository) {
        this.productRepository = productRepository;
        this.merchantProductRepository = merchantProductRepository;
    }

    public MerchantProduct addMerchantProduct(Merchant merchant, Product product, Integer stock, Double price,
            String description) {
        Product savedProduct = productRepository.save(product);

        MerchantProduct merchantProduct = new MerchantProduct();
        merchantProduct.setMerchant(merchant);
        merchantProduct.setProduct(savedProduct);
        merchantProduct.setStock(stock);
        merchantProduct.setPrice(price);
        merchantProduct.setDescription(description);

        return merchantProductRepository.save(merchantProduct);
    }

    public MerchantProduct updateMerchantProduct(Merchant merchant, Long merchantProductId, Product product,
            Integer stock, Double price, String description) {
        MerchantProduct merchantProduct = merchantProductRepository.findById(merchantProductId)
                .orElseThrow(() -> new IllegalArgumentException("Merchant product not found"));
        if (!merchantProduct.getMerchant().getId().equals(merchant.getId())) {
            throw new IllegalArgumentException("Unauthorized");
        }
        Product existingProduct = merchantProduct.getProduct();
        existingProduct.setName(product.getName());
        existingProduct.setBrand(product.getBrand());
        existingProduct.setCategory(product.getCategory());
        merchantProduct.setStock(stock);
        merchantProduct.setPrice(price);
        merchantProduct.setDescription(description);
        return merchantProductRepository.save(merchantProduct);
    }

    // Rename for consistency and add merchant check
    public void deleteMerchantProduct(Merchant merchant, Long merchantProductId) {
        MerchantProduct merchantProduct = merchantProductRepository.findById(merchantProductId)
                .orElseThrow(() -> new IllegalArgumentException("Merchant product not found"));
        if (!merchantProduct.getMerchant().getId().equals(merchant.getId())) {
            throw new IllegalArgumentException("Unauthorized");
        }
        merchantProductRepository.delete(merchantProduct);
        productRepository.delete(merchantProduct.getProduct());
    }

    public List<MerchantProduct> getMerchantProducts(Merchant merchant) {
        return merchantProductRepository.findByMerchant(merchant);
    }

    public List<MerchantProduct> searchMerchantProducts(Merchant merchant, String searchTerm) {
        return merchantProductRepository.findByMerchantAndProductNameContainingIgnoreCase(merchant, searchTerm);
    }

    public List<MerchantProduct> getInStockMerchantProducts(Merchant merchant) {
        return merchantProductRepository.findByMerchantAndStockGreaterThan(merchant, 0);
    }

    public Product getProductById(Long productId) {
        return productRepository.findById(productId).orElse(null);
    }

    public List<Map<String, Object>> batchUploadMerchantProducts(Merchant merchant,
            List<Map<String, Object>> merchantProducts) {
        // TODO: Implement batch upload logic
        return List.of();
    }
}