package dev.paul.cartlink.merchant.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
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
