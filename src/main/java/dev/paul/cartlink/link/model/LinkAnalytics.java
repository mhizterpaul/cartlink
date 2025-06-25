package dev.paul.cartlink.link.model;

import jakarta.persistence.*;
import lombok.Data;
import dev.paul.cartlink.merchant.model.MerchantProduct;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Entity
@Table(name = "link_analytics")
public class LinkAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long analyticsId;

    @Column(nullable = false)
    private Integer totalUniqueClicks = 0;

    @Column(nullable = false)
    private Integer totalUniqueMobile = 0;

    @Column(nullable = false)
    private Integer totalUniqueDesktops = 0;

    @ManyToMany
    @JoinTable(name = "link_analytics_products", joinColumns = @JoinColumn(name = "analyticsId"), inverseJoinColumns = @JoinColumn(name = "id"))
    private List<MerchantProduct> merchantProducts;

    @ElementCollection
    @CollectionTable(name = "link_analytics_sources", joinColumns = @JoinColumn(name = "analytics_id"))
    @MapKeyColumn(name = "source")
    @Column(name = "clicks")
    private Map<String, Integer> uniqueSourceClicks = new HashMap<>();

    private String geolocation;

    private Double bounceRate;

    private Number conversionRate;

    private Integer TotalOrders;

    private Long averageTimeSpent; // in seconds

    @Column(nullable = false)
    private LocalDateTime lastUpdated = LocalDateTime.now();
}
