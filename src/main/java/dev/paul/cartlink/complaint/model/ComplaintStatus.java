package dev.paul.cartlink.complaint.model;

import lombok.Getter;

@Getter
public enum ComplaintStatus {
    PENDING,
    IN_PROGRESS,
    RESOLVED,
    REJECTED
}