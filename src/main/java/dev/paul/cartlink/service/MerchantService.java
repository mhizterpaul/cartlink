package dev.paul.cartlink.service;

import dev.paul.cartlink.model.Merchant;
import dev.paul.cartlink.model.Wallet;
import dev.paul.cartlink.model.Review;
import dev.paul.cartlink.model.Complaint;
import dev.paul.cartlink.model.Order;
import dev.paul.cartlink.repository.MerchantRepository;
import dev.paul.cartlink.repository.WalletRepository;
import dev.paul.cartlink.repository.ReviewRepository;
import dev.paul.cartlink.repository.ComplaintRepository;
import dev.paul.cartlink.repository.OrderRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MerchantService {

    private final MerchantRepository merchantRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityService securityService;
    private final ReviewRepository reviewRepository;
    private final ComplaintRepository complaintRepository;
    private final OrderRepository orderRepository;

    @Autowired
    public MerchantService(
            MerchantRepository merchantRepository,
            WalletRepository walletRepository,
            PasswordEncoder passwordEncoder,
            SecurityService securityService,
            ReviewRepository reviewRepository,
            ComplaintRepository complaintRepository,
            OrderRepository orderRepository) {
        this.merchantRepository = merchantRepository;
        this.walletRepository = walletRepository;
        this.passwordEncoder = passwordEncoder;
        this.securityService = securityService;
        this.reviewRepository = reviewRepository;
        this.complaintRepository = complaintRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public Merchant registerMerchant(Merchant merchant) {
        if (merchant.getEmail() == null || merchant.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (merchantRepository.existsByEmail(merchant.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Create and set wallet
        Wallet wallet = new Wallet();
        wallet.setBalance(0.0);
        wallet = walletRepository.save(wallet);
        merchant.setWallet(wallet);

        // Encode password
        merchant.setPassword(passwordEncoder.encode(merchant.getPassword()));

        return merchantRepository.save(merchant);
    }

    public String login(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        Merchant merchant = merchantRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(password, merchant.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        return securityService.generateToken(merchant);
    }

    public Merchant getMerchantByEmail(String email) {
        return merchantRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Merchant not found"));
    }

    public String refreshToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token is required");
        }

        // Validate the token and get the merchant
        if (!securityService.validateToken(token)) {
            throw new IllegalArgumentException("Invalid token");
        }

        // Get merchant email from token
        String email = securityService.getEmailFromToken(token);

        // Get merchant details
        Merchant merchant = merchantRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Merchant not found"));

        // Generate new token
        return securityService.generateToken(merchant);
    }

    @Transactional
    public void requestPasswordReset(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        Merchant merchant = merchantRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Email not found"));

        String resetToken = UUID.randomUUID().toString();
        // TODO: Send reset token via email
    }

    @Transactional
    public void resetPassword(String email, String resetToken, String newPassword) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        Merchant merchant = merchantRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Email not found"));

        // TODO: Validate reset token
        merchant.setPassword(passwordEncoder.encode(newPassword));
        merchantRepository.save(merchant);
    }

    public void logout(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token is required");
        }

        if (!securityService.validateToken(token)) {
            throw new IllegalArgumentException("Invalid token");
        }

        securityService.invalidateToken(token);
    }

    public Merchant getMerchantById(Long merchantId) {
        return merchantRepository.findById(merchantId)
                .orElseThrow(() -> new IllegalArgumentException("Merchant not found"));
    }

    public Merchant getCurrentMerchant() {
        // For now, return the test merchant (ID: 151)
        return merchantRepository.findById(151L).orElseThrow();
    }

    public Merchant updateMerchantProfile(Merchant merchant) {
        Merchant existingMerchant = getCurrentMerchant();
        existingMerchant.setFirstName(merchant.getFirstName());
        existingMerchant.setLastName(merchant.getLastName());
        existingMerchant.setPhoneNumber(merchant.getPhoneNumber());
        return merchantRepository.save(existingMerchant);
    }

    public List<Review> getMerchantReviews() {
        return reviewRepository.findByMerchant_merchantId(151L);
    }

    public List<Complaint> getMerchantComplaints() {
        return complaintRepository.findByOrder_MerchantProduct_Merchant_MerchantId(151L);
    }

    public List<Order> getMerchantOrders() {
        return orderRepository.findByMerchantProduct_Merchant_merchantId(151L);
    }

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // Get total sales
        double totalSales = orderRepository.findByMerchantProduct_Merchant_merchantId(151L)
                .stream()
                .mapToDouble(order -> order.getOrderSize() * order.getMerchantProduct().getProduct().getPrice())
                .sum();

        // Get total orders
        long totalOrders = orderRepository.countByMerchantProduct_Merchant_merchantId(151L);

        // Get total customers (unique customers who placed orders)
        long totalCustomers = orderRepository.findDistinctCustomerIdByMerchantProduct_Merchant_merchantId(151L).size();

        // Get today's sales
        double todaySales = orderRepository.findByMerchantProduct_Merchant_merchantIdAndCreatedAtAfter(151L,
                java.time.LocalDateTime.now().withHour(0).withMinute(0).withSecond(0))
                .stream()
                .mapToDouble(order -> order.getOrderSize() * order.getMerchantProduct().getProduct().getPrice())
                .sum();

        stats.put("todaySales", todaySales);
        stats.put("totalSales", totalSales);
        stats.put("totalOrders", totalOrders);
        stats.put("totalCustomers", totalCustomers);

        return stats;
    }
}