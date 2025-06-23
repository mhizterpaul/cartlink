package dev.paul.cartlink.product.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import dev.paul.cartlink.merchant.dto.MerchantProduct;
import dev.paul.cartlink.order.model.Order;

import java.util.Set;
import java.util.HashSet;

@Data
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
    @JsonManagedReference(value = "product-link-merchant-product")
    private MerchantProduct merchantProduct;

    @Column(nullable = false, unique = true)
    private String url;

    @Column(nullable = false)
    private String qrCode;

    private Integer clicks = 0;
    private Integer conversions = 0;

    @OneToMany(mappedBy = "productLink", cascade = CascadeType.ALL)
    @JsonManagedReference("productLink-orders")
    private Set<Order> orders = new HashSet<>();

    public Long getId() {
        return linkId;
    }

    public void setProduct(dev.paul.cartlink.product.model.Product product) {
        // TODO: Set the merchantProduct association based on the product and merchant
    }

    public void setMerchant(dev.paul.cartlink.merchant.model.Merchant merchant) {
        // TODO: Set the merchantProduct association based on the merchant and product
    }

    public void setUrl(String url) {
        this.url = url;
    }
}