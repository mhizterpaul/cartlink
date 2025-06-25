package dev.paul.cartlink.payment.controller;

import dev.paul.cartlink.order.model.Order;
import dev.paul.cartlink.order.repository.OrderRepository;
import dev.paul.cartlink.payment.model.Payment;
import dev.paul.cartlink.payment.model.PaymentMethod;
import dev.paul.cartlink.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private OrderRepository orderRepository;

    @PostMapping("/initiate")
    public ResponseEntity<?> initiatePayment(@RequestParam Long orderId,
                                             @RequestParam PaymentMethod method,
                                             @RequestParam double amount,
                                             @RequestParam String currency,
                                             @RequestParam String txRef) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Order not found");
        }
        Payment payment = paymentService.initiatePayment(orderOpt.get(), method, amount, currency, txRef);
        return ResponseEntity.ok(payment);
    }

    @PostMapping("/refund/{orderId}")
    public ResponseEntity<?> refundPayment(@PathVariable Long orderId) {
        paymentService.autoRefundStaleOrders(); // For demo, trigger refund logic
        return ResponseEntity.ok("Refund process triggered (see logs for actual refund)");
    }
} 