package dev.paul.cartlink.repository;

import dev.paul.cartlink.model.LinkAnalytics;
import dev.paul.cartlink.model.ProductLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LinkAnalyticsRepository extends JpaRepository<LinkAnalytics, Long> {
    List<LinkAnalytics> findByProductLink(ProductLink productLink);
    List<LinkAnalytics> findByProductLinkAndTimestampBetween(ProductLink productLink, LocalDateTime startDate, LocalDateTime endDate);
    List<LinkAnalytics> findByProductLinkAndSource(ProductLink productLink, String source);
} 