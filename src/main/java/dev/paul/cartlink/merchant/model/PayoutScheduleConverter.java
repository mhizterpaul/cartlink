package dev.paul.cartlink.merchant.model;

import dev.paul.cartlink.merchant.model.Wallet.PayoutSchedule;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PayoutScheduleConverter implements AttributeConverter<PayoutSchedule, Integer> {

    @Override
    public Integer convertToDatabaseColumn(PayoutSchedule schedule) {
        return (schedule != null) ? schedule.getDays() : null;
    }

    @Override
    public PayoutSchedule convertToEntityAttribute(Integer days) {
        if (days == null) {
            return null;
        }
        for (PayoutSchedule schedule : PayoutSchedule.values()) {
            if (schedule.getDays() == days) {
                return schedule;
            }
        }
        throw new IllegalArgumentException("Invalid payout schedule days: " + days);
    }
}
