package dev.paul.cartlink.order.model;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import dev.paul.cartlink.complaint.model.Complaint;
import dev.paul.cartlink.customer.model.Customer;
import dev.paul.cartlink.merchant.dto.MerchantProduct;
import dev.paul.cartlink.product.model.ProductLink;

import com.fasterxml.jackson.annotation.JsonBackReference;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    @JsonBackReference("customer-orders")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "merchant_product_id")
    @JsonBackReference("merchantProduct-orders")
    private MerchantProduct merchantProduct;

    @ManyToOne
    @JoinColumn(name = "product_link_id")
    @JsonBackReference("productLink-orders")
    private ProductLink productLink;

    private Integer quantity;
    private Double totalPrice;
    private LocalDateTime orderDate;
    private LocalDateTime lastUpdated;
    private String trackingId;
    private Boolean paid;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @OneToMany(mappedBy = "order")
    @JsonManagedReference("order-complaints")
    private Set<Complaint> complaints = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        orderDate = LocalDateTime.now();
        lastUpdated = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }

    // Alias methods to match service usage
    public void setOrderSize(Integer size) {
        this.quantity = size;
    }

    public void setUpdatedAt(LocalDateTime date) {
        this.lastUpdated = date;
    }
}
