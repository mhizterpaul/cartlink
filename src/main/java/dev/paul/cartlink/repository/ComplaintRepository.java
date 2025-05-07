package dev.paul.cartlink.repository;

import dev.paul.cartlink.model.Complaint;
import dev.paul.cartlink.model.Customer;
import dev.paul.cartlink.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    List<Complaint> findByCustomer(Customer customer);
    List<Complaint> findByOrder(Order order);
} 