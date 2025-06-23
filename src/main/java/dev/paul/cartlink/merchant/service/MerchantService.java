package dev.paul.cartlink.merchant.service;

import dev.paul.cartlink.order.model.Order;
import dev.paul.cartlink.order.repository.OrderRepository;
import dev.paul.cartlink.analytics.model.LinkAnalytics;
import dev.paul.cartlink.analytics.repository.LinkAnalyticsRepository;
import dev.paul.cartlink.complaint.model.Complaint;
import dev.paul.cartlink.complaint.repository.ComplaintRepository;
import dev.paul.cartlink.config.SecurityService;
import dev.paul.cartlink.customer.model.Review;
import dev.paul.cartlink.customer.repository.ReviewRepository;
import dev.paul.cartlink.merchant.model.Merchant;
import dev.paul.cartlink.merchant.model.Wallet;
import dev.paul.cartlink.merchant.repository.MerchantRepository;
import dev.paul.cartlink.merchant.repository.WalletRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Comparator;

@Service
public class MerchantService {

    private final MerchantRepository merchantRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityService securityService;
    private final ReviewRepository reviewRepository;
    private final ComplaintRepository complaintRepository;
    private final OrderRepository orderRepository;
    private final LinkAnalyticsRepository linkAnalyticsRepository;

    @Autowired
    public MerchantService(
            MerchantRepository merchantRepository,
            WalletRepository walletRepository,
            PasswordEncoder passwordEncoder,
            SecurityService securityService,
            ReviewRepository reviewRepository,
            ComplaintRepository complaintRepository,
            OrderRepository orderRepository,
            LinkAnalyticsRepository linkAnalyticsRepository) {
        this.merchantRepository = merchantRepository;
        this.walletRepository = walletRepository;
        this.passwordEncoder = passwordEncoder;
        this.securityService = securityService;
        this.reviewRepository = reviewRepository;
        this.complaintRepository = complaintRepository;
        this.orderRepository = orderRepository;
        this.linkAnalyticsRepository = linkAnalyticsRepository;
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
        Merchant currentMerchant = getCurrentMerchant();
        return reviewRepository.findByMerchant_merchantId(currentMerchant.getMerchantId());
    }

    public List<Complaint> getMerchantComplaints() {
        dev.paul.cartlink.merchant.model.Merchant currentMerchant = getCurrentMerchant();
        return complaintRepository.findByOrder_MerchantProduct_Merchant_MerchantId(currentMerchant.getMerchantId());
    }

    public List<Order> getMerchantOrders() {
        Merchant currentMerchant = getCurrentMerchant();
        return orderRepository.findByMerchantProduct_Merchant_merchantId(currentMerchant.getMerchantId());
    }

    public Map<String, Object> getDashboardStats() {
        Merchant currentMerchant = getCurrentMerchant();
        Map<String, Object> stats = new HashMap<>();

        // Get total sales
        double totalSales = orderRepository.findByMerchantProduct_Merchant_merchantId(currentMerchant.getMerchantId())
                .stream()
                .mapToDouble(order -> Optional.ofNullable(order.getQuantity()).orElse(0)
                        * Optional.ofNullable(order.getMerchantProduct().getProduct().getPrice()).orElse(0.0))
                .sum();

        // Get total orders
        long totalOrders = orderRepository.countByMerchantProduct_Merchant_merchantId(currentMerchant.getMerchantId());

        // Get total customers (unique customers who placed orders)
        long totalCustomers = orderRepository
                .findDistinctCustomerIdByMerchantProduct_Merchant_merchantId(currentMerchant.getMerchantId()).size();

        // Get today's sales
        double todaySales = orderRepository
                .findByMerchantProduct_Merchant_merchantIdAndOrderDateAfter(currentMerchant.getMerchantId(),
                        java.time.LocalDateTime.now().withHour(0).withMinute(0).withSecond(0))
                .stream()
                .mapToDouble(order -> Optional.ofNullable(order.getQuantity()).orElse(0)
                        * Optional.ofNullable(order.getMerchantProduct().getProduct().getPrice()).orElse(0.0))
                .sum();

        stats.put("todaySales", todaySales);
        stats.put("totalSales", totalSales);
        stats.put("totalOrders", totalOrders);
        stats.put("totalCustomers", totalCustomers);

        return stats;
    }

    public List<Map<String, Object>> getSalesDataForChart() {
        Merchant currentMerchant = getCurrentMerchant();
        List<Order> orders = orderRepository.findByMerchantProduct_Merchant_merchantId(currentMerchant.getMerchantId());

        Map<Month, Double> monthlySales = orders.stream()
                .collect(Collectors.groupingBy(
                        order -> order.getOrderDate().getMonth(),
                        Collectors.summingDouble(order -> Optional.ofNullable(order.getQuantity()).orElse(0) * Optional
                                .ofNullable(order.getMerchantProduct().getProduct().getPrice()).orElse(0.0))));

        return monthlySales.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("name", entry.getKey().getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
                    data.put("sales", entry.getValue());
                    return data;
                })
                .sorted(Comparator.comparingInt(
                        data -> Month.valueOf(((String) data.get("name")).toUpperCase(Locale.ENGLISH)).getValue()))
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getTrafficDataForChart() {
        Merchant currentMerchant = getCurrentMerchant();
        List<Long> productLinkIds = currentMerchant.getMerchantProducts().stream()
                .flatMap(merchantProduct -> merchantProduct.getProductLinks().stream())
                .map(productLink -> productLink.getLinkId())
                .collect(Collectors.toList());

        List<LinkAnalytics> analytics = linkAnalyticsRepository.findByProductLink_LinkIdIn(productLinkIds);

        Map<String, Long> trafficSourceCounts = analytics.stream()
                .collect(Collectors.groupingBy(
                        analytic -> analytic.getSource() != null && !analytic.getSource().isEmpty()
                                ? analytic.getSource()
                                : "Direct/Unknown",
                        Collectors.counting()));

        return trafficSourceCounts.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("name", entry.getKey());
                    data.put("value", entry.getValue());
                    return data;
                })
                .collect(Collectors.toList());
    }
}