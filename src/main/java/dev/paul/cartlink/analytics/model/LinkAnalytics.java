package dev.paul.cartlink.analytics.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

import dev.paul.cartlink.product.model.ProductLink;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "link_analytics")
public class LinkAnalytics {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long analyticsId;

    @ManyToOne
    @JoinColumn(name = "product_link_id", nullable = false)
    private ProductLink productLink;

    private String source; // e.g., "facebook", "instagram", "direct"
    private String device; // e.g., "mobile", "desktop", "tablet"
    private String location; // e.g., "US", "UK", "NG"
    private String ipAddress;
    private String userAgent;
    private Long timeSpent; // Time spent on page in seconds

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();
}