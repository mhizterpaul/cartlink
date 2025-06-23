package dev.paul.cartlink.analytics.service;

import dev.paul.cartlink.analytics.model.LinkAnalytics;
import dev.paul.cartlink.analytics.repository.LinkAnalyticsRepository;
import dev.paul.cartlink.product.model.ProductLink;
import dev.paul.cartlink.product.repository.ProductLinkRepository;
import jakarta.servlet.http.HttpServletRequest;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class LinkAnalyticsService {
    private final LinkAnalyticsRepository linkAnalyticsRepository;
    private final ProductLinkRepository productLinkRepository;
    private final UserAgentAnalyzer uaAnalyzer;

    public LinkAnalyticsService(LinkAnalyticsRepository linkAnalyticsRepository,
            ProductLinkRepository productLinkRepository) {
        this.linkAnalyticsRepository = linkAnalyticsRepository;
        this.productLinkRepository = productLinkRepository;
        this.uaAnalyzer = UserAgentAnalyzer.newBuilder()
                .withField("DeviceClass")
                .withField("DeviceName")
                .withField("DeviceBrand")
                .withField("OperatingSystemName")
                .withField("OperatingSystemVersion")
                .build();
    }

    @Transactional
    public void recordPageView(Long linkId, HttpServletRequest request, String source) {
        // Skip if the request is from the merchant
        if (isMerchantRequest(request)) {
            return;
        }

        Optional<ProductLink> productLink = productLinkRepository.findById(linkId);
        if (productLink.isEmpty()) {
            return;
        }

        LinkAnalytics analytics = new LinkAnalytics();
        analytics.setProductLink(productLink.get());
        analytics.setSource(source);
        analytics.setIpAddress(getClientIp(request));
        analytics.setUserAgent(request.getHeader("User-Agent"));

        // Parse user agent for device info
        UserAgent userAgent = uaAnalyzer.parse(request.getHeader("User-Agent"));
        String deviceInfo = String.format("%s %s %s",
                userAgent.getValue("DeviceClass"),
                userAgent.getValue("DeviceBrand"),
                userAgent.getValue("DeviceName"));
        analytics.setDevice(deviceInfo);
        analytics.setLocation(getLocationFromIp(analytics.getIpAddress())); // You'll need to implement this

        linkAnalyticsRepository.save(analytics);

        // Update product link stats
        ProductLink link = productLink.get();
        link.setClicks(link.getClicks() + 1);
        productLinkRepository.save(link);
    }

    @Transactional
    public void recordTimeSpent(Long linkId, Long timeSpentSeconds) {
        Optional<ProductLink> productLink = productLinkRepository.findById(linkId);
        if (productLink.isEmpty()) {
            return;
        }

        LinkAnalytics analytics = new LinkAnalytics();
        analytics.setProductLink(productLink.get());
        analytics.setTimeSpent(timeSpentSeconds);
        linkAnalyticsRepository.save(analytics);
    }

    public List<LinkAnalytics> getAnalyticsForLink(Long linkId) {
        return linkAnalyticsRepository.findByProductLink_LinkId(linkId);
    }

    private boolean isMerchantRequest(HttpServletRequest request) {
        // Check if the request is coming from the merchant's domain or IP
        String referer = request.getHeader("Referer");

        // Add your merchant domain/IP checks here
        return referer != null && (referer.contains("merchant.cartlink.com") ||
                referer.contains("admin.cartlink.com"));
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0];
        }
        return request.getRemoteAddr();
    }

    private String getLocationFromIp(String ipAddress) {
        // Implement IP geolocation here
        // You can use a service like MaxMind GeoIP2 or IP-API
        return "Unknown"; // Placeholder
    }
}