package dev.paul.cartlink.auth.service;

import dev.paul.cartlink.merchant.model.Merchant;
import dev.paul.cartlink.merchant.repository.MerchantRepository;
import dev.paul.cartlink.util.EmailService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final MerchantRepository merchantRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(MerchantRepository merchantRepository, EmailService emailService, PasswordEncoder passwordEncoder) {
        this.merchantRepository = merchantRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    public void sendVerificationEmail(Merchant merchant) {
        String token = UUID.randomUUID().toString();
        merchant.setVerificationToken(token);
        merchant.setVerificationTokenExpiry(LocalDateTime.now().plusHours(24));
        merchantRepository.save(merchant);

        String link = "http://localhost:8080/api/auth/verify-email?token=" + token;
        emailService.sendEmail(merchant.getEmail(), "Verify your email", "Click the following link to verify your email: " + link);
    }

    public boolean verifyEmail(String token, String ipAddress) {
        Optional<Merchant> merchantOptional = merchantRepository.findByVerificationToken(token);
        if (merchantOptional.isEmpty()) {
            return false;
        }

        Merchant merchant = merchantOptional.get();
        if (merchant.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            return false;
        }

        merchant.setEmailVerified(true);
        merchant.setVerificationToken(null);
        merchant.setVerificationTokenExpiry(null);
        merchant.getIpAddresses().add(ipAddress);
        merchantRepository.save(merchant);
        return true;
    }

    public void sendPasswordResetEmail(String email) {
        Optional<Merchant> merchantOptional = merchantRepository.findByEmail(email);
        if (merchantOptional.isEmpty()) {
            // Don't reveal that the user doesn't exist
            return;
        }

        Merchant merchant = merchantOptional.get();
        String token = UUID.randomUUID().toString();
        merchant.setPasswordResetToken(token);
        merchant.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(1));
        merchantRepository.save(merchant);

        String link = "http://localhost:8080/api/auth/reset-password?token=" + token;
        emailService.sendEmail(merchant.getEmail(), "Reset your password", "Click the following link to reset your password: " + link);
    }

    public boolean resetPassword(String token, String password) {
        Optional<Merchant> merchantOptional = merchantRepository.findByPasswordResetToken(token);
        if (merchantOptional.isEmpty()) {
            return false;
        }

        Merchant merchant = merchantOptional.get();
        if (merchant.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
            return false;
        }

        merchant.setPassword(passwordEncoder.encode(password));
        merchant.setPasswordResetToken(null);
        merchant.setPasswordResetTokenExpiry(null);
        merchantRepository.save(merchant);
        return true;
    }
} 