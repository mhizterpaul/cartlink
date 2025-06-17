package dev.paul.cartlink.analytics.repository;

import dev.paul.cartlink.analytics.model.LinkAnalytics;
import dev.paul.cartlink.product.model.ProductLink;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LinkAnalyticsRepository extends JpaRepository<LinkAnalytics, Long> {
    List<LinkAnalytics> findByProductLink(ProductLink productLink);

    List<LinkAnalytics> findByProductLinkAndTimestampBetween(ProductLink productLink, LocalDateTime startDate,
            LocalDateTime endDate);

    List<LinkAnalytics> findByProductLinkAndSource(ProductLink productLink, String source);

    List<LinkAnalytics> findByProductLink_LinkId(Long linkId);

    List<LinkAnalytics> findByProductLink_LinkIdIn(List<Long> linkIds);
}