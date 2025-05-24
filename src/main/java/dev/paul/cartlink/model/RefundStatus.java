package dev.paul.cartlink.model;

import lombok.Getter;

@Getter
public enum RefundStatus {
    PENDING,
    APPROVED,
    REJECTED,
    PROCESSED
} 