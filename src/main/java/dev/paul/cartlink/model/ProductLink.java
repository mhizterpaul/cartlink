package dev.paul.cartlink.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "product_link")
public class ProductLink {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long linkId;

    @ManyToOne
    @JoinColumn(name = "merchant_product_id", nullable = false)
    private MerchantProduct merchantProduct;

    @Column(unique = true)
    private String url;

    @Column(columnDefinition = "TEXT")
    private String qrCode;

    private Integer clicks = 0;
    private Integer conversions = 0;

    @OneToMany(mappedBy = "productLink", cascade = CascadeType.ALL)
    private List<LinkAnalytics> analytics;
} 