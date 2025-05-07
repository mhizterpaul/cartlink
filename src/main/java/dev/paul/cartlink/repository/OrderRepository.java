package dev.paul.cartlink.repository;

import dev.paul.cartlink.model.Merchant;
import dev.paul.cartlink.model.Order;
import dev.paul.cartlink.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByMerchantProduct_Merchant(Merchant merchant);
    List<Order> findByMerchantProduct_MerchantAndStatus(Merchant merchant, OrderStatus status);
    List<Order> findByMerchantProduct_MerchantAndCreatedAtBetween(Merchant merchant, LocalDateTime startDate, LocalDateTime endDate);
    List<Order> findByProductLink_LinkId(Long linkId);
} 