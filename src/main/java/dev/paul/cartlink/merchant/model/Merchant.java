package dev.paul.cartlink.merchant.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import dev.paul.cartlink.customer.model.Review;
import dev.paul.cartlink.complaint.model.Complaint;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "merchant", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class Merchant implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long merchantId;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Password is required")
    @Column(nullable = false)
    private String password;

    @NotBlank(message = "First name is required")
    @Column(nullable = false)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Column(nullable = false)
    private String lastName;

    private String middleName;
    private String image;
    private String phoneNumber;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "wallet_id", referencedColumnName = "wallet_id", nullable = false, unique = true)
    private Wallet wallet;

    private Double rating = 0.0;
    private Integer ratingCount = 0;

    @OneToMany(mappedBy = "merchant", cascade = CascadeType.ALL)
    @JsonManagedReference(value = "merchant-reviews")
    private List<Review> reviews;

    @OneToMany(mappedBy = "merchant", cascade = CascadeType.ALL)
    @JsonManagedReference(value = "merchant-products")
    private List<MerchantProduct> merchantProducts;

    @OneToMany(mappedBy = "merchant", cascade = CascadeType.ALL)
    private Set<Complaint> complaints = new HashSet<>();

    private boolean emailVerified = false;

    private String verificationToken;

    private LocalDateTime verificationTokenExpiry;

    private String passwordResetToken;

    private LocalDateTime passwordResetTokenExpiry;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "merchant_ip_addresses", joinColumns = @JoinColumn(name = "merchant_id"))
    @Column(name = "ip_address")
    private List<String> ipAddresses = new ArrayList<>();

    public Merchant(String email, String password, String firstName, String lastName) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_MERCHANT"));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public Long getId() {
        return merchantId;
    }
}
