package dev.paul.cartlink.cart.dto;

import dev.paul.cartlink.payment.model.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CheckoutResponse {
    private Long orderId;
    private PaymentStatus paymentStatus;
    private String paymentUrl; // For redirecting to payment gateway, if needed
    private String message;
} 