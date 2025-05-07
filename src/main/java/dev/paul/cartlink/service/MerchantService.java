package dev.paul.cartlink.service;

import dev.paul.cartlink.model.Merchant;
import dev.paul.cartlink.repository.MerchantRepository;
import dev.paul.cartlink.repository.WalletRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class MerchantService {

    private final MerchantRepository merchantRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityService securityService;

    public MerchantService(MerchantRepository merchantRepository,
                         WalletRepository walletRepository,
                         PasswordEncoder passwordEncoder,
                         SecurityService securityService) {
        this.merchantRepository = merchantRepository;
        this.walletRepository = walletRepository;
        this.passwordEncoder = passwordEncoder;
        this.securityService = securityService;
    }

    @Transactional
    public Merchant registerMerchant(Merchant merchant) {
        if (merchantRepository.existsByEmail(merchant.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        merchant.setPassword(passwordEncoder.encode(merchant.getPassword()));
        return merchantRepository.save(merchant);
    }

    public String login(String email, String password) {
        Merchant merchant = merchantRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(password, merchant.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        return securityService.generateToken(merchant);
    }

    @Transactional
    public void requestPasswordReset(String email) {
        Merchant merchant = merchantRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Email not found"));

        String resetToken = UUID.randomUUID().toString();
        // TODO: Send reset token via email
    }

    @Transactional
    public void resetPassword(String email, String resetToken, String newPassword) {
        Merchant merchant = merchantRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Email not found"));

        // TODO: Validate reset token
        merchant.setPassword(passwordEncoder.encode(newPassword));
        merchantRepository.save(merchant);
    }

    public Merchant getMerchantById(Long merchantId) {
        return merchantRepository.findById(merchantId)
                .orElseThrow(() -> new IllegalArgumentException("Merchant not found"));
    }
} 