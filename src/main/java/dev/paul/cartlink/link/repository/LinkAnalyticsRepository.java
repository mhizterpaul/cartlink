package dev.paul.cartlink.link.repository;

import dev.paul.cartlink.link.model.LinkAnalytics;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import dev.paul.cartlink.link.model.Link;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LinkAnalyticsRepository extends JpaRepository<LinkAnalytics, Long> {

    List<LinkAnalytics> findByLinkAndLastUpdatedBetween(Link link, LocalDateTime startDateTime, LocalDateTime endDateTime);

    List<LinkAnalytics> findByLinkId(Long linkId);

    List<LinkAnalytics> findByLinkIdIn(List<Long> linkIds);

    @Query("SELECT la FROM LinkAnalytics la JOIN la.uniqueSourceClicks usc WHERE la.link = :link AND KEY(usc) = :source")
    List<LinkAnalytics> findByLinkAndUniqueSourceClicksKey(@Param("link") Link link, @Param("source") String source);
}