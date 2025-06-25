package dev.paul.cartlink.link.service;

import dev.paul.cartlink.link.model.LinkAnalytics;
import dev.paul.cartlink.link.repository.LinkAnalyticsRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LinkAnalyticsService {
    private final LinkAnalyticsRepository linkAnalyticsRepository;

    public LinkAnalyticsService(LinkAnalyticsRepository linkAnalyticsRepository) {
        this.linkAnalyticsRepository = linkAnalyticsRepository;
    }

    public List<LinkAnalytics> getAnalyticsForLink(Long linkId) {
        return linkAnalyticsRepository.findByProductLink_LinkId(linkId);
    }

    @Transactional
    public void deleteAnalytics(Long analyticsId) {
        linkAnalyticsRepository.deleteById(analyticsId);
    }

    public LinkAnalytics getAnalyticsById(Long analyticsId) {
        return linkAnalyticsRepository.findById(analyticsId).orElse(null);
    }

    public List<LinkAnalytics> getAnalyticsByMerchantProductId(Long merchantProductId) {
        return linkAnalyticsRepository.findAll().stream()
                .filter(la -> la.getMerchantProducts().stream().anyMatch(mp -> mp.getId().equals(merchantProductId)))
                .collect(Collectors.toList());
    }

    public List<LinkAnalytics> getAnalyticsByDateRange(java.time.LocalDateTime start, java.time.LocalDateTime end) {
        return linkAnalyticsRepository.findAll().stream()
                .filter(la -> la.getLastUpdated() != null && !la.getLastUpdated().isBefore(start)
                        && !la.getLastUpdated().isAfter(end))
                .collect(Collectors.toList());
    }

    public List<LinkAnalytics> getAnalyticsBySource(String source) {
        return linkAnalyticsRepository.findAll().stream()
                .filter(la -> la.getUniqueSourceClicks().containsKey(source))
                .collect(Collectors.toList());
    }

    public void updateSourceClicks(Long analyticsId, String source, int clicks) {
        LinkAnalytics analytics = linkAnalyticsRepository.findById(analyticsId).orElse(null);
        if (analytics != null) {
            analytics.getUniqueSourceClicks().put(source, clicks);
            analytics.setLastUpdated(java.time.LocalDateTime.now());
            linkAnalyticsRepository.save(analytics);
        }
    }

    // --- AGGREGATE ANALYTICS FOR API ---
    public LinkStatsResponse getLinkStats(Long linkId, String startDate, String endDate) {
        // For demo, just return total clicks and average time spent for all analytics
        // for the link
        List<LinkAnalytics> analyticsList = getAnalyticsForLink(linkId);
        int totalClicks = analyticsList.stream().mapToInt(LinkAnalytics::getTotalUniqueClicks).sum();
        double avgTime = analyticsList.stream().filter(a -> a.getAverageTimeSpent() != null)
                .mapToLong(LinkAnalytics::getAverageTimeSpent).average().orElse(0);
        return new LinkStatsResponse(totalClicks, avgTime);
    }

    public static class LinkStatsResponse {
        public int totalClicks;
        public double averageTimeSpent;

        public LinkStatsResponse(int totalClicks, double averageTimeSpent) {
            this.totalClicks = totalClicks;
            this.averageTimeSpent = averageTimeSpent;
        }
    }

    // Traffic sources endpoint
    public List<SourceClicks> getLinkTraffic(Long linkId) {
        List<LinkAnalytics> analytics = getAnalyticsForLink(linkId);
        java.util.Map<String, Integer> sourceClicks = new java.util.HashMap<>();
        for (LinkAnalytics la : analytics) {
            if (la.getUniqueSourceClicks() != null) {
                la.getUniqueSourceClicks().forEach((src, count) -> sourceClicks.merge(src, count, Integer::sum));
            }
        }
        return sourceClicks.entrySet().stream()
                .map(e -> new SourceClicks(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    public static class SourceClicks {
        public String source;
        public int clicks;

        public SourceClicks(String source, int clicks) {
            this.source = source;
            this.clicks = clicks;
        }
    }

    @Transactional
    public void updateTotalOrders(dev.paul.cartlink.link.model.Link link) {
        if (link == null || link.getAnalytics() == null) {
            return;
        }
        dev.paul.cartlink.link.model.LinkAnalytics analytics = link.getAnalytics();
        if (analytics.getTotalOrders() == null) {
            analytics.setTotalOrders(1);
        } else {
            analytics.setTotalOrders(analytics.getTotalOrders() + 1);
        }
        analytics.setLastUpdated(java.time.LocalDateTime.now());
        linkAnalyticsRepository.save(analytics);
    }

}