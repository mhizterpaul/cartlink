package dev.paul.cartlink.model;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long orderId;

    @ManyToOne
    @JoinColumn(name = "merchant_product_id", nullable = false)
    private MerchantProduct merchantProduct;

    private Integer orderSize;

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PENDING;

    private Boolean paid = false;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "product_link_id")
    private ProductLink productLink;
    
    private String trackingId;
    
    // Explicit setter for trackingId in case Lombok-generated one isn't recognized
    public void setTrackingId(String trackingId) {
        this.trackingId = trackingId;
    }
    
    // Explicit getter for trackingId in case Lombok-generated one isn't recognized
    public String getTrackingId() {
        return this.trackingId;
    }
}
