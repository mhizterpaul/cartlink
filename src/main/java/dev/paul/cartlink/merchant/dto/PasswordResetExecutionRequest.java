package dev.paul.cartlink.merchant.dto;

public class PasswordResetExecutionRequest {
    private String email;
    private String resetToken;
    private String newPassword;

    public PasswordResetExecutionRequest() {
    }

    public PasswordResetExecutionRequest(String email, String resetToken, String newPassword) {
        this.email = email;
        this.resetToken = resetToken;
        this.newPassword = newPassword;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}