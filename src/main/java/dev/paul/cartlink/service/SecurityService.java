package dev.paul.cartlink.service;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import dev.paul.cartlink.dto.SignUpRequest;
import dev.paul.cartlink.dto.Type;
import dev.paul.cartlink.model.Customer;
import dev.paul.cartlink.model.Merchant;
import dev.paul.cartlink.model.Wallet;
import dev.paul.cartlink.repository.CustomerRepository;
import dev.paul.cartlink.repository.MerchantRepository;
import dev.paul.cartlink.repository.WalletRepository;
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
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final TokenBlacklistService tokenBlacklistService;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public SecurityService(MerchantRepository merchantRepository,
            CustomerRepository customerRepository,
            WalletRepository walletRepository,
            PasswordEncoder passwordEncoder,
            TokenBlacklistService tokenBlacklistService) throws Exception {
        this.merchantRepository = merchantRepository;
        this.customerRepository = customerRepository;
        this.walletRepository = walletRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenBlacklistService = tokenBlacklistService;

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
            return merchant;
        }

        // If not found as merchant, try to find as customer
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return customer;
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
            if (tokenBlacklistService.isBlacklisted(token)) {
                return false;
            }
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

        if (Type.MERCHANT.equals(signUpRequest.getType())) {
            Merchant merchant = new Merchant();
            merchant.setEmail(email);
            merchant.setPassword(passwordEncoder.encode(password));
            merchant.setFirstName(signUpRequest.getFirstName());
            merchant.setLastName(signUpRequest.getLastName());
            merchant.setPhoneNumber(signUpRequest.getMobile());

            // Create a wallet for the merchant
            Wallet wallet = new Wallet();
            wallet.setBalance(0.0); // Set default balance
            wallet = walletRepository.save(wallet);
            merchant.setWallet(wallet);

            return merchantRepository.save(merchant);
        } else if (Type.CUSTOMER.equals(signUpRequest.getType())) {
            Customer customer = new Customer();
            customer.setEmail(email);
            customer.setFirstName(signUpRequest.getFirstName());
            customer.setLastName(signUpRequest.getLastName());
            customer.setPhoneNumber(signUpRequest.getMobile());
            return customerRepository.save(customer);
        } else {
            throw new IllegalArgumentException("Invalid user type. Must be either 'MERCHANT' or 'CUSTOMER'");
        }
    }

    public void invalidateToken(String token) {
        try {
            var decodedJwt = jwtDecoder.decode(token);
            long expirationTime = decodedJwt.getExpiresAt().toEpochMilli();
            tokenBlacklistService.blacklistToken(token, expirationTime);
        } catch (JwtException e) {
            // Token is already invalid, no need to blacklist
        }
    }
}
