package com.example.paymentserviceapp.persistency;

import com.example.paymentserviceapp.persistence.entity.Payment;
import com.example.paymentserviceapp.persistence.entity.Payment_;
import com.example.paymentserviceapp.persistence.entity.PaymentStatus;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public final class PaymentSpecifications {

    public static Specification<Payment> hasCurrency(String currency) {
        return (root, query, cb) -> cb.equal(root.get(Payment_.currency), currency);
    }

    public static Specification<Payment> amountBetween(BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> cb.between(root.get(Payment_.amount), min, max);
    }

    public static Specification<Payment> createdBetween(OffsetDateTime after, OffsetDateTime before) {
        return (root, query, cb) -> cb.between(root.get(Payment_.createdAt), after, before);
    }

    public static Specification<Payment> hasStatus(PaymentStatus status) {
        return (root, query, cb) -> cb.equal(root.get(Payment_.status), status);
    }
}
