package dev.paul.cartlink.product.service;

import dev.paul.cartlink.analytics.model.LinkAnalytics;
import dev.paul.cartlink.analytics.repository.LinkAnalyticsRepository;
import dev.paul.cartlink.merchant.dto.MerchantProduct;
import dev.paul.cartlink.merchant.model.Merchant;
import dev.paul.cartlink.model.*;
import dev.paul.cartlink.product.model.ProductLink;
import dev.paul.cartlink.product.repository.ProductLinkRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ProductLinkService {

    private final ProductLinkRepository productLinkRepository;
    private final LinkAnalyticsRepository linkAnalyticsRepository;

    public ProductLinkService(ProductLinkRepository productLinkRepository,
            LinkAnalyticsRepository linkAnalyticsRepository) {
        this.productLinkRepository = productLinkRepository;
        this.linkAnalyticsRepository = linkAnalyticsRepository;
    }

    @Transactional
    public ProductLink generateProductLink(MerchantProduct merchantProduct) {
        ProductLink productLink = new ProductLink();
        productLink.setMerchantProduct(merchantProduct);
        productLink.setUrl(generateUniqueUrl());
        productLink.setQrCode(generateQRCode(productLink.getUrl()));
        productLink.setClicks(0);
        productLink.setConversions(0);

        return productLinkRepository.save(productLink);
    }

    public List<ProductLink> getMerchantProductLinks(Merchant merchant) {
        return productLinkRepository.findByMerchantProduct_Merchant_MerchantId(merchant.getMerchantId());
    }

    @Transactional
    public void trackLinkClick(ProductLink productLink, String source, String device,
            String location, String ipAddress, String userAgent) {
        productLink.setClicks(productLink.getClicks() + 1);
        productLinkRepository.save(productLink);

        LinkAnalytics analytics = new LinkAnalytics();
        analytics.setProductLink(productLink);
        analytics.setSource(source);
        analytics.setDevice(device);
        analytics.setLocation(location);
        analytics.setIpAddress(ipAddress);
        analytics.setUserAgent(userAgent);

        linkAnalyticsRepository.save(analytics);
    }

    @Transactional
    public void trackConversion(ProductLink productLink) {
        productLink.setConversions(productLink.getConversions() + 1);
        productLinkRepository.save(productLink);
    }

    public List<LinkAnalytics> getLinkAnalytics(ProductLink productLink, LocalDateTime startDate,
            LocalDateTime endDate) {
        return linkAnalyticsRepository.findByProductLinkAndTimestampBetween(productLink, startDate, endDate);
    }

    public List<LinkAnalytics> getTrafficSources(ProductLink productLink) {
        return linkAnalyticsRepository.findByProductLink(productLink);
    }

    private String generateUniqueUrl() {
        return UUID.randomUUID().toString();
    }

    private String generateQRCode(String url) {
        // TODO: Implement QR code generation using a library like ZXing
        return "QR_CODE_" + url;
    }
}