package dev.paul.cartlink.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long userId;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Password is required")
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
    private String firstName;
    private String lastName;
    @NotBlank(message = "Account type is required")
    private String type;

    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public void setPassword(String password) {
        this.password = passwordEncoder.encode(password);
    }

    public boolean matchesPassword(String rawPassword) {
        return passwordEncoder.matches(rawPassword, this.password);
    }
}
