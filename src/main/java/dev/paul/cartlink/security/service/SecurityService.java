package dev.paul.cartlink.security.service;

import dev.paul.cartlink.auth.TokenBlacklistService;
import dev.paul.cartlink.customer.model.Customer;
import dev.paul.cartlink.customer.repository.CustomerRepository;
import dev.paul.cartlink.merchant.dto.SignUpRequest;
import dev.paul.cartlink.merchant.dto.Type;
import dev.paul.cartlink.merchant.model.Merchant;
import dev.paul.cartlink.merchant.model.Wallet;
import dev.paul.cartlink.merchant.repository.MerchantRepository;
import dev.paul.cartlink.merchant.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SecurityService implements UserDetailsService {

    private final MerchantRepository merchantRepository;
    private final CustomerRepository customerRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final TokenBlacklistService tokenBlacklistService;

    @Value("${rsa.private-key}")
    private String privateKeyPath;

    @Value("${rsa.public-key}")
    private String publicKeyPath;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Override
    @Transactional(readOnly = true)
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
        try {

            Map<String, Object> claims = new HashMap<>();
            if (userDetails instanceof Merchant merchant) {
                claims.put("roles", merchant.getAuthorities().stream()
                        .map(authority -> authority.getAuthority())
                        .toList());
            }

            JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                    .issuer("self")
                    .subject(userDetails.getUsername())
                    .claims(claimsMap -> claimsMap.putAll(claims))
                    .build();

            return jwtEncoder.encode(JwtEncoderParameters.from(claimsSet)).getTokenValue();
        } catch (Exception e) {
            throw new RuntimeException("Error generating token", e);
        }
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
