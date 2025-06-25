package dev.paul.cartlink.merchant.service;

import dev.paul.cartlink.order.model.Order;
import dev.paul.cartlink.order.repository.OrderRepository;
import dev.paul.cartlink.security.service.SecurityService;
import dev.paul.cartlink.link.model.LinkAnalytics;
import dev.paul.cartlink.link.repository.LinkAnalyticsRepository;
import dev.paul.cartlink.complaint.model.Complaint;
import dev.paul.cartlink.complaint.repository.ComplaintRepository;
import dev.paul.cartlink.customer.model.Review;
import dev.paul.cartlink.customer.repository.ReviewRepository;
import dev.paul.cartlink.merchant.model.Merchant;
import dev.paul.cartlink.merchant.model.Wallet;
import dev.paul.cartlink.merchant.repository.MerchantRepository;
import dev.paul.cartlink.merchant.repository.WalletRepository;

import dev.paul.cartlink.auth.service.AuthService;
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
import java.util.function.Function;

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
    private final AuthService authService;

    @Autowired
    public MerchantService(
            MerchantRepository merchantRepository,
            WalletRepository walletRepository,
            PasswordEncoder passwordEncoder,
            SecurityService securityService,
            ReviewRepository reviewRepository,
            ComplaintRepository complaintRepository,
            OrderRepository orderRepository,
            LinkAnalyticsRepository linkAnalyticsRepository,
            AuthService authService) {
        this.merchantRepository = merchantRepository;
        this.walletRepository = walletRepository;
        this.passwordEncoder = passwordEncoder;
        this.securityService = securityService;
        this.reviewRepository = reviewRepository;
        this.complaintRepository = complaintRepository;
        this.orderRepository = orderRepository;
        this.linkAnalyticsRepository = linkAnalyticsRepository;
        this.authService = authService;
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

        Merchant savedMerchant = merchantRepository.save(merchant);
        authService.sendVerificationEmail(savedMerchant);
        return savedMerchant;
    }

    public String login(String email, String password, String ipAddress) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        Merchant merchant = merchantRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(password, merchant.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        if (!merchant.isEmailVerified()) {
            throw new IllegalArgumentException("Email not verified");
        }

        // Update and rank IP addresses
        List<String> ipAddresses = merchant.getIpAddresses();
        ipAddresses.add(ipAddress);

        Map<String, Long> ipCounts = ipAddresses.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        List<String> sortedIps = ipAddresses.stream()
                .distinct()
                .sorted(Comparator.comparing((String ip) -> ipCounts.get(ip)).reversed())
                .collect(Collectors.toList());

        merchant.setIpAddresses(sortedIps);
        merchantRepository.save(merchant);

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
                        * Optional.ofNullable(order.getMerchantProduct().getPrice()).orElse(0.0))
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
                        * Optional.ofNullable(order.getMerchantProduct().getPrice()).orElse(0.0))
                .sum();

        stats.put("todaySales", todaySales);
        stats.put("totalSales", totalSales);
        stats.put("totalOrders", totalOrders);
        stats.put("totalCustomers", totalCustomers);

        return stats;
    }

    // Fix: Use getSalesDataForChart for sales chart data
    public List<Map<String, Object>> getSalesDataForChart(String period, String startDate, String endDate) {
        Merchant currentMerchant = getCurrentMerchant();
        List<Order> orders = orderRepository.findByMerchantProduct_Merchant_merchantId(currentMerchant.getMerchantId());
        java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
        java.util.Map<String, Double> salesData = new java.util.HashMap<>();
        if (period != null && period.equalsIgnoreCase("week")) {
            // Group by week
            orders.stream().collect(Collectors.groupingBy(
                    order -> order.getOrderDate().with(java.time.DayOfWeek.MONDAY).toLocalDate().toString(),
                    Collectors.summingDouble(order -> Optional.ofNullable(order.getQuantity()).orElse(0)
                            * Optional.ofNullable(order.getMerchantProduct().getPrice()).orElse(0.0))))
                    .forEach(salesData::put);
        } else if (period != null && period.equalsIgnoreCase("month")) {
            // Group by month
            orders.stream().collect(Collectors.groupingBy(
                    order -> order.getOrderDate().getMonth().getDisplayName(java.time.format.TextStyle.SHORT,
                            java.util.Locale.ENGLISH),
                    Collectors.summingDouble(order -> Optional.ofNullable(order.getQuantity()).orElse(0)
                            * Optional.ofNullable(order.getMerchantProduct().getPrice()).orElse(0.0))))
                    .forEach(salesData::put);
        } else if (period != null && period.equalsIgnoreCase("quarter")) {
            // Group by quarter
            orders.stream().collect(Collectors.groupingBy(
                    order -> "Q" + ((order.getOrderDate().getMonthValue() - 1) / 3 + 1),
                    Collectors.summingDouble(order -> Optional.ofNullable(order.getQuantity()).orElse(0)
                            * Optional.ofNullable(order.getMerchantProduct().getPrice()).orElse(0.0))))
                    .forEach(salesData::put);
        } else if (startDate != null && endDate != null) {
            java.time.LocalDate start = java.time.LocalDate.parse(startDate, dtf);
            java.time.LocalDate end = java.time.LocalDate.parse(endDate, dtf);
            orders.stream().filter(order -> {
                java.time.LocalDate orderDate = order.getOrderDate().toLocalDate();
                return (orderDate.isEqual(start) || orderDate.isAfter(start))
                        && (orderDate.isEqual(end) || orderDate.isBefore(end));
            }).collect(Collectors.groupingBy(
                    order -> order.getOrderDate().toLocalDate().toString(),
                    Collectors.summingDouble(order -> Optional.ofNullable(order.getQuantity()).orElse(0)
                            * Optional.ofNullable(order.getMerchantProduct().getPrice()).orElse(0.0))))
                    .forEach(salesData::put);
        } else {
            // Default: group by month
            orders.stream().collect(Collectors.groupingBy(
                    order -> order.getOrderDate().getMonth().getDisplayName(java.time.format.TextStyle.SHORT,
                            java.util.Locale.ENGLISH),
                    Collectors.summingDouble(order -> Optional.ofNullable(order.getQuantity()).orElse(0)
                            * Optional.ofNullable(order.getMerchantProduct().getPrice()).orElse(0.0))))
                    .forEach(salesData::put);
        }
        return salesData.entrySet().stream().map(entry -> {
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("name", entry.getKey());
            data.put("sales", entry.getValue());
            return data;
        }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getTrafficDataForChart() {
        Merchant currentMerchant = getCurrentMerchant();
        // Collect all LinkAnalytics for this merchant's products
        List<LinkAnalytics> analytics = currentMerchant.getMerchantProducts().stream()
                .flatMap(mp -> mp.getLinkAnalytics() != null ? mp.getLinkAnalytics().stream()
                        : java.util.stream.Stream.empty())
                .collect(Collectors.toList());

        // Aggregate all uniqueSourceClicks
        Map<String, Long> trafficSourceCounts = new HashMap<>();
        for (LinkAnalytics analytic : analytics) {
            if (analytic.getUniqueSourceClicks() != null) {
                analytic.getUniqueSourceClicks().forEach((source, count) -> {
                    String key = (source != null && !source.isEmpty()) ? source : "Direct/Unknown";
                    trafficSourceCounts.put(key, trafficSourceCounts.getOrDefault(key, 0L) + count);
                });
            }
        }

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