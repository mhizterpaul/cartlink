package dev.paul.cartlink.customer.model;

import lombok.Getter;

@Getter
public enum RefundStatus {
    PENDING,
    APPROVED,
    REJECTED,
    PROCESSED
}