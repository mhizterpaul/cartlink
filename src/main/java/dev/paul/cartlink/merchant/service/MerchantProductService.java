package dev.paul.cartlink.product.service;

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

    public MerchantProduct addProduct(Merchant merchant, Product product, Integer stock, Double price,
            Double discount, String logisticsProvider) {
        Product savedProduct = productRepository.save(product);

        MerchantProduct merchantProduct = new MerchantProduct();
        merchantProduct.setMerchant(merchant);
        merchantProduct.setProduct(savedProduct);
        merchantProduct.setStock(stock);
        merchantProduct.setPrice(price);

        return merchantProductRepository.save(merchantProduct);
    }

    public MerchantProduct updateProduct(Long merchantProductId, Product product, Integer stock,
            Double price, Double discount) {
        MerchantProduct merchantProduct = merchantProductRepository.findById(merchantProductId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        Product existingProduct = merchantProduct.getProduct();
        existingProduct.setName(product.getName());
        existingProduct.setDescription(product.getDescription());

        merchantProduct.setStock(stock);
        merchantProduct.setPrice(price);

        return merchantProductRepository.save(merchantProduct);
    }

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

    public Product getProductById(Long productId) {
        return productRepository.findById(productId).orElse(null);
    }

    public List<Map<String, Object>> batchUploadProducts(Merchant merchant, List<Map<String, Object>> products) {
        List<Map<String, Object>> failed = new java.util.ArrayList<>();
        for (Map<String, Object> req : products) {
            try {
                Product product = new Product();
                product.setName((String) req.get("name"));
                // Set other fields as needed
                addProduct(
                        product);
            } catch (Exception e) {
                failed.add(req);
            }
        }
        return failed;
    }
}