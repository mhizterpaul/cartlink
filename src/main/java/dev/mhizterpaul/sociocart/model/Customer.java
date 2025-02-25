package dev.mhizterpaul.sociocart.model;
import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "customer")
public class Customer {
    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE)
    private Long custoomerId;
    private String firstName;
    private String lastName;
    private String middleName;
    private String image;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "wallet_id", referencedColumnName = "wallet_id", nullable = false, unique = true)
    private Wallet wallet_id;
}



