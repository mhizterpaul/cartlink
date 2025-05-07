package dev.paul.cartlink.repository;

import dev.paul.cartlink.model.Customer;
import dev.paul.cartlink.model.Order;
import dev.paul.cartlink.model.RefundRequest;
import dev.paul.cartlink.model.RefundStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RefundRequestRepository extends JpaRepository<RefundRequest, Long> {
    List<RefundRequest> findByCustomer(Customer customer);
    List<RefundRequest> findByOrder(Order order);
    List<RefundRequest> findByStatus(RefundStatus status);
    List<RefundRequest> findByRequestedAtBeforeAndStatus(LocalDateTime date, RefundStatus status);
} 