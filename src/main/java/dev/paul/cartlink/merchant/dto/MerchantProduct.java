package dev.paul.cartlink.merchant.dto;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import dev.paul.cartlink.merchant.model.Merchant;
import dev.paul.cartlink.order.model.Order;
import dev.paul.cartlink.product.model.Product;
import dev.paul.cartlink.product.model.ProductLink;

import java.util.Set;
import java.util.HashSet;

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
    @JsonManagedReference(value = "merchant-product-merchant")
    private Merchant merchant; // Links to Merchant entity

    @ManyToOne
    @JoinColumn(name = "productId", nullable = false)
    @JsonManagedReference(value = "merchant-product-product")
    private Product product; // Links to Product entity

    @OneToMany(mappedBy = "merchantProduct", cascade = CascadeType.ALL)
    @JsonManagedReference("merchantProduct-orders")
    private Set<Order> orders = new HashSet<>();

    @OneToMany(mappedBy = "merchantProduct", cascade = CascadeType.ALL)
    @JsonManagedReference("merchantProduct-productLinks")
    private Set<ProductLink> productLinks = new HashSet<>();

    private Integer stock; // Optional: Stores stock quantity
    private Double price; // Price specific to the merchant
    private Double discount;
    private String logisticsProvider;
}
