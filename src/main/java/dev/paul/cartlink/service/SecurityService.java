package dev.paul.cartlink.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import dev.paul.cartlink.dto.SignUpRequest;
import dev.paul.cartlink.model.Customer;
import dev.paul.cartlink.model.Merchant;
import dev.paul.cartlink.repository.CustomerRepository;
import dev.paul.cartlink.repository.MerchantRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.UUID;

@Service
public class SecurityService implements UserDetailsService {

    private final MerchantRepository merchantRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public SecurityService(MerchantRepository merchantRepository,
                           CustomerRepository customerRepository,
                           PasswordEncoder passwordEncoder) throws Exception {
        this.merchantRepository = merchantRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;

        // Generate RSA key pair
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        // Create RSA key
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();

        // Create JWK source
        JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(rsaKey));

        // Initialize JWT encoder and decoder
        this.jwtEncoder = new NimbusJwtEncoder(jwkSource);
        this.jwtDecoder = NimbusJwtDecoder.withPublicKey(publicKey).build();
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Try to find merchant first
        Merchant merchant = merchantRepository.findByEmail(email).orElse(null);
        if (merchant != null) {
            return org.springframework.security.core.userdetails.User.builder()
                    .username(merchant.getEmail())
                    .password(merchant.getPassword())
                    .roles("MERCHANT")
                    .build();
        }

        // If not found as merchant, try to find as customer
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return org.springframework.security.core.userdetails.User.builder()
                .username(customer.getEmail())
                .password(customer.getPassword())
                .roles("CUSTOMER")
                .build();
    }

    public String generateToken(UserDetails userDetails) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(jwtExpiration))
                .claim("roles", userDetails.getAuthorities())
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public boolean validateToken(String token) {
        try {
            jwtDecoder.decode(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public String getEmailFromToken(String token) {
        return jwtDecoder.decode(token).getSubject();
    }

    @Transactional
    public UserDetails registerUser(SignUpRequest signUpRequest) {
        String email = signUpRequest.getEmail();
        String password = signUpRequest.getPassword();

        if (merchantRepository.existsByEmail(email) || customerRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        if ("MERCHANT".equalsIgnoreCase(signUpRequest.getType())) {
            Merchant merchant = new Merchant();
            merchant.setEmail(email);
            merchant.setPassword(passwordEncoder.encode(password));
            merchant.setFirstName(signUpRequest.getFirstName());
            merchant.setLastName(signUpRequest.getLastName());
            merchant.setPhoneNumber(signUpRequest.getMobile());
            return merchantRepository.save(merchant);
        } else {
            Customer customer = new Customer();
            customer.setEmail(email);
            customer.setPassword(passwordEncoder.encode(password));
            customer.setFirstName(signUpRequest.getFirstName());
            customer.setLastName(signUpRequest.getLastName());
            customer.setPhoneNumber(signUpRequest.getMobile());
            return customerRepository.save(customer);
        }
    }
}
