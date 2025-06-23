package dev.paul.cartlink.merchant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String refreshToken;
    private String type;
    private String email;
    private String name;
    private String message;
    private String merchantId;
    private MerchantDetails merchantDetails;

    public String getMerchantId() {
        return merchantId;
    }

    public String getToken() {
        return token;
    }

    public static class MerchantDetails {
        private String email;
        private String name;

        // Add other fields as needed
        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}