package dev.mhizterpaul.sociocart.model;
import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "merchant")
public class Merchant {
    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE)
    private Long merchantId;
    private String firstName;
    private String lastName;
    private String middleName;
    private String image;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "wallet_id", referencedColumnName = "wallet_id", nullable = false, unique = true)
    private Wallet wallet_id;
    //merchant rating and rating count
    //merchant reviews
}



