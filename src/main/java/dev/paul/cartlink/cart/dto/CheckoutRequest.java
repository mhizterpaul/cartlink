package dev.paul.cartlink.cart.dto;

import dev.paul.cartlink.payment.model.PaymentMethod;
import lombok.Data;

@Data
public class CheckoutRequest {
    private PaymentMethod paymentMethod;
    private String currency;
    // Add more fields as needed (e.g., customer info, address, etc.)
} 