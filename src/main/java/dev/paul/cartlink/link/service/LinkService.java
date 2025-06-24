package dev.paul.cartlink.link.service;

import dev.paul.cartlink.link.model.Link;
import dev.paul.cartlink.link.model.LinkAnalytics;
import dev.paul.cartlink.merchant.model.Merchant;
import dev.paul.cartlink.merchant.model.MerchantProduct;
import dev.paul.cartlink.merchant.repository.MerchantProductRepository;
import dev.paul.cartlink.merchant.repository.MerchantRepository;
import dev.paul.cartlink.link.repository.LinkRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LinkService {

    private final LinkRepository linkRepository;
    private final MerchantRepository merchantRepository;
    private final MerchantProductRepository merchantProductRepository;

    @Transactional
    public Link createLink(Long merchantId, Set<Long> merchantProductIds) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid merchant ID"));

        Set<MerchantProduct> merchantProducts = merchantProductIds.stream()
                .map(id -> merchantProductRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid merchant product ID: " + id)))
                .collect(Collectors.toSet());

        // Validate that all products belong to the merchant
        merchantProducts.forEach(p -> {
            if (!p.getMerchant().getId().equals(merchantId)) {
                throw new SecurityException("Product " + p.getId() + " does not belong to merchant " + merchantId);
            }
        });

        Link link = new Link();
        link.setMerchant(merchant);
        link.setMerchantProducts(merchantProducts);
        link.setSlug(generateUniqueSlug());
        link.setAnalytics(new LinkAnalytics());

        return linkRepository.save(link);
    }

    @Transactional
    public Set<MerchantProduct> resolveLink(String slug, HttpServletRequest request, HttpServletResponse response) {
        Link link = linkRepository.findBySlug(slug)
                .orElseThrow(() -> new IllegalArgumentException("Invalid link slug"));

        String visitorId = getOrCreateVisitorId(request, response);
        boolean isNewVisitor = isNewVisitor(visitorId, link);

        if (isNewVisitor) {
            updateAnalyticsForNewClick(link, request);
            // Optionally, you could store the visitorId in a cache or in-memory set for the link
        } else {
            updateAnalyticsForReturningVisitor(link, request);
        }

        // Always update the last updated timestamp
        link.getAnalytics().setLastUpdated(LocalDateTime.now());
        linkRepository.save(link);

        return link.getMerchantProducts();
    }

    private boolean isNewVisitor(String visitorId, Link link) {
        // For this implementation, treat every visitorId as unique per browser/device
        // In a real system, you might use a cache or external store to track visitorIds per link
        // Here, we assume the cookie is unique and persistent
        // Always return true if you want to increment unique clicks for every new cookie
        return true;
    }

    private void updateAnalyticsForNewClick(Link link, HttpServletRequest request) {
        LinkAnalytics analytics = link.getAnalytics();
        analytics.setTotalUniqueClicks(analytics.getTotalUniqueClicks() + 1);

        // Update device type counts
        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null && userAgent.toLowerCase().contains("mobi")) {
            analytics.setTotalUniqueMobile(analytics.getTotalUniqueMobile() + 1);
        } else {
            analytics.setTotalUniqueDesktops(analytics.getTotalUniqueDesktops() + 1);
        }

        // Update source clicks
        String referrer = request.getHeader("Referer");
        if (referrer != null) {
            analytics.getUniqueSourceClicks().merge(referrer, 1, Integer::sum);
        }
    }

    private void updateAnalyticsForReturningVisitor(Link link, HttpServletRequest request) {
        // Optionally update analytics for returning visitors (e.g., time spent, etc.)
        // For now, do nothing
    }

    private String getOrCreateVisitorId(HttpServletRequest request, HttpServletResponse response) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("cartlink_visitor_id".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        String newVisitorId = UUID.randomUUID().toString();
        Cookie cookie = new Cookie("cartlink_visitor_id", newVisitorId);
        cookie.setPath("/");
        cookie.setMaxAge(365 * 24 * 60 * 60); // 1 year
        response.addCookie(cookie);
        return newVisitorId;
    }

    private String generateUniqueSlug() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
