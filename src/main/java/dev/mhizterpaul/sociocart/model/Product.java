package dev.mhizterpaul.sociocart.model;
import jakarta.persistence.*;
import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "product")
public class Product {
    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE)
    private Long productId;
    private String name;
    private String model;
    private String manufacturer;
    //specification
    //description
}








