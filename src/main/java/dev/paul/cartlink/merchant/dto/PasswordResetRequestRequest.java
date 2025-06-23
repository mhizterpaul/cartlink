package dev.paul.cartlink.merchant.dto;

public class PasswordResetRequestRequest {
    private String email;

    public PasswordResetRequestRequest() {
    }

    public PasswordResetRequestRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}