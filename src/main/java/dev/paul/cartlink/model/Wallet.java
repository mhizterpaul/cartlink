package dev.paul.cartlink.model;

import dev.paul.cartlink.converter.PayoutScheduleConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "wallet")
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "wallet_id")
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
