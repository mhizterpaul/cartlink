package dev.paul.cartlink.link.repository;

import dev.paul.cartlink.link.model.LinkAnalytics;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import dev.paul.cartlink.link.model.Link;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LinkAnalyticsRepository extends JpaRepository<LinkAnalytics, Long> {

    List<LinkAnalytics> findByProductLinkAndTimestampBetween(Link linkId, LocalDateTime createdAt);

    List<LinkAnalytics> findByProductLinkAndSource(Link productLink, String source);

    List<LinkAnalytics> findByProductLink_LinkId(Long linkId);

    List<LinkAnalytics> findByProductLink_LinkIdIn(List<Long> linkIds);
}