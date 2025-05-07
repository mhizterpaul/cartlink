package dev.paul.cartlink.service;

import dev.paul.cartlink.model.*;
import dev.paul.cartlink.repository.OrderRepository;
import dev.paul.cartlink.repository.RefundRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RefundService {

    private final RefundRequestRepository refundRequestRepository;
    private final OrderRepository orderRepository;

    public RefundService(RefundRequestRepository refundRequestRepository,
                        OrderRepository orderRepository) {
        this.refundRequestRepository = refundRequestRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public RefundRequest requestRefund(Customer customer, Long orderId, String reason,
                                     String accountNumber, String bankName, String accountName) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (!order.getCustomer().equals(customer)) {
            throw new IllegalArgumentException("Order does not belong to customer");
        }

        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.PROCESSING) {
            throw new IllegalArgumentException("Order is not eligible for refund");
        }

        RefundRequest refundRequest = new RefundRequest();
        refundRequest.setOrder(order);
        refundRequest.setCustomer(customer);
        refundRequest.setReason(reason);
        // TODO: Calculate the actual amount from order items once the Order class is fixed
        refundRequest.setAmount(0.0);
        refundRequest.setStatus(RefundStatus.PENDING);
        refundRequest.setAccountNumber(accountNumber);
        refundRequest.setBankName(bankName);
        refundRequest.setAccountName(accountName);

        return refundRequestRepository.save(refundRequest);
    }

    public List<RefundRequest> getCustomerRefunds(Customer customer) {
        return refundRequestRepository.findByCustomer(customer);
    }

    public List<RefundRequest> getOrderRefunds(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        return refundRequestRepository.findByOrder(order);
    }

    @Transactional
    public RefundRequest updateRefundStatus(Long refundId, RefundStatus newStatus) {
        RefundRequest refundRequest = refundRequestRepository.findById(refundId)
                .orElseThrow(() -> new IllegalArgumentException("Refund request not found"));

        refundRequest.setStatus(newStatus);
        if (newStatus == RefundStatus.PROCESSED) {
            refundRequest.setProcessedAt(LocalDateTime.now());
        }

        return refundRequestRepository.save(refundRequest);
    }

    @Transactional
    public void processPendingRefunds() {
        LocalDateTime fourteenDaysAgo = LocalDateTime.now().minusDays(14);
        List<RefundRequest> pendingRefunds = refundRequestRepository
                .findByRequestedAtBeforeAndStatus(fourteenDaysAgo, RefundStatus.PENDING);

        for (RefundRequest refund : pendingRefunds) {
            refund.setStatus(RefundStatus.APPROVED);
            refundRequestRepository.save(refund);
        }
    }
} 