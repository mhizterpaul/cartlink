package dev.paul.cartlink.product.service;

import dev.paul.cartlink.product.model.Coupon;
import dev.paul.cartlink.product.model.Product;
import dev.paul.cartlink.product.repository.CouponRepository;
import dev.paul.cartlink.product.repository.ProductRepository;
import dev.paul.cartlink.merchant.model.Merchant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class CouponService {
    private final CouponRepository couponRepository;
    private final ProductRepository productRepository;

    public CouponService(CouponRepository couponRepository, ProductRepository productRepository) {
        this.couponRepository = couponRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public Coupon createCoupon(Long productId, Merchant merchant, Double discount, Instant validFrom,
            Instant validUntil, Integer maxUsage, Integer maxUsers) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        Coupon coupon = new Coupon();
        coupon.setProduct(product);
        coupon.setMerchant(merchant);
        coupon.setDiscount(discount);
        coupon.setValidFrom(validFrom);
        coupon.setValidUntil(validUntil);
        coupon.setMaxUsage(maxUsage);
        coupon.setMaxUsers(maxUsers);
        return couponRepository.save(coupon);
    }

    public List<Coupon> getCouponsForProduct(Long productId) {
        return couponRepository.findByProduct_ProductId(productId);
    }

    public void deleteCoupon(Long couponId) {
        couponRepository.deleteById(couponId);
    }
}