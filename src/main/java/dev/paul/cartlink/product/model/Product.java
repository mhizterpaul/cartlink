package dev.paul.cartlink.product.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long productId;

    private Long typeId;

    @NotBlank(message = "Product name is required")
    private String name;

    @NotBlank(message = "Manufacturer is required")
    private String brand;

    @NotNull(message = "Production year is required")
    private Integer productionYear;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private Double price;

    @NotNull(message = "Units in stock is required")
    @Positive(message = "Units in stock must be positive")
    private Integer unitsInStock;

    @NotNull(message = "Product type is required")
    private String productType;

    @Column(columnDefinition = "TEXT")
    @NotBlank(message = "Description is required")
    private String description;

    private Boolean payOnDelivery;

    @ElementCollection
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url")
    private List<String> images;

    @ElementCollection
    @CollectionTable(name = "product_videos", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "video_url")
    private List<String> videos;

    private Boolean imported;

    @ManyToOne
    @JoinColumn(name = "merchant_id")
    private dev.paul.cartlink.merchant.model.Merchant merchant;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Coupon> coupons;

    // --- Dynamic Form Core Fields ---
    private String category;

    public Long getId() {
        return productId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    public Long getTypeId() {
        return this.typeId;
    }

    public void setStock(int stock) {
        this.unitsInStock = stock;
    }

    public void setMerchant(dev.paul.cartlink.merchant.model.Merchant merchant) {
        this.merchant = merchant;
    }

    public Set<Coupon> getCoupons() {
        return coupons;
    }

    public void setCoupons(Set<Coupon> coupons) {
        this.coupons = coupons;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
