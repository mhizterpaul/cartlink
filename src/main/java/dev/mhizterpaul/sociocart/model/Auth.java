package dev.mhizterpaul.sociocart.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "auth", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class Auth {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long authId;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password must be at least 8 characters, contain at least one letter, one number, and one special character"
    )
    @Column(nullable = false)
    private String password;
    @Pattern(
            regexp = "^\\+[1-9][0-9]{0,2}[0-9]{4,13}$",
            message = "Mobile number must start with '+', followed by a 1 to 3-digit country code, and a 4 to 13-digit subscriber number, with a total length of at most 15 digits"
    )
    @Size(max = 15, message = "Mobile number must not exceed 15 digits in total (E.164 standard)")
    @Column(nullable = true)
    private String mobile;
    private Boolean mfa;
}
