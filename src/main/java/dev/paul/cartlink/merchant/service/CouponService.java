package dev.paul.cartlink.merchant.service;

import dev.paul.cartlink.merchant.model.MerchantProduct;
import dev.paul.cartlink.merchant.repository.MerchantProductRepository;
import dev.paul.cartlink.merchant.model.Coupon;
import dev.paul.cartlink.merchant.model.Merchant;
import dev.paul.cartlink.merchant.repository.CouponRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class CouponService {
    private final CouponRepository couponRepository;
    private final MerchantProductRepository merchantProductRepository;

    public CouponService(CouponRepository couponRepository, MerchantProductRepository merchantProductRepository) {
        this.couponRepository = couponRepository;
        this.merchantProductRepository = merchantProductRepository;
    }

    @Transactional
    public Coupon createCoupon(Long merchantProductId, Merchant merchant, Double discount, Instant validFrom,
            Instant validUntil, Integer maxUsage, Integer maxUsers) {
        MerchantProduct product = merchantProductRepository.findById(merchantProductId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        Coupon coupon = new Coupon();
        coupon.setMerchantProduct(product);
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