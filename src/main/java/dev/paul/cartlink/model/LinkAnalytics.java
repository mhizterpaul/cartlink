package dev.paul.cartlink.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Setter
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

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();
} 