package dev.paul.cartlink.merchant.dto;

public class CouponCreateRequest {
    private Double discount;
    private String validFrom;
    private String validUntil;
    private Integer maxUsage;
    private Integer maxUsers;

    public CouponCreateRequest() {
    }

    public CouponCreateRequest(Double discount, String validFrom, String validUntil, Integer maxUsage,
            Integer maxUsers) {
        this.discount = discount;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
        this.maxUsage = maxUsage;
        this.maxUsers = maxUsers;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public String getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(String validFrom) {
        this.validFrom = validFrom;
    }

    public String getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(String validUntil) {
        this.validUntil = validUntil;
    }

    public Integer getMaxUsage() {
        return maxUsage;
    }

    public void setMaxUsage(Integer maxUsage) {
        this.maxUsage = maxUsage;
    }

    public Integer getMaxUsers() {
        return maxUsers;
    }

    public void setMaxUsers(Integer maxUsers) {
        this.maxUsers = maxUsers;
    }
}