package dev.paul.cartlink.dto;

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
}