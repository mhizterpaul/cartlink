package dev.paul.cartlink.product.service;

import dev.paul.cartlink.merchant.dto.MerchantProduct;
import dev.paul.cartlink.merchant.model.Merchant;
import dev.paul.cartlink.merchant.repository.MerchantProductRepository;
import dev.paul.cartlink.model.*;
import dev.paul.cartlink.product.model.Product;
import dev.paul.cartlink.product.repository.ProductRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final MerchantProductRepository merchantProductRepository;

    public ProductService(ProductRepository productRepository,
            MerchantProductRepository merchantProductRepository) {
        this.productRepository = productRepository;
        this.merchantProductRepository = merchantProductRepository;
    }

    @Transactional
    public MerchantProduct addProduct(Merchant merchant, Product product, Integer stock, Double price,
            Double discount, String logisticsProvider) {
        Product savedProduct = productRepository.save(product);

        MerchantProduct merchantProduct = new MerchantProduct();
        merchantProduct.setMerchant(merchant);
        merchantProduct.setProduct(savedProduct);
        merchantProduct.setStock(stock);
        merchantProduct.setPrice(price);
        merchantProduct.setDiscount(discount);
        merchantProduct.setLogisticsProvider(logisticsProvider);

        return merchantProductRepository.save(merchantProduct);
    }

    @Transactional
    public MerchantProduct updateProduct(Long merchantProductId, Product product, Integer stock,
            Double price, Double discount, String logisticsProvider) {
        MerchantProduct merchantProduct = merchantProductRepository.findById(merchantProductId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        Product existingProduct = merchantProduct.getProduct();
        existingProduct.setName(product.getName());
        existingProduct.setModel(product.getModel());
        existingProduct.setManufacturer(product.getManufacturer());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setSpecifications(product.getSpecifications());

        merchantProduct.setStock(stock);
        merchantProduct.setPrice(price);
        merchantProduct.setDiscount(discount);
        merchantProduct.setLogisticsProvider(logisticsProvider);

        return merchantProductRepository.save(merchantProduct);
    }

    @Transactional
    public void deleteProduct(Long merchantProductId) {
        MerchantProduct merchantProduct = merchantProductRepository.findById(merchantProductId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        merchantProductRepository.delete(merchantProduct);
        productRepository.delete(merchantProduct.getProduct());
    }

    public List<MerchantProduct> getMerchantProducts(Merchant merchant) {
        return merchantProductRepository.findByMerchant(merchant);
    }

    public List<MerchantProduct> searchMerchantProducts(Merchant merchant, String searchTerm) {
        return merchantProductRepository.findByMerchantAndProductNameContainingIgnoreCase(merchant, searchTerm);
    }

    public List<MerchantProduct> getInStockProducts(Merchant merchant) {
        return merchantProductRepository.findByMerchantAndStockGreaterThan(merchant, 0);
    }
}