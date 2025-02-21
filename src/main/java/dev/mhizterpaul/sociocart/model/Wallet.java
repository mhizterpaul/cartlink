package dev.mhizterpaul.sociocart.model;

import dev.mhizterpaul.sociocart.converter.PayoutScheduleConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "wallet")
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long walletId;

    private Double balance;

    @Convert(converter = PayoutScheduleConverter.class)
    @Column(nullable = true)
    private PayoutSchedule payoutSchedule;

    public enum PayoutSchedule {
        THREE_DAYS(3),
        SEVEN_DAYS(7),
        FOURTEEN_DAYS(14),
        THIRTY_DAYS(30);

        @Getter
        private final int days;

        PayoutSchedule(int days) {
            this.days = days;
        }
    }
}
