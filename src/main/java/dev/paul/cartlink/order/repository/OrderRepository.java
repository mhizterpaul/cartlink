package dev.paul.cartlink.order.repository;

import dev.paul.cartlink.merchant.model.Merchant;
import dev.paul.cartlink.order.model.Order;
import dev.paul.cartlink.order.model.OrderStatus;
import dev.paul.cartlink.customer.model.Customer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByMerchantProduct_Merchant_merchantId(Long merchantId);

    List<Order> findByMerchantProduct_Merchant_merchantIdAndOrderDateAfter(Long merchantId, LocalDateTime date);

    long countByMerchantProduct_Merchant_merchantId(Long merchantId);

    @Query("SELECT DISTINCT o.customer.customerId FROM Order o WHERE o.merchantProduct.merchant.merchantId = :merchantId")
    Set<Long> findDistinctCustomerIdByMerchantProduct_Merchant_merchantId(Long merchantId);

    List<Order> findByMerchantProduct_Merchant(Merchant merchant);

    List<Order> findByMerchantProduct_MerchantAndStatus(Merchant merchant, OrderStatus status);

    List<Order> findByMerchantProduct_MerchantAndOrderDateBetween(Merchant merchant, LocalDateTime startDate,
            LocalDateTime endDate);

    // Alias method to match service usage
    default List<Order> findByMerchantProduct_MerchantAndCreatedAtBetween(Merchant merchant, LocalDateTime startDate,
            LocalDateTime endDate) {
        return findByMerchantProduct_MerchantAndOrderDateBetween(merchant, startDate, endDate);
    }

    List<Order> findByProductLink_LinkId(Long linkId);

    List<Order> findByCustomer(Customer customer);

    @Query("SELECT o FROM Order o WHERE o.status = dev.paul.cartlink.order.model.OrderStatus.DELIVERED AND o.paid = true AND o.lastUpdated < :cutoffDate")
    List<Order> findDeliveredPaidOrdersOlderThan(LocalDateTime cutoffDate);

    @Query("SELECT o FROM Order o WHERE o.status = dev.paul.cartlink.order.model.OrderStatus.PAID AND o.lastUpdated < :cutoffDate AND o.status <> dev.paul.cartlink.order.model.OrderStatus.SHIPPED")
    List<Order> findStaleUnshippedPaidOrdersOlderThan(LocalDateTime cutoffDate);
}