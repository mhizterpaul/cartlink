package dev.paul.cartlink.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "merchant_product")
public class MerchantProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id; // Unique ID for each row

    @ManyToOne
    @JoinColumn(name = "merchantId", nullable = false)
    private Merchant merchant; // Links to Merchant entity

    @ManyToOne
    @JoinColumn(name = "productId", nullable = false)
    private Product product; // Links to Product entity

    private Integer stock; // Optional: Stores stock quantity
    private Double price; // Price specific to the merchant
    private Double discount;
    private String logisticsProvider;
}
