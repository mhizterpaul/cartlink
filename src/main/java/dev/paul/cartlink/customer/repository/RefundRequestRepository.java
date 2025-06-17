package dev.paul.cartlink.customer.repository;

import dev.paul.cartlink.customer.model.Customer;
import dev.paul.cartlink.customer.model.RefundRequest;
import dev.paul.cartlink.customer.model.RefundStatus;
import dev.paul.cartlink.order.model.Order;

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