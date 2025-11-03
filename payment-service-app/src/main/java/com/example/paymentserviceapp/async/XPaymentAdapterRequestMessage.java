package com.example.paymentserviceapp.async;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class XPaymentAdapterRequestMessage implements Message {

    private UUID paymentGuid;

    private BigDecimal amount;

    private String currency;

    private UUID messageId;

    private OffsetDateTime occurredAt;

    @Override
    public UUID getMessageId() {
        return messageId;
    }

    @Override
    public OffsetDateTime getOccurredAt() {
        return occurredAt;
    }
}

