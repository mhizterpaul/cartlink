package dev.paul.cartlink.payment.service;

import dev.paul.cartlink.order.model.Order;
import dev.paul.cartlink.order.model.OrderStatus;
import dev.paul.cartlink.order.repository.OrderRepository;
import dev.paul.cartlink.payment.model.Payment;
import dev.paul.cartlink.payment.model.PaymentMethod;
import dev.paul.cartlink.payment.model.PaymentStatus;
import dev.paul.cartlink.payment.repository.PaymentRepository;
import dev.paul.cartlink.merchant.model.Merchant;
import dev.paul.cartlink.merchant.model.Wallet;
import dev.paul.cartlink.merchant.repository.MerchantRepository;
import dev.paul.cartlink.merchant.repository.WalletRepository;
import dev.paul.cartlink.complaint.repository.ComplaintRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private MerchantRepository merchantRepository;
    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private ComplaintRepository complaintRepository;

    @Transactional
    public Payment initiatePayment(Order order, PaymentMethod method, double amount, String currency, String txRef) {
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setMethod(method);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setAmount(amount);
        payment.setCurrency(currency);
        payment.setTxRef(txRef);
        paymentRepository.save(payment);
        return payment;
    }

    @Transactional
    public void handlePaymentSuccess(String txRef, String flwRef) {
        Optional<Payment> paymentOpt = paymentRepository.findByTxRef(txRef);
        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();
            payment.setStatus(PaymentStatus.SUCCESSFUL);
            payment.setFlwRef(flwRef);
            payment.setPaidAt(LocalDateTime.now());
            paymentRepository.save(payment);
            Order order = payment.getOrder();
            order.setStatus(OrderStatus.PAID);
            order.setPaid(true);
            orderRepository.save(order);
        }
    }

    @Transactional
    public void handlePaymentFailure(String txRef) {
        Optional<Payment> paymentOpt = paymentRepository.findByTxRef(txRef);
        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            Order order = payment.getOrder();
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
        }
    }

    @Transactional
    public void payMerchant(Long orderId) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            if (order.getStatus() == OrderStatus.DELIVERED) {
                Merchant merchant = order.getMerchantProduct().getMerchant();
                Wallet wallet = merchant.getWallet();
                wallet.setBalance(wallet.getBalance() + order.getTotalPrice());
                walletRepository.save(wallet);
                order.setStatus(OrderStatus.COMPLETED);
                orderRepository.save(order);
            }
        }
    }

    @Scheduled(cron = "0 0 3 * * *") // 3 AM daily
    @Transactional
    public void autoRefundStaleOrders() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(14);
        List<Order> staleOrders = orderRepository.findStaleUnshippedPaidOrdersOlderThan(cutoffDate);
        for (Order order : staleOrders) {
            if (!complaintRepository.existsByOrder_OrderId(order.getOrderId())) {
                Payment payment = order.getPayment();
                if (payment != null && payment.getStatus() == PaymentStatus.SUCCESSFUL) {
                    payment.setStatus(PaymentStatus.REFUNDED);
                    payment.setRefundedAt(LocalDateTime.now());
                    paymentRepository.save(payment);
                    order.setStatus(OrderStatus.REFUNDED);
                    orderRepository.save(order);
                }
            }
        }
    }

    @Scheduled(cron = "0 30 3 * * *") // 3:30 AM daily
    @Transactional
    public void autoPayMerchantsForDeliveredOrders() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(14);
        List<Order> eligibleOrders = orderRepository.findDeliveredPaidOrdersOlderThan(cutoffDate);
        for (Order order : eligibleOrders) {
            Merchant merchant = order.getMerchantProduct().getMerchant();
            Wallet wallet = merchant.getWallet();
            wallet.setBalance(wallet.getBalance() + order.getTotalPrice());
            walletRepository.save(wallet);
            order.setStatus(OrderStatus.COMPLETED);
            orderRepository.save(order);
            // Optionally, mark order as paid out or log payout
        }
    }
} 