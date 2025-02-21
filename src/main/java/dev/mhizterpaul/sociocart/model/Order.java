package dev.mhizterpaul.sociocart.model;
import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE)
    private Long orderId;

    @OneToOne
    @JoinColumn(name = "merchant_product_id", nullable = false, unique = true) // Ensures one-to-one mapping
    private MerchantProduct merchantProduct; // One order is linked to one MerchantProduct

    private Integer orderSize; // Order size (number of items in the order)
    private Boolean completed;
    private Boolean paid;
    //analytics?
    private Long customerId;
    private Long trackingId;
}



