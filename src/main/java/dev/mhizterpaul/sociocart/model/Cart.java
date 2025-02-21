package dev.mhizterpaul.sociocart.model;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders")
public class Cart {
    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE)
    private Long cartId;

    @OneToMany
    @JoinColumn(name = "cart_id") // FK in Order table
    private List<Order> orders; // A cart contains multiple orders

    private Double totalPrice;
    private Long customerId;
    private Boolean paid;
    private Boolean completed;
    private Long timestamp;
}



