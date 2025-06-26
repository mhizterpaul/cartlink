package dev.paul.cartlink.merchant.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonBackReference;

import dev.paul.cartlink.link.model.LinkAnalytics;
import dev.paul.cartlink.order.model.Order;
import dev.paul.cartlink.product.model.Product;

import java.net.InetAddress;
import java.util.Set;
import java.util.HashSet;
import java.util.List;

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

    @OneToMany(cascade = CascadeType.ALL)
   @JoinColumn(name = "merchant_product_id")
private Set<LinkAnalytics> linkAnalytics = new HashSet<>();

    

    @OneToMany(mappedBy = "merchantProduct", cascade = CascadeType.ALL)
    @JsonBackReference("merchantProduct-coupons")
    private Set<Coupon> coupons = new HashSet<>();

    @NotNull(message = "Product type is required")
    private String merchantProductType;

    @ElementCollection
    @CollectionTable(name = "merchant_safe_ips", joinColumns = @JoinColumn(name = "merchant_id"))
    @Column(name = "ip_address")
    private List<InetAddress> safeIpAddresses;


    public String getmerchantProductType() {
        return merchantProductType;
    }

    public void setmerchantProductType(String merchantProductType) {
        this.merchantProductType = merchantProductType;
    }

    private Integer stock; // Optional: Stores stock quantity
    private Double price; // Price specific to the merchant

    private String description; // Description of the product

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCoupons(Set<Coupon> coupons) {
        this.coupons = coupons;
    }

    public Set<Coupon> getCoupons() {
        return coupons;
    }
}
