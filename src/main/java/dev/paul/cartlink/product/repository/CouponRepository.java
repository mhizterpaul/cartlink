package dev.paul.cartlink.product.repository;

import dev.paul.cartlink.product.model.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    List<Coupon> findByProduct_ProductId(Long productId);
}