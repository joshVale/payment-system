package com.example.paymentserviceapp.persistency;

import com.example.paymentserviceapp.persistence.entity.Payment;
import com.example.paymentserviceapp.util.DateTimeUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PaymentFilterFactory {

    public static Specification<Payment> fromFilter(PaymentFilter filter) {
        Specification<Payment> spec = (root, query, cb) -> cb.conjunction();

        if (StringUtils.hasText(filter.currency())) {
            spec = spec.and(PaymentSpecifications.hasCurrency(filter.currency()));
        }

        if (filter.minAmount() != null || filter.maxAmount() != null) {
            spec = spec.and(PaymentSpecifications.amountBetween(filter.minAmount(), filter.maxAmount()));
        }

        if (filter.createdAfter() != null || filter.createdBefore() != null) {
            final OffsetDateTime after = DateTimeUtils.toOffsetDateTimeOrMin(filter.createdAfter());
            final OffsetDateTime before = DateTimeUtils.toOffsetDateTimeOrMax(filter.createdBefore());
            spec = spec.and(PaymentSpecifications.createdBetween(after, before));
        }

        if (filter.status() != null) {
            spec = spec.and(PaymentSpecifications.hasStatus(filter.status()));
        }

        return spec;
    }
}
