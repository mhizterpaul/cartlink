package dev.paul.cartlink.customer.service;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;

@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequest {
        @Email(message = "Invalid email format")
        @NotBlank(message = "Email is required")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", message = "Password must be at least 8 characters, contain at least one letter, one number, and one special character")
        private String password;

        @Pattern(regexp = "^\\+[1-9][0-9]{0,2}[0-9]{4,13}$", message = "Mobile number must start with '+', followed by a 1 to 3-digit country code, and a 4 to 13-digit subscriber number, with a total length of at most 15 digits")
        @Size(max = 15, message = "Mobile number must not exceed 15 digits in total (E.164 standard)")
        private String mobile;

        @NotBlank(message = "First name is required")
        private String firstName;

        @NotBlank(message = "Last name is required")
        private String lastName;

        private String middleName;
        private String phoneNumber;
        private String type; // "MERCHANT" or "CUSTOMER"
        private String street;
        private String city;
        private String state;
        private String country;
        private String postalCode;

        public SignUpRequest(String email, String firstName, String lastName, String phoneNumber, String street,
                        String city, String state, String country, String postalCode) {
                this.email = email;
                this.firstName = firstName;
                this.lastName = lastName;
                this.phoneNumber = phoneNumber;
                this.street = street;
                this.city = city;
                this.state = state;
                this.country = country;
                this.postalCode = postalCode;
        }

        public String getStreet() {
                return street;
        }

        public void setStreet(String street) {
                this.street = street;
        }

        public String getCity() {
                return city;
        }

        public void setCity(String city) {
                this.city = city;
        }

        public String getState() {
                return state;
        }

        public void setState(String state) {
                this.state = state;
        }

        public String getCountry() {
                return country;
        }

        public void setCountry(String country) {
                this.country = country;
        }

        public String getPostalCode() {
                return postalCode;
        }

        public void setPostalCode(String postalCode) {
                this.postalCode = postalCode;
        }
}