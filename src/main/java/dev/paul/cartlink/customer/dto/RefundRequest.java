package dev.paul.cartlink.customer.dto;

public class RefundRequest {
    private Long orderId;
    private String reason;
    private Double amount;
    private String accountNumber;
    private String bankName;
    private String accountName;

    public RefundRequest() {
    }

    public RefundRequest(Long orderId, String reason, Double amount, String accountNumber, String bankName,
            String accountName) {
        this.orderId = orderId;
        this.reason = reason;
        this.amount = amount;
        this.accountNumber = accountNumber;
        this.bankName = bankName;
        this.accountName = accountName;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }
}