package dev.paul.cartlink.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long productId;

    @NotBlank(message = "Product name is required")
    private String name;

    @NotBlank(message = "Manufacturer is required")
    private String manufacturer;

    @NotNull(message = "Production year is required")
    private Integer productionYear;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private Double price;

    @NotNull(message = "Units in stock is required")
    @Positive(message = "Units in stock must be positive")
    private Integer unitsInStock;

    @NotNull(message = "Product type is required")
    @Enumerated(EnumType.STRING)
    private ProductType type;

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

    // Electronics specific fields
    @Embedded
    private Dimensions dimensions;

    private String model;

    @Column(columnDefinition = "TEXT")
    private String spec;

    private String warranty;
    private String marketVariant;
    private String powerRating;
    private String electronicsType;

    // Fashion specific fields
    private String sex;
    private String fashionType;
    private String size;
    private String material;

    // Cosmetics specific fields
    private String skinType;

    @Column(columnDefinition = "TEXT")
    private String ingredients;

    @Column(columnDefinition = "TEXT")
    private String applicationDirection;

    @ElementCollection
    @CollectionTable(name = "product_specifications", joinColumns = @JoinColumn(name = "product_id"))
    @MapKeyColumn(name = "spec_key")
    @Column(name = "spec_value", columnDefinition = "TEXT")
    private Map<String, String> specifications;
}

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
class Dimensions {
    private Double length;
    private Double width;
    private Double height;
}

enum ProductType {
    ELECTRONICS,
    FASHION,
    COSMETICS
}
