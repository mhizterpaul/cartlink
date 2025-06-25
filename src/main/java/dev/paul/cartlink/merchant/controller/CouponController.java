package dev.paul.cartlink.merchant.controller;

import dev.paul.cartlink.merchant.dto.CouponCreateRequest;
import dev.paul.cartlink.merchant.dto.CouponDetailsResponse;
import dev.paul.cartlink.merchant.dto.CouponIdResponse;
import dev.paul.cartlink.merchant.model.Coupon;
import dev.paul.cartlink.merchant.model.Merchant;
import dev.paul.cartlink.merchant.service.CouponService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/merchants/{merchantId}/products/{productId}/coupons")
public class CouponController {
    @Autowired
    private CouponService couponService;

    @PostMapping
    public ResponseEntity<CouponIdResponse> createCoupon(@PathVariable Long merchantId,
            @PathVariable Long productId,
            @RequestBody CouponCreateRequest request,
            @AuthenticationPrincipal Merchant merchant) {
        Coupon coupon = couponService.createCoupon(
                productId,
                merchant,
                request.getDiscount(),
                Instant.parse(request.getValidFrom()),
                Instant.parse(request.getValidUntil()),
                request.getMaxUsage(),
                request.getMaxUsers());
        CouponIdResponse response = new CouponIdResponse();
        response.setCouponId(coupon.getId());
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping
    public ResponseEntity<List<CouponDetailsResponse>> getCoupons(@PathVariable Long productId) {
        List<Coupon> coupons = couponService.getCouponsForProduct(productId);
        List<CouponDetailsResponse> response = coupons.stream().map(c -> {
            CouponDetailsResponse dto = new CouponDetailsResponse();
            dto.setCouponId(c.getId());
            dto.setDiscount(c.getDiscount());
            dto.setValidFrom(c.getValidFrom().toString());
            dto.setValidUntil(c.getValidUntil().toString());
            dto.setMaxUsage(c.getMaxUsage());
            dto.setMaxUsers(c.getMaxUsers());
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{couponId}")
    public ResponseEntity<CouponIdResponse> deleteCoupon(@PathVariable Long couponId) {
        couponService.deleteCoupon(couponId);
        CouponIdResponse response = new CouponIdResponse();
        return ResponseEntity.ok(response);
    }
}