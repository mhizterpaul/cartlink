package dev.paul.cartlink.complaint.service;

import dev.paul.cartlink.complaint.model.ComplaintStatus;
import dev.paul.cartlink.complaint.repository.ComplaintRepository;
import dev.paul.cartlink.customer.model.Customer;
import dev.paul.cartlink.model.*;
import dev.paul.cartlink.order.model.Order;
import dev.paul.cartlink.order.repository.OrderRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final OrderRepository orderRepository;

    public ComplaintService(ComplaintRepository complaintRepository,
            OrderRepository orderRepository) {
        this.complaintRepository = complaintRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public Complaint submitComplaint(Customer customer, Long orderId, String title,
            String description, String category) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (!order.getCustomer().equals(customer)) {
            throw new IllegalArgumentException("Order does not belong to customer");
        }

        Complaint complaint = new Complaint();
        complaint.setOrder(order);
        complaint.setCustomer(customer);
        complaint.setTitle(title);
        complaint.setDescription(description);
        complaint.setCategory(category);
        complaint.setStatus(ComplaintStatus.PENDING);

        return complaintRepository.save(complaint);
    }

    public List<Complaint> getCustomerComplaints(Customer customer) {
        return complaintRepository.findByCustomer(customer);
    }

    public List<Complaint> getOrderComplaints(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        return complaintRepository.findByOrder(order);
    }

    @Transactional
    public Complaint updateComplaintStatus(Long complaintId, ComplaintStatus newStatus) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found"));

        complaint.setStatus(newStatus);
        if (newStatus == ComplaintStatus.RESOLVED) {
            complaint.setResolvedAt(LocalDateTime.now());
        }

        return complaintRepository.save(complaint);
    }
}