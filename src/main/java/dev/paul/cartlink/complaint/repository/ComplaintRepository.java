package dev.paul.cartlink.complaint.repository;

import dev.paul.cartlink.complaint.model.Complaint;
import dev.paul.cartlink.customer.model.Customer;
import dev.paul.cartlink.order.model.Order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    List<Complaint> findByCustomer(Customer customer);

    List<Complaint> findByOrder(Order order);

    List<Complaint> findByOrder_MerchantProduct_Merchant_MerchantId(Long merchantId);

    boolean existsByOrder_OrderId(Long orderId);
}